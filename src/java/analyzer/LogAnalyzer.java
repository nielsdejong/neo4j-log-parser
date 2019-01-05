package analyzer;

import parser.QueryLogEntry;
import parser.QueryLogParser;
import reader.QueryLogFileCollector;
import reader.QueryLogFileReader;
import writer.SummaryPrinter;
import writer.TSVWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogAnalyzer
{
    private String[] folderNamesToIgnore = {""};

    public static void main(String[] args) {
        new LogAnalyzer().process( "/home/niels/Desktop/customer stuff/" );
    }

    private void process( String logFolder ){

        // First, find all folders with log files.
        System.out.println("[LOG COLLECTOR] Looking for log files...");
        System.out.println("[LOG COLLECTOR] Ignoring all folders containing '" + folderNamesToIgnore[0] + "'");
        Map<String, List<String>> fileNamesPerFolder = new QueryLogFileCollector().getAllFilesInFolder( new HashMap<>(), logFolder, folderNamesToIgnore );
        System.out.println( "[LOG COLLECTOR] "+fileNamesPerFolder.size() + " log folders found." );


        QueryLogFileReader reader = new QueryLogFileReader();
        QueryLogParser parser = new QueryLogParser();
        for ( Map.Entry<String, List<String>> entry : fileNamesPerFolder.entrySet() ){

            System.out.println( "[LOG READER] Reading log files of " + entry.getKey());
            List<String> lines = reader.readAllFilesInSingleFolder( entry.getKey(), entry.getValue() );

            System.out.println( "[PARSER] Parsing logs of " + entry.getKey());
            Map<String,List<QueryLogEntry>> queriesByCypherString = parser.parseAllQueriesInSingleFolder( entry.getKey(), lines );

            System.out.println( "[WRITER] Writing output of " + entry.getKey());
            String name = entry.getKey().substring( logFolder.length() ).replace( "/", "-" );
            TSVWriter.writeParsedLog( name, queriesByCypherString );
            SummaryPrinter.printSummary( logFolder + name, queriesByCypherString) ;
            System.out.println();
        }

    }
}

