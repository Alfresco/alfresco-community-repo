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
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Change or delete references capability
 * 
 * @author Roy Wetherall
 */
public class ChangeOrDeleteReferencesCapability extends DeclarativeCapability
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability#evaluateImpl(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected int evaluateImpl(NodeRef nodeRef)
    {
        // Can't be sure, because we don't have information about the target so we still abstain
        return AccessDecisionVoter.ACCESS_ABSTAIN;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.AbstractCapability#evaluate(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    public int evaluate(NodeRef source, NodeRef target)
    {
        if (getFilePlanService().isFilePlanComponent(source))
        {
            if (target != null)
            {
                if (getFilePlanService().isFilePlanComponent(target) == true)
                {
                    if (checkConditions(source) == true && checkConditions(target) == true)
                    {                        
                        if (checkPermissions(source) == true && checkPermissions(target) == true)
                        {                                
                            return AccessDecisionVoter.ACCESS_GRANTED;
                        }
                    }
                }
            }
            else
            {
                if (checkConditions(source) == true)
                {
                    if (checkPermissions(source) == true)
                    {
                        return AccessDecisionVoter.ACCESS_GRANTED;
                    }
                }
            }

            return AccessDecisionVoter.ACCESS_DENIED;
        }
        else
        {
            return AccessDecisionVoter.ACCESS_ABSTAIN;
        }
    }
}