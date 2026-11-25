package org.dataflowanalysis.contextawareframework.analysis.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.contextawareframework.analysis.context.VertexContext;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

public class ContextAwareVertex extends DFDVertex {
    private VertexContext vertexContext;

    public ContextAwareVertex(Node node, Map<Pin, DFDVertex> pinDFDVertexMap, Map<Pin, Flow> pinFlowMap,
            VertexContext vertexContext) {
        super(node, pinDFDVertexMap, pinFlowMap);
        this.vertexContext = vertexContext;
    }

    public List<ContextAwareVertex> exploreContext() {
        return vertexContext.computeAlternatives(this);
    }

    @Override
    public void evaluateDataFlow() {
        if (!super.isEvaluated()) {
            super.evaluateDataFlow();
            this.vertexCharacteristics.get()
                    .addAll(vertexContext.getVertexCharacteristics(this));
            this.outgoingDataCharacteristics.get()
                    .addAll(vertexContext.getDataCharacteristics(this));
        }
    }

    @Override
    public void evaluateDataFlow(List<Pin> outPinsForOutgoing) {
        if (!super.isEvaluated()) {
            super.evaluateDataFlow(outPinsForOutgoing);
            this.vertexCharacteristics.get()
                    .addAll(vertexContext.getVertexCharacteristics(this));
            this.outgoingDataCharacteristics.get()
                    .addAll(vertexContext.getDataCharacteristics(this));
        }
    }

    public ContextAwareVertex copy(Map<DFDVertex, DFDVertex> mapping, VertexContext vertexContext) {
        Map<Pin, DFDVertex> copiedPinDFDVertexMap = new HashMap<>();
        this.pinDFDVertexMap.keySet()
                .forEach(key -> {
                    var oldVertex = this.pinDFDVertexMap.get(key);
                    var newVertex = mapping.getOrDefault(oldVertex, this.pinDFDVertexMap.get(key)
                            .copy(mapping));
                    copiedPinDFDVertexMap.put(key, newVertex);
                    mapping.putIfAbsent(oldVertex, newVertex);
                });
        return new ContextAwareVertex(this.referencedElement, copiedPinDFDVertexMap, new HashMap<>(this.pinFlowMap),
                vertexContext);
    }

    /**
     * Creates a clone of the vertex without considering data characteristics nor vertex characteristics
     */
    @Override
    public ContextAwareVertex copy(Map<DFDVertex, DFDVertex> mapping) {
        Map<Pin, DFDVertex> copiedPinDFDVertexMap = new HashMap<>();
        this.pinDFDVertexMap.keySet()
                .forEach(key -> {
                    var oldVertex = this.pinDFDVertexMap.get(key);
                    var newVertex = mapping.getOrDefault(oldVertex, this.pinDFDVertexMap.get(key)
                            .copy(mapping));
                    copiedPinDFDVertexMap.put(key, newVertex);
                    mapping.putIfAbsent(oldVertex, newVertex);
                });
        return new ContextAwareVertex(this.referencedElement, copiedPinDFDVertexMap, new HashMap<>(this.pinFlowMap),
                vertexContext);
    }

    public VertexContext getVertexContext() {
        return vertexContext;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ContextAwareVertex vertex))
            return false;
        if (!this.referencedElement.equals(vertex.getReferencedElement()))
            return false;
        if (!this.vertexContext.equals(vertex.getVertexContext()))
            return false;
        if (!this.getPinDFDVertexMap()
                .keySet()
                .equals(vertex.getPinDFDVertexMap()
                        .keySet()))
            return false;
        for (var key : this.getPinDFDVertexMap()
                .keySet()) {
            if (!this.getPinDFDVertexMap()
                    .get(key)
                    .equals(vertex.getPinDFDVertexMap()
                            .get(key)))
                return false;
        }
        return true;
    }
}
