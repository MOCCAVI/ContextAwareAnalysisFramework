package org.contextawareframework.analysis.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.analysis.dsl.query.QueryDSL;
import org.dataflowanalysis.contextawareframework.analysis.ContextAwareDataFlowAnalysis;
import org.dataflowanalysis.contextawareframework.analysis.ContextAwareDataFlowAnalysisBuilder;
import org.dataflowanalysis.contextawareframework.analysis.core.ContextAwareFlowGraphCollection;
import org.dataflowanalysis.contextawareframework.analysis.testmodels.Activator;
import org.junit.jupiter.api.Test;

public class OnlineShopTest {

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
        flowGraphs.evaluate();
        return flowGraphs;
    }

    @Test
    public void shouldFindViolation() {
        ContextAwareFlowGraphCollection flowGraphs = evaluatedFlowGraphs("violation.context");

        var violations = new ConstraintDSL().ofData()
                .withLabel("Sensitivity", "Personal")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", "USA")
                .create()
                .findViolations(flowGraphs);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void shouldFindNoViolation() {
        ContextAwareFlowGraphCollection flowGraphs = evaluatedFlowGraphs("no-violation.context");

        var violations = new ConstraintDSL().ofData()
                .withLabel("Sensitivity", "Personal")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", "USA")
                .create()
                .findViolations(flowGraphs);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void dataContextApplicationPropagatesDataCharacteristic() {
        ContextAwareFlowGraphCollection flowGraphs = evaluatedFlowGraphs("DataApplication/violation.context");
        var violations = new ConstraintDSL().ofData()
                .withLabel("Classification", "Restricted")
                .neverFlows()
                .toVertex()
                .withVertexName("Database")
                .create()
                .findViolations(flowGraphs);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void shouldFindViolationWithFlowNameBindingPredicate() {
        ContextAwareFlowGraphCollection flowGraphs = evaluatedFlowGraphs("FlowNamePredicate/violation.context");

        var violations = new ConstraintDSL().ofData()
                .withLabel("Sensitivity", "Personal")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", "USA")
                .create()
                .findViolations(flowGraphs);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void shouldFindNoViolationWithFlowNameBindingPredicate() {
        ContextAwareFlowGraphCollection flowGraphs = evaluatedFlowGraphs("FlowNamePredicate/no-violation.context");

        var violations = new ConstraintDSL().ofData()
                .withLabel("Sensitivity", "Personal")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", "USA")
                .create()
                .findViolations(flowGraphs);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void shouldFindViolationWithDataCharacteristicBindingPredicate() {
        ContextAwareFlowGraphCollection flowGraphs = evaluatedFlowGraphs("DataCharacteristicPredicate/violation.context");

        var violations = new ConstraintDSL().ofData()
                .withLabel("Sensitivity", "Personal")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", "USA")
                .create()
                .findViolations(flowGraphs);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void shouldFindNoViolationWithDataCharacteristicBindingPredicate() {
        ContextAwareFlowGraphCollection flowGraphs = evaluatedFlowGraphs("DataCharacteristicPredicate/no-violation.context");

        var violations = new ConstraintDSL().ofData()
                .withLabel("Sensitivity", "Personal")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", "USA")
                .create()
                .findViolations(flowGraphs);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void vertexCharacteristicBindingPredicateBindsWhenCharacteristicPresent() {
        ContextAwareFlowGraphCollection flowGraphs = evaluatedFlowGraphs("VertexCharacteristicPredicate/violation.context");

        var violations = new QueryDSL().ofNode()
                .withVertexName("User")
                .withCharacteristic("Location", "USA")
                .build()
                .query(flowGraphs);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void vertexCharacteristicBindingPredicateDoesNotBindWhenCharacteristicAbsent() {
        ContextAwareFlowGraphCollection flowGraphs = evaluatedFlowGraphs("VertexCharacteristicPredicate/no-violation.context");

        var violations = new QueryDSL().ofNode()
                .withVertexName("User")
                .withCharacteristic("Location", "USA")
                .build()
                .query(flowGraphs);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void shouldFindViolationWithAndBindingPredicate() {
        ContextAwareFlowGraphCollection flowGraphs = evaluatedFlowGraphs("AndPredicate/violation.context");

        var violations = new ConstraintDSL().ofData()
                .withLabel("Sensitivity", "Personal")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", "USA")
                .create()
                .findViolations(flowGraphs);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void shouldFindNoViolationWithAndBindingPredicate() {
        ContextAwareFlowGraphCollection flowGraphs = evaluatedFlowGraphs("AndPredicate/no-violation.context");

        var violations = new ConstraintDSL().ofData()
                .withLabel("Sensitivity", "Personal")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", "USA")
                .create()
                .findViolations(flowGraphs);
        assertTrue(violations.isEmpty());
    }
}
