 
package org.alfresco.module.org_alfresco_module_rm.bootstrap;

import org.alfresco.module.org_alfresco_module_rm.patch.ModulePatchExecuter;
import org.alfresco.repo.module.ImporterModuleComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Custom implementation of module component importer
 *
 * @author Roy Wetherall
 * @since 2.0
 */
public class BootstrapImporterModuleComponent extends ImporterModuleComponent
{
    /** rm config folder name */
    private static final String CONFIG_NODEID = "rm_config_folder";

    /** node service */
    private NodeService nodeService;

    /** module patch executer */
    private ModulePatchExecuter modulePatchExecuter;

    /** record contributors group bootstrap component */
    private RecordContributorsGroupBootstrapComponent recordContributorsGroupBootstrapComponent;

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param modulePatchExecuter   module patch executer
     */
    public void setModulePatchExecuter(ModulePatchExecuter modulePatchExecuter)
    {
        this.modulePatchExecuter = modulePatchExecuter;
    }

    /**
     * @param recordContributorsGroupBootstrapComponent record contributors group bootstrap component
     */
    public void setRecordContributorsGroupBootstrapComponent(RecordContributorsGroupBootstrapComponent recordContributorsGroupBootstrapComponent)
    {
        this.recordContributorsGroupBootstrapComponent = recordContributorsGroupBootstrapComponent;
    }

    /**
     * Need to check whether this module has already been executed.
     *
     * @see org.alfresco.repo.module.ImporterModuleComponent#executeInternal()
     */
    @Override
    protected void executeInternal() throws Throwable
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, CONFIG_NODEID);
        if (!nodeService.exists(nodeRef))
        {
            super.executeInternal();

            // Bootstrap creation of initial data.
            recordContributorsGroupBootstrapComponent.createRecordContributorsGroup();

            // init module schema number
            modulePatchExecuter.initSchemaVersion();
        }
    }
}
