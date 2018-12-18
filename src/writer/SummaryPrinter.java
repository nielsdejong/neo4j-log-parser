package writer;

import analyzer.Query;

import java.util.List;
import java.util.Map;

public class SummaryPrinter
{
    public static void printSummary( String name, Map<String,List<Query>> queryMap ){
        int queryCounter = 0;
        long totalRunningTime = 0;
        for ( Map.Entry<String, List<Query>> entry : queryMap.entrySet() )
        {
            queryCounter += entry.getValue().size();
            for ( Query query : entry.getValue() ){
                totalRunningTime += query.executionTime;
            }
        }
        System.out.println();
        System.out.println("------------------------------");
        System.out.println("        "+name);
        System.out.println("------------------------------");
        System.out.println( queryCounter + " total queries.");
        System.out.println( queryMap.size() + " different cypher queries.");
        System.out.println( totalRunningTime + " is the total execution time (ms).");
        System.out.println( (totalRunningTime / 1000) + " is the total execution time (s).");
        System.out.println( (totalRunningTime / 1000.0 / 3600.0) + " is the total execution time (hours).");
    }
}
