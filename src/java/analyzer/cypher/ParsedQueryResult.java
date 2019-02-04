package analyzer.cypher;

import analyzer.cypher.anonymized.AnonymousLabelAndNameMapper;
import scala.Tuple2;
import scala.Tuple4;
import scala.collection.immutable.Set;
import org.neo4j.cypher.internal.ir.v4_0.PatternRelationship;
import org.neo4j.cypher.internal.v4_0.expressions.Expression;
import org.neo4j.cypher.internal.v4_0.expressions.HasLabels;
import org.neo4j.cypher.internal.v4_0.expressions.LabelName;
import scala.collection.JavaConversions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsedQueryResult
{
    private List<ParsedRelationshipBlock> blocks = new ArrayList<>();
    Map<String, List<String>> queryGraph = new HashMap<>();
    public Map<String, List<ParsedRelationshipBlock>> blocksConnectedToNode = new HashMap<>();
    public boolean isUpdate;
    public boolean hasMerge;


    public ParsedQueryResult(  Tuple4<Set<PatternRelationship>,Set<Expression>, Object, Object> tuple4 )
    {
        convertToParsedRelBlocks( tuple4._1(), tuple4._2() );
        isUpdate = (boolean) tuple4._3();
        hasMerge = (boolean) tuple4._4();
    }

    private void convertToParsedRelBlocks( Set<PatternRelationship> patternRelationshipSet, Set<Expression> expressionSet ){
        AnonymousLabelAndNameMapper.resetForNames();

        Map<String, List<String>> nodesAndTheirLabels = new HashMap<>();

        for ( Expression expression : JavaConversions.asJavaIterable( expressionSet ) ){
            if ( expression instanceof HasLabels ) {
                HasLabels hasLabels = (HasLabels) expression;
                String nodeName = hasLabels.expression().asCanonicalStringVal();

                List<String> labels = new ArrayList<>();
                for ( LabelName labelName : JavaConversions.asJavaIterable( hasLabels.labels() ) ){
                    labels.add( labelName.asCanonicalStringVal() );
                }
                nodesAndTheirLabels.put( nodeName, labels );

            } else {
                System.out.println("[ERROR] Parsed query expression contains something else than HasLabels!");
            }
        }
        for ( PatternRelationship patternRelationship : JavaConversions.asJavaIterable( patternRelationshipSet) )
        {
            ParsedRelationshipBlock block = new ParsedRelationshipBlock( patternRelationship, nodesAndTheirLabels );
            this.blocks.add( block );
        }

        for (  ParsedRelationshipBlock block : blocks )
        {
            // Create an internal tree-like representation for the query graph.
            if ( !queryGraph.containsKey( block.leftNodeName ) )
            {
                queryGraph.put( block.leftNodeName, new ArrayList<>() );
                blocksConnectedToNode.put( block.leftNodeName, new ArrayList<>() );
            }
            if ( !queryGraph.containsKey( block.rightNodeName ) )
            {
                queryGraph.put( block.rightNodeName, new ArrayList<>() );
                blocksConnectedToNode.put( block.rightNodeName, new ArrayList<>() );
            }
            queryGraph.get( block.leftNodeName ).add( block.rightNodeName );
            queryGraph.get( block.rightNodeName ).add( block.leftNodeName );
            blocksConnectedToNode.get( block.rightNodeName ).add( block );
            blocksConnectedToNode.get( block.leftNodeName).add( block );
        }
    }

    public String toString(){
        String string = "";
        for ( ParsedRelationshipBlock block : blocks )
            string +=  block.toAnonymyzedString() + "    ";

        return string;
    }

    public int countMaxJoins(){
        int count = 0;
        for (  ParsedRelationshipBlock block : blocks ){
            count += block.getMaxLength();
        }

        return count;
    }

    public int countMinJoins(){
        int count = 0;
        for (  ParsedRelationshipBlock block : blocks ){
            count += block.getMinLength();
        }
        return count;
    }

    public int isChainQuery(){
        if ( hasAnyEdgeInQuery() == 0){
            return 0;
        }
        for ( List<String> connected : queryGraph.values()){
            if ( connected.size() > 2 ){
                return 0;
            }
        }
        return 1;
    }


    public int hasAnyEdgeInQuery()
    {
        if ( blocks == null){
            return 0;
        } else if ( blocks.size() == 0 ) {
            return 0;
        }
        return 1;
    }

    public int isXLengthQuery(int minInclusive, int maxInclusive){
        int maxJoins = countMaxJoins();
        if ( maxJoins >= minInclusive && maxJoins <= maxInclusive ){
            return 1;
        }
        return 0;
    }
    public int isXLengthQuery(int x){
        if ( countMaxJoins() == x){
            return 1;
        }
        return 0;
    }
    public int isSingleEdgeQuery(){
        if ( hasAnyEdgeInQuery() == 0){
            return 0;
        }
        if ( blocks.size() > 1 || blocks.size() == 0){
            return 0;
        }
        if ( blocks.get( 0 ).getMaxLength() > 1){
            return 0;
        }
        // there is a single edge with length one.
        return 1;
    }


    public int isTree(){
        if ( hasAnyEdgeInQuery() == 0){
            return 0;
        }
        List<String> seen = new ArrayList<>();

        if ( blocks.size() == 0 ){
            return 1;
        }
        boolean hasLoops = simpleBFSfindLoops( seen, null, blocks.get( 0 ).rightNodeName );

        // Trees can't have loops.
        if ( hasLoops == true) {
            return 0;
        }
        // Our BFS has seen all nodes ----> It's a tree
        Object[] a = seen.toArray();
        Object[] b = queryGraph.keySet().toArray();
        Arrays.sort( a );
        Arrays.sort( b );
        if ( Arrays.equals( a, b )){
            return 1;
        }
        return 0;
    }

    public int hasLoops(){
        if ( hasAnyEdgeInQuery() == 0){
            return 0;
        }
        List<String> seen = new ArrayList<>();

        if ( blocks.size() == 0 ){
            return 0;
        }

        // If it's a forest, every one of the trees could have loopsl
        if ( isTree() == 0){
            for ( ParsedRelationshipBlock block : blocks )
            {
                boolean hasLoops = simpleBFSfindLoops( seen, null, block.leftNodeName );
                if ( hasLoops ){
                    return 1;
                }
            }
            return 0;
        }else{
            // If a tree, then no loops
            return 0;
        }
        
    }

    private boolean simpleBFSfindLoops( List<String> seen, String prevNode, String current ){
        if ( seen.contains( current )){
            return true;
        }
        seen.add( current );
        for ( String connected : queryGraph.get( current )){
            if ( connected.equals( prevNode )){
                continue;
            }
            if ( simpleBFSfindLoops( seen, current, connected ) == true ){
                return true;
            }
        }
        return false;
    }


}

