package analyzer;

import cypher.Query;
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
        new LogAnalyzer().process( "/home/niels/Desktop/customer stuff/" );
    }

    private void process( String logFolder ){

        // First, find all folders with log files.
        System.out.print("(1/5) Looking for log files...");
        Map<String, List<String>> fileNamesPerFolder = QueryLogFileCollector.getAllFilesInFolder( new HashMap<>(), logFolder );
        System.out.println( " done. "+fileNamesPerFolder.size() + " log folders found." );

        // Then, read all log files.
        System.out.print( "(2/5) Reading all log files...");
        Map<String, List<String>> allLinesPerFolder = new QueryLogFileReader().readAllLinesForAllFiles( fileNamesPerFolder );
        System.out.println( " done.");

        // Then, parse the queries.
        System.out.println("(3/5) Parsing queries...");
        Map<String, Map<String, List<Query>>> queries = new QueryLogParser().parseAndMapQueries( allLinesPerFolder );
        System.out.println( "Done! " + queries.size()+ " folders with queries are parsed." );

        // Write the results to the output files.
        System.out.print("(4/5) Writing parsed log to CSV & Printing Results.");
        for ( Map.Entry<String, Map<String,List<Query>>> singleFolderQueries : queries.entrySet() ){
            String name = singleFolderQueries.getKey().substring( logFolder.length() ).replace( "/", "-" );
            TSVWriter.writeParsedLog( name, singleFolderQueries.getValue() );
            SummaryPrinter.printSummary( name, singleFolderQueries.getValue() );
        }
    }
}

