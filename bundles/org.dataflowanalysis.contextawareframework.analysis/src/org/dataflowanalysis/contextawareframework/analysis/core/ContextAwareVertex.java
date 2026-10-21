package org.dataflowanalysis.contextawareframework.analysis.core;

import java.util.HashMap;
import java.util.Map;

import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

public class ContextAwareVertex extends DFDVertex {

	public ContextAwareVertex(Node node, Map<Pin, ContextAwareVertex> pinDFDVertexMap, Map<Pin, Flow> pinFlowMap) {
		super(node, pinDFDVertexMap, pinFlowMap);
	}

	/**
     * Creates a clone of the vertex without considering data characteristics nor vertex characteristics
     */
	@Override
    public ContextAwareVertex copy(Map<ContextAwareVertex, ContextAwareVertex> mapping) {
        Map<Pin, DFDVertex> copiedPinDFDVertexMap = new HashMap<>();
        this.pinDFDVertexMap.keySet()
                .forEach(key -> {
                    var oldVertex = this.pinDFDVertexMap.get(key);
                    var newVertice = mapping.getOrDefault(oldVertex, this.pinDFDVertexMap.get(key)
                            .copy(mapping));
                    copiedPinDFDVertexMap.put(key, newVertice);
                    mapping.putIfAbsent(oldVertex, newVertice);
                });
        return new ContextAwareVertex(this.referencedElement, copiedPinDFDVertexMap, new HashMap<>(this.pinFlowMap));
    }
}
