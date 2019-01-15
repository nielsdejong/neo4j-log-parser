package writer;

import parser.QueryLogEntry;

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
            String seperator = "\t ";
            BufferedWriter writer = new BufferedWriter( new PrintWriter( "output/"+name+".tsv" ) );
            writer.write( "cypher_query \t " +
                    "count \t " +
                    "isMatchQuery \t" +
                    "isUpdateQuery \t" +
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
                String line = "";
                line += entry.getKey().replace( seperator, "," ).substring( 0, Math.min( 1000, entry.getKey().length() ) );
                line += seperator;
                line += entry.getValue().size();
                line += seperator;
                line += entry.getValue().get( 0 ).isMatchQuery();
                line += seperator;
                line += entry.getValue().get( 0 ).isUpdateQuery();
                line += seperator;
                line += entry.getValue().get( 0 ).minQueryGraphSize;
                line += seperator;
                line += entry.getValue().get( 0 ).maxQueryGraphSize;
                line += seperator;
                line = getParsedInfo( seperator, entry, line );

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
            writer.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private static String getParsedInfo( String seperator, Map.Entry<String,List<QueryLogEntry>> entry, String line )
    {
        if ( entry.getValue().get( 0 ).parsed == null ){
            return line + seperator + seperator + seperator + seperator + seperator + seperator;
        }
        line += entry.getValue().get( 0 ).parsed.hasAnyEdgeInQuery();
        line += seperator;
        line += entry.getValue().get( 0 ).parsed.isSingleEdgeQuery();
        line += seperator;
        line += entry.getValue().get( 0 ).parsed.isChainQuery();
        line += seperator;
        line += entry.getValue().get( 0 ).parsed.isTree();
        line += seperator;
        line += entry.getValue().get( 0 ).parsed.hasLoops();
        line += seperator;
        line += entry.getValue().get( 0 ).parsed;
        line += seperator;
        return line;
    }
}
