# neo4j-log-parser
Parsers and analyzes Neo4j query logs.
For each unique Cypher query in the log, we record the following:
- number of time this unique Cypher query has appeared.
- Average execution time (ms) for this query.
- percentage of the total execution time this type of query has taken.
- number of relationships in the query.
