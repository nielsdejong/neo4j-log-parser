package analyzer;

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
    public String local_folder;
    public String ip;

//    2018-07-30 14:32:16.150+0100 INFO  7091 ms: (planning: 0, waiting: 0) - 184925 page hits, 2824 page faults - bolt-session
//    bolt
//    dps_user
//    neo4j-java/1.4.6-b8ff39653225eff62c8290de0df5bd4459048c60
//
//    client/10.247.246.138:53113
//    server/10.247.246.12:7687>
//    dps_user - MATCH (root:ManagedObject)-[:child*]->(mo) WHERE root.` _bucket` = $bucket AND root.` _fdn` = $fdn AND NOT mo.` _fdn` STARTS WITH BLAH


}
