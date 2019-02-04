package analyzer.writer;

import analyzer.cypher.ParsedRelationshipBlockChain;
import analyzer.cypher.structure.SubGraphGenerator;
import analyzer.parser.query.QueryLogEntry;
import analyzer.writer.summary.FrequentPatternSummaryTSVWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrequentPatternTSVWriter
{

    private static final float SUMMARIZED_FREQUENT_PATTERN_COUNT_PERCENTAGE_MINIMUM = 0.1f;
    public static void writeParsedLog( String name, Map<String,List<QueryLogEntry>> queryMap, int actualQueryCount )
    {
        try
        {
            SubGraphGenerator subGraphGenerator = new SubGraphGenerator();
            String seperator = " \t ";
            BufferedWriter writer = new BufferedWriter( new PrintWriter( "pattern_output/"+name+".tsv" ) );
            writer.write( "pattern \t length \t count \t original");
            writer.newLine();

            Map<ParsedRelationshipBlockChain, Integer> seenBlockChainsWithCount = new HashMap<>(  );

            System.out.println("Total queries to find subgraphs from: " + queryMap.entrySet().size());

            extractFrequentSubPatterns( queryMap, subGraphGenerator, seenBlockChainsWithCount );
            System.out.println();

            // Sort the output in a nice way
            Object[] frequentPatternArray = seenBlockChainsWithCount.entrySet().toArray();
            sortFrequentPatternsDescending( frequentPatternArray );
            writeOutput( name, actualQueryCount, seperator, writer, frequentPatternArray );

            writer.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private static void extractFrequentSubPatterns( Map<String,List<QueryLogEntry>> queryMap, SubGraphGenerator subGraphGenerator,
            Map<ParsedRelationshipBlockChain,Integer> seenBlockChainsWithCount )
    {
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
    }

    private static void sortFrequentPatternsDescending( Object[] array )
    {
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
    }

    private static void writeOutput( String name, int actualQueryCount, String seperator, BufferedWriter writer, Object[] sortedFrequentPatternArray ) throws IOException
    {
        int oneLengthFrequentPatterns = 0;
        int twoLengthFrequentPatterns = 0;
        int threeLengthFrequentPatterns = 0;
        int fourLengthFrequentPatterns = 0;
        int fiveLengthFrequentPatterns = 0;

        for (Object e : sortedFrequentPatternArray) {
            Map.Entry<ParsedRelationshipBlockChain, Integer> entry = (Map.Entry<ParsedRelationshipBlockChain, Integer>) e;
            Integer frequentPatternCount = entry.getValue();
            ParsedRelationshipBlockChain frequentPattern = entry.getKey();
            int frequentPatternLength = frequentPattern.chain.size();


            if ( frequentPatternCount >= SUMMARIZED_FREQUENT_PATTERN_COUNT_PERCENTAGE_MINIMUM * actualQueryCount )
            {
                switch ( frequentPatternLength ){
                    case 1:
                        oneLengthFrequentPatterns += 1;
                        break;
                    case 2:
                        twoLengthFrequentPatterns += 1;
                        break;
                    case 3:
                        threeLengthFrequentPatterns += 1;
                        break;
                    case 4:
                        fourLengthFrequentPatterns += 1;
                        break;
                    case 5:
                        fiveLengthFrequentPatterns += 1;
                        break;
                    default:
                        // do nothing
                        break;
                }
            }

            writer.write(
                frequentPattern.toAnonPatternString() + seperator +
                    frequentPatternLength + seperator +
                    frequentPatternCount + seperator +
                    frequentPattern.originalUnmodified );
            writer.newLine();
        }
        FrequentPatternSummaryTSVWriter.writeSummaryCounts(
                name,
                actualQueryCount,
                oneLengthFrequentPatterns,
                twoLengthFrequentPatterns,
                threeLengthFrequentPatterns,
                fourLengthFrequentPatterns,
                fiveLengthFrequentPatterns);
    }

}
