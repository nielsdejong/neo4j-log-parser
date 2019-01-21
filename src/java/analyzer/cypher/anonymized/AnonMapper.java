package analyzer.cypher.anonymized;

import java.util.HashMap;
import java.util.Map;

public class AnonMapper
{
    static String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    static int currentNodeLabelCounter;
    static int currentNodeNameCounter;
    static int currentRelTypeCounter;
    static int currentRelNameCounter;
    static Map<String, String> nodeLabelMap;
    static Map<String, String> relTypeMap;
    static Map<String, String> nodeNameMap;
    static Map<String, String> relNameMap;

    public static void resetForLabels() {
      nodeLabelMap = new HashMap<>();
      relTypeMap = new HashMap<>();
      currentNodeLabelCounter = 0;
      currentRelTypeCounter = 0;
    }

    public static void resetForNames() {
        nodeNameMap = new HashMap<>();
        relNameMap = new HashMap<>();
        currentNodeNameCounter = 0;
        currentRelNameCounter = 0;
    }

    public static String getNodeLabel( String realName )
    {
        if ( nodeLabelMap.containsKey( realName )) {
            return nodeLabelMap.get( realName );
        }
        String anonymyzedName = anonymousNumberedName( currentNodeLabelCounter );
        nodeLabelMap.put( realName, anonymyzedName );
        currentNodeLabelCounter += 1;
        return anonymyzedName;
    }

    public static String getNodeName( String realName )
    {
        if ( nodeNameMap.containsKey( realName )) {
            return nodeNameMap.get( realName );
        }
        String anonymyzedName = "n" + currentNodeNameCounter;
        nodeNameMap.put( realName, anonymyzedName );
        currentNodeNameCounter += 1;
        return anonymyzedName;
    }


    public static String getRelType( String realName )
    {
        if ( relTypeMap.containsKey( realName )) {
            return relTypeMap.get( realName );
        }
        String anonymyzedName = anonymousNumberedName( currentRelTypeCounter );
        relTypeMap.put( realName, anonymyzedName );
        currentRelTypeCounter += 1;
        return anonymyzedName;
    }

    private static String anonymousNumberedName( int currentRelTypeCounter )
    {
        String suffix = "";
        if ( currentRelTypeCounter > 26){
            suffix = "" + currentRelTypeCounter / 26;
        }
        return allowed.substring( currentRelTypeCounter % 26, (currentRelTypeCounter % 26 + 1)) + suffix;
    }

    public static String getRelName( String realName )
    {
        if ( relNameMap.containsKey( realName )) {
            return relNameMap.get( realName );
        }
        String anonymyzedName = "e" + currentRelNameCounter;
        relNameMap.put( realName, anonymyzedName );
        currentRelNameCounter += 1;
        return anonymyzedName;
    }
}
