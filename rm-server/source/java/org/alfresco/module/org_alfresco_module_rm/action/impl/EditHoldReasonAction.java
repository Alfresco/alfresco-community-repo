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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Edit freeze reason Action
 * 
 * @author Roy Wetherall
 */
public class EditHoldReasonAction extends RMActionExecuterAbstractBase
{
    private static final String MSG_HOLD_EDIT_REASON_NONE = "rm.action.hold-edit-reason-none";
    private static final String MSG_HOLD_EDIT_TYPE = "rm.action.hold-edit-type";
    
    /** Parameter names */
    public static final String PARAM_REASON = "reason";
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        QName nodeType = this.nodeService.getType(actionedUponNodeRef);
        if (this.dictionaryService.isSubClass(nodeType, TYPE_HOLD) == true)
        {
            // Get the property values
            String reason = (String)action.getParameterValue(PARAM_REASON);
            if (reason == null || reason.length() == 0)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_HOLD_EDIT_REASON_NONE));
            }
            
            // Set the hold reason
            nodeService.setProperty(actionedUponNodeRef, PROP_HOLD_REASON, reason);

        }
        else
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_HOLD_EDIT_TYPE, TYPE_HOLD.toString(), actionedUponNodeRef.toString()));
        }                
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#getProtectedAspects()
     */
    @Override
    public Set<QName> getProtectedAspects()
    {
        HashSet<QName> qnames = new HashSet<QName>();
        qnames.add(ASPECT_FROZEN);
        return qnames;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#getProtectedProperties()
     */
    @Override
    public Set<QName> getProtectedProperties()
    {
        HashSet<QName> qnames = new HashSet<QName>();
        qnames.add(PROP_HOLD_REASON);
        return qnames;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#isExecutableImpl(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, boolean)
     */
    @Override
    protected boolean isExecutableImpl(NodeRef filePlanComponent, Map<String, Serializable> parameters, boolean throwException)
    {
        QName nodeType = this.nodeService.getType(filePlanComponent);
        if (this.dictionaryService.isSubClass(nodeType, TYPE_HOLD) == true)
        {
            return true;
        }
        else
        {
            if(throwException)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_HOLD_EDIT_TYPE, TYPE_HOLD.toString(), filePlanComponent.toString()));
            }
            else
            {
                return false;
            }
        }        
    }

    
}