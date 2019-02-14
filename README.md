# neo4j-log-analyzer
Parsers and analyzes Neo4j query logs to build a structural summary and discover frequent patterns. 

## Input:
- 1 or more folders containing query.log files.

## Output:

- For each of the logs:
  - For each unique Cypher query:
    - The type of query (read, write, custom procedure)
    - The number of edges in the query graph (0, 1, 2, 3, 4, 5-9, >10)
    - Structural properties of the query (chain, loops, tree, forest)
    - Running time for the different executions of the unique queries.
  - A list of frequent sub-patterns as mined from the query graphs.
 
- Summary information over all logs:
  - The total counts of the number of unique queries, read/write queries and custom procedure calls.
  - The number of k-length patterns that are frequent (>10% of query count)
  - Aggregated information of the its queries' structure and shapes.
  
## How to run:
- Run the main class (LogAnalyzer) with a single argument (the location of your Neo4j query log folders).  When processing many logs, a large amount of assigned memory is recommended. (i.e. run with the `-Xmx16000m` parameter)

## Notes:
The log analyzer requires a modified version of Neo4j 4.0 with a special Cypher parser class in Scala. This class is added to the repository: `scala/CypherSpecialLogParsing.scala`. 
