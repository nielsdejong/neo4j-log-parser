/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 * This file is a commercial add-on to Neo4j Enterprise Edition.
 */
package org.neo4j.cypher.internal.special

import org.neo4j.cypher.internal.MasterCompiler
import org.neo4j.cypher.internal.compatibility.v4_0.WrappedMonitors
import org.neo4j.cypher.internal.compiler.v4_0.phases._
import org.neo4j.cypher.internal.compiler.v4_0.planner.ResolveTokens
import org.neo4j.cypher.internal.compiler.v4_0.planner.logical.idp.IDPQueryGraphSolver
import org.neo4j.cypher.internal.compiler.v4_0.planner.logical.{CachedMetricsFactory, OptionalMatchRemover, SimpleMetricsFactory}
import org.neo4j.cypher.internal.compiler.v4_0.{CypherPlannerConfiguration, StatsDivergenceCalculator, defaultUpdateStrategy}
import org.neo4j.cypher.internal.ir.v4_0._
import org.neo4j.cypher.internal.javacompat.GraphDatabaseCypherService
import org.neo4j.cypher.internal.planner.v4_0.spi.PlanningAttributes.{Cardinalities, ProvidedOrders, Solveds}
import org.neo4j.cypher.internal.planner.v4_0.spi.{IDPPlannerName, PlanContext, PlannerNameFor, PlanningAttributes}
import org.neo4j.cypher.internal.runtime.interpreted.TransactionalContextWrapper
import org.neo4j.cypher.internal.special.test.TestGraphDatabaseFactory
import org.neo4j.cypher.internal.spi.v4_0.TransactionBoundPlanContext
import org.neo4j.cypher.internal.v4_0.ast.semantics.SemanticState
import org.neo4j.cypher.internal.v4_0.expressions._
import org.neo4j.cypher.internal.v4_0.frontend.phases.CompilationPhaseTracer.NO_TRACING
import org.neo4j.cypher.internal.v4_0.frontend.phases._
import org.neo4j.cypher.internal.v4_0.rewriting.{Deprecations, RewriterStepSequencer}
import org.neo4j.cypher.internal.v4_0.util.attribution.SequentialIdGen
import org.neo4j.cypher.internal.v4_0.util.{Foldable, InputPosition}
import org.neo4j.internal.kernel.api.Transaction
import org.neo4j.internal.kernel.api.security.LoginContext
import org.neo4j.kernel.impl.query.Neo4jTransactionalContextFactory
import org.neo4j.kernel.monitoring.Monitors
import org.neo4j.values.virtual.VirtualValues.EMPTY_MAP

class CypherSpecialLogParsing {

  private val stuff = PreparatoryRewriting(Deprecations.V1) andThen
    SemanticAnalysis(warn = true).adds(BaseContains[SemanticState]) andThen
    CompilationPhases.lateAstRewriting andThen
    ResolveTokens andThen
    CreatePlannerQuery.adds(CompilationContains[UnionQuery]) andThen
    OptionalMatchRemover

  val graph = new GraphDatabaseCypherService(new TestGraphDatabaseFactory().newImpermanentDatabase())
  val contextFactory = Neo4jTransactionalContextFactory.create(graph)
  val implicitTx = graph.beginTransaction(Transaction.Type.`implicit`, LoginContext.AUTH_DISABLED)
  private def plannerContextCreator(query: String, metricsFactory: CachedMetricsFactory,
                                    config: CypherPlannerConfiguration,
                                    planContext: PlanContext,
                                    queryGraphSolver: IDPQueryGraphSolver, enterprise: Boolean): PlannerContext = {
    val logicalPlanIdGen = new SequentialIdGen()
    PlannerContextCreator.create(NO_TRACING, devNullLogger, planContext, query, Set(),
      None, WrappedMonitors(new Monitors), metricsFactory,
      queryGraphSolver, config, defaultUpdateStrategy, MasterCompiler.CLOCK,
      logicalPlanIdGen, null)
  }

