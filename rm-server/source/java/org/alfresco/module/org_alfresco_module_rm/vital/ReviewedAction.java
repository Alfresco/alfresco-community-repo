/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
	    VitalRecordDefinition vrDef = vitalRecordService.getVitalRecordDefinition(actionedUponNodeRef);
        if (vrDef != null && vrDef.isEnabled())
        {
    	    if (recordService.isRecord(actionedUponNodeRef))
    	    {
    	        reviewRecord(actionedUponNodeRef, vrDef);
	        }
    	    else if (recordFolderService.isRecordFolder(actionedUponNodeRef))
    	    {
    	        for (NodeRef record : recordService.getRecords(actionedUponNodeRef))
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

            this.nodeService.setProperty(nodeRef, PROP_REVIEW_AS_OF, reviewAsOf);
            //TODO And record previous review date, time, user
        }
	}
}
