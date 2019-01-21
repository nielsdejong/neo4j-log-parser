package analyzer.cypher.structure;

import analyzer.cypher.ParsedQueryResult;
import analyzer.cypher.ParsedRelationshipBlock;
import analyzer.cypher.ParsedRelationshipBlockChain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubGraphGenerator
{

    private Map<String, List<ParsedRelationshipBlockChain>> cache = new HashMap<>();

    /**
     * For each block in the query graph, we do a BFS to find its connected blocks, and thus find the subgraphs that originate from the block.
     */
    private List<ParsedRelationshipBlockChain> chainBuildingBFS( Map<String, List<ParsedRelationshipBlock>> blocksConnectedToNodes, List<ParsedRelationshipBlockChain> chainsComingIntoStartNode, String startNode, int depth ){
        // BFS is done. Sometimes we go below zero due to var. length relationship patterns.
        if ( depth <= 0 )
        {
            return chainsComingIntoStartNode;
        }

        List<ParsedRelationshipBlockChain> theNewChains = new ArrayList<>(  );
        for ( ParsedRelationshipBlock block : blocksConnectedToNodes.get( startNode )){


            int minLength = block.getMinLength();

            // Don't go too deep (applies especially to RPQ's)
            int maxLength = Math.min( block.getMaxLength(), depth);

            for ( String type : block.types ){
                // reverse if if it's reversely connected to the end of the chain.
                ParsedRelationshipBlock blockCopy = ( block.rightNodeName == startNode ) ?
                                                    new ParsedRelationshipBlock( block.rightNodeName, block.rightLabels, block.relName, type, block.leftLabels, block.leftNodeName, block.direction.reversed(), 1, 1 ) :
                                                    new ParsedRelationshipBlock( block.leftNodeName, block.leftLabels, block.relName, type, block.rightLabels, block.rightNodeName, block.direction, 1, 1 );

                // Store also the version with missing node labels.
                ParsedRelationshipBlock blockCopyWithoutLeftLabels = new ParsedRelationshipBlock( blockCopy.leftNodeName, new ArrayList<>(), blockCopy.relName, blockCopy.types, blockCopy.rightLabels, blockCopy.rightNodeName, blockCopy.direction, blockCopy.getMinLength(), blockCopy.getMaxLength() );
                ParsedRelationshipBlock blockCopyWithoutRightLabels = new ParsedRelationshipBlock( blockCopy.leftNodeName, blockCopy.leftLabels, blockCopy.relName, blockCopy.types, new ArrayList<>(), blockCopy.rightNodeName, blockCopy.direction, blockCopy.getMinLength(), blockCopy.getMaxLength() );
                ParsedRelationshipBlock blockCopyWithoutEitherLabels = new ParsedRelationshipBlock( blockCopy.leftNodeName, new ArrayList<>(), blockCopy.relName, blockCopy.types, new ArrayList<>(), blockCopy.rightNodeName, blockCopy.direction, blockCopy.getMinLength(), blockCopy.getMaxLength() );

                String otherNodeID = (block.leftNodeName == startNode ) ? block.rightNodeName : block.leftNodeName;

                for ( int i = minLength; i <= maxLength; i++ )
                {
                    List<ParsedRelationshipBlockChain> newChainsByLength = new ArrayList<>(  );
                    for (ParsedRelationshipBlockChain chain : chainsComingIntoStartNode ){

                        // If the chain does NOT already contain this block:
                        if ( ! chain.relIds.contains( block.relName ) && ! chain.nodeIds.contains( block.rightNodeName) ){

                            // Add the block (:A)-[:X]->(:B) to the chain
                            newChainsByLength.add( new ParsedRelationshipBlockChain( chain, blockCopy, i, blockCopyWithoutLeftLabels, blockCopyWithoutEitherLabels, blockCopyWithoutRightLabels ));

                            // Add the block (...)-[:X]->(:B)
                            if ( blockCopy.leftLabels.size() > 0 && chain.chain.size() == 0 )
                            {
                                newChainsByLength.add( new ParsedRelationshipBlockChain( chain, blockCopyWithoutLeftLabels, i, blockCopyWithoutLeftLabels, blockCopyWithoutEitherLabels, blockCopyWithoutEitherLabels ) );
                            }

                            // Add the block (:A)-[:X]->(...)
                            if ( blockCopy.rightLabels.size() > 0 )
                            {
                                newChainsByLength.add( new ParsedRelationshipBlockChain( chain, blockCopyWithoutRightLabels, i, blockCopyWithoutEitherLabels, blockCopyWithoutEitherLabels, blockCopyWithoutRightLabels ) );
                            }

                            // Add the block (...)-[:X]->(...)
                            if ( blockCopy.leftLabels.size() > 0 && chain.chain.size() == 0 && blockCopy.rightLabels.size() > 0)
                            {
                                newChainsByLength.add( new ParsedRelationshipBlockChain( chain, blockCopyWithoutEitherLabels, i, blockCopyWithoutEitherLabels, blockCopyWithoutEitherLabels, blockCopyWithoutEitherLabels ) );
                            }
                        }
                    }
                    // Apprehend to last node in the chain
                    theNewChains.addAll( chainBuildingBFS( blocksConnectedToNodes, newChainsByLength, otherNodeID, depth - i ) );
                }
            }

        }
        theNewChains.addAll( chainsComingIntoStartNode );
        return theNewChains;
    }

    /**
     * Find all linear (chain-shaped) subpatterns in a graph, given a maximum length.
     * We do some caching, since there are many different queries with the same query graph.
     * @param maxSubPatternSize
     * @return
     */
    public List<ParsedRelationshipBlockChain> getAllSubPatternsInQueryGraph( ParsedQueryResult parsedQueryResult, int maxSubPatternSize ){

        if ( cache.containsKey( parsedQueryResult.toString() )){
            return cache.get( parsedQueryResult.toString() );
        }

        List<ParsedRelationshipBlockChain> allBlocksInGraph = new ArrayList<>();

        for ( String nodeName : parsedQueryResult.blocksConnectedToNode.keySet() )
        {
            List<ParsedRelationshipBlockChain> emptyArrayList = new ArrayList<>( );
            emptyArrayList.add( new ParsedRelationshipBlockChain(  ) );
            allBlocksInGraph.addAll ( chainBuildingBFS( parsedQueryResult.blocksConnectedToNode, emptyArrayList, nodeName, maxSubPatternSize ));
        }

        cache.put( parsedQueryResult.toString(), allBlocksInGraph );
        return allBlocksInGraph;
    }

}
