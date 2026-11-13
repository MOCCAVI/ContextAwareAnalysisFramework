package org.dataflowanalysis.contextawareframework.analysis.resource;

import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.contextawareframework.metamodel.Context.Context;
import org.dataflowanalysis.contextawareframework.metamodel.Context.ContextPackage;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryPackage;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramPackage;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public abstract class ContextAwareResourceProvider extends DFDResourceProvider {
    public abstract Context getContext();

    @Override
    public void setupResources() {
        this.resources.getPackageRegistry()
                .put(dataflowdiagramPackage.eNS_URI, dataflowdiagramPackage.eINSTANCE);
        this.resources.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put(dataflowdiagramPackage.eNAME, new XMIResourceFactoryImpl());
        this.resources.getPackageRegistry()
                .put(datadictionaryPackage.eNS_URI, datadictionaryPackage.eINSTANCE);
        this.resources.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put(datadictionaryPackage.eNAME, new XMIResourceFactoryImpl());
        this.resources.getPackageRegistry()
                .put(ContextPackage.eNS_URI, ContextPackage.eINSTANCE);
        this.resources.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put(ContextPackage.eNAME, new XMIResourceFactoryImpl());
    }

    /**
     * Returns the data flow diagram model that the resource loader has loaded
     * @return Data flow diagram model saved in the resources
     */
    public abstract DataFlowDiagram getDataFlowDiagram();

    /**
     * Returns the data dictionary model that the resource loader has loaded
     * @return Data dictionary model saved in the resources
     */
    public abstract DataDictionary getDataDictionary();

    /**
     * Determines, whether the resource loader has sufficient resources to run the analysis
     * @return This method returns true, if the analysis can be executed with the resource loader. Otherwise, the method
     * returns false
     */
    @Override
    public boolean sufficientResourcesLoaded() {
        return this.getDataFlowDiagram() != null && this.getDataDictionary() != null && this.getContext() != null;
    }
}
