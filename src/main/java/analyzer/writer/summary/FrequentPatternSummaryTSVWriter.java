package analyzer.writer.summary;

import analyzer.writer.files.Outputs;

import java.io.BufferedWriter;
import java.io.IOException;

public class FrequentPatternSummaryTSVWriter
{
    public static void writeSummaryCounts( String name,
            int totalQueryCount,
            int oneLengthCount,
            int twoLengthCount,
            int threeLengthCount,
            int fourLengthCount,
            int fiveLengthCount ){
        String seperator = "\t ";
        try (BufferedWriter writer = Outputs.base().dir( "summary" ).file( "frequent_patterns.tsv" ) )
        {
            writer.write( name + seperator );
            writer.write( totalQueryCount + seperator );
            writer.write( oneLengthCount + seperator );
            writer.write( twoLengthCount + seperator );
            writer.write( threeLengthCount + seperator );
            writer.write( fourLengthCount + seperator );
            writer.write( "" + fiveLengthCount );
            writer.newLine();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
}
