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

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * 
 * Edit review as of date action
 * 
 * @author Roy Wetherall
 */
public class EditReviewAsOfDateAction extends RMActionExecuterAbstractBase
{
    /** I18N */
    private static final String MSG_SPECIFY_VALID_DATE = "rm.action.specify-avlid-date";
    private static final String MSG_REVIEW_DETAILS_ONLY = "rm.action.review-details-only";
    
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(EditReviewAsOfDateAction.class);
    
    public static final String PARAM_AS_OF_DATE = "asOfDate";

	/**
	 * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
	 *      org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
	{
	    if (recordsManagementService.isRecord(actionedUponNodeRef) == true &&
	        this.nodeService.hasAspect(actionedUponNodeRef, ASPECT_VITAL_RECORD) == true)
	    {
	        // Get the action parameter
	        Date reviewAsOf = (Date)action.getParameterValue(PARAM_AS_OF_DATE);
            if (reviewAsOf == null)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_SPECIFY_VALID_DATE));
            }
	        
	        // Set the as of date    
	        this.nodeService.setProperty(actionedUponNodeRef, PROP_REVIEW_AS_OF, reviewAsOf);
	         
	    }
	}

	/**
	 * 
	 * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList)
	{
		// Intentionally empty
	}
	
	@Override
    public Set<QName> getProtectedProperties()
    {
        HashSet<QName> qnames = new HashSet<QName>();
        qnames.add(PROP_REVIEW_AS_OF);
        return qnames;
    }

    @Override
    protected boolean isExecutableImpl(NodeRef filePlanComponent, Map<String, Serializable> parameters, boolean throwException)
    {
        boolean result = false;
        if (recordsManagementService.isRecord(filePlanComponent) == true &&
            this.nodeService.hasAspect(filePlanComponent, ASPECT_VITAL_RECORD) == true)
        {
            result = true;
        }
        else
        {
            if (throwException == true)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_REVIEW_DETAILS_ONLY));
            }
        }
        return result;
    }	
}
