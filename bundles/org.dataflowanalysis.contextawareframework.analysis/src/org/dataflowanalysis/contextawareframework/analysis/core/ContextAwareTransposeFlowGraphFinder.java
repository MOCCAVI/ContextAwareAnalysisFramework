package org.dataflowanalysis.contextawareframework.analysis.core;

import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraphFinder;
import org.dataflowanalysis.contextawareframework.analysis.resource.ContextAwareResourceProvider;

public class ContextAwareTransposeFlowGraphFinder extends DFDTransposeFlowGraphFinder {

	public ContextAwareTransposeFlowGraphFinder(ContextAwareResourceProvider resourceProvider) {
		super(resourceProvider);
	}

}
