package analyzer.parser.query;

import analyzer.cypher.ParsedQueryResult;

/**
 * Simple data class that contains the information from a single entry in the query log.
 */
public class QueryLogEntry
{
    public String cypherQuery;
    public int maxQueryGraphSize;
    public int minQueryGraphSize;
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
        if ( parsed == null ){
            return 0;
        }
        if ( parsed.isUpdate ){
            return 1;
        }
        return 0;
//        if( cypherQuery.toUpperCase().contains( "CREATE " ) || cypherQuery.toUpperCase().contains( "SET " ) ||cypherQuery.toUpperCase().contains( "MERGE " ) || cypherQuery.toUpperCase().contains( "DELETE " ) || cypherQuery.toUpperCase().contains( "REMOVE " )){
//            return 1;
//        }
//        return 0;
    }

    public int isReadQuery(){
        if ( cypherQuery == null ){
            return 0;
        }
        if ( cypherQuery.toUpperCase().contains( "MATCH " ) || cypherQuery.toUpperCase().contains( "MERGE " )){
            return 1;
        }
        return 0;
    }


    public int isUnboundedVariableLength(){
        if ( parsed == null || cypherQuery == null ){
            return 0;
        }
        if ( parsed.countMaxJoins() >= 1000000){
            return 1;
        }
        return 0;
    }



    // The query does only a MERGE, so no other write queries. This is the class of queries where we cannot be sure if writes happened.
    public int hasMergeInQuery(){
        if ( parsed == null ){
            return 0;
        }
        if (parsed.hasMerge){
            return 1;
        }
//        if ( cypherQuery.toUpperCase().contains( "MERGE " ) &&
//                ! (cypherQuery.toUpperCase().contains( "CREATE " ) || cypherQuery.toUpperCase().contains( "DELETE " ) || cypherQuery.toUpperCase().contains( "REMOVE " )) ){
//            return 1;
//        }
        return 0;
    }
}
