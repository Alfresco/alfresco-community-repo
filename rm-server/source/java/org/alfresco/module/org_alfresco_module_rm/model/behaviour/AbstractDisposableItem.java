/**
 * 
 */
package org.alfresco.module.org_alfresco_module_rm.model.behaviour;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Abstract disposable item, containing commonality between record and record folder.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public abstract class AbstractDisposableItem extends BaseBehaviourBean
{
    /** unwanted aspects */
    protected QName[] unwantedAspects =
    {
        ASPECT_VITAL_RECORD,
        ASPECT_DISPOSITION_LIFECYCLE,
        RecordsManagementSearchBehaviour.ASPECT_RM_SEARCH
    };
    
    /** disposition service */
    protected DispositionService dispositionService;
    
    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }
    
    /**
     * Removes unwanted aspects
     *
     * @param nodeService
     * @param nodeRef
     */
    protected void cleanDisposableItem(NodeService nodeService, NodeRef nodeRef)
    {
        // Remove unwanted aspects
        for (QName aspect : unwantedAspects)
        {
            if (nodeService.hasAspect(nodeRef, aspect))
            {
                nodeService.removeAspect(nodeRef, aspect);
            }
        }
        
        // remove the current disposition action (if there is one)
        DispositionAction dispositionAction = dispositionService.getNextDispositionAction(nodeRef);
        if (dispositionAction != null)
        {
            nodeService.deleteNode(dispositionAction.getNodeRef());
        }
    }

}
