package parser;

import scala.Tuple2;
import scala.collection.immutable.Set;

import org.neo4j.cypher.internal.ir.v4_0.PatternRelationship;
import org.neo4j.cypher.internal.v4_0.expressions.Expression;

public class ParsedQueryResult
{
    public final Set<PatternRelationship> patternRelationshipSet;
    public final Set<Expression> expressionSet;
    public ParsedQueryResult(  Tuple2<Set<PatternRelationship>,Set<Expression>> tuple2 )
    {
        this.patternRelationshipSet = tuple2._1;
        this.expressionSet = tuple2._2;
    }

    public String toString(){
        return patternRelationshipSet + ", " + expressionSet;
    }
}

