/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.capability.declarative;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CompositeCapability;
import org.alfresco.service.cmr.repository.NodeRef;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * Generic implementation of a composite capability
 *
 * @author Roy Wetherall
 */
public class DeclarativeCompositeCapability extends DeclarativeCapability
                                            implements CompositeCapability
{
    /** set of capabilities */
    private Set<Capability> capabilities;

    /**
     * @param capabilities   set of capabilities
     */
    public void setCapabilities(Set<Capability> capabilities)
    {
        this.capabilities = capabilities;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CompositeCapability#getCapabilities()
     */
    @Override
    public Set<Capability> getCapabilities()
    {
        return this.capabilities;
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
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Evaluating child capability " + capability.getName() + " on nodeRef " + nodeRef.toString() + " for composite capability " + name);
            }

            int capabilityResult = capability.evaluate(nodeRef);
            if (capabilityResult != AccessDecisionVoter.ACCESS_DENIED)
            {
                result = AccessDecisionVoter.ACCESS_ABSTAIN;
                if (!isUndetermined() && capabilityResult == AccessDecisionVoter.ACCESS_GRANTED)
                {
                    result = AccessDecisionVoter.ACCESS_GRANTED;
                }
                break;
            }
            else
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Access denied for child capability " + capability.getName() + " on nodeRef " + nodeRef.toString() + " for composite capability " + name);
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
        if (getFilePlanService().isFilePlanComponent(source) &&
            getFilePlanService().isFilePlanComponent(target))
        {
            // Check the kind of the object, the permissions and the conditions
            if (checkKinds(source) && checkPermissions(source) && checkConditions(source))
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
