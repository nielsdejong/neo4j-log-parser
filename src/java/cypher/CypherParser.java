package cypher;

/**
 * Simple and stupid cypher parsing.
 */
public class CypherParser
{
    public static CypherQuery parse( String cypherString ){
        String matchString = "";
        String whereString = "";
        String returnString = "";
        return new CypherQuery( matchString, whereString, returnString );
    }
}
