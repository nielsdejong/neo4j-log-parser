package analyzer.writer;

import analyzer.parser.QueryLogEntry;
import analyzer.writer.summary.QueryCountsTSVWriter;
import analyzer.writer.summary.QueryShapeTSVWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class GeneralAnalysisTSVWriter
{
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
            int totalChainQueries = 0;
            int totalStarQueries = 0;
            int totalTreeQueries = 0;
            int totalForestQueries = 0;
            int totalWithLoops = 0;
            int totalWithRPQs = 0;
            int maxSizeObserved = 0;
            int minSizeObserved = 0;


            String seperator = "\t ";
            BufferedWriter writer = new BufferedWriter( new PrintWriter( "output/"+name+".tsv" ) );
            writer.write( "cypher_query \t " +
                    "count \t " +
                    "isReadQuery \t" +
                    "isUpdateQuery \t" +
                    "doesProbablyUpdate \t" +
                    "min_qg_size \t " +
                    "max_qg_size \t " +
                    "hasAnyEdgeInQuery \t" +
                    "isSingleEdgeQuery \t" +
                    "isChainQuery \t " +
                    "isTree \t " +
                    "hasLoops \t"+
                    "parsed \t " +
                    "avg_run_time_ms \t " +
                    "total_run_time_ms" );
            writer.newLine();
            for ( Map.Entry<String, List<QueryLogEntry>> entry : queries.entrySet() )
            {
                // The Cypher string of this query.
                String line = "";
                line += entry.getKey().replace( seperator, "," ).substring( 0, Math.min( 1000, entry.getKey().length() ) );
                line += seperator;

                // The count of this query.
                int count = entry.getValue().size();
                line += count;
                totalQueryCount += count;
                totalUniqueQueryCount += 1;
                line += seperator;

                // Whether this query is a read-only query.
                int readOnly = entry.getValue().get( 0 ).isReadQuery();
                line += readOnly;
                totalReadOnlyCount += readOnly * count;
                line += seperator;

                // Whether this query is a read/write query.
                int readOrWrite = entry.getValue().get( 0 ).isUpdateQuery();
                line += readOrWrite;
                totalReadAndWriteCount += readOrWrite * count;
                line += seperator;

                // The query is a "call" custom procedure query.
                if ( readOnly == 0 && readOrWrite == 0){
                    totalCustomProcedureCount += 1 * count;
                }
                // Whether this query only does a merge and no further updates.
                int mergeOnlyQuery = entry.getValue().get( 0 ).isOnlyMergeQuery();
                line += mergeOnlyQuery;
                totalMergeOnlyWriteQueryCount += mergeOnlyQuery * count;
                line += seperator;

                int queryisRPQ = entry.getValue().get( 0 ).isRPQ();
                totalWithRPQs += queryisRPQ * count;

                // Get info from the parsed query data.
                // We only do this for actual cypher queries, no weird custom calls!

                if ( entry.getValue().get( 0 ).parsed == null || ( readOnly == 0 && readOrWrite == 0)){
                    // We couldn't parse this query.
                    line += seperator + seperator + seperator + seperator + seperator + seperator;
                } else {
                    actualQueryCount += count;

                    int hasEdgesInQuery = entry.getValue().get( 0 ).parsed.hasAnyEdgeInQuery();
                    line += hasEdgesInQuery;
                    totalQueriesWithEdges += hasEdgesInQuery * count;
                    line += seperator;

                    if ( hasEdgesInQuery == 0 ){
                        totalEmptyQueryGraphQueries += count;
                    }

                    int singleEdgeQuery = entry.getValue().get( 0 ).parsed.isSingleEdgeQuery();
                    line += singleEdgeQuery;
                    totalSingleEdgeQueries += singleEdgeQuery * count;
                    line += seperator;

                    int isChainQuery = entry.getValue().get( 0 ).parsed.isChainQuery();
                    line += isChainQuery;
                    totalChainQueries += isChainQuery * count;
                    line += seperator;

                    int isTreeQuery = entry.getValue().get( 0 ).parsed.isTree();
                    line += isTreeQuery;
                    totalTreeQueries += isTreeQuery * count;
                    line += seperator;

                    int queryHasLoops = entry.getValue().get( 0 ).parsed.hasLoops();
                    line += queryHasLoops;
                    totalWithLoops += queryHasLoops * count;
                    line += seperator;

                    // if the query has edges and no loops, it must be a forest.
                    if ( hasEdgesInQuery == 1 && queryHasLoops == 0){
                        totalForestQueries += 1 * count;
                    }

                    minSizeObserved = Math.min( minSizeObserved, entry.getValue().get( 0 ).minQueryGraphSize );
                    if ( queryisRPQ == 1){
                        maxSizeObserved = 1000000;
                    } else {
                        maxSizeObserved = Math.max( maxSizeObserved, entry.getValue().get( 0 ).maxQueryGraphSize );
                    }

                    line += entry.getValue().get( 0 ).parsed;
                    line += seperator;

                    // The min/max query graph sizes of this query.
                    line += entry.getValue().get( 0 ).minQueryGraphSize;
                    line += seperator;
                    line += entry.getValue().get( 0 ).maxQueryGraphSize;
                    line += seperator;

                }

                float sumOfRunningTime = 0;
                for( QueryLogEntry q : entry.getValue() ){
                    sumOfRunningTime += q.executionTime;
                }
                line += (int) (sumOfRunningTime / entry.getValue().size());
                line += seperator;
                line += (int) sumOfRunningTime;
                writer.write( line );
                writer.newLine();
            }
            QueryCountsTSVWriter.writeSummaryCounts( name, totalQueryCount, totalUniqueQueryCount, totalReadOnlyCount, totalReadAndWriteCount, totalMergeOnlyWriteQueryCount, totalCustomProcedureCount );
            QueryShapeTSVWriter.writeSummaryCounts( name, actualQueryCount, totalQueriesWithEdges, totalSingleEdgeQueries, totalEmptyQueryGraphQueries, totalChainQueries, totalTreeQueries, totalForestQueries, totalWithLoops, totalWithRPQs, minSizeObserved, maxSizeObserved );
            writer.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

}
