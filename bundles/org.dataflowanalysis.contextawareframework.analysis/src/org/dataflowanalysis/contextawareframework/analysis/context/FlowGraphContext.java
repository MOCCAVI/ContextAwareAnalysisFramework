package org.dataflowanalysis.contextawareframework.analysis.context;

import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextApplication;

import java.util.List;

public class FlowGraphContext {
    private final List<ContextApplication> applicableContextApplications;

    public FlowGraphContext(List<ContextApplication> applicableContextApplications) {
        this.applicableContextApplications = applicableContextApplications;
    }

    public List<ContextApplication> getApplicableContextApplications() {
        return applicableContextApplications;
    }
}
