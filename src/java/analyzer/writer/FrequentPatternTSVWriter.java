package analyzer.writer;

import analyzer.cypher.ParsedRelationshipBlockChain;
import analyzer.cypher.structure.SubGraphGenerator;
import analyzer.parser.QueryLogEntry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrequentPatternTSVWriter
{
    public static void writeParsedLog( String name, Map<String,List<QueryLogEntry>> queryMap )
    {
        try
        {
            SubGraphGenerator subGraphGenerator = new SubGraphGenerator();
            String seperator = " \t ";
            BufferedWriter writer = new BufferedWriter( new PrintWriter( "pattern_output/"+name+".tsv" ) );
            writer.write( "pattern \t count");
            writer.newLine();

            Map<ParsedRelationshipBlockChain, Integer> seenBlockChainsWithCount = new HashMap<>(  );

            System.out.println("Total queries to find subgraphs from: " + queryMap.entrySet().size());
            int count = 0;
            for ( Map.Entry<String, List<QueryLogEntry>> entry : queryMap.entrySet() )
            {
                System.out.print(".");
                if ( entry.getValue().size() == 0 || entry.getValue().get( 0 ).parsed == null ){
                    continue;
                }

                for ( ParsedRelationshipBlockChain block : subGraphGenerator.getAllSubPatternsInQueryGraph( entry.getValue().get( 0 ).parsed,  5 ) ){
                    if ( ! seenBlockChainsWithCount.containsKey( block ) ){
                        seenBlockChainsWithCount.put( block, 0 );
                    }
                    // We see these block chains for a analyzer.cypher query, and the query occurs X times, therefore we see the block (in total) X times.
                    seenBlockChainsWithCount.put(block, seenBlockChainsWithCount.get( block ) + (entry.getValue().size()) );
                }
            }

            System.out.println();

            // Sort the output in a nice way
            Object[] array = seenBlockChainsWithCount.entrySet().toArray();
            Arrays.sort(array, ( o1, o2 ) ->
            {
                Map.Entry<ParsedRelationshipBlockChain, Integer> entry1 = (Map.Entry<ParsedRelationshipBlockChain, Integer>) o1;
                Map.Entry<ParsedRelationshipBlockChain, Integer> entry2 = (Map.Entry<ParsedRelationshipBlockChain, Integer>) o2;

                int compareCounts = entry1.getValue().compareTo( entry2.getValue() );
                if ( compareCounts != 0 ){
                    return -entry1.getValue().compareTo( entry2.getValue() );
                }else{
                    int compareChainLength =  entry1.getKey().compareTo( entry2.getKey() );
                    if ( compareChainLength != 0){
                        return - compareChainLength;
                    } else {
                        return - Integer.compare( entry1.getKey().nonEmptyNodesWithLabels, entry2.getKey().nonEmptyNodesWithLabels);
                    }
                }

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
