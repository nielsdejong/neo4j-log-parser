import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogAnalyzer
{

    private String nextLine;

    public static void main(String[] args) {
        new LogAnalyzer().process( "query-logs" );
    }

    private void process( String logFolder ){

        System.out.println("(1/5) Started!");
        System.out.println("(2/5) Looking for log files...");
        List<String> fileNames = getAllFilesInFolder("query-logs");
        List<String> allLines = new ArrayList<>();

        System.out.println( "(3/5) "+fileNames.size() + " log files found.");
        for ( int i = 0; i < fileNames.size(); i++ )
        {
            allLines.addAll( this.readLog( logFolder + "/" + fileNames.get( i ) ) );
        }

        System.out.println( "(4/5) All files are read.");
        Map<String, List<Query>> queries = convertToQueryObjectsMap( allLines );
        System.out.println( "(5/5) Queries are mapped to their Cypher strings." );
        writeParsedLog( queries );
        printSummary( queries );
    }


    private List<String> getAllFilesInFolder( String folderLocation ){
        File folder = new File( folderLocation );
        File[] listOfFiles = folder.listFiles();
        List<String> fileNames = new ArrayList<>();

        for ( File listOfFile : listOfFiles )
        {
            if ( listOfFile.isFile() )
            {
                fileNames.add( listOfFile.getName() );
            }
        }
        return fileNames;
    }

    private List<String> readLog( String file ) {
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

    private void printSummary( Map<String, List<Query>> queryMap ){
        int queryCounter = 0;
        int totalRunningTime = 0;
        for ( Map.Entry<String, List<Query>> entry : queryMap.entrySet() )
        {
            queryCounter += entry.getValue().size();
            for ( Query query : entry.getValue() ){
                totalRunningTime += query.executionTime;
            }
        }
        System.out.println();
        System.out.println("------------------------------");
        System.out.println("|           SUMMARY          |");
        System.out.println("------------------------------");
        System.out.println( queryCounter + " total queries.");
        System.out.println( queryMap.size() + " different cypher queries.");
        System.out.println( totalRunningTime + " is the total execution time (ms).");
    }

    private Map<String, List<Query>> convertToQueryObjectsMap( List<String> queryStrings ){
        Map<String, List<Query>> queries = new HashMap<>();

        for ( String queryString : queryStrings )
        {
            Query query = new Query( queryString );
            if( !queries.containsKey( query.cypherQuery )) {
                queries.put( query.cypherQuery, new ArrayList<>() );
            }
            queries.get( query.cypherQuery ).add( query );
        }
        return queries;
    }


    private void writeParsedLog( Map<String, List<Query>> queries )
    {
        try
        {

            BufferedWriter writer = new BufferedWriter( new PrintWriter( "output.csv" ) );
            writer.write( "cypher_query, count, nr_joins, avg_run_time_ms, total_run_time_ms" );
            writer.newLine();
            for ( Map.Entry<String, List<Query>> entry : queries.entrySet() )
            {
                String line = "";
                line += entry.getKey().replace( ",", "(COMMA)" );
                line += ", ";
                line += entry.getValue().size();
                line += ", ";
                line += entry.getValue().get( 0 ).relCount;
                line += ", ";
                float sumOfRunningTime = 0;
                for( Query q : entry.getValue() ){
                    sumOfRunningTime += q.executionTime;
                }
                line += (int) (sumOfRunningTime / entry.getValue().size());
                line += ", ";
                line += (int) sumOfRunningTime;
                writer.write( line );
                writer.newLine();
            }
            writer.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
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

