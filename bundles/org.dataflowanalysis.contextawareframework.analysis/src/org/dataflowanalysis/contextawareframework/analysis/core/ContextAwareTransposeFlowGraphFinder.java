package org.dataflowanalysis.contextawareframework.analysis.core;

import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraph;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.contextawareframework.analysis.context.FlowGraphContext;
import org.dataflowanalysis.contextawareframework.analysis.context.VertexContext;
import org.dataflowanalysis.contextawareframework.analysis.resource.ContextAwareResourceProvider;
import org.dataflowanalysis.contextawareframework.analysis.util.ContextUtils;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextApplication;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ContextAwareTransposeFlowGraphFinder extends DFDTransposeFlowGraphFinder {
    private final ContextAwareResourceProvider resourceProvider;

    public ContextAwareTransposeFlowGraphFinder(ContextAwareResourceProvider resourceProvider) {
        super(resourceProvider);
        this.resourceProvider = resourceProvider;
    }

    @Override
    public List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs() {
        List<? extends AbstractTransposeFlowGraph> transposeFlowGraphs = super.findTransposeFlowGraphs();
        return transposeFlowGraphs.parallelStream()
                .map(this::augmentTransposeFlowGraph)
                .toList();
    }

    public ContextAwareTransposeFlowGraph augmentTransposeFlowGraph(AbstractTransposeFlowGraph transposeFlowGraph) {
		FlowGraphContext flowGraphContext = determineFlowGraphContext((DFDTransposeFlowGraph) transposeFlowGraph);
        Map<DFDVertex, DFDVertex> mapping = new IdentityHashMap<>();
        this.populateFlowGraphMapping(mapping, (DFDVertex) transposeFlowGraph.getSink(), flowGraphContext);
        return new ContextAwareTransposeFlowGraph(mapping.get(transposeFlowGraph.getSink()), flowGraphContext);
    }

    public void populateFlowGraphMapping(Map<DFDVertex, DFDVertex> mapping, DFDVertex sink, FlowGraphContext flowGraphContext) {
        sink.getPreviousElements().parallelStream()
                .map(DFDVertex.class::cast)
                .forEach(it -> this.populateFlowGraphMapping(mapping, it, flowGraphContext));
        mapping.put(sink, augmentVertex(sink, flowGraphContext).copy(mapping));
    }

    public FlowGraphContext determineFlowGraphContext(DFDTransposeFlowGraph transposeFlowGraph) {
        List<ContextApplication> applicableFlowGraphContext = this.resourceProvider.getContext()
                .getContextDefinitions()
                .parallelStream()
                .filter(it -> new ContextUtils(resourceProvider).isApplicable(it, transposeFlowGraph))
                .toList();
        return new FlowGraphContext(applicableFlowGraphContext);
    }

    public ContextAwareVertex augmentVertex(DFDVertex vertex, FlowGraphContext flowGraphContext) {
        return new ContextAwareVertex(vertex.getReferencedElement(), vertex.getPinDFDVertexMap(),
                vertex.getPinFlowMap(), this.determineVertexContext(vertex, flowGraphContext));
    }

    public VertexContext determineVertexContext(DFDVertex vertex, FlowGraphContext flowGraphContext) {
		List<ContextApplication> contextApplications = flowGraphContext.getApplicableContextApplications().parallelStream()
				.filter(it -> new ContextUtils(resourceProvider).isApplicable(it, vertex))
				.toList();
        return new VertexContext(this.resourceProvider, contextApplications);
    }
}
