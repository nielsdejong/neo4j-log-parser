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

        // CREATE UNIQUE is no longer supported, so we replace it
        entireLine = entireLine.replace( "CREATE UNIQUE", "MERGE" );


        // If we fail to parse the line, return an empty query log entry.
        if ( !tryParseLineInLog( fileName, entireLine, query ) )
            return new QueryLogEntry();

        // If we stumble upon an EXPLAIN or a PROFILE, we skip this line too.
        if ( query.cypherQuery == null || query.cypherQuery.startsWith( "profile" ) || query.cypherQuery.startsWith( "explain") || query.cypherQuery.startsWith( "EXPLAIN" )){
            System.out.println("[PARSER] ERROR! QUERY CONTAINS EXPLAIN/PROFILE KEYWORD: " + query.cypherQuery);
            return query;
        }
        // Parse the cypher bit of the log entry.
        parseCypher( fileName, query );

        // TODO: Make shorter? query.cypherQuery = query.cypherQuery.substring( 0, Math.min(query.cypherQuery.length(), 100) );
        return query;
    }

    /**
     * This method parses a single line in a log, and stores the resulting information in the QueryLogEntry object passed to it.
     * @param fileName  the name of the folder this log entry lives in.
     * @param entireLine  a single line in the log.
     * @param query an object storing information about a single line in the log. l
     * @return
     */
    private boolean tryParseLineInLog( String fileName, String entireLine, QueryLogEntry query )
    {
        String[] querySplitByTabs = entireLine.split("\t");
        if ( querySplitByTabs.length == 1 ) {
            printParsingError( fileName, entireLine );
            return true;
        }
        if ( querySplitByTabs.length == 5 ) {
            query.timeStampAndRuntimeInfo = querySplitByTabs[0];
            query.protocol = querySplitByTabs[1];
            query.ip = querySplitByTabs[2];
            query.local_folder = querySplitByTabs[3];
            query.query = querySplitByTabs[4];
            query.user = querySplitByTabs[4].split( "-" )[0].trim();
        }
        else if ( querySplitByTabs.length == 8 )
        {
            query.timeStampAndRuntimeInfo = querySplitByTabs[0];
            query.protocol = querySplitByTabs[1];
            query.user = querySplitByTabs[2];
            query.version = querySplitByTabs[3];
            query.empty = querySplitByTabs[4];
            query.client = querySplitByTabs[5];
            query.server = querySplitByTabs[6];
            query.query = querySplitByTabs[7];
        }else{
            printParsingError( fileName, entireLine );
            return false;
        }

        if(query.user.equals( "null" ))
            query.user = "";


        getActualCypher( query );
        query.relCount = getRelCount( query );
        getQueryExecutionTime( query );
        return true;
    }

    // The string 'query.query' will have this shape: "$user - MATCH (n) RETURN (n) - {} - {}"
    // We filter out the actual cypher from this bit.
    private void getActualCypher( QueryLogEntry query )
    {
        String partOfQueryAfterUserInfo;
        if ( query.query.split( query.user + " - ").length == 2){
            partOfQueryAfterUserInfo = query.query.split( query.user + " - ")[1];
        } else {
            partOfQueryAfterUserInfo = query.query.split( " - ")[1];
        }
        query.cypherQuery = partOfQueryAfterUserInfo.split( "- \\{" )[0];
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

    private void parseCypher( String fileName, QueryLogEntry query )
    {
        try{
            ParsedQueryResult parsedQueryResult = cache.get( query.cypherQuery );
            if ( parsedQueryResult == null ) {
                parsedQueryResult = getParsedQueryResult( fileName, query, parsedQueryResult );
            }
            query.parsed = parsedQueryResult;
        }catch(Exception e ){
            e.printStackTrace();
        }
    }

    private ParsedQueryResult getParsedQueryResult( String fileName, QueryLogEntry query, ParsedQueryResult parsedQueryResult )
    {
        try
        {
            parsedQueryResult = new ParsedQueryResult( cypherSpecialParser.doParsing( query.cypherQuery ) );
            cache.put( query.cypherQuery, parsedQueryResult );
        } catch ( Exception e ){
            // Sometimes a ' character disappears in some of the logs, so just retry with this extra character...
            if ( !query.cypherQuery.endsWith( "'" )){
                query.cypherQuery += "'";
                return getParsedQueryResult( fileName, query, parsedQueryResult );
            }
            //e.printStackTrace();
            printParsingError( fileName, query.cypherQuery );
        }
        return parsedQueryResult;
    }

    private static void printParsingError( String fileName, String entireLine )
    {
        System.out.println("[PARSER] ERROR, FILE="+fileName+", CANNOT PARSE: " + entireLine);
    }
}
