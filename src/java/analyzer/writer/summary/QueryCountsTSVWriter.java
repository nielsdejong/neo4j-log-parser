package analyzer.writer.summary;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class QueryCountsTSVWriter
{
    public static void writeSummaryCounts( String name,  int totalQueryCount, int totalUniqueQueryCount, int totalReadOnlyCount, int totalReadAndWriteCount, int totalMergeCount, int totalCustomProcedureCount ){
        String seperator = "\t ";
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter("summarized_output/counts.tsv", true));
            writer.write( name + seperator );
            writer.write( totalQueryCount + seperator );
            writer.write( totalUniqueQueryCount + seperator );
            writer.write( totalReadOnlyCount + seperator );
            writer.write( totalReadAndWriteCount + seperator );
            writer.write( totalMergeCount + seperator );
            writer.write( totalCustomProcedureCount + "" );
            writer.newLine();
            writer.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
}
