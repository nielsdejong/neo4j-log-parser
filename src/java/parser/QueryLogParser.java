package parser;

import cypher.Query;

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
    public Map<String,Map<String,List<Query>>> parseAndMapQueries( Map<String, List<String>> queryStringsPerFolder ){
        Map<String, Map<String, List<Query>>> queriesByFolder = new HashMap<>();
        CypherQueryParser parser = new CypherQueryParser();

        for ( Map.Entry<String, List<String>> folderAndQueryStrings : queryStringsPerFolder.entrySet() )
        {
            parser.cache = new HashMap<>();
            parseAllQueriesInSingleFolder( parser, queriesByFolder, folderAndQueryStrings );
        }

        return queriesByFolder;
    }

    private void parseAllQueriesInSingleFolder( CypherQueryParser parser, Map<String,Map<String,List<Query>>> queriesByFolder, Map.Entry<String,List<String>> folderAndQueryStrings )
    {
        Map<String, List<Query>> queriesMappedByCypherString = new HashMap<>();

        for ( String singleQuery : folderAndQueryStrings.getValue())
        {
            Query query = parser.parse( folderAndQueryStrings.getKey(), singleQuery );
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

}
