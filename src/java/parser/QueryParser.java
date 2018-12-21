package parser;

import analyzer.Query;

public class QueryParser
{
    public static Query parse ( String fileName, String entireLine ){

        Query query = new Query();
        String[] tabbed = entireLine.split("\t");

        if ( tabbed.length == 1 ) {
            printParsingError( fileName, entireLine );
            return new Query();
        }
        if ( tabbed.length == 5 ) {
            query.timeStampAndRuntimeInfo = tabbed[0];
            query.protocol = tabbed[1];
            query.ip = tabbed[2];
            query.local_folder = tabbed[3];
            query.query = tabbed[4];
            query.user = "reader_user";
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
            return new Query();
        }

        // Sometimes it breaks.
        if(query.user.equals( "null" ))
            query.user = "";

        query.cypherQuery = query.query.split( query.user + " - ")[1];
        query.relCount = query.cypherQuery.split( "\\[" ).length - 1 +  query.cypherQuery.split( "--" ).length - 1;

        if ( !query.cypherQuery.startsWith( "MATCH" ) )
        {
            //System.out.println("[ERROR!] QUERY DOES NOT START WITH MATCH! " +query.cypherQuery);
        }
        query.cypherQuery = query.cypherQuery.substring( 0, Math.min(query.cypherQuery.length(), 1000) );

        String[] secondSplit = query.timeStampAndRuntimeInfo.split( " INFO  " );
        if ( secondSplit.length > 1)
        {
            String millsecondsAsString = secondSplit[1].split( " ms: \\(pla" )[0];
            query.executionTime = Integer.parseInt( millsecondsAsString );
        }
        return query;
    }

    private static void printParsingError( String fileName, String entireLine )
    {
        System.out.println("[ERROR] FILE="+fileName+", CANNOT PARSE: " + entireLine);
    }
}
