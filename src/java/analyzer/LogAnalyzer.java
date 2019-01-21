package analyzer;

import analyzer.parser.QueryLogEntry;
import analyzer.parser.QueryLogParser;
import analyzer.reader.QueryLogFileCollector;
import analyzer.reader.QueryLogFileReader;
import analyzer.writer.GeneralAnalysisTSVWriter;
import analyzer.writer.SummaryPrinter;
import analyzer.writer.FrequentPatternTSVWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.cypher.internal.special.CypherSpecialLogParsing;

public class LogAnalyzer
{
    // These should be LOWERCASE!
    private String[] folderNamesToIgnore = {"ignore_me"};

    public static void main(String[] args) {
        new LogAnalyzer().processLogFilesInFolder( "/home/niels/Desktop/customer stuff/Ericsson-5553" );
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
            FrequentPatternTSVWriter.writeParsedLog( name, queriesByCypherString);

            System.out.println("[SUMMARY] Printing summary..." );
            SummaryPrinter.printSummary( logFolder + name, queriesByCypherString);
            System.out.println();

            total += lines.size();
        }
        System.out.println("DONE! " + total + " total lines in all query logs.");
    }


    private void parseAndPrintQuery( String query, CypherSpecialLogParsing cypherSpecialParser )
    {
//        System.out.println("");
//        System.out.println( query );
//        Tuple2<Set<PatternRelationship>,Set<Expression>> result = cypherSpecialParser.doParsing( query );
//        for ( PatternRelationship rel : JavaConversions.asJavaCollection( cypherSpecialParser.doParsing( query )._1)) {
//            System.out.println(rel);
//        }
//        System.out.println( cypherSpecialParser.doParsing( query )._1 );
//        System.out.println( cypherSpecialParser.doParsing( query )._2 );
    }
}

