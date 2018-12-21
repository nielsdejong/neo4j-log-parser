package cypher;

public class CypherQuery
{
    public CypherQuery( String matchString, String whereString, String returnString )
    {
        this.matchString = matchString;
        this.whereString = whereString;
        this.returnString = returnString;
    }

    String matchString;
    String whereString;
    String returnString;
}
