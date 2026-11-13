package org.dataflowanalysis.contextawareframework.analysis;

import java.nio.file.Paths;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.ResourceUtils;
import org.dataflowanalysis.contextawareframework.analysis.resource.ContextAwareResourceProvider;
import org.dataflowanalysis.contextawareframework.analysis.resource.URIContextAwareResourceProvider;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.URI;

public class ContextAwareDataFlowAnalysisBuilder extends DFDDataFlowAnalysisBuilder {
    private final Logger logger = LoggerManager.getLogger(ContextAwareDataFlowAnalysisBuilder.class);

    protected String contextPath;
    protected Optional<ContextAwareResourceProvider> customResourceProvider;

    public ContextAwareDataFlowAnalysisBuilder() {
        this.customResourceProvider = Optional.empty();
    }

    /**
     * Sets standalone mode of the analysis
     * @return Builder of the analysis
     */
    @Override
    public ContextAwareDataFlowAnalysisBuilder standalone() {
        super.standalone();
        return this;
    }

    /**
     * Sets the modeling project name of the analysis
     * @return Builder of the analysis
     */
    @Override
    public ContextAwareDataFlowAnalysisBuilder modelProjectName(String modelProjectName) {
        super.modelProjectName(modelProjectName);
        return this;
    }

    /**
     * Uses a plugin activator class for the given project
     * @param pluginActivator Plugin activator class of the modeling project
     * @return Returns builder object of the analysis
     */
    @Override
    public ContextAwareDataFlowAnalysisBuilder usePluginActivator(Class<? extends Plugin> pluginActivator) {
        super.usePluginActivator(pluginActivator);
        return this;
    }

    /**
     * Sets the data dictionary used by the analysis
     * @return Builder of the analysis
     */
    @Override
    public ContextAwareDataFlowAnalysisBuilder useDataDictionary(String dataDictionaryPath) {
        this.dataDictionaryPath = dataDictionaryPath;
        return this;
    }

    /**
     * Sets the data dictionary used by the analysis
     * @return Builder of the analysis
     */
    @Override
    public ContextAwareDataFlowAnalysisBuilder useDataFlowDiagram(String dataFlowDiagramPath) {
        this.dataFlowDiagramPath = dataFlowDiagramPath;
        return this;
    }

    /**
     * Sets the context model used by the analysis
     * @return Builder of the analysis
     */
    public ContextAwareDataFlowAnalysisBuilder useContext(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    /**
     * Registers a custom resource provider for the analysis
     * @param resourceProvider Custom resource provider of the analysis
     */
    public ContextAwareDataFlowAnalysisBuilder useCustomResourceProvider(
            ContextAwareResourceProvider resourceProvider) {
        this.customResourceProvider = Optional.of(resourceProvider);
        return this;
    }

    /**
     * Determines the effective resource provider that should be used by the analysis
     */
    private ContextAwareResourceProvider getEffectiveResourceProvider() {
        if (this.customResourceProvider.isEmpty()) {
            URI dataDictionaryUri = this.modelProjectName.isEmpty()
                    ? URI.createFileURI(Paths.get(this.dataDictionaryPath)
                            .toAbsolutePath()
                            .toString())
                    : ResourceUtils.createRelativePluginURI(this.dataDictionaryPath, this.modelProjectName);
            URI dataFlowDiagramUri = this.modelProjectName.isEmpty()
                    ? URI.createFileURI(Paths.get(this.dataFlowDiagramPath)
                            .toAbsolutePath()
                            .toString())
                    : ResourceUtils.createRelativePluginURI(this.dataFlowDiagramPath, this.modelProjectName);
            URI contextUri = this.modelProjectName.isEmpty() ? URI.createFileURI(Paths.get(this.contextPath)
                    .toAbsolutePath()
                    .toString()) : ResourceUtils.createRelativePluginURI(this.contextPath, this.modelProjectName);

            return new URIContextAwareResourceProvider(dataFlowDiagramUri, dataDictionaryUri, contextUri);
        }
        return this.customResourceProvider.get();
    }

    /**
     * Validates the stored data
     */
    @Override
    protected void validate() {
        super.validate();
        if (this.customResourceProvider.isEmpty()
                && (this.dataDictionaryPath == null || this.dataDictionaryPath.isEmpty())) {
            logger.error("A data dictionary is required to run the data flow analysis",
                    new IllegalStateException("The Context-Aware DFD analysis requires a data dictionary"));
        }
        if (this.customResourceProvider.isEmpty()
                && (this.dataFlowDiagramPath == null || this.dataFlowDiagramPath.isEmpty())) {
            logger.error("A data flow diagram is required to run the data flow analysis",
                    new IllegalStateException("The Context-Aware DFD analysis requires a data flow diagram"));
        }
        if (this.customResourceProvider.isEmpty() && (this.contextPath == null || this.contextPath.isEmpty())) {
            logger.error("A context model is required to run the data flow analysis",
                    new IllegalStateException("The Context-Aware DFD analysis requires a context model"));
        }
    }

    /**
     * Builds a new analysis from the given data
     */
    @Override
    public ContextAwareDataFlowAnalysis build() {
        this.validate();
        ContextAwareResourceProvider resourceProvider = this.getEffectiveResourceProvider();
        resourceProvider.validate();
        return new ContextAwareDataFlowAnalysis(resourceProvider, this.pluginActivator, this.modelProjectName);

    }
}
