package analyzer.writer;

import analyzer.parser.query.QueryLogEntry;
import analyzer.writer.summary.QueryCountsTSVWriter;
import analyzer.writer.summary.QueryShapeTSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GeneralAnalysisTSVWriter
{
    // TODO: Don't use static variables and methods here

    public static final boolean ANONYMOUS = false;
    public static int ACTUAL_QUERY_COUNT = 0;
//    public static void writeDistinctQueries( String name, Map<String,List<QueryLogEntry>> queries ) {
//
//        new File("output").mkdirs();
//        BufferedWriter writer = new BufferedWriter( new PrintWriter( "output/"+name+".tsv" ) );
//        writer.write( "cypher_query \t " +
//                "count \t " +
//                "isReadQuery \t" +
//    }
    public static void writeParsedLog( String name, Map<String,List<QueryLogEntry>> queries )
    {
        try
        {
            int totalQueryCount = 0;
            int actualQueryCount = 0;
            int totalUniqueQueryCount = 0;
            int totalReadOnlyCount = 0;
            int totalReadAndWriteCount = 0;
            int totalMergeOnlyWriteQueryCount = 0;
            int totalCustomProcedureCount = 0;
            int totalEmptyQueryGraphQueries = 0;
            int totalQueriesWithEdges = 0;
            int totalSingleEdgeQueries = 0;
            int totalTwoEdgeQueries = 0;
            int totalThreeEdgeQueries = 0;
            int totalFourEdgeQueries = 0;
            int totalFiveToNineEdgeQueries = 0;
            int totalMoreThanTenEdgeQueries = 0;
            int totalChainQueries = 0;
            int totalStarQueries = 0;
            int totalTreeQueries = 0;
            int totalForestQueries = 0;
            int totalWithLoops = 0;
            int totalWithUnboundedVariableLength = 0;
            int maxSizeObserved = 0;
            int minSizeObserved = 0;


            String seperator = "\t ";
            new File("output").mkdirs();
            BufferedWriter writer = new BufferedWriter( new PrintWriter( "output/"+name+".tsv" ) );
            writer.write( "cypher_query \t " +
                    "count \t " +
                    "isReadQuery \t" +
                    "isUpdateQuery \t" +
                    "isUnsureUpdate \t" +
                    "hasAnyEdgeInQuery \t" +
                    "is1EdgeQuery \t" +
                    "is2EdgeQuery \t" +
                    "is3EdgeQuery \t" +
                    "is4EdgeQuery \t" +
                    "is5-9EdgeQuery \t" +
                    "isChainQuery \t " +
                    "isTree \t " +
                    "hasLoops \t"+
                    "parsed \t " +
                    "min_qg_size \t " +
                    "max_qg_size \t " +
                    "run_time_mean \t " +
                    "run_time_p=0 \t " +
                    "run_time_p=50 \t " +
                    "run_time_p=75 \t " +
                    "run_time_p=90 \t " +
                    "run_time_p=99 \t " +
                    "run_time_p=100"
                    );
            writer.newLine();
            int counter = 0;
            for ( Map.Entry<String, List<QueryLogEntry>> entry : queries.entrySet() )
            {
                counter++;
                List<QueryLogEntry> queryLogEntries = entry.getValue();

                // The Cypher string of this query (if the data is not to be anonymized)
                String line = "";
                if ( ANONYMOUS ) {
                    line += counter;
                }else {
                    line += entry.getKey().replace( seperator, "," ).substring( 0, Math.min( 10000, entry.getKey().length() ) );
                }
                line += seperator;

                // The count of this query.
                int count = queryLogEntries.size();
                line += count;
                totalQueryCount += count;
                totalUniqueQueryCount += 1;
                line += seperator;

                // Whether this query is a read-only query.
                int readOnly = queryLogEntries.get( 0 ).isReadQuery();
                line += readOnly;
                totalReadOnlyCount += readOnly * count;
                line += seperator;

                // Whether this query is a read/write query.
                int readOrWrite = queryLogEntries.get( 0 ).isUpdateQuery();
                line += readOrWrite;
                totalReadAndWriteCount += readOrWrite * count;
                line += seperator;

                // The query is a "call" custom procedure query.
                if ( readOnly == 0 && readOrWrite == 0){
                    totalCustomProcedureCount += 1 * count;
                }
                // Whether this query only does a merge and no further updates.
                int mergeOnlyQuery = queryLogEntries.get( 0 ).hasMergeInQuery();
                line += mergeOnlyQuery;
                totalMergeOnlyWriteQueryCount += mergeOnlyQuery * count;
                line += seperator;

                int queryIsUnboundedVariableLength = queryLogEntries.get( 0 ).isUnboundedVariableLength();
                totalWithUnboundedVariableLength += queryIsUnboundedVariableLength * count;

                // Get info from the parsed query data.
                // We only do this for actual cypher queries, no weird custom calls!

                if ( queryLogEntries.get( 0 ).parsed == null || ( readOnly == 0 && readOrWrite == 0)){
                    // We couldn't parse this query.
                    line += seperator + seperator + seperator + seperator + seperator + seperator;
                } else {
                    actualQueryCount += count;

                    int hasEdgesInQuery = queryLogEntries.get( 0 ).parsed.hasAnyEdgeInQuery();
                    line += hasEdgesInQuery;
                    totalQueriesWithEdges += hasEdgesInQuery * count;
                    line += seperator;

                    if ( hasEdgesInQuery == 0 ){
                        totalEmptyQueryGraphQueries += count;
                    }

                    int singleEdgeQuery = queryLogEntries.get( 0 ).parsed.isSingleEdgeQuery();
                    line += singleEdgeQuery;
                    totalSingleEdgeQueries += singleEdgeQuery * count;
                    line += seperator;

                    int twoEdgeQuery = queryLogEntries.get( 0 ).parsed.isXLengthQuery(2);
                    line += twoEdgeQuery;
                    totalTwoEdgeQueries += twoEdgeQuery * count;
                    line += seperator;

                    int threeEdgeQuery = queryLogEntries.get( 0 ).parsed.isXLengthQuery(3);
                    line += threeEdgeQuery;
                    totalThreeEdgeQueries += threeEdgeQuery * count;
                    line += seperator;

                    int fourEdgeQuery = queryLogEntries.get( 0 ).parsed.isXLengthQuery(4);
                    line += fourEdgeQuery;
                    totalFourEdgeQueries += fourEdgeQuery * count;
                    line += seperator;

                    int fiveToNineEdgeQuery = queryLogEntries.get( 0 ).parsed.isXLengthQuery(5, 9);
                    line += fiveToNineEdgeQuery;
                    totalFiveToNineEdgeQueries += fiveToNineEdgeQuery * count;
                    line += seperator;

                    int isChainQuery = queryLogEntries.get( 0 ).parsed.isChainQuery();
                    line += isChainQuery;
                    totalChainQueries += isChainQuery * count;
                    line += seperator;

                    int isTreeQuery = queryLogEntries.get( 0 ).parsed.isTree();
                    line += isTreeQuery;
                    totalTreeQueries += isTreeQuery * count;
                    line += seperator;

                    int queryHasLoops = queryLogEntries.get( 0 ).parsed.hasLoops();
                    line += queryHasLoops;
                    totalWithLoops += queryHasLoops * count;
                    line += seperator;

                    // if the query has edges and no loops, it must be a forest.
                    if ( hasEdgesInQuery == 1 && queryHasLoops == 0){
                        totalForestQueries += 1 * count;
                    }

                    line += queryLogEntries.get( 0 ).parsed;
                    line += seperator;

                    // The min/max query graph sizes of this query.
                    line += queryLogEntries.get( 0 ).minQueryGraphSize;
                    line += seperator;
                    int maxQueryGraphSize = queryLogEntries.get( 0 ).maxQueryGraphSize;
                    if ( maxQueryGraphSize == 1000000 ){
                        line += "*";
                    }else {
                        line += maxQueryGraphSize;
                    }
                    line += seperator;

                }


                // Calculate the sum of all the running times.
                // Also sort the list of observed running times.
                float sumOfRunningTime = 0;
                int totalQueryLogEntries = queryLogEntries.size();
                int[] executionTimes = new int[totalQueryLogEntries];
                for ( int i = 0; i < queryLogEntries.size(); i++ )
                {
                    executionTimes[i] = queryLogEntries.get( i ).executionTime;
                    sumOfRunningTime += queryLogEntries.get( i ).executionTime;
                }
                Arrays.sort( executionTimes );
                // mean
                line += (int) (sumOfRunningTime / queryLogEntries.size());
                line += seperator;

                // 0%, 50%, 75%, 90%, 99%, 100% percentiles
                line += (int) executionTimes[0] + seperator;
                line += (int) executionTimes[(int)(totalQueryLogEntries * 0.50)] + seperator;
                line += (int) executionTimes[(int)(totalQueryLogEntries * 0.75)] + seperator;
                line += (int) executionTimes[(int)(totalQueryLogEntries * 0.90)] + seperator;
                line += (int) executionTimes[(int)(totalQueryLogEntries * 0.99)] + seperator;
                line += (int) executionTimes[totalQueryLogEntries - 1] + "";


                writer.write( line );
                writer.newLine();

                // Check min and max qg size...
                minSizeObserved = Math.min( minSizeObserved, entry.getValue().get( 0 ).minQueryGraphSize );
                if ( queryIsUnboundedVariableLength == 1){
                    maxSizeObserved = 1000000;
                } else {
                    maxSizeObserved = Math.max( maxSizeObserved, entry.getValue().get( 0 ).maxQueryGraphSize );
                }
            }
            ACTUAL_QUERY_COUNT = actualQueryCount;
            QueryCountsTSVWriter.writeSummaryCounts( name, totalQueryCount, totalUniqueQueryCount, totalReadOnlyCount, totalReadAndWriteCount, totalMergeOnlyWriteQueryCount, totalCustomProcedureCount );
            totalMoreThanTenEdgeQueries = totalQueriesWithEdges - totalSingleEdgeQueries - totalTwoEdgeQueries - totalThreeEdgeQueries - totalFourEdgeQueries - totalFiveToNineEdgeQueries;
            QueryShapeTSVWriter.writeSummaryCounts( name, actualQueryCount, totalQueriesWithEdges, totalSingleEdgeQueries, totalTwoEdgeQueries, totalThreeEdgeQueries, totalFourEdgeQueries, totalFiveToNineEdgeQueries, totalMoreThanTenEdgeQueries, totalEmptyQueryGraphQueries, totalChainQueries, totalTreeQueries, totalForestQueries, totalWithLoops, totalWithUnboundedVariableLength, minSizeObserved, maxSizeObserved );
            writer.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

}
