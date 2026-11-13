package org.contextawareframework.analysis.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.contextawareframework.analysis.ContextAwareDataFlowAnalysis;
import org.dataflowanalysis.contextawareframework.analysis.ContextAwareDataFlowAnalysisBuilder;
import org.dataflowanalysis.contextawareframework.analysis.core.ContextAwareFlowGraphCollection;
import org.dataflowanalysis.contextawareframework.analysis.testmodels.Activator;
import org.junit.jupiter.api.Test;

public class OnlineShopTest {

    @Test
    public void shouldFindViolation() {
        ContextAwareDataFlowAnalysis analysis = new ContextAwareDataFlowAnalysisBuilder().standalone()
                .modelProjectName("org.dataflowanalysis.contextawareframework.analysis.testmodels")
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram("models/OnlineShop/default.dataflowdiagram")
                .useDataDictionary("models/OnlineShop/default.datadictionary")
                .useContext("models/OnlineShop/violation.context")
                .build();

        analysis.initializeAnalysis();

        ContextAwareFlowGraphCollection flowGraphs = analysis.findFlowGraphs();
        flowGraphs.evaluate();

        AnalysisConstraint constraint = new ConstraintDSL().ofData()
                .withLabel("Sensitivity", "Personal")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", "USA")
                .create();

        var violations = constraint.findViolations(flowGraphs);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void shouldFindNoViolation() {
        ContextAwareDataFlowAnalysis analysis = new ContextAwareDataFlowAnalysisBuilder().standalone()
                .modelProjectName("org.dataflowanalysis.contextawareframework.analysis.testmodels")
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram("models/OnlineShop/default.dataflowdiagram")
                .useDataDictionary("models/OnlineShop/default.datadictionary")
                .useContext("models/OnlineShop/no-violation.context")
                .build();

        analysis.initializeAnalysis();

        ContextAwareFlowGraphCollection flowGraphs = analysis.findFlowGraphs();
        flowGraphs.evaluate();

        AnalysisConstraint constraint = new ConstraintDSL().ofData()
                .withLabel("Sensitivity", "Personal")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", "USA")
                .create();

        var violations = constraint.findViolations(flowGraphs);
        assertTrue(violations.isEmpty());
    }
}
