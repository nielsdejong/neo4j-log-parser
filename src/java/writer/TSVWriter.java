package writer;

import parser.QueryLogEntry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class TSVWriter
{
    public static void writeParsedLog( String name, Map<String,List<QueryLogEntry>> queries )
    {
        try
        {
            String seperator = "\t ";
            BufferedWriter writer = new BufferedWriter( new PrintWriter( "output/"+name+".tsv" ) );
            writer.write( "cypher_query \t count \t nr_joins \t parsed \t avg_run_time_ms \t total_run_time_ms" );
            writer.newLine();
            for ( Map.Entry<String, List<QueryLogEntry>> entry : queries.entrySet() )
            {
                String line = "";
                line += entry.getValue().get( 0 ).cypherQuery.replace( seperator, "," );
                line += seperator;
                line += entry.getValue().size();
                line += seperator;
                line += entry.getValue().get( 0 ).relCount;
                line += seperator;
                line += entry.getValue().get( 0 ).parsed;
                line += seperator;

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

}
