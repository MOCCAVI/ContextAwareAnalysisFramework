package org.dataflowanalysis.contextawareframework.analysis.core;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;

import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraph;

public class ContextAwareTransposeFlowGraph extends DFDTransposeFlowGraph {

	public ContextAwareTransposeFlowGraph(AbstractVertex<?> sink) {
		super(sink);
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
        return new DFDTransposeFlowGraph(newSink);
    }

    @Override
    public AbstractTransposeFlowGraph copy() {
        return this.copy(new IdentityHashMap<>());
    }

    @Override
    public AbstractTransposeFlowGraph copy(Map<ContextAwareVertex, ContextAwareVertex> mapping) {
    	ContextAwareVertex copiedSink = ((ContextAwareVertex) sink).copy(mapping);
        copiedSink.unify(new HashSet<>());
        return new DFDTransposeFlowGraph(copiedSink);
    }

}
