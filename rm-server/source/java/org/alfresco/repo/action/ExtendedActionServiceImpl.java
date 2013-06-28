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
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionDefinition;
import org.alfresco.repo.action.evaluator.ActionConditionEvaluator;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Extended action service implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedActionServiceImpl extends ActionServiceImpl implements ApplicationContextAware
{
    /** Records management service */
    private RecordsManagementService recordsManagementService;
    
    private ApplicationContext extendedApplicationContext;

    public void setApplicationContext(ApplicationContext applicationContext)
    {
        super.setApplicationContext(applicationContext);
        extendedApplicationContext = applicationContext;
    }

    /**
     * @param recordsManagementService  records management service
     */
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }
    
    public ActionConditionDefinition getActionConditionDefinition(String name)
    {
        // get direct access to action condition definition (i.e. ignoring public flag of executer)
        ActionConditionDefinition definition = null;
        Object bean = extendedApplicationContext.getBean(name);
        if (bean != null && bean instanceof ActionConditionEvaluator)
        {
            ActionConditionEvaluator evaluator = (ActionConditionEvaluator) bean;
            definition = evaluator.getActionConditionDefintion();
        }
        return definition;
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
                if (actionDefinition instanceof RecordsManagementActionDefinition)
                {
                    if (kind != null)
                    {                        
                        Set<FilePlanComponentKind> applicableKinds = ((RecordsManagementActionDefinition)actionDefinition).getApplicableKinds();
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
}
