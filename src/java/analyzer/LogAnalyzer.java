package analyzer;

import parser.QueryParser;
import org.neo4j.cypher.internal.special.CypherSpecialLogParsing;
import writer.SummaryPrinter;
import writer.TSVWriter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogAnalyzer
{
    private String nextLine;

    public static void main(String[] args) {
        CypherSpecialLogParsing cypher = new CypherSpecialLogParsing();
        //new CypherSpecialLogParsing().doParsing( "MATCH (a:A)-[:R1*2..5]->(b:B) RETURN b" );
        for ( int i = 0; i < 100; i++ )
        {
            cypher.doParsing( "MATCH (a:A)-[:R1*2..5]->(b:B) RETURN b" );
        }
        //new LogAnalyzer().process( "/home/niels/Desktop/customer stuff/" );
    }

    private void process( String logFolder ){
        System.out.print("(4/5) Writing parsed log to CSV...");
        Map<String, List<String>> fileNamesPerFolder = getAllFiles( logFolder );
        Map<String, List<String>> allLinesPerFolder = readAllLinesForAllFiles( fileNamesPerFolder );
        Map<String, Map<String, List<Query>>> queries = parseAndMapQueries( allLinesPerFolder );


        // Print output
        for ( Map.Entry<String, Map<String,List<Query>>> singleFolderQueries : queries.entrySet() ){
            String name = singleFolderQueries.getKey().substring( logFolder.length() ).replace( "/", "-" );
            TSVWriter.writeParsedLog( name, singleFolderQueries.getValue() );
            SummaryPrinter.printSummary( name, singleFolderQueries.getValue() );
        }
    }

    private Map<String, List<String>> readAllLinesForAllFiles( Map<String, List<String>> fileNames )
    {
        Map<String, List<String>> allLines = new HashMap<>();
        System.out.print( "(2/5) Reading all log files...");
        for ( Map.Entry<String, List<String>> entry : fileNames.entrySet() )
        {
            List<String> lines = new ArrayList<>();
            for ( String fileName : entry.getValue() )
            {
                lines.addAll( this.readAllLinesForFile( fileName ) );
            }
            allLines.put( entry.getKey(), lines );
        }
        System.out.println( " done.");
        return allLines;
    }

    private List<String> readAllLinesForFile( String file ) {
        List<String> queries = new ArrayList<>();
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String query;
            nextLine = br.readLine();
            while ( ( query = readSingleQuery(br) ) != null)
            {
                queries.add( query );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return queries;
    }

    private Map<String,List<String>> getAllFiles( String logFolder )
    {
        System.out.print("(1/5) Looking for log files...");
        Map<String, List<String>> fileNamesPerFolder = LogFileCollector.getAllFilesInFolder( new HashMap<>(), logFolder );
        System.out.println( " done. "+fileNamesPerFolder.size() + " log folders found." );
        return fileNamesPerFolder;
    }


    // Returns, for each unique neo4j database, the queries in the log, differentiated by their cypher string.
    private Map<String, Map<String, List<Query>>> parseAndMapQueries( Map<String, List<String>> queryStringsPerFolder ){
        System.out.println("(3/5) Parsing queries...");
        Map<String, Map<String, List<Query>>> queriesByFolder = new HashMap<>();

        for ( Map.Entry<String, List<String>> folderAndQueryStrings : queryStringsPerFolder.entrySet() )
        {
            parseAllQueriesInSingleFolder( queriesByFolder, folderAndQueryStrings );
        }
        System.out.println( "Done! " + queriesByFolder.size()+ " folders with queries are parsed." );
        return queriesByFolder;
    }

    private void parseAllQueriesInSingleFolder( Map<String,Map<String,List<Query>>> queriesByFolder, Map.Entry<String,List<String>> folderAndQueryStrings )
    {
        Map<String, List<Query>> queriesMappedByCypherString = new HashMap<>();

        for ( String singleQuery : folderAndQueryStrings.getValue())
        {
            Query query = QueryParser.parse( folderAndQueryStrings.getKey(), singleQuery );
            if( query.cypherQuery != null)
            {
                if ( !queriesMappedByCypherString.containsKey( query.cypherQuery ) )
                {
                    queriesMappedByCypherString.put( query.cypherQuery, new ArrayList<>() );
                }
                queriesMappedByCypherString.get( query.cypherQuery ).add( query );
            }
        }
        queriesByFolder.put( folderAndQueryStrings.getKey(), queriesMappedByCypherString );
    }

    private String readSingleQuery( BufferedReader reader ) throws IOException
    {
        String query =  nextLine;
        while( nextLine != null )
        {
            nextLine = reader.readLine();
            if ( nextLine == null )
            {
                return query;
            }
            else if( nextLine.startsWith( "2018-" ))
            {
                return query;
            } else {
                if (! nextLine.startsWith( "//" ))
                {
                    query +=  " " + nextLine;
                }
            }
        }
        return null;
    }
}

