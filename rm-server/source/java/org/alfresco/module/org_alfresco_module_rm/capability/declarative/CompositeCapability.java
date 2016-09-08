/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.capability.declarative;

import java.util.List;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Generic implementation of a composite capability 
 * 
 * @author Roy Wetherall
 */
public class CompositeCapability extends DeclarativeCapability
{
    /** List of capabilities */
    private List<Capability> capabilities;
    
    /**
     * @param capabilites   list of capabilities
     */
    public void setCapabilities(List<Capability> capabilities)
    {
        this.capabilities = capabilities;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability#evaluateImpl(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public int evaluateImpl(NodeRef nodeRef)
    {   
        int result = AccessDecisionVoter.ACCESS_DENIED;
        
        // Check each capability using 'OR' logic
        for (Capability capability : capabilities)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Evaluating child capability " + capability.getName() + " on nodeRef " + nodeRef.toString() + " for composite capability " + name);
            }

            int capabilityResult = capability.evaluate(nodeRef);
            if (capabilityResult != AccessDecisionVoter.ACCESS_DENIED) 
            {
                result = AccessDecisionVoter.ACCESS_ABSTAIN;
                if (isUndetermined() == false && capabilityResult == AccessDecisionVoter.ACCESS_GRANTED)
                {
                    result = AccessDecisionVoter.ACCESS_GRANTED;
                }
                break;
            }
            else
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Access denied for child capability " + capability.getName() + " on nodeRef " + nodeRef.toString() + " for composite capability " + name);
                }
            }
        }
        
        return result;
    }
    
    /**
     * If a target capability is specified then we evaluate that.  Otherwise we combine the results of the provided capabilities.
     * 
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability#evaluate(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public int evaluate(NodeRef source, NodeRef target)
    {
        int result = AccessDecisionVoter.ACCESS_ABSTAIN;
        
        // Check we are dealing with a file plan component
        if (getFilePlanService().isFilePlanComponent(source) == true && 
            getFilePlanService().isFilePlanComponent(target) == true)
        {
            // Check the kind of the object, the permissions and the conditions
            if (checkKinds(source) == true && checkPermissions(source) == true && checkConditions(source) == true)
            {
                if (targetCapability != null)
                {
                    result = targetCapability.evaluate(target);
                }
                
                if (AccessDecisionVoter.ACCESS_DENIED != result)
                {
                    // Check each capability using 'OR' logic
                    for (Capability capability : capabilities)
                    {
                        result = capability.evaluate(source, target);
                        if (result == AccessDecisionVoter.ACCESS_GRANTED) 
                        {                            
                            break;
                        }
                    }
                    
                }
            }
            else
            {
                result = AccessDecisionVoter.ACCESS_DENIED;
            }
        }
        
        return result;
    }
}
