package org.contextawareframework.analysis.tests;

import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.contextawareframework.analysis.ContextAwareDataFlowAnalysis;
import org.dataflowanalysis.contextawareframework.analysis.ContextAwareDataFlowAnalysisBuilder;
import org.dataflowanalysis.contextawareframework.analysis.core.ContextAwareFlowGraphCollection;
import org.dataflowanalysis.contextawareframework.analysis.testmodels.Activator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ContextResolverTest {

    private ContextAwareFlowGraphCollection evaluatedFlowGraphs(String contextFile) {
        ContextAwareDataFlowAnalysis analysis = new ContextAwareDataFlowAnalysisBuilder().standalone()
                .modelProjectName("org.dataflowanalysis.contextawareframework.analysis.testmodels")
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram("models/OnlineShop/default.dataflowdiagram")
                .useDataDictionary("models/OnlineShop/default.datadictionary")
                .useContext("models/OnlineShop/" + contextFile)
                .build();

        analysis.initializeAnalysis();

        ContextAwareFlowGraphCollection flowGraphs = analysis.findFlowGraphs();
        return flowGraphs;
    }

    @Test
    public void shouldFindMoreTransposeFlowGraphs() {
        ContextAwareFlowGraphCollection flowGraphs = this.evaluatedFlowGraphs("resolving-context.context");
        assertEquals(3, flowGraphs.getTransposeFlowGraphs().size());
        ContextAwareFlowGraphCollection exploredFlowGraphs = flowGraphs.exploreContext();
        exploredFlowGraphs.evaluate();
        assertEquals(9, exploredFlowGraphs.getTransposeFlowGraphs().size());
        var violations = new ConstraintDSL().ofData()
                .withLabel("Sensitivity", "Personal")
                .neverFlows()
                .toVertex()
                .withVertexName("Database")
                .withoutCharacteristic("Location", "EU")
                .create()
                .findViolations(exploredFlowGraphs);
        assertEquals(2, violations.size());
    }
}
