package org.dataflowanalysis.contextawareframework.analysis.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dfd.core.DFDCharacteristicValue;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.contextawareframework.analysis.core.ContextAwareVertex;
import org.dataflowanalysis.contextawareframework.analysis.resource.ContextAwareResourceProvider;
import org.dataflowanalysis.contextawareframework.analysis.util.ContextUtils;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextApplication;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextType;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextValue;
import org.dataflowanalysis.contextawareframework.metamodel.Context.DFDDataContextApplication;
import org.dataflowanalysis.contextawareframework.metamodel.Context.DFDNodeContextApplication;

public class VertexContext {
    private static final Logger logger = LoggerManager.getLogger(VertexContext.class);

    private final ContextAwareResourceProvider resourceProvider;
    private final List<ContextApplication> appliedContext;
    private final ContextUtils contextUtils;
    private final Map<ContextType, List<ContextValue>> exploredContext;

    public VertexContext(ContextAwareResourceProvider resourceProvider, List<ContextApplication> appliedContext) {
        this.resourceProvider = resourceProvider;
        this.appliedContext = appliedContext;
        this.contextUtils = new ContextUtils(resourceProvider);
        this.exploredContext = new HashMap<>();
    }

    public VertexContext(ContextAwareResourceProvider resourceProvider, List<ContextApplication> appliedContext, Map<ContextType, List<ContextValue>> exploredContext) {
        this.resourceProvider = resourceProvider;
        this.appliedContext = appliedContext;
        this.contextUtils = new ContextUtils(resourceProvider);
        this.exploredContext = exploredContext;
    }

    public List<ContextAwareVertex> computeAlternatives(ContextAwareVertex vertex) {
        List<ContextAwareVertex> alternatives = new ArrayList<>();
        alternatives.add(vertex);
        for (ContextApplication contextApplication : this.appliedContext) {
            boolean hasContextValue = false;
            for (ContextValue contextValue : contextApplication.getContextType().getContextValue()) {
                if (contextValue.getContextBinding() != null && contextValue.getContextBinding().getContextBindingPredicates().stream().anyMatch(it -> contextUtils.isApplicable(it, vertex)))  {
                    hasContextValue = true;
                    break;
                }
            }
            if (!hasContextValue) {
                alternatives = this.applyAlternatives(alternatives, contextApplication.getContextType());
            }
        }
        return alternatives;
    }

    private List<ContextAwareVertex> applyAlternatives(List<ContextAwareVertex> alternatives, ContextType type) {
        List<ContextAwareVertex> result = new ArrayList<>();
        for (ContextAwareVertex vertex: alternatives) {
            for (ContextValue value : type.getContextValue()) {
                result.add(vertex.copy(new IdentityHashMap<>(), this.introduceMapping(type, value)));
            }
        }
        return result;
    }

    public VertexContext introduceMapping(ContextType contextType, ContextValue contextValue) {
        HashMap<ContextType, List<ContextValue>> exploredContext = new HashMap<>(this.exploredContext);
        exploredContext.put(contextType, List.of(contextValue));
        return new VertexContext(this.resourceProvider, this.appliedContext, exploredContext);
    }

    public List<CharacteristicValue> getVertexCharacteristics(ContextAwareVertex vertex) {
        List<CharacteristicValue> addedCharacteristics = new ArrayList<>();
        List<DFDNodeContextApplication> nodeContextApplications = appliedContext.parallelStream()
                .filter(DFDNodeContextApplication.class::isInstance)
                .map(DFDNodeContextApplication.class::cast)
                .toList();
        List<ContextType> contextTypes = nodeContextApplications.parallelStream()
                .map(ContextApplication::getContextType)
                .toList();
        Map<ContextType, List<ContextValue>> evaluatedContext = contextUtils.evaluateContext(contextTypes, vertex);
        evaluatedContext.putAll(this.exploredContext);
        for (DFDNodeContextApplication nodeContextApplication : nodeContextApplications) {
            List<CharacteristicValue> characteristicValues = new ArrayList<>();
            if(evaluatedContext.get(nodeContextApplication.getContextType()).isEmpty()) {
                logger.warn("Applying context failed: No context values");
            }
            evaluatedContext.get(nodeContextApplication.getContextType())
                    .forEach(it -> {
                        characteristicValues.add(new DFDCharacteristicValue(
                                contextUtils.getLabelType(it.getContextType()), contextUtils.getLabel(it)));
                    });
            addedCharacteristics.addAll(characteristicValues);
        }
        return addedCharacteristics;
    }

    /**
     * Evaluates the data characteristics added by the context to the given vertex
     * @param vertex Vertex to which the context is applied
     * @return Returns a list of data characteristics that are added to the output pin of the vertex
     */
    public List<DataCharacteristic> getDataCharacteristics(ContextAwareVertex vertex) {
        List<DataCharacteristic> addedCharacteristics = new ArrayList<>();
        List<DFDDataContextApplication> dataContextApplications = appliedContext.parallelStream()
                .filter(DFDDataContextApplication.class::isInstance)
                .map(DFDDataContextApplication.class::cast)
                .toList();
        List<ContextType> contextTypes = dataContextApplications.parallelStream()
                .map(ContextApplication::getContextType)
                .toList();
        Map<ContextType, List<ContextValue>> evaluatedContext = contextUtils.evaluateContext(contextTypes, vertex);
        evaluatedContext.putAll(this.exploredContext);
        for (DFDDataContextApplication dataContextApplication : dataContextApplications) {
            List<CharacteristicValue> characteristics = new ArrayList<>();
            if(evaluatedContext.get(dataContextApplication.getContextType()).isEmpty()) {
                logger.warn("Applying context failed: No context values");
            }
            evaluatedContext.get(dataContextApplication.getContextType())
                    .forEach(it -> {
                        characteristics.add(new DFDCharacteristicValue(contextUtils.getLabelType(it.getContextType()), contextUtils.getLabel(it)));
                    });
            addedCharacteristics.add(new DataCharacteristic(dataContextApplication.getPin().getId(), characteristics));
        }
        return addedCharacteristics;
    }

    public List<ContextApplication> getAppliedContext() {
        return appliedContext;
    }
}
