package org.alfresco.opencmis.mapping;

import org.alfresco.opencmis.dictionary.CMISActionEvaluator;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.chemistry.opencmis.commons.enums.Action;

public class CanDeleteDocumentEvaluator extends AbstractActionEvaluator
{
    private CMISActionEvaluator currentVersionEvaluator;

    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    protected CanDeleteDocumentEvaluator(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, Action.CAN_DELETE_OBJECT);
        this.currentVersionEvaluator = new PermissionActionEvaluator(serviceRegistry,
                Action.CAN_DELETE_OBJECT, PermissionService.DELETE_NODE);
    }

    public boolean isAllowed(CMISNodeInfo nodeInfo)
    {
    	boolean isAllowed = true;

        if(!nodeInfo.isCurrentVersion() || nodeInfo.hasPWC())
        {
        	// not allowed if not current version or is checked out
            isAllowed = false;
        }
        else
        {
        	isAllowed = currentVersionEvaluator.isAllowed(nodeInfo);
        }

        return isAllowed;
    }

}
