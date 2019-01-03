package cypher;

import scala.None;
import scala.collection.JavaConversions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.cypher.internal.ir.v4_0.PatternLength;
import org.neo4j.cypher.internal.ir.v4_0.PatternRelationship;
import org.neo4j.cypher.internal.ir.v4_0.VarPatternLength;
import org.neo4j.cypher.internal.v4_0.expressions.RelTypeName;
import org.neo4j.cypher.internal.v4_0.expressions.SemanticDirection;

public class ParsedRelationshipBlock
{
    private String leftNode;
    private List<String> leftLabels;
    private String rightNode;
    private List<String> rightLabels;
    private SemanticDirection direction;
    private String relName;
    private List<String> types = new ArrayList<>();

    private int minLength = 1;
    private int maxLength = 1;

    public ParsedRelationshipBlock ( PatternRelationship patternRelationship, Map<String, List<String>> nodeLabelMap )
    {
        this.leftNode = patternRelationship.left();
        this.rightNode = patternRelationship.right();
        this.leftLabels = nodeLabelMap.get( leftNode );
        this.rightLabels = nodeLabelMap.get( rightNode );
        this.direction = patternRelationship.dir();
        this.relName = patternRelationship.name();
        PatternLength patternLength = patternRelationship.length();
        if ( patternLength instanceof VarPatternLength ){
            VarPatternLength varPatternLength = (VarPatternLength) patternLength;
            minLength = varPatternLength.min();
            maxLength = varPatternLength.implicitPatternNodeCount();
            if ( varPatternLength.max().toString().equals( "None" )){
                maxLength = Integer.MAX_VALUE;
            }

        }
        for ( RelTypeName relTypeName : JavaConversions.asJavaIterable( patternRelationship.types()) )
            types.add( relTypeName.name() );

    }

    public String toString(){
        return
                "(" + leftNode + ":" + leftLabels + ")" +
                "-[" + relName + ":" + types + "*" + minLength + ".." + maxLength +" ]->" +
                "(" + rightNode + ":" + rightLabels + ")";
    }
}
