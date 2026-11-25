package org.dataflowanalysis.contextawareframework.analysis.core;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraphCollection;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.contextawareframework.analysis.resource.ContextAwareResourceProvider;

public class ContextAwareFlowGraphCollection extends DFDFlowGraphCollection {
    private final Logger logger = LoggerManager.getLogger(ContextAwareFlowGraphCollection.class);

    public ContextAwareFlowGraphCollection(ContextAwareResourceProvider resourceProvider) {
        super();
        super.initialize(resourceProvider);
    }

    public ContextAwareFlowGraphCollection(ContextAwareResourceProvider resourceProvider, List<ContextAwareTransposeFlowGraph> flowGraphs) {
        super(resourceProvider, flowGraphs);
    }

    public ContextAwareFlowGraphCollection exploreContext() {
        List<ContextAwareTransposeFlowGraph> exploredFlowGraphs = this.getTransposeFlowGraphs().parallelStream()
                .filter(ContextAwareTransposeFlowGraph.class::isInstance)
                .map(ContextAwareTransposeFlowGraph.class::cast)
                .map(ContextAwareTransposeFlowGraph::exploreContext)
                .flatMap(List::stream)
                .toList();
        return new ContextAwareFlowGraphCollection((ContextAwareResourceProvider) this.resourceProvider, exploredFlowGraphs);
    }

    /**
     * Find a list of transpose flow graphs that are contained in the model provided by the given resource provider
     * @return Returns a list of (unevaluated) transpose flow graphs
     */
    public List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs() {
        if (!(this.resourceProvider instanceof ContextAwareResourceProvider contextResourceProvider)) {
            logger.error("Cannot find transpose flow graphs for non-dfd resource provider");
            throw new IllegalArgumentException();
        }
        this.transposeFlowGraphFinder = new ContextAwareTransposeFlowGraphFinder(contextResourceProvider);

        return transposeFlowGraphFinder.findTransposeFlowGraphs();
    }

}
