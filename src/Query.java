public class Query
{
    public String fullText;
    public String cypherQuery;
    public int relCount;
    public int executionTime;


    public Query( String fullText ){
        this.fullText = fullText;

        String[] split1 = fullText.split( ">\tneo4j -" );
        if( split1.length > 1 )
        {
            this.cypherQuery = split1[1].split( " - *" )[0];
        } else {
            this.cypherQuery = "error";
        }
        this.relCount = cypherQuery.split( "\\[" ).length - 1 +  cypherQuery.split( "--" ).length - 1;

        String[] split2 = fullText.split( " INFO  " );
        String millsecondsAsString = split2[1].split( " ms: \\(pla" )[0];
        executionTime = Integer.parseInt( millsecondsAsString );
    }
}
