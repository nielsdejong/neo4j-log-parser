package cypher;

import scala.Tuple2;
import scala.collection.immutable.Set;
import org.neo4j.cypher.internal.ir.v4_0.PatternRelationship;
import org.neo4j.cypher.internal.v4_0.expressions.Expression;
import org.neo4j.cypher.internal.v4_0.expressions.HasLabels;
import org.neo4j.cypher.internal.v4_0.expressions.LabelName;
import scala.collection.JavaConversions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsedQueryResult
{
    private String string;

    public ParsedQueryResult(  Tuple2<Set<PatternRelationship>,Set<Expression>> tuple2 )
    {
        generateString( tuple2 );
    }

    private void generateString( Tuple2<Set<PatternRelationship>,Set<Expression>> tuple2 ){
        Set<PatternRelationship> patternRelationshipSet = tuple2._1;
        Set<Expression> expressionSet = tuple2._2;
        string = "";

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

            }else{
                System.out.println("[ERROR] Parsed query expression contains something else than HasLabels!");
            }
        }
        for ( PatternRelationship patternRelationship : JavaConversions.asJavaIterable( patternRelationshipSet) )
        {
            ParsedRelationshipBlock block = new ParsedRelationshipBlock( patternRelationship, nodesAndTheirLabels );
            string +=  block;
        }
    }
    public String toString(){
        return string;
    }
}

