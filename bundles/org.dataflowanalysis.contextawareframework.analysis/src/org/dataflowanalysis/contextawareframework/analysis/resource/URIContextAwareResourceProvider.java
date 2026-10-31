package org.dataflowanalysis.contextawareframework.analysis.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.contextawareframework.metamodel.Context.Context;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class URIContextAwareResourceProvider extends ContextAwareResourceProvider {
	private static final Logger logger = LoggerManager.getLogger(URIContextAwareResourceProvider.class);
	
	private final URI dataFlowDiagramURI;
	private final URI dataDictionaryURI;
	private final URI contextURI;
	private DataFlowDiagram dataFlowDiagram;
	private DataDictionary dataDictionary;
	private Context context;
	
	public URIContextAwareResourceProvider(URI dataFlowDiagramURI, URI dataDictionaryURI, URI contextURI) {
		this.dataFlowDiagramURI = dataFlowDiagramURI;
		this.dataDictionaryURI = dataDictionaryURI;
		this.contextURI = contextURI;
	}

	@Override
	public void loadRequiredResources() {
		this.dataFlowDiagram = (DataFlowDiagram) this.loadModelContent(dataFlowDiagramURI);
        this.dataDictionary = (DataDictionary) this.loadModelContent(dataDictionaryURI);
        this.context = (Context) this.loadModelContent(contextURI);
        List<Resource> loadedResources;
        do {
            loadedResources = new ArrayList<>(this.resources.getResources());
            loadedResources.forEach(EcoreUtil::resolveAll);
        } while (loadedResources.size() != this.resources.getResources()
                .size());
        EcoreUtil.resolveAll(this.resources);
        for (var resource : this.resources.getResources()) {
            if (!resource.getErrors()
                    .isEmpty()) {
                logger.error("Errors occurred during loading a model:");
                logger.error(resource.getErrors()
                        .stream()
                        .map(Resource.Diagnostic::getMessage)
                        .toList());
            }
        }
	}
	
	@Override
	public DataFlowDiagram getDataFlowDiagram() {
		return this.dataFlowDiagram;
	}

	@Override
	public DataDictionary getDataDictionary() {
		return this.dataDictionary;
	}

	@Override
	public Context getContext() {
		return this.context;
	}

}
