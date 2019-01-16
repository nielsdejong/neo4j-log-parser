package cypher;

import cypher.anonymized.AnonMapper;
import scala.Tuple2;
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
    Map<String, List<ParsedRelationshipBlock>> blocksConnectedToNode = new HashMap<>();

    public ParsedQueryResult(  Tuple2<Set<PatternRelationship>,Set<Expression>> tuple2 )
    {
        convertToParsedRelBlocks( tuple2._1(), tuple2._2() );
    }

    private void convertToParsedRelBlocks( Set<PatternRelationship> patternRelationshipSet, Set<Expression> expressionSet ){
        AnonMapper.resetForNames();

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

    public String countMaxJoins(){
        int count = 0;
        for (  ParsedRelationshipBlock block : blocks ){
            count += block.getMaxLength();
        }

        // It has an RPQ.
        if ( count >= 10000 ){
            return "*";
        }
        return "" + count;
    }

    public String countMinJoins(){
        int count = 0;
        for (  ParsedRelationshipBlock block : blocks ){
            count += block.getMinLength();
        }
        return "" + count;
    }

    public int isChainQuery(){
        for ( List<String> connected : queryGraph.values()){
            if ( connected.size() > 2){
                return 0;
            }
        }
        return 1;
    }


    public int hasAnyEdgeInQuery()
    {
        if ( blocks.size() == 0 )
        {
            return 0;
        }
        return 1;
    }
    public int isSingleEdgeQuery(){
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

    private List<ParsedRelationshipBlockChain> chainBuildingBFS( List<ParsedRelationshipBlockChain> chainsComingIntoStartNode, String startNode, int depth ){

//        System.out.println( startNode +  " : " + depth );
//        for ( ParsedRelationshipBlockChain chain : chainsComingIntoStartNode ){
//            System.out.println(chain.toAnonPatternString());
//        }
//        System.out.println();
        if ( depth == 0 )
        {
            return chainsComingIntoStartNode;
        }

        List<ParsedRelationshipBlockChain> theNewChains = new ArrayList<>(  );
        for ( ParsedRelationshipBlock block : blocksConnectedToNode.get( startNode )){

            // reverse if if it's reversely connected to the end of the chain.
            ParsedRelationshipBlock blockCopy = ( block.rightNodeName == startNode ) ?
                    new ParsedRelationshipBlock( block.rightNodeName, block.rightLabels, block.relName, block.types, block.leftLabels, block.leftNodeName, block.direction.reversed(), block.getMinLength(), block.getMaxLength() ) :
                    new ParsedRelationshipBlock( block.leftNodeName, block.leftLabels, block.relName, block.types, block.rightLabels, block.rightNodeName, block.direction, block.getMinLength(), block.getMaxLength() );

            // Store also the version with missing node labels.
            ParsedRelationshipBlock blockCopyWithoutLeftLabels = new ParsedRelationshipBlock( blockCopy.leftNodeName, new ArrayList<>(), blockCopy.relName, blockCopy.types, blockCopy.rightLabels, blockCopy.rightNodeName, blockCopy.direction, blockCopy.getMinLength(), blockCopy.getMaxLength() );
            ParsedRelationshipBlock blockCopyWithoutRightLabels = new ParsedRelationshipBlock( blockCopy.leftNodeName, blockCopy.leftLabels, blockCopy.relName, blockCopy.types, new ArrayList<>(), blockCopy.rightNodeName, blockCopy.direction, blockCopy.getMinLength(), blockCopy.getMaxLength() );
            ParsedRelationshipBlock blockCopyWithoutEitherLabels = new ParsedRelationshipBlock( blockCopy.leftNodeName, new ArrayList<>(), blockCopy.relName, blockCopy.types, new ArrayList<>(), blockCopy.rightNodeName, blockCopy.direction, blockCopy.getMinLength(), blockCopy.getMaxLength() );

            List<ParsedRelationshipBlockChain> newChains = new ArrayList<>(  );
            for (ParsedRelationshipBlockChain chain : chainsComingIntoStartNode ){
                if ( ! chain.relIds.contains( block.relName ) && ! chain.nodeIds.contains( block.rightNodeName) ){
                    newChains.add( new ParsedRelationshipBlockChain( chain, blockCopy ));

                    if ( blockCopy.leftLabels.size() > 0 && chain.chain.size() == 0 ) {
                        newChains.add( new ParsedRelationshipBlockChain( chain, blockCopyWithoutLeftLabels ) );

                        if ( blockCopy.rightLabels.size() > 0){
                            newChains.add( new ParsedRelationshipBlockChain( chain, blockCopyWithoutEitherLabels ) );
                        }
                    }

                    if ( blockCopy.rightLabels.size() > 0 )
                        newChains.add( new ParsedRelationshipBlockChain( chain, blockCopyWithoutRightLabels) );
                }
            }

            String otherNodeID = (block.leftNodeName == startNode ) ? block.rightNodeName : block.leftNodeName;
            theNewChains.addAll( chainBuildingBFS( newChains, otherNodeID, depth - 1 ) );
        }
        //chainsComingIntoStartNode.addAll( newChains );
        theNewChains.addAll( chainsComingIntoStartNode );
        return theNewChains;
    }

    /**
     * Find all linear (chain-shaped) subpatterns in a graph, given a maximum length.
     * @param maxSubPatternSize
     * @return
     */
    public List<ParsedRelationshipBlockChain> getAllSubPatternsInQueryGraph( int maxSubPatternSize ){
        List<ParsedRelationshipBlockChain> allBlocksInGraph = new ArrayList<>();



        for ( String nodeName : blocksConnectedToNode.keySet() )
        {
            List<ParsedRelationshipBlockChain> emptyArrayList = new ArrayList<>( );
            emptyArrayList.add( new ParsedRelationshipBlockChain(  ) );
            allBlocksInGraph.addAll ( chainBuildingBFS( emptyArrayList, nodeName, maxSubPatternSize ));
        }

//        for ( ParsedRelationshipBlock block : blocks ){
//            allBlocksInGraph.add( new ParsedRelationshipBlockChain ( block ));
//            allBlocksInGraph.add( new ParsedRelationshipBlockChain ( new ParsedRelationshipBlock( emptyArrayList, block.types, block.rightLabels, block.direction )));
//            allBlocksInGraph.add( new ParsedRelationshipBlockChain ( new ParsedRelationshipBlock( block.leftLabels, emptyArrayList, block.rightLabels, block.direction )));
//            allBlocksInGraph.add( new ParsedRelationshipBlockChain ( new ParsedRelationshipBlock( block.leftLabels, block.types, emptyArrayList, block.direction )));
//            allBlocksInGraph.add( new ParsedRelationshipBlockChain ( new ParsedRelationshipBlock( emptyArrayList, block.types, emptyArrayList, block.direction )));
//            allBlocksInGraph.add( new ParsedRelationshipBlockChain ( new ParsedRelationshipBlock( emptyArrayList, emptyArrayList, block.rightLabels, block.direction )));
//            allBlocksInGraph.add( new ParsedRelationshipBlockChain ( new ParsedRelationshipBlock( block.leftLabels, emptyArrayList, emptyArrayList, block.direction )));
//            allBlocksInGraph.add( new ParsedRelationshipBlockChain ( new ParsedRelationshipBlock( emptyArrayList, emptyArrayList, emptyArrayList, block.direction )));
//        }
        return allBlocksInGraph;
    }
}

