package org.alfresco.repo.action;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Asynchronous action execution queue policies
 * 
 * @author Roy Wetherall
 */
public interface AsynchronousActionExecutionQueuePolicies
{
    /**
     * Policy invoked when an async action has completed execution
     */
    public interface OnAsyncActionExecute extends ClassPolicy
    {
        /** QName of the policy */
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onAsyncActionExecute");
        
        /**
         * @param action                    action
         * @param actionedUponNodeRef       actioned upon node reference
         */
        public void onAsyncActionExecute(Action action, NodeRef actionedUponNodeRef);
    }
	
}
