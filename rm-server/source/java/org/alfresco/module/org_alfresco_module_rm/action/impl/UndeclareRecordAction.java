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
 * Undeclare record action
 * 
 * @author Roy Wetherall
 */
public class UndeclareRecordAction extends RMActionExecuterAbstractBase
{
    /** I18N */
    private static final String MSG_RECORDS_ONLY_UNDECLARED = "rm.action.records_only_undeclared";
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (recordsManagementService.isRecord(actionedUponNodeRef) == true)
        {
            if (recordsManagementService.isRecordDeclared(actionedUponNodeRef) == true)
            {
                // Remove the declared aspect
                this.nodeService.removeAspect(actionedUponNodeRef, ASPECT_DECLARED_RECORD);
            }
        }
        else
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_RECORDS_ONLY_UNDECLARED));
        }
    }
    
    @Override
    public Set<QName> getProtectedAspects()
    {
        HashSet<QName> qnames = new HashSet<QName>();
        qnames.add(ASPECT_DECLARED_RECORD);
        return qnames;
    }

    @Override
    protected boolean isExecutableImpl(NodeRef filePlanComponent, Map<String, Serializable> parameters, boolean throwException)
    {
        if (recordsManagementService.isRecord(filePlanComponent) == true)
        {
            if (recordsManagementService.isRecordDeclared(filePlanComponent) == true)
            {
               return true;
            }
            return false;
        }
        else
        {
            if(throwException)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_RECORDS_ONLY_UNDECLARED));
            }
            else
            {
                return false;
            }
        }
    }
    
    
}
