package org.contextawareframework.analysis.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraph;
import org.dataflowanalysis.contextawareframework.analysis.ContextAwareDataFlowAnalysis;
import org.dataflowanalysis.contextawareframework.analysis.ContextAwareDataFlowAnalysisBuilder;
import org.dataflowanalysis.contextawareframework.analysis.core.ContextAwareFlowGraphCollection;
import org.dataflowanalysis.contextawareframework.analysis.resource.ContextAwareResourceProvider;
import org.dataflowanalysis.contextawareframework.analysis.testmodels.Activator;
import org.dataflowanalysis.contextawareframework.analysis.util.ContextUtils;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextApplication;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextFactory;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextType;
import org.dataflowanalysis.contextawareframework.metamodel.Context.DFDDataContextApplication;
import org.dataflowanalysis.contextawareframework.metamodel.Context.DFDFlowContextApplication;
import org.dataflowanalysis.contextawareframework.metamodel.Context.DFDNodeContextApplication;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ContextUtilsTest {

    private ContextAwareResourceProvider resourceProvider;
    private ContextUtils contextUtils;
    private List<DFDTransposeFlowGraph> flowGraphs;
    private ContextType contextType;

    @BeforeEach
    public void setup() {
        ContextAwareDataFlowAnalysis analysis = new ContextAwareDataFlowAnalysisBuilder().standalone()
                .modelProjectName("org.dataflowanalysis.contextawareframework.analysis.testmodels")
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram("models/OnlineShop/default.dataflowdiagram")
                .useDataDictionary("models/OnlineShop/default.datadictionary")
                .useContext("models/OnlineShop/violation.context")
                .build();

        analysis.initializeAnalysis();
        this.resourceProvider = analysis.getResourceProvider();
        this.contextUtils = new ContextUtils(resourceProvider);
        this.contextType = ContextFactory.eINSTANCE.createContextType();

        ContextAwareFlowGraphCollection flowGraphCollection = analysis.findFlowGraphs();
        flowGraphCollection.evaluate();
        this.flowGraphs = flowGraphCollection.getTransposeFlowGraphs()
                .stream()
                .map(DFDTransposeFlowGraph.class::cast)
                .toList();
    }

    private Node findNode(String entityName) {
        return resourceProvider.getDataFlowDiagram()
                .getNodes()
                .stream()
                .filter(node -> node.getEntityName()
                        .equals(entityName))
                .findFirst()
                .orElseThrow();
    }

    private Pin findOutPin(String nodeName, String pinName) {
        return findNode(nodeName).getBehavior()
                .getOutPin()
                .stream()
                .filter(pin -> pin.getEntityName()
                        .equals(pinName))
                .findFirst()
                .orElseThrow();
    }

    private Pin findInPin(String nodeName, String pinName) {
        return findNode(nodeName).getBehavior()
                .getInPin()
                .stream()
                .filter(pin -> pin.getEntityName()
                        .equals(pinName))
                .findFirst()
                .orElseThrow();
    }

    private boolean isApplicableToAnyFlowGraph(ContextApplication contextApplication) {
        return flowGraphs.stream()
                .anyMatch(flowGraph -> contextUtils.isApplicable(contextApplication, flowGraph));
    }

    @Test
    public void nodeContextApplicationIsApplicableToMatchingNode() {
        DFDNodeContextApplication contextApplication = ContextFactory.eINSTANCE.createDFDNodeContextApplication();
        contextApplication.setContextType(contextType);
        contextApplication.setNode(findNode("Database"));

        assertTrue(isApplicableToAnyFlowGraph(contextApplication));
    }

    @Test
    public void nodeContextApplicationIsNotApplicableToUnrelatedNode() {
        Node unrelatedNode = dataflowdiagramFactory.eINSTANCE.createProcess();
        unrelatedNode.setEntityName("Unrelated");

        DFDNodeContextApplication contextApplication = ContextFactory.eINSTANCE.createDFDNodeContextApplication();
        contextApplication.setContextType(contextType);
        contextApplication.setNode(unrelatedNode);

        assertFalse(isApplicableToAnyFlowGraph(contextApplication));
    }

    @Test
    public void dataContextApplicationIsApplicableToMatchingBehaviorAndPin() {
        DFDDataContextApplication contextApplication = ContextFactory.eINSTANCE.createDFDDataContextApplication();
        contextApplication.setContextType(contextType);
        contextApplication.setBehavior(findNode("Database").getBehavior());
        contextApplication.setPin(findOutPin("Database", "Database_out_items"));

        assertTrue(isApplicableToAnyFlowGraph(contextApplication));
    }

    @Test
    public void dataContextApplicationIsNotApplicableToPinFromOtherBehavior() {
        DFDDataContextApplication contextApplication = ContextFactory.eINSTANCE.createDFDDataContextApplication();
        contextApplication.setContextType(contextType);
        contextApplication.setBehavior(findNode("Database").getBehavior());
        contextApplication.setPin(findOutPin("encrypt", "encrypt_out_data"));

        assertFalse(isApplicableToAnyFlowGraph(contextApplication));
    }

    @Test
    public void flowContextApplicationIsApplicableToExistingFlow() {
        DFDFlowContextApplication contextApplication = ContextFactory.eINSTANCE.createDFDFlowContextApplication();
        contextApplication.setContextType(contextType);
        contextApplication.setSourcePin(findOutPin("Database", "Database_out_items"));
        contextApplication.setDestinationPin(findInPin("display", "display_in_items"));

        assertTrue(isApplicableToAnyFlowGraph(contextApplication));
    }

    @Test
    public void flowContextApplicationIsNotApplicableToNonExistentFlow() {
        DFDFlowContextApplication contextApplication = ContextFactory.eINSTANCE.createDFDFlowContextApplication();
        contextApplication.setContextType(contextType);
        contextApplication.setSourcePin(findOutPin("Database", "Database_out_items"));
        contextApplication.setDestinationPin(findInPin("Database", "Database_in"));

        assertFalse(isApplicableToAnyFlowGraph(contextApplication));
    }
}
