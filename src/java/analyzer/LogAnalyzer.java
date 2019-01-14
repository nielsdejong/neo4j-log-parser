package analyzer;

import parser.QueryLogEntry;
import parser.QueryLogParser;
import reader.QueryLogFileCollector;
import reader.QueryLogFileReader;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.immutable.Set;
import writer.PatternTSVWriter;
import writer.SummaryPrinter;
import writer.TSVWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.cypher.internal.ir.v4_0.PatternRelationship;
import org.neo4j.cypher.internal.special.CypherSpecialLogParsing;
import org.neo4j.cypher.internal.v4_0.expressions.Expression;

public class LogAnalyzer
{
    private String[] folderNamesToIgnore = {"Ericsson"};

    public static void main(String[] args) {
        //new LogAnalyzer().processLogFilesInFolder( "/home/niels/Desktop/customer stuff/5647/" );
        new LogAnalyzer().processLogFilesInFolder( "/home/niels/Desktop/customer stuff/Homeaway-6224/" );
        //new LogAnalyzer().processTestQueryStrings();
    }

    private void processLogFilesInFolder( String logFolder ){

        // First, find all folders with log files.
        System.out.println("[LOG COLLECTOR] Looking for log files...");
        System.out.println("[LOG COLLECTOR] Ignoring all folders containing '" + folderNamesToIgnore[0] + "'");
        Map<String, List<String>> fileNamesPerFolder = new QueryLogFileCollector().getAllFilesInFolder( new HashMap<>(), logFolder, folderNamesToIgnore );
        System.out.println( "[LOG COLLECTOR] "+fileNamesPerFolder.size() + " log folders found." );


        QueryLogFileReader reader = new QueryLogFileReader();
        QueryLogParser parser = new QueryLogParser();
        for ( Map.Entry<String, List<String>> entry : fileNamesPerFolder.entrySet() ){

            System.out.println( "[LOG READER] Reading log files of " + entry.getKey());
            List<String> lines = reader.readAllFilesInSingleFolder( entry.getKey(), entry.getValue() );

            System.out.println( "[PARSER] Parsing logs of " + entry.getKey());
            Map<String,List<QueryLogEntry>> queriesByCypherString = parser.parseAllQueriesInSingleFolder( entry.getKey(), lines );

            System.out.println( "[WRITER] Writing output of " + entry.getKey());

            // Make a friendly name for the results
            String name = entry.getKey().substring( logFolder.length() ).replace( "/", "-" );

            // Write general analysis results.
            TSVWriter.writeParsedLog( name, queriesByCypherString );

            // Write frequent subpattern analysis results.
            PatternTSVWriter.writeParsedLog( name, queriesByCypherString);

            // Print a summary.
            SummaryPrinter.printSummary( logFolder + name, queriesByCypherString);
            System.out.println();
        }
    }

