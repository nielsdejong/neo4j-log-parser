package reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a single query log.
 */
public class QueryLogFileReader
{
    private String nextLine;

    @Deprecated
    public Map<String,List<String>> readAllLinesForAllFiles( Map<String, List<String>> fileNames )
    {
        Map<String, List<String>> allLines = new HashMap<>();

        int counter = 0;
        for ( Map.Entry<String, List<String>> entry : fileNames.entrySet() )
        {
            List<String> lines = readAllFilesInSingleFolder( entry.getKey(), entry.getValue() );
            allLines.put( entry.getKey(), lines );
        }
        System.out.println( "Read all files. Total number of log lines read: " + counter + " lines.");
        return allLines;
    }

    public List<String> readAllFilesInSingleFolder( String folderName, List<String> fileNames )
    {
        List<String> lines = new ArrayList<>();

        for ( String fileName : fileNames )
        {
            lines.addAll( this.readAllLinesForFile( fileName ) );
        }
        // allLines.put( folderName, lines );

        System.out.println( "[LOG READER] Folder " + folderName + " has " + lines.size() + " lines.");
        return lines;
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
