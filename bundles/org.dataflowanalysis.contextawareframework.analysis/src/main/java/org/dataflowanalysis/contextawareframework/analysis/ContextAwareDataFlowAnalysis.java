package org.dataflowanalysis.contextawareframework.analysis;

import java.util.Optional;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.contextawareframework.analysis.core.ContextAwareFlowGraphCollection;
import org.dataflowanalysis.contextawareframework.analysis.resource.ContextAwareResourceProvider;
import org.eclipse.core.runtime.Plugin;

public class ContextAwareDataFlowAnalysis extends DFDConfidentialityAnalysis {

    public ContextAwareDataFlowAnalysis(ContextAwareResourceProvider resourceProvider,
            Optional<Class<? extends Plugin>> modelProjectActivator, String modelProjectName) {
        super(resourceProvider, modelProjectActivator, modelProjectName);
    }

    @Override
    public void initializeAnalysis() {
        super.initializeAnalysis();
    }

    public ContextAwareResourceProvider getResourceProvider() {
        return (ContextAwareResourceProvider) this.resourceProvider;
    }

    @Override
    public ContextAwareFlowGraphCollection findFlowGraphs() {
        return new ContextAwareFlowGraphCollection(this.getResourceProvider());
    }
}
