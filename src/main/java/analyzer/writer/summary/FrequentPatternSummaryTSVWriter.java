package analyzer.writer.summary;

import java.io.BufferedWriter;
import java.io.FileWriter;
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
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter("summarized_output/frequent_patterns.tsv", true));
            writer.write( name + seperator );
            writer.write( totalQueryCount + seperator );
            writer.write( oneLengthCount + seperator );
            writer.write( twoLengthCount + seperator );
            writer.write( threeLengthCount + seperator );
            writer.write( fourLengthCount + seperator );
            writer.write( "" + fiveLengthCount );
            writer.newLine();
            writer.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
}
