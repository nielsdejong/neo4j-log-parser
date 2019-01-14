package cypher;

import org.apache.commons.collections.ListUtils;

import java.util.ArrayList;
import java.util.List;

public class ParsedRelationshipBlockChain
{

    List<ParsedRelationshipBlock> chain = new ArrayList<>();
    List<String> relIds = new ArrayList<>();
    public ParsedRelationshipBlockChain ( ParsedRelationshipBlock... blocks ){
        for ( ParsedRelationshipBlock block : blocks )
        {
            chain.add( block );
            relIds.add( block.relName );
        }
    }
    public ParsedRelationshipBlockChain ( ParsedRelationshipBlockChain oldChain, ParsedRelationshipBlock newBlock ){
       chain.addAll( oldChain.chain );
       relIds.addAll( oldChain.relIds );
       chain.add( newBlock );
       relIds.add( newBlock.relName );
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
        for ( ParsedRelationshipBlock block : chain )
        {
            patternString += "("+block.labels( block.anonLeftLabels ) +")"+block.getLeftRelPrefix() +"-[" + block.labels (block.anonTypes) + block.relCountAsString() + "]-" + block.getRightRelPrefix();
        }
        if ( chain.size() >= 1 )
            patternString += "("+chain.get( chain.size() - 1 ).labels( chain.get( chain.size() - 1 ).anonRightLabels ) +")";
        return patternString;
    }
}
