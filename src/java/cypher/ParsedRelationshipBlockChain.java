package cypher;

import org.apache.commons.collections.ListUtils;

import java.util.ArrayList;
import java.util.List;

public class ParsedRelationshipBlockChain
{

    public int nonEmptyNodesWithLabels = 0;

    List<ParsedRelationshipBlock> chain = new ArrayList<>();
    List<String> relIds = new ArrayList<>();
    List<String> nodeIds = new ArrayList<>();

    public ParsedRelationshipBlockChain ( ParsedRelationshipBlock... blocks ){
        for ( ParsedRelationshipBlock block : blocks )
        {
            chain.add( block );
            relIds.add( block.relName );
            nodeIds.add( block.leftNodeName );
        }
    }
    public ParsedRelationshipBlockChain ( ParsedRelationshipBlockChain oldChain, ParsedRelationshipBlock newBlock ){
       chain.addAll( oldChain.chain );
       relIds.addAll( oldChain.relIds );
       chain.add( newBlock );
       nodeIds.add( newBlock.leftNodeName );
       relIds.add( newBlock.relName );

       nonEmptyNodesWithLabels = oldChain.nonEmptyNodesWithLabels;
       if ( !newBlock.rightLabels.isEmpty() ){
           nonEmptyNodesWithLabels += 1;
       }
    }
    @Override
    public int hashCode(){
        String hashString = "";
        for ( ParsedRelationshipBlock block : chain )
        {
            hashString += block.hashableString();
        }
        return hashString.hashCode();
    }

    @Override
    public boolean equals( Object o ){
        if ( !(o instanceof ParsedRelationshipBlockChain) ){
            return false;
        }
        ParsedRelationshipBlockChain other = (ParsedRelationshipBlockChain) o;
        return ListUtils.isEqualList( this.chain, other.chain );
    }

    @Override
    public String toString(){
        return chain.toString();
    }

    public String toAnonPatternString(){
        String patternString = "";
        for ( int i = 0; i < chain.size(); i++ )
        {
            if ( i == 0 ){
                patternString += "("+chain.get( 0 ).labels( chain.get( 0 ).anonLeftLabels ) +")";
            }
            ParsedRelationshipBlock block = chain.get( i );
            patternString += block.getLeftRelPrefix() +"-[" + block.labels (block.anonTypes) + block.relCountAsString() + "]-" + block.getRightRelPrefix() + "("+ block.labels( block.anonRightLabels ) +")";
        }


        return patternString;
    }


    public int compareTo( ParsedRelationshipBlockChain other ){
        if ( this.chain.size() == other.chain.size() )
            return 0;
        else if (this.chain.size() > other.chain.size() )
            return 1;
        else
            return -1;
    }
}
