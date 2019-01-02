package cypher;

import parser.ParsedQueryResult;

/**
 * Simple data class that contains the information from a Query.
 */
public class Query
{
    public String cypherQuery;
    public int relCount;
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
}
