package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses an entire query log. Uses a Cypher query parser.
 */
public class QueryLogParser
{
    // Returns, for each unique neo4j database, the queries in the log, differentiated by their cypher string.
    public Map<String,Map<String,List<QueryLogEntry>>> parseAndMapQueries( Map<String, List<String>> queryStringsPerFolder ){
        Map<String, Map<String, List<QueryLogEntry>>> queriesByFolder = new HashMap<>();
        CypherQueryParser parser = new CypherQueryParser();

        for ( Map.Entry<String, List<String>> folderAndQueryStrings : queryStringsPerFolder.entrySet() )
        {
            parser.cache = new HashMap<>();
            parseAllQueriesInSingleFolder( parser, queriesByFolder, folderAndQueryStrings );
        }

        return queriesByFolder;
    }

    private void parseAllQueriesInSingleFolder( CypherQueryParser parser, Map<String,Map<String,List<QueryLogEntry>>> queriesByFolder, Map.Entry<String,List<String>> folderAndQueryStrings )
    {
        int size = folderAndQueryStrings.getValue().size();
        System.out.println( "[PARSER] Parsing queries in folder: " + folderAndQueryStrings.getKey() + " (log_size = " + size + ")");
        Map<String, List<QueryLogEntry>> queriesMappedByCypherString = new HashMap<>();
        int counter = 0;

        for ( String singleQueryString : folderAndQueryStrings.getValue())
        {
            // Just to check the progress we keep a counter.
            counter = updateCounter( size, counter );

            QueryLogEntry query = parser.parse( folderAndQueryStrings.getKey(), singleQueryString );
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

    private int updateCounter( int size, int counter )
    {
        counter++;
        if ( counter % 1000 == 0 ){
            System.out.println( "[PARSER] " + counter + "/" + size );
        }
        return counter;
    }
}
