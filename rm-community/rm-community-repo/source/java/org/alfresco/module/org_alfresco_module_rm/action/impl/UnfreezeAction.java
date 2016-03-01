 
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Unfreeze Action
 *
 * @author Roy Wetherall
 */
public class UnfreezeAction extends RMActionExecuterAbstractBase
{
   /**
    * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
    */
   @SuppressWarnings("deprecation")
   @Override
   protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
   {
       getFreezeService().unFreeze(actionedUponNodeRef);
   }
}