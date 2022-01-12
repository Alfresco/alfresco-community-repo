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

package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Composite capability condition implementation that required at least one of the
 * capability conditions to be true.
 *
 * @author Roy Wetherall
 */
public class AtLeastOneCondition extends AbstractCapabilityCondition
{
    /** capability conditions */
    private List<CapabilityCondition> conditions;

    /**
     * @param conditions    capability conditions
     */
    public void setConditions(List<CapabilityCondition> conditions)
    {
        this.conditions = conditions;
    }
    
    /**
     * Don't use the transaction cache for the composite condition
     * 
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluate(NodeRef nodeRef)
    {
        return evaluateImpl(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;

        if (conditions != null)
        {
            for (CapabilityCondition condition : conditions)
            {
                if (condition.evaluate(nodeRef))
                {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }
}
