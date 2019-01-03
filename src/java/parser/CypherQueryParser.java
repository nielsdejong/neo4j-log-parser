package parser;

import cypher.ParsedQueryResult;

import java.util.Map;

import org.neo4j.cypher.internal.special.CypherSpecialLogParsing;

public class CypherQueryParser
{
    CypherSpecialLogParsing cypherSpecialParser;
    public Map<String,ParsedQueryResult> cache;


    public CypherQueryParser(){
        cypherSpecialParser = new CypherSpecialLogParsing();
    }

    public QueryLogEntry parse ( String fileName, String entireLine )
    {
        QueryLogEntry query = new QueryLogEntry();

        // If we fail to parse the line, return an empty query log entry.
        if ( !tryParseLineInLog( fileName, entireLine, query ) )
            return new QueryLogEntry();

        // If we stumble upon an EXPLAIN or a PROFILE, we skip this line too.
        if ( query.cypherQuery.startsWith( "profile" ) || query.cypherQuery.startsWith( "explain") || query.cypherQuery.startsWith( "EXPLAIN" )){
            System.out.println("[PARSING ERROR] QUERY CONTAINS EXPLAIN/PROFILE KEYWORD: " + query.cypherQuery);
            return query;
        }

        // Parse the cypher bit of the log entry.
        parseCypher( query );

        // TODO: Make shorter? query.cypherQuery = query.cypherQuery.substring( 0, Math.min(query.cypherQuery.length(), 100) );
        return query;
    }

    private boolean tryParseLineInLog( String fileName, String entireLine, QueryLogEntry query )
    {
        String[] tabbed = entireLine.split("\t");
        if ( tabbed.length == 1 ) {
            printParsingError( fileName, entireLine );
            return true;
        }
        if ( tabbed.length == 5 ) {
            query.timeStampAndRuntimeInfo = tabbed[0];
            query.protocol = tabbed[1];
            query.ip = tabbed[2];
            query.local_folder = tabbed[3];
            query.query = tabbed[4];
            query.user = tabbed[4].split( "-" )[0].trim();
        }
        else if ( tabbed.length == 8 )
        {
            query.timeStampAndRuntimeInfo = tabbed[0];
            query.protocol = tabbed[1];
            query.user = tabbed[2];
            query.version = tabbed[3];
            query.empty = tabbed[4];
            query.client = tabbed[5];
            query.server = tabbed[6];
            query.query = tabbed[7];
        }else{
            printParsingError( fileName, entireLine );
            return false;
        }

        if(query.user.equals( "null" ))
            query.user = "";

        query.cypherQuery = query.query.split( query.user + " - ")[1].split( "- \\{" )[0];
        query.relCount = getRelCount( query );
        getQueryExecutionTime( query );
        return true;
    }

    private int getRelCount( QueryLogEntry query )
    {
        return query.cypherQuery.split( "\\[" ).length - 1 +  query.cypherQuery.split( "--" ).length - 1;
    }

    private void getQueryExecutionTime( QueryLogEntry query )
    {
        String[] secondSplit = query.timeStampAndRuntimeInfo.split( " INFO  " );
        if ( secondSplit.length > 1)
        {
            String millsecondsAsString = secondSplit[1].split( " ms: " )[0];
            query.executionTime = Integer.parseInt( millsecondsAsString );
        }
    }

    private void parseCypher( QueryLogEntry query )
    {
        try{
            ParsedQueryResult parsedQueryResult = cache.get( query.cypherQuery );
            if ( parsedQueryResult == null ) {
                parsedQueryResult  = new ParsedQueryResult(cypherSpecialParser.doParsing( query.cypherQuery ));
                cache.put( query.cypherQuery, parsedQueryResult );
            }
            query.parsed = parsedQueryResult;
        }catch(Exception e ){
            e.printStackTrace();
        }
    }

    private static void printParsingError( String fileName, String entireLine )
    {
        System.out.println("[PARSING ERROR] FILE="+fileName+", CANNOT PARSE: " + entireLine);
    }
}
