package analyzer.writer.summary;

import analyzer.writer.files.Outputs;

import java.io.BufferedWriter;
import java.io.IOException;

public class QueryShapeTSVWriter
{
    public static void writeSummaryCounts( String name,
            int totalQueryCount,
            int totalQueriesWithEdges,
            int totalSingleEdgeQueries,
            int totalTwoEdgeQueries,
            int totalThreeEdgeQueries,
            int totalFourEdgeQueries,
            int totalFiveToNineEdgeQueries,
            int totalTenOrMoreEdgeQueries,
            int totalEmptyQueryGraphQueries,
            int totalChainQueries,
            int totalTreeQueries,
            int totalForestQueries,
            int totalWithLoops,
            int totalWithRPQs,
            int minSizeObserved,
            int maxSizeObserved ){
        String seperator = "\t ";
        try (BufferedWriter writer = Outputs.base().dir( "summary" ).file( "shapes.tsv" ) )
        {
            writer.write( name + seperator );
            writer.write( totalQueryCount + seperator );
            writer.write( totalQueriesWithEdges + seperator );
            writer.write( totalSingleEdgeQueries + seperator );
            writer.write( totalTwoEdgeQueries + seperator );
            writer.write( totalThreeEdgeQueries + seperator );
            writer.write( totalFourEdgeQueries + seperator );
            writer.write( totalFiveToNineEdgeQueries + seperator );
            writer.write( totalTenOrMoreEdgeQueries + seperator );
            writer.write( totalEmptyQueryGraphQueries + seperator );
            writer.write( totalChainQueries + seperator );
            writer.write( totalTreeQueries + seperator );
            writer.write( totalForestQueries + seperator );
            writer.write( totalWithLoops + seperator );
            writer.write( totalWithRPQs + seperator );
            writer.write( minSizeObserved + seperator );
            if ( maxSizeObserved == 1000000){
                writer.write( "*" );
            }else{
                writer.write( maxSizeObserved + "" );
            }
            writer.newLine();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
}
