 
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Relinquish Hold Action
 *
 * @author Roy Wetherall
 */
public class RelinquishHoldAction extends RMActionExecuterAbstractBase
{
   /** I18N */
   private static final String MSG_NOT_HOLD_TYPE = "rm.action.not-hold-type";

   /**
    * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
    */
   @SuppressWarnings("deprecation")
   @Override
   protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
   {
      if (getFreezeService().isHold(actionedUponNodeRef))
      {
          getFreezeService().relinquish(actionedUponNodeRef);
      }
      else
      {
         throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NOT_HOLD_TYPE, TYPE_HOLD.toString(), actionedUponNodeRef.toString()));
      }
   }
}