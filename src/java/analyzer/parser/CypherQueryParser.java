package analyzer.parser;

import analyzer.cypher.ParsedQueryResult;

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

        // Parse the analyzer.cypher bit of the log entry.
        parseCypher( fileName, query );

        // Now that the query is parsed, we can do other things, e.g. count the number of relationships.
        if ( query.parsed != null) {
            query.maxQueryGraphSize = query.parsed.countMaxJoins();
            query.minQueryGraphSize = query.parsed.countMinJoins();
        }

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
            printParsingError( fileName, entireLine, querySplitByTabs );
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
            printParsingError( fileName, entireLine, querySplitByTabs );
            return false;
        }

        if(query.user.equals( "null" ))
            query.user = "";


        query.cypherQuery = getActualCypher( query );
        query.executionTime = getQueryExecutionTime( query );
        return true;
    }

    // The string 'query.query' will have this shape: "$user - MATCH (n) RETURN (n) - {} - {}"
    // We filter out the actual analyzer.cypher from this bit.
    private String getActualCypher( QueryLogEntry query )
    {
        String partOfQueryAfterUserInfo;
        if ( query.query.split( query.user + " - ").length == 2){
            partOfQueryAfterUserInfo = query.query.split( query.user + " - ")[1];
        } else {
            partOfQueryAfterUserInfo = query.query.split( " - ")[1];
        }
        return partOfQueryAfterUserInfo.split( "- \\{" )[0];
    }

    private int getQueryExecutionTime( QueryLogEntry query )
    {
        String[] secondSplit = query.timeStampAndRuntimeInfo.split( " INFO  " );
        if ( secondSplit.length > 1)
        {
            String millsecondsAsString = secondSplit[1].split( " ms: " )[0];
            return Integer.parseInt( millsecondsAsString );
        }
        return 0;
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
            if ( query.cypherQuery != null && !query.cypherQuery.endsWith( "'" )){
                query.cypherQuery += "'";
                return getParsedQueryResult( fileName, query, parsedQueryResult );
            }
            //e.printStackTrace();
            printParsingError( fileName, query.cypherQuery, new String[]{} );
        }
        return parsedQueryResult;
    }

    private static void printParsingError( String fileName, String entireLine, String[] querySplitByTabs )
    {
        System.out.println("[PARSER] ERROR, FILE="+fileName+", CANNOT PARSE: " + entireLine);
    }
}
