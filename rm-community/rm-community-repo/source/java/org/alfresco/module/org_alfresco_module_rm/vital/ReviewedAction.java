 
package org.alfresco.module.org_alfresco_module_rm.vital;

import java.util.Date;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reviewed action.
 *
 * @author Neil McErlean
 */
public class ReviewedAction extends RMActionExecuterAbstractBase
{
    private static Log logger = LogFactory.getLog(ReviewedAction.class);

	/**
	 *
	 * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
	 *      org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
	{
	    VitalRecordDefinition vrDef = getVitalRecordService().getVitalRecordDefinition(actionedUponNodeRef);
        if (vrDef != null && vrDef.isEnabled())
        {
    	    if (getRecordService().isRecord(actionedUponNodeRef))
    	    {
    	        reviewRecord(actionedUponNodeRef, vrDef);
	        }
    	    else if (getRecordFolderService().isRecordFolder(actionedUponNodeRef))
    	    {
    	        for (NodeRef record : getRecordService().getRecords(actionedUponNodeRef))
                {
                    reviewRecord(record, vrDef);
                }
    	    }
	    }
	}

	/**
	 * Make record as reviewed.
	 *
	 * @param nodeRef
	 * @param vrDef
	 */
	private void reviewRecord(NodeRef nodeRef, VitalRecordDefinition vrDef)
	{
        // Calculate the next review date
        Date reviewAsOf = vrDef.getNextReviewDate();
        if (reviewAsOf != null)
        {
            // Log
            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                    msg.append("Setting new reviewAsOf property [")
                       .append(reviewAsOf)
                       .append("] on ")
                       .append(nodeRef);
                 logger.debug(msg.toString());
            }

            this.getNodeService().setProperty(nodeRef, PROP_REVIEW_AS_OF, reviewAsOf);
            //TODO And record previous review date, time, user
        }
	}
}