  def doParsing(inputQuery: String): (Set[PatternRelationship], Set[Expression], Boolean, Boolean) = {

    var query = inputQuery.trim()
    if ( query.substring(0, 7).toLowerCase.equals( "profile") || query.substring(0, 7).toLowerCase.equals("explain")){
        query = query.substring(7)
    }


    val neo4jtxContext = contextFactory.newContext(implicitTx, query, EMPTY_MAP)
    val txContextWrapper = TransactionalContextWrapper(neo4jtxContext)
    val planContext = TransactionBoundPlanContext(txContextWrapper, devNullLogger)
    val startState = LogicalPlanState(query, None, PlannerNameFor(IDPPlannerName.name), PlanningAttributes(new Solveds, new Cardinalities, new ProvidedOrders))
    val plannerContext = plannerContextCreator(query, CachedMetricsFactory(SimpleMetricsFactory), CypherPlannerConfiguration(
      queryCacheSize = 0,
      statsDivergenceCalculator = StatsDivergenceCalculator.divergenceNoDecayCalculator(0, 0),
      useErrorsOverWarnings = false,
      idpMaxTableSize = 128,
      idpIterationDuration = 1000,
      errorIfShortestPathFallbackUsedNeo4j 3.5's AtRuntime = false,
      errorIfShortestPathHasCommonNodesAtRuntime = false,
      legacyCsvQuoteEscaping = false,
      csvBufferSize = 2 * 1024 * 1024,
      nonIndexedLabelWarningThreshold = 0,
      planWithMinimumCardinalityEstimates = true,
      lenientCreateRelationship = true)
      , planContext, null, true)
    val parsingBaseState = CompilationPhases.parsing(RewriterStepSequencer.newPlain).transform(startState, plannerContext)
    val resultState = stuff.transform(parsingBaseState, plannerContext)

    // Store the results
    var patterns = Set[PatternRelationship]()
    var hasLabels = Set[Expression]()

    // go through all union queries
    var makesPaths = false
    var merge = false

    // TODO: optimize this somehow, we are doing many tree BFS'es now... (maybe write own function into FoldableAny).

    val foldableAny = Foldable.FoldableAny(resultState.maybeUnionQuery)

    foldableAny.findByAllClass[SetLabelPattern].foreach( pattern => {
        makesPaths = true
    })

    foldableAny.findByAllClass[DeleteExpression].foreach( pattern => {
        makesPaths = true
    })

    foldableAny.findByAllClass[CreateRelationship].foreach( pattern => {
      makesPaths = true
    })

    foldableAny.findByAllClass[CreateNode].foreach( pattern => {
      makesPaths = true
    })

    foldableAny.findByAllClass[RemoveLabelPattern].foreach( pattern => {
      makesPaths = true
    })

    foldableAny.findByAllClass[MergeNodePattern].foreach( pattern => {
      makesPaths = true
      merge = true
    })

    foldableAny.findByAllClass[MergeRelationshipPattern].foreach( pattern => {
      makesPaths = true
      merge = true
    })

    foldableAny.findByAllClass[CreatePattern].foreach( pattern => {
      makesPaths = true
      patterns ++= pattern.relationships.map(r => PatternRelationship(r.idName, (r.startNode, r.endNode), r.direction, Seq(r.relType), SimplePatternLength))
      hasLabels ++= pattern.nodes.map(n => HasLabels(Variable(n.idName)(InputPosition.NONE), n.labels)(InputPosition.NONE))
    })

    foldableAny.findByAllClass[MergePattern].foreach(pattern => {
      makesPaths = true
      merge = true
      patterns ++= pattern.matchGraph.patternRelationships

    })
    foldableAny.findByAllClass[HasLabels].foreach(pattern => {
      hasLabels += pattern
    })

    var tempName = "UNNAMED_NODE_"
    var counter = 0

    foldableAny.findByAllClass[PatternExpression].foreach(pattern => {
      var element = pattern.pattern.element

      // This hacky piece of code makes sure that we can name unspecified nodes in pattern expressions, which is not done by the analyzer.parser...
      var start = if ( element.variable != None ) element.variable.get.name else {counter += 1; tempName + counter }
      var end = if ( element.rightNode.variable != None ) element.rightNode.variable.get.name else { counter += 1; tempName + counter }

      patterns += PatternRelationship(element.relationship.variable.toString, (start, end), element.relationship.direction, element.relationship.types, SimplePatternLength)

      var other = element.element
      while (!other.isInstanceOf[NodePattern]) {
          var otherRelChain = other.asInstanceOf[RelationshipChain]

          var start = if ( otherRelChain.variable != None ) otherRelChain.variable.get.name else { counter += 1; tempName + counter}
          var end = if ( otherRelChain.rightNode.variable != None ) otherRelChain.rightNode.variable.get.name else { counter += 1; tempName + counter }

          patterns += PatternRelationship( otherRelChain.relationship.variable.toString, (start, end), otherRelChain.relationship.direction, otherRelChain.relationship.types, SimplePatternLength)
          other = otherRelChain.element
      }
    })

    return ( patterns, hasLabels, makesPaths, merge )
  }
}
