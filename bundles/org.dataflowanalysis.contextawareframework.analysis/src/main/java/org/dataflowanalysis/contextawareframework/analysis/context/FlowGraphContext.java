package org.dataflowanalysis.contextawareframework.analysis.context;

import java.util.List;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextApplication;

public class FlowGraphContext {
    private final List<ContextApplication> applicableContextApplications;

    public FlowGraphContext(List<ContextApplication> applicableContextApplications) {
        this.applicableContextApplications = applicableContextApplications;
    }

    public List<ContextApplication> getApplicableContextApplications() {
        return applicableContextApplications;
    }
}
