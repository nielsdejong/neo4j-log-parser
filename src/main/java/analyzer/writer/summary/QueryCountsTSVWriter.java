package analyzer.writer.summary;

import analyzer.writer.files.Outputs;

import java.io.BufferedWriter;
import java.io.IOException;

public class QueryCountsTSVWriter
{
    public static void writeSummaryCounts( String name,  int totalQueryCount, int totalUniqueQueryCount, int totalReadOnlyCount, int totalReadAndWriteCount, int totalMergeCount, int totalCustomProcedureCount ){
        String seperator = "\t ";
        try (BufferedWriter writer = Outputs.base().dir( "summary" ).file( "counts.tsv" ) )
        {
            writer.write( name + seperator );
            writer.write( totalQueryCount + seperator );
            writer.write( totalUniqueQueryCount + seperator );
            writer.write( totalReadOnlyCount + seperator );
            writer.write( totalReadAndWriteCount + seperator );
            writer.write( totalMergeCount + seperator );
            writer.write( totalCustomProcedureCount + "" );
            writer.newLine();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
}
