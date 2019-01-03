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

    public static void main(String[] args) {
        new LogAnalyzer().process( "/home/niels/Desktop/customer stuff/WesternUnion-5705/" );
    }

    private void process( String logFolder ){

        // First, find all folders with log files.
        System.out.println("[LOG COLLECTOR] Looking for log files...");
        Map<String, List<String>> fileNamesPerFolder = new QueryLogFileCollector().getAllFilesInFolder( new HashMap<>(), logFolder );
        System.out.println( "[LOG COLLECTOR] "+fileNamesPerFolder.size() + " log folders found." );

        // Then, read all log files.
        System.out.println( "[LOG READER] Reading all log files...");
        Map<String, List<String>> allLinesPerFolder = new QueryLogFileReader().readAllLinesForAllFiles( fileNamesPerFolder );
        System.out.println( "[LOG READER] done.");

        // Then, parse the queries.
        System.out.println("[PARSER] Parsing queries...");
        Map<String, Map<String, List<QueryLogEntry>>> queries = new QueryLogParser().parseAndMapQueries( allLinesPerFolder );
        System.out.println( "[PARSER] Done! " + queries.size()+ " folders with queries are parsed." );

        // Write the results to the output files.
        System.out.println("[OUTPUT WRITER] Writing parsed log to CSV & Printing summary...");
        for ( Map.Entry<String, Map<String,List<QueryLogEntry>>> singleFolderQueries : queries.entrySet() ){
            String name = singleFolderQueries.getKey().substring( logFolder.length() ).replace( "/", "-" );
            TSVWriter.writeParsedLog( name, singleFolderQueries.getValue() );
            SummaryPrinter.printSummary( logFolder + name, singleFolderQueries.getValue() );
        }
    }
}

