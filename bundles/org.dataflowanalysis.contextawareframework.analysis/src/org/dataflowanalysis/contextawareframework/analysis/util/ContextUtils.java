package org.dataflowanalysis.contextawareframework.analysis.util;

import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraph;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.contextawareframework.analysis.core.ContextAwareVertex;
import org.dataflowanalysis.contextawareframework.analysis.resource.ContextAwareResourceProvider;
import org.dataflowanalysis.contextawareframework.metamodel.Context.AndContextBindingPredicate;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextApplication;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextBindingPredicate;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextType;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextValue;
import org.dataflowanalysis.contextawareframework.metamodel.Context.DFDDataContextApplication;
import org.dataflowanalysis.contextawareframework.metamodel.Context.DFDFlowContextApplication;
import org.dataflowanalysis.contextawareframework.metamodel.Context.DFDNodeContextApplication;
import org.dataflowanalysis.contextawareframework.metamodel.Context.DataCharacteristicBindingPredicate;
import org.dataflowanalysis.contextawareframework.metamodel.Context.FlowNameContextBindingPredicate;
import org.dataflowanalysis.contextawareframework.metamodel.Context.NodeNameContextBindingPredicate;
import org.dataflowanalysis.contextawareframework.metamodel.Context.VertexCharacteristicBindingPredicate;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ContextUtils {
    private ContextAwareResourceProvider resourceProvider;

    public ContextUtils(ContextAwareResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    public boolean isApplicable(ContextApplication contextApplication, DFDTransposeFlowGraph flowGraph) {
        return flowGraph.getVertices()
                .parallelStream()
                .filter(DFDVertex.class::isInstance)
                .map(DFDVertex.class::cast)
                .anyMatch(it -> isApplicable(contextApplication, it));
    }

    public boolean isApplicable(ContextApplication contextApplication, DFDVertex vertex) {
        if (contextApplication instanceof DFDDataContextApplication dataContextApplication) {
            return vertex.getReferencedElement()
                    .getBehavior()
                    .equals(dataContextApplication.getBehavior()) && vertex.getReferencedElement()
                    .getBehavior()
                    .getOutPin()
                    .contains(dataContextApplication.getPin());
        } else if (contextApplication instanceof DFDNodeContextApplication nodeContextApplication) {
            return vertex.getReferencedElement()
                    .equals(nodeContextApplication.getNode());
        } else if (contextApplication instanceof DFDFlowContextApplication flowContextApplication) {
            return vertex.getReferencedElement()
                    .getBehavior()
                    .getOutPin()
                    .contains(flowContextApplication.getSourcePin()) && resourceProvider.getDataFlowDiagram()
                    .getFlows()
                    .parallelStream()
                    .filter(it -> it.getSourcePin()
                            .equals(flowContextApplication.getSourcePin()))
                    .anyMatch(it -> it.getDestinationPin()
                            .equals(flowContextApplication.getDestinationPin()));
        } else {
            throw new UnsupportedOperationException("Unknown context application");
        }
    }

    public boolean isApplicable(ContextBindingPredicate predicate, ContextAwareVertex vertex) {
        if (predicate instanceof AndContextBindingPredicate andContextBindingPredicate) {
            return andContextBindingPredicate.getContextBindingPredicates()
                    .stream()
                    .allMatch(it -> isApplicable(it, vertex));
        } else if (predicate instanceof DataCharacteristicBindingPredicate dataCharacteristicBindingPredicate) {
            return vertex.getAllDataCharacteristics()
                    .stream()
                    .map(DataCharacteristic::getAllCharacteristics)
                    .flatMap(List::stream)
                    .anyMatch(it -> it.getValueName()
                            .equalsIgnoreCase(dataCharacteristicBindingPredicate.getDataCharacteristic()
                                    .getEntityName()));
        } else if (predicate instanceof FlowNameContextBindingPredicate flowNameContextBindingPredicate) {
            return vertex.getPinFlowMap()
                    .values()
                    .stream()
                    .anyMatch(it -> it.getEntityName()
                            .equalsIgnoreCase(flowNameContextBindingPredicate.getName()));
        } else if (predicate instanceof NodeNameContextBindingPredicate nodeNameContextBindingPredicate) {
            return vertex.getReferencedElement()
                    .getEntityName()
                    .equalsIgnoreCase(nodeNameContextBindingPredicate.getName());
        } else if (predicate instanceof VertexCharacteristicBindingPredicate vertexCharacteristicBindingPredicate) {
            return vertex.getAllVertexCharacteristics()
                    .stream()
                    .anyMatch(it -> it.getValueName()
                            .equalsIgnoreCase(vertexCharacteristicBindingPredicate.getVertexCharacteristic()
                                    .getEntityName()));
        } else {
            throw new UnsupportedOperationException("Unknown context binding predicate");
        }
    }

    public Map<ContextType, List<ContextValue>> evaluateContext(List<ContextType> contextTypes,
            ContextAwareVertex vertex) {
        Map<ContextType, List<ContextValue>> evaluatedContext = new HashMap<>();
        for (ContextType contextType : contextTypes) {
            List<ContextValue> contextValues = contextType.getContextValue()
                    .stream()
                    .filter(contextValue -> contextValue.getContextBinding() != null)
                    .filter(contextValue -> contextValue.getContextBinding()
                            .getContextBindingPredicates()
                            .stream()
                            .anyMatch(it -> this.isApplicable(it, vertex)))
                    .collect(Collectors.toCollection(ArrayList::new));
            evaluatedContext.put(contextType, contextValues);
        }
        return evaluatedContext;
    }

    public LabelType getLabelType(ContextType contextType) {
        Optional<LabelType> labelType = this.resourceProvider.getDataDictionary().getLabelTypes().parallelStream()
                .filter(it -> it.getEntityName().equalsIgnoreCase(contextType.getName()))
                .findAny();
        if (labelType.isPresent()) {
            return labelType.get();
        } else {
            LabelType createdLabelType = datadictionaryFactory.eINSTANCE.createLabelType();
            createdLabelType.setId(UUID.randomUUID().toString());
            createdLabelType.setEntityName(contextType.getName());
            this.resourceProvider.getDataDictionary().getLabelTypes().add(createdLabelType);
            return createdLabelType;
        }
    }

    public Label getLabel(ContextValue contextValue) {
        LabelType labelType = this.getLabelType(contextValue.getContextType());
        Optional<Label> label = labelType.getLabel().parallelStream()
                .filter(it -> it.getEntityName().equalsIgnoreCase(contextValue.getName()))
                .findAny();
        if (label.isPresent()) {
            return label.get();
        } else {
            Label createdLabel = datadictionaryFactory.eINSTANCE.createLabel();
            createdLabel.setId(UUID.randomUUID().toString());
            createdLabel.setEntityName(contextValue.getName());
            labelType.getLabel().add(createdLabel);
            return createdLabel;
        }
    }
}
