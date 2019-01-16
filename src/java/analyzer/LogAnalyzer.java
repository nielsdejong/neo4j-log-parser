package analyzer;

import parser.QueryLogEntry;
import parser.QueryLogParser;
import reader.QueryLogFileCollector;
import reader.QueryLogFileReader;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.immutable.Set;
import writer.GeneralAnalysisTSVWriter;
import writer.SummaryPrinter;
import writer.FrequentPatternTSVWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.cypher.internal.ir.v4_0.PatternRelationship;
import org.neo4j.cypher.internal.special.CypherSpecialLogParsing;
import org.neo4j.cypher.internal.v4_0.expressions.Expression;

public class LogAnalyzer
{
    private String[] folderNamesToIgnore = {"Ericsson"};

    public static void main(String[] args) {
        //new LogAnalyzer().processLogFilesInFolder( "/home/niels/Desktop/customer stuff/" );
        new LogAnalyzer().processLogFilesInFolder( "/home/niels/Desktop/customer stuff/WesternUnion-5705/3" ); // gets stuck somehow
        //new LogAnalyzer().processTestQueryStrings();
    }

    private void processLogFilesInFolder( String logFolder ){

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

            // Make a friendly name for the results
            String name = entry.getKey().substring( logFolder.length() ).replace( "/", "-" );

            // Write general analysis results.
            GeneralAnalysisTSVWriter.writeParsedLog( name, queriesByCypherString );

            System.out.println( "[WRITER] Doing frequent subpattern analysis..." + entry.getKey());

            // Write frequent subpattern analysis results.
            FrequentPatternTSVWriter.writeParsedLog( name, queriesByCypherString);

            System.out.println("[SUMMARY] Printing summary..." );

            // Print a summary.
            SummaryPrinter.printSummary( logFolder + name, queriesByCypherString);
            System.out.println();
        }
    }



    private void parseAndPrintQuery( String query, CypherSpecialLogParsing cypherSpecialParser )
    {
        System.out.println("");
        System.out.println( query );
        Tuple2<Set<PatternRelationship>,Set<Expression>> result = cypherSpecialParser.doParsing( query );
        for ( PatternRelationship rel : JavaConversions.asJavaCollection( cypherSpecialParser.doParsing( query )._1)) {
            System.out.println(rel);
        }
       // System.out.println( cypherSpecialParser.doParsing( query )._1 );
        System.out.println( cypherSpecialParser.doParsing( query )._2 );
    }
}

