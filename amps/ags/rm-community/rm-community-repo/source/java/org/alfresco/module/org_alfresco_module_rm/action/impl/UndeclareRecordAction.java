/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
