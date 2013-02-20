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
package org.alfresco.repo.rule;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanAuthenticationService;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;

/**
 * Extended rule service implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedRuleServiceImpl extends RuleServiceImpl
{
    private boolean runAsRmAdmin = true;
    
    private FilePlanAuthenticationService filePlanAuthenticationService;
    
    private RecordsManagementService recordsManagementService;
    
    public void setRunAsRmAdmin(boolean runAsRmAdmin)
    {
        this.runAsRmAdmin = runAsRmAdmin;
    }
    
    public void setFilePlanAuthenticationService(FilePlanAuthenticationService filePlanAuthenticationService)
    {
        this.filePlanAuthenticationService = filePlanAuthenticationService;
    }
    
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }
    
    @Override
    public void executeRule(final Rule rule, final NodeRef nodeRef, final Set<ExecutedRuleData> executedRules)
    {
        if (isFilePlanComponentRule(rule) == true && runAsRmAdmin == true)
        {     
            filePlanAuthenticationService.runAsRmAdmin(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    ExtendedRuleServiceImpl.super.executeRule(rule, nodeRef, executedRules);
                    return null;
                }
            });
        }
        else
        {
            // just execute the rule as the current user
            super.executeRule(rule, nodeRef, executedRules);
        }       
    }
    
    private boolean isFilePlanComponentRule(Rule rule)
    {
        NodeRef nodeRef = getOwningNodeRef(rule);
        return recordsManagementService.isFilePlanComponent(nodeRef);
    }   
}
