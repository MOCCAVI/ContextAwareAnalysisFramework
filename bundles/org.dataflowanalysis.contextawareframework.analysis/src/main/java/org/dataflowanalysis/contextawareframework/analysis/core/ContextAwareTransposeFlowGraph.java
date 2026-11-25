package org.dataflowanalysis.contextawareframework.analysis.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraph;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.contextawareframework.analysis.context.FlowGraphContext;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextApplication;

public class ContextAwareTransposeFlowGraph extends DFDTransposeFlowGraph {
    private FlowGraphContext flowGraphContext;

    public ContextAwareTransposeFlowGraph(AbstractVertex<?> sink, FlowGraphContext flowGraphContext) {
        super(sink);
        this.flowGraphContext = flowGraphContext;
    }

    public List<ContextAwareTransposeFlowGraph> exploreContext() {
        List<ContextAwareTransposeFlowGraph> exploredTransposeFlowGraph = new ArrayList<>();
        exploredTransposeFlowGraph.add(this);
        for (ContextAwareVertex originalVertex : this.getVertices().parallelStream().filter(ContextAwareVertex.class::isInstance).map(ContextAwareVertex.class::cast).toList()) {
            exploredTransposeFlowGraph = this.applyVertexAlternative(originalVertex, exploredTransposeFlowGraph);
        }
        return exploredTransposeFlowGraph;
    }

    private List<ContextAwareTransposeFlowGraph> applyVertexAlternative(ContextAwareVertex originalVertex,  List<ContextAwareTransposeFlowGraph> contextAwareTransposeFlowGraphs) {
        List<ContextAwareTransposeFlowGraph> result = new ArrayList<>();
        for (ContextAwareTransposeFlowGraph contextAwareTransposeFlowGraph : contextAwareTransposeFlowGraphs) {
            ContextAwareVertex vertex = contextAwareTransposeFlowGraph.getVertices().parallelStream()
                    .filter(ContextAwareVertex.class::isInstance)
                    .map(ContextAwareVertex.class::cast)
                    .filter(it -> it.getReferencedElement().getId().equals(originalVertex.getReferencedElement().getId()))
                    .findAny().orElseThrow();
            for (ContextAwareVertex replacement : vertex.exploreContext()) {
                Map<DFDVertex, DFDVertex> mapping = new HashMap<>();
                mapping.put(vertex, replacement);
                result.add((ContextAwareTransposeFlowGraph) contextAwareTransposeFlowGraph.copy(mapping));
            }
        }
        return result;
    }

    /**
     * Evaluates the transpose flow graph, beginning at the sink
     * @return Returns a new evaluated transpose flow graph with references to evaluated vertices
     */
    @Override
    public AbstractTransposeFlowGraph evaluate() {
        ContextAwareVertex newSink = ((ContextAwareVertex) sink).copy(new IdentityHashMap<>());
        newSink.unify(new HashSet<>());
        newSink.evaluateDataFlow();
        return new ContextAwareTransposeFlowGraph(newSink, flowGraphContext);
    }

    @Override
    public AbstractTransposeFlowGraph copy() {
        return this.copy(new IdentityHashMap<>());
    }

    @Override
    public AbstractTransposeFlowGraph copy(Map<DFDVertex, DFDVertex> mapping) {
        ContextAwareVertex copiedSink = (ContextAwareVertex) mapping.getOrDefault(sink, ((ContextAwareVertex) sink).copy(mapping));
        copiedSink.unify(new HashSet<>());
        return new ContextAwareTransposeFlowGraph(copiedSink, flowGraphContext);
    }

}
