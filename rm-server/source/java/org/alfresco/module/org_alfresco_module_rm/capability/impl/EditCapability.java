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
package org.alfresco.module.org_alfresco_module_rm.capability.impl;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;

/**
 * Edit capability, checks the permission and whether the current user is the owner of the 
 * object.
 * 
 * @author Roy Wetherall
 */
public class EditCapability extends DeclarativeCapability 
{
    private OwnableService ownableService;

    private OwnableService getOwnableService()
    {
        if (ownableService == null)
        {
            ownableService = (OwnableService)applicationContext.getBean("OwnableService");
        }
        return ownableService;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    public int evaluate(final NodeRef nodeRef)
    {
        // The default state is not knowing anything
        int result = AccessDecisionVoter.ACCESS_ABSTAIN;
        
        // Check we are dealing with a file plan component
        if (rmService.isFilePlanComponent(nodeRef) == true)
        {
            // Now the default state is denied
            result = AccessDecisionVoter.ACCESS_DENIED;
            
            // Check the kind of the object, the permissions and the conditions
            if (checkKinds(nodeRef) == true && checkConditions(nodeRef) == true)
            {
                if (checkPermissions(nodeRef) == true)
                {
                    result = AccessDecisionVoter.ACCESS_GRANTED;
                }
                else
                {
                    result = AuthenticationUtil.runAs(new RunAsWork<Integer>()
                    {
                        @Override
                        public Integer doWork() throws Exception
                        {
                            Integer result = Integer.valueOf(AccessDecisionVoter.ACCESS_DENIED);
                                
                            // Since we know this is undeclared if you are the owner then you should be able to 
                            // edit the records meta-data (otherwise how can it be declared by the user?)
                            if (getOwnableService().hasOwner(nodeRef) == true)
                            {
                                String user = AuthenticationUtil.getFullyAuthenticatedUser();
                                if (user != null &&
                                    getOwnableService().getOwner(nodeRef).equals(user) == true)
                                {
                                    result = Integer.valueOf(AccessDecisionVoter.ACCESS_GRANTED);
                                }
                            }
                            
                            return result;
                        }
                        
                    }, AuthenticationUtil.getSystemUserName()).intValue();
                }
            }
        }
 
        return result;        
    }
}