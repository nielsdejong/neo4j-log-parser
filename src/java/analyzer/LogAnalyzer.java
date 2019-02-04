package analyzer;

import analyzer.parser.query.QueryLogEntry;
import analyzer.parser.QueryLogParser;
import analyzer.reader.collector.QueryLogFileCollector;
import analyzer.reader.QueryLogFileReader;
import analyzer.writer.GeneralAnalysisTSVWriter;
import analyzer.writer.SummaryPrinter;
import analyzer.writer.FrequentPatternTSVWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogAnalyzer
{
    // These names should be provided LOWERCASE! (in the reverse case we only do this file.)
    private String[] folderNamesToIgnore = {"zzzhomeaway"};

    public static void main(String[] args) {
        new LogAnalyzer().processLogFilesInFolder( "/home/niels/Desktop/customer stuff/" );
    }

    private void processLogFilesInFolder( String logFolder ){

        // First, find all folders with log files.
        System.out.println("[LOG COLLECTOR] Looking for log files...");
        System.out.println("[LOG COLLECTOR] Ignoring all folders containing '" + folderNamesToIgnore[0] + "'");
        Map<String, List<String>> fileNamesPerFolder = new QueryLogFileCollector().getAllFilesInFolder( new HashMap<>(), logFolder, folderNamesToIgnore );
        System.out.println( "[LOG COLLECTOR] "+fileNamesPerFolder.size() + " log folders found." );

        QueryLogFileReader reader = new QueryLogFileReader();
        QueryLogParser parser = new QueryLogParser();

        int total = 0;
        for ( Map.Entry<String, List<String>> entry : fileNamesPerFolder.entrySet() ){
            // Make a friendly name for the results
            String name = entry.getKey().substring( logFolder.length() ).replace( "/", "-" );

            System.out.println( "[LOG READER] Reading log files of " + entry.getKey());
            List<String> lines = reader.readAllFilesInSingleFolder( entry.getKey(), entry.getValue() );

            System.out.println( "[PARSER] Parsing logs of " + entry.getKey());
            Map<String,List<QueryLogEntry>> queriesByCypherString = parser.parseAllQueriesInSingleFolder( entry.getKey(), lines );

            System.out.println( "[WRITER] Writing output of " + entry.getKey());
            GeneralAnalysisTSVWriter.writeParsedLog( name, queriesByCypherString );

            System.out.println( "[WRITER] Doing frequent subpattern analysis..." );
            FrequentPatternTSVWriter.writeParsedLog( name, queriesByCypherString, GeneralAnalysisTSVWriter.ACTUAL_QUERY_COUNT );

            System.out.println("[SUMMARY] Printing summary..." );
            SummaryPrinter.printSummary( logFolder + name, queriesByCypherString);
            System.out.println();

            total += lines.size();
        }
        System.out.println("DONE! " + total + " total lines in all query logs.");
    }
}