    private void processTestQueryStrings(){
        // A query that does work
        String query0 = "MATCH (f:JIRAIssue { Key: 'ITDQM-189'})-[r:AFFECTS_VERSION]->(t:JIRAVersion) DELETE r";

        /* Some queries that do not work */

        // Create/Delete queries
        String query1 = "MATCH (o:JIRABoard { ID: 694}), (t:JIRASprint { ID: 3818}) CREATE (o)-[:EATS]->(t2:BOB)";

        // Merge queries
        String query2 = " MATCH(d:D{a:'1'})  MERGE (m:M{a:'2'})  MERGE (d)<-[:B]-(m2: M2{Value:'1',UnitOfMeasure:'Boolean',DateTimeUTC:'2018-06-29 22:34:58.932',TimeStamp:apoc.date.parse('2018-06-29 22:34:58.932','ms','yyyy-MM-dd hh:mm:ss.SSS')})-[:BELONGS_TO]->(metric) ";

        // Query with pattern in where clause
        String query3 = "MATCH (m:MONEYTRANSFER)-[:RECEIVED_BY]->(p:RECEIVER{RECEIVERID:{RECEIVER_ID}}) WHERE NOT exists((:APPLE)-[:EATS]->(:POTATO)-[:LEMON]->(p)) and toInteger(substring(m.MTCN,0,2)) > 16 return count(DISTINCT m.MTCN) as drtTransCountByRec; ";

        // Profile/explain keywords
        // TODO: Just remove manually
        String query4 = "profile MATCH (c1:ClaimRoom{state:'CO'}) USING INDEX c1:ClaimRoom(state) WITH SIZE(collect(c1)) as TotalNum, collect(c1.claimRoomID) as id MATCH (c:ClaimRoom)-[:CONNECTED]->(i:Items) WHERE c.claimRoomID in id WITH i, TotalNum, count(c.claimNum) as NumClaims ORDER BY NumClaims DESC, i.itemID LIMIT 20 RETURN i.itemID,  NumClaims, round(100 * (100.0* NumClaims/ TotalNum))/100 as Perc, TotalNum";

        // Optional matches
        String query5 = "MATCH (re:RECEIVER{RECEIVERID: \"AMANPREETSINGHIN\"}) <-[:RECEIVED_BY]-(mt:MONEYTRANSFER)  WHERE toInteger(substring(mt.MTCN,0,2)) >  16 OPTIONAL MATCH (mt)<-[:SENT_BY]-(pcp:PCP) OPTIONAL MATCH (mt)-[:HAS_SENDER]->(sender:SENDER) OPTIONAL MATCH (mt) <-[:PAYIN_PI]-(pi:PAYMENTINSTRUMENT) OPTIONAL MATCH (mt)-[:HAS_DEVICE]->(de:DEVICE) OPTIONAL MATCH (mt)-[:HAS_IP]->(ip:IPADDRESS) OPTIONAL MATCH (mt)-[:HAS_EMAIL]->(ea:EMAILADDRESS) OPTIONAL MATCH (mt)-[:HAS_ADDRESS]->(ad:ADDRESS) OPTIONAL MATCH (mt)-[:HAS_WEB_SESSION]->(se:SESSION) OPTIONAL MATCH (mt)-[:HAS_PHONE]->(ph:PHONE) RETURN DISTINCT  mt.TRANSACTION_TIMESTAMP, mt.SPACKET_DISPOSITION, mt.TRANSACTION_DRT_COMMENT, mt.MTCN, pcp.CUSTOMERNUMBER, mt.CHANNEL, mt.RISKSEGMENT, pi.ISSUING_COUNTRY_CODE, pi.PAYMENTMETHOD_RISKBINLOOKUP_BIN, pi.LAST4, pi.PAYMENTMETHOD_WUPAY_POSTAUTH_IBANENCRYPTED, mt.GROSSAMOUNT, mt.SENDER_COUNTRYCODE, re.RECEIVER_COUNTRYCODE, (sender.FIRSTNAME+\" \"+sender.LASTNAME), (re.FIRSTNAME +\" \"+re.LASTNAME), ea.EMAILADDR, ad.STREETADDRESS1, reduce(s=\"\", phonenu in collect(ph.PHONENUMBER) |  phonenu+\",\"+s) as phone , mt.NAP, ea.VENDOR_EMLS_RESPONSE_QUERY_RESULTS_ITEM_EASCORE, mt.MODEL_DIST_REAL_IP_VS_SENDER_LOCATION, ip.IPADDRESSLOCCOUNTRYCODE, ip.REALIPADDRESSLOCCOUNTRYCODE, de.DEVICEBROWSERLANG, de.DEVICESCREEN, mt.VARIABLE_CN_RECEIVER_STATE_APP_VALUE, mt.VARIABLE_CN_RECEIVERCOUNTRY_APP_VALUE, mt.VARIABLE_CN_RECEIVERNAME_APP_VALUE, mt.VARIABLE_CN_CREDITCARD_APP_VALUE, mt.VARIABLE_CN_BANKACCOUNT_APP_VALUE, pi.PAYMENTMETHOD_WUPAY_POSTAUTH_FULLNAME  ORDER BY mt.MTCN DESC ";



        CypherSpecialLogParsing cypherSpecialParser = new CypherSpecialLogParsing();
        parseAndPrintQuery( query0, cypherSpecialParser );
        parseAndPrintQuery( query1, cypherSpecialParser );
        parseAndPrintQuery( query2, cypherSpecialParser );
        parseAndPrintQuery( query3, cypherSpecialParser );
        parseAndPrintQuery( query4, cypherSpecialParser );
        parseAndPrintQuery( query5, cypherSpecialParser );
    }

    private void parseAndPrintQuery( String query, CypherSpecialLogParsing cypherSpecialParser )
    {
        System.out.println("");
        System.out.println( query );
        Tuple2<Set<PatternRelationship>,Set<Expression>> result = cypherSpecialParser.doParsing( query );
        for ( PatternRelationship rel : JavaConversions.asJavaCollection( cypherSpecialParser.doParsing( query )._1)) {
            System.out.println(rel);
        }
       // System.out.println( cypherSpecialParser.doParsing( query )._1 );
        System.out.println( cypherSpecialParser.doParsing( query )._2 );
    }
}

