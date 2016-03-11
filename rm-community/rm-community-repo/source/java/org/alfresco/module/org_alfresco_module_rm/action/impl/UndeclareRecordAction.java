package org.alfresco.module.org_alfresco_module_rm.action.impl;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Undeclare record action
 *
 * @author Roy Wetherall
 */
public class UndeclareRecordAction extends RMActionExecuterAbstractBase
{
    /** Logger */
    private static Log logger = LogFactory.getLog(UndeclareRecordAction.class);

    /** I18N */
    private static final String MSG_RECORDS_ONLY_UNDECLARED = "rm.action.records_only_undeclared";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
    	if (getNodeService().exists(actionedUponNodeRef))
    	{
	        if (getRecordService().isRecord(actionedUponNodeRef))
	        {
	        	// repoen if already complete and not frozen
	            if (getRecordService().isDeclared(actionedUponNodeRef) &&
	                !getFreezeService().isFrozen(actionedUponNodeRef))
	            {
	                // Remove the declared aspect
	                this.getNodeService().removeAspect(actionedUponNodeRef, ASPECT_DECLARED_RECORD);
	            }
	        }
	        else
	        {
	            if (logger.isWarnEnabled())
	            {
	                logger.warn(I18NUtil.getMessage(MSG_RECORDS_ONLY_UNDECLARED));
	            }
	        }
    	}
    }
}
