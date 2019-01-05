package cypher;

import cypher.anonymized.AnonMapper;
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
    private String relName;
    private List<String> types = new ArrayList<>();

    private String anonLeftNode;
    private List<String> anonLeftLabels = new ArrayList<>();
    private String anonRightNode;
    private List<String> anonRightLabels = new ArrayList<>();
    private String anonRelName;
    private List<String> anonTypes = new ArrayList<>();

    private SemanticDirection direction;


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

        anonymyzeLabelsAndTypes();
    }

    public String toString(){
        return
                "(" + leftNode + labels(leftLabels) + ")" + getLeftRelPrefix() +
                "-[" + relName + labels(types) + relCountAsString() +" ]-" + getRightRelPrefix() +
        "(" + rightNode + labels(rightLabels) + ")";
    }

    private String getLeftRelPrefix()
    {
        return direction.toString().equals( "INCOMING" ) ? "<" : "";
    }

    private String getRightRelPrefix()
    {
        return direction.toString().equals( "OUTGOING" ) ? ">" : "";
    }


    public String toAnonymyzedString(){
        return
                "(" + anonLeftNode + labels(anonLeftLabels) + ")" + getLeftRelPrefix() +
                        "-[" + anonRelName + labels(anonTypes)  + relCountAsString() +"]-" + getRightRelPrefix() +
                        "(" + anonRightNode + labels(anonRightLabels) + ")";
    }

    public String relCountAsString(){
        if ( minLength != maxLength ){
            return "*"+minLength + ".." + maxLength;
        }else if ( minLength == 1){
            return "";
        }else{
            return "*" + minLength;
        }
    }

    public String labels( List<String> labels ){

        if ( labels.size() == 0 ){
            return "";
        } else if ( labels.size() == 1 ){
            return ":"+labels.get( 0 );
        }else {
            return ":"+labels.toString();
        }
    }

    public void anonymyzeLabelsAndTypes(){

        // Anonimyze the labels and types.
        for ( String type : types )
            anonTypes.add( AnonMapper.getRelType( type ) );

        if ( leftLabels != null)
            for ( String leftLabel : leftLabels )
                anonLeftLabels.add( AnonMapper.getNodeLabel( leftLabel ) );

        if ( rightLabels != null)
            for ( String rightLabel : rightLabels )
                anonRightLabels.add( AnonMapper.getNodeLabel( rightLabel ) );

        // Anonimyze the names
        anonLeftNode = AnonMapper.getNodeName( leftNode );
        anonRightNode = AnonMapper.getNodeName( rightNode );
        anonRelName = AnonMapper.getRelName( relName );
    }
}
