package parser;

import cypher.ParsedQueryResult;
import scala.Tuple2;

/**
 * Simple data class that contains the information from a single entry in the query log.
 */
public class QueryLogEntry
{
    public String cypherQuery;
    public String maxQueryGraphSize;
    public String minQueryGraphSize;
    public int executionTime;
    public String protocol;
    public String timeStampAndRuntimeInfo;
    public String user;
    public String version;
    public String empty;
    public String client;
    public String server;
    public String query;
    public ParsedQueryResult parsed;
    public String local_folder;
    public String ip;

    public int isUpdateQuery(){
        //CREATE SET MERGE DELETE REMOVE
        if ( cypherQuery == null ){
            return 0;
        }
        if( cypherQuery.toUpperCase().contains( "CREATE " ) || cypherQuery.contains( "SET " ) ||cypherQuery.contains( "MERGE " ) || cypherQuery.contains( "DELETE " ) || cypherQuery.contains( "REMOVE " )){
            return 1;
        }
        return 0;
    }

    public int isMatchQuery(){
        if ( cypherQuery == null ){
            return 0;
        }
        if ( cypherQuery.toUpperCase().contains( "MATCH " ) || cypherQuery.toUpperCase().contains( "MERGE " )){
            return 1;
        }
        return 0;
    }
}
