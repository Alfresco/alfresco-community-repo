/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Extended action service implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedActionServiceImpl extends ActionServiceImpl
{
    /** Records management service */
    private RecordsManagementService recordsManagementService;
    
    /**
     * @param recordsManagementService  records management service
     */
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }
    
    /**
     * @see org.alfresco.repo.action.ActionServiceImpl#getActionDefinitions(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<ActionDefinition> getActionDefinitions(NodeRef nodeRef)
    {
        List<ActionDefinition> result = null;
        
        // first use the base implementation to get the list of action definitions
        List<ActionDefinition> actionDefinitions = super.getActionDefinitions(nodeRef);
        
        if (nodeRef == null)
        {
            // nothing to filter
            result = actionDefinitions;
        }
        else
        {
            // get the file component kind of the node reference
            FilePlanComponentKind kind = recordsManagementService.getFilePlanComponentKind(nodeRef);
            result = new ArrayList<ActionDefinition>(actionDefinitions.size());
            
            // check each action definition
            for (ActionDefinition actionDefinition : actionDefinitions)
            {
                if (actionDefinition instanceof ExtendedActionDefinition)
                {
                    if (kind != null)
                    {                        
                        Set<FilePlanComponentKind> applicableKinds = ((ExtendedActionDefinition)actionDefinition).getApplicableKinds();
                        if (applicableKinds == null || applicableKinds.size() == 0 || applicableKinds.contains(kind))
                        {
                            // an RM action can only act on a RM artifact
                            result.add(actionDefinition);
                        }
                    }  
                }
                else
                {
                    if (kind == null)
                    {
                        // a non-RM action can only act on a non-RM artifact
                        result.add(actionDefinition);
                    }                    
                }
            }            
        }
        
        return result;
    }
    
    /**
     * @see org.alfresco.repo.action.ActionServiceImpl#postCommit()
     */
    @Override
    public void postCommit()
    {
        super.postCommit();
    }
}
