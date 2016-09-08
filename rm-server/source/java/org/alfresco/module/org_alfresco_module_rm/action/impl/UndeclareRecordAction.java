/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
    	if (nodeService.exists(actionedUponNodeRef) == true)
    	{
	        if (recordService.isRecord(actionedUponNodeRef) == true)
	        {
	        	// repoen if already complete and not frozen
	            if (recordService.isDeclared(actionedUponNodeRef) == true &&
	                freezeService.isFrozen(actionedUponNodeRef) == false)
	            {
	                // Remove the declared aspect
	                this.nodeService.removeAspect(actionedUponNodeRef, ASPECT_DECLARED_RECORD);
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
