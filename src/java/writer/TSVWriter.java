package writer;

import cypher.ParsedRelationshipBlockChain;
import parser.QueryLogEntry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TSVWriter
{
    public static void writeParsedLog( String name, Map<String,List<QueryLogEntry>> queryMap )
    {
        try
        {
            String seperator = " \t ";
            BufferedWriter writer = new BufferedWriter( new PrintWriter( "pattern_output/"+name+".tsv" ) );
            writer.write( "pattern \t count");
            writer.newLine();

            Map<ParsedRelationshipBlockChain, Integer> seenBlockChainsWithCount = new HashMap<>(  );

            for ( Map.Entry<String, List<QueryLogEntry>> entry : queryMap.entrySet() )
            {
                for ( ParsedRelationshipBlockChain block : entry.getValue().get( 0 ).parsed.getAllSubPatternsInQueryGraph( 8 ) ){
                    if ( ! seenBlockChainsWithCount.containsKey( block ) ){
                        seenBlockChainsWithCount.put( block, 0 );
                    }
                    // We see these block chains for a cypher query, and the query occurs X times, therefore we see the block (in total) X times.
                    seenBlockChainsWithCount.put(block, seenBlockChainsWithCount.get( block ) + (1 * entry.getValue().size() ) );
                }

            }

            Object[] array = seenBlockChainsWithCount.entrySet().toArray();

            Arrays.sort(array, ( o1, o2 ) ->
            {
                Map.Entry<ParsedRelationshipBlockChain, Integer> entry1 = (Map.Entry<ParsedRelationshipBlockChain, Integer>) o1;
                Map.Entry<ParsedRelationshipBlockChain, Integer> entry2 = (Map.Entry<ParsedRelationshipBlockChain, Integer>) o2;
                return -entry1.getValue().compareTo( entry2.getValue() );
            } );

            for (Object e : array) {
                Map.Entry<ParsedRelationshipBlockChain, Integer> entry = (Map.Entry<ParsedRelationshipBlockChain, Integer>) e;
                writer.write( entry.getKey().toAnonPatternString() + seperator + entry.getValue() );
                writer.newLine();
            }

            writer.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
}
