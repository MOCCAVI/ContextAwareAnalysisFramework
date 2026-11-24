package org.dataflowanalysis.contextawareframework.analysis.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dfd.core.DFDCharacteristicValue;
import org.dataflowanalysis.contextawareframework.analysis.core.ContextAwareVertex;
import org.dataflowanalysis.contextawareframework.analysis.resource.ContextAwareResourceProvider;
import org.dataflowanalysis.contextawareframework.analysis.util.ContextUtils;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextApplication;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextType;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextValue;
import org.dataflowanalysis.contextawareframework.metamodel.Context.DFDDataContextApplication;
import org.dataflowanalysis.contextawareframework.metamodel.Context.DFDNodeContextApplication;

public class VertexContext {
    private final ContextAwareResourceProvider resourceProvider;
    private final List<ContextApplication> appliedContext;
    private final ContextUtils contextUtils;

    public VertexContext(ContextAwareResourceProvider resourceProvider, List<ContextApplication> appliedContext) {
        this.resourceProvider = resourceProvider;
        this.appliedContext = appliedContext;
        this.contextUtils = new ContextUtils(resourceProvider);
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
        for (DFDNodeContextApplication nodeContextApplication : nodeContextApplications) {
            List<CharacteristicValue> characteristicValues = new ArrayList<>();
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
        for (DFDDataContextApplication dataContextApplication : dataContextApplications) {
            List<CharacteristicValue> characteristics = new ArrayList<>();
            evaluatedContext.get(dataContextApplication.getContextType())
                    .forEach(it -> {
                        characteristics.add(new DFDCharacteristicValue(contextUtils.getLabelType(it.getContextType()), contextUtils.getLabel(it)));
                    });
            addedCharacteristics.add(new DataCharacteristic(dataContextApplication.getPin().getEntityName(), characteristics));
        }
        return addedCharacteristics;
    }

    public List<ContextApplication> getAppliedContext() {
        return appliedContext;
    }
}
