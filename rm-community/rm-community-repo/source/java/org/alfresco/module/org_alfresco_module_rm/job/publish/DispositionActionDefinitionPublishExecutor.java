package org.alfresco.module.org_alfresco_module_rm.job.publish;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.action.impl.BroadcastDispositionActionDefinitionUpdateAction;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Disposition action definition publish executor
 *
 * @author Roy Wetherall
 */
public class DispositionActionDefinitionPublishExecutor extends BasePublishExecutor
{
    /** Node service */
    private NodeService nodeService;

    /** Records management action service */
    private RecordsManagementActionService rmActionService;

    /**
     * Set node service
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set records management service
     * @param rmActionService   records management service
     */
    public void setRmActionService(RecordsManagementActionService rmActionService)
    {
        this.rmActionService = rmActionService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.job.publish.PublishExecutor#getName()
     */
    @Override
    public String getName()
    {
        return RecordsManagementModel.UPDATE_TO_DISPOSITION_ACTION_DEFINITION;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.job.publish.PublishExecutor#publish(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void publish(NodeRef nodeRef)
    {
        List<QName> updatedProps = (List<QName>)nodeService.getProperty(nodeRef, RecordsManagementModel.PROP_UPDATED_PROPERTIES);
        if (updatedProps != null)
        {
            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put(BroadcastDispositionActionDefinitionUpdateAction.CHANGED_PROPERTIES, (Serializable)updatedProps);
            rmActionService.executeRecordsManagementAction(nodeRef, BroadcastDispositionActionDefinitionUpdateAction.NAME, params);
        }
    }
}
