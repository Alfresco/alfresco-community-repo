/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Indicates whether the given disposition action is scheduled next
 *
 * @author Roy Wetherall
 */
public class IsScheduledCapabilityCondition extends AbstractCapabilityCondition
{
    /** Disposition action */
    private String dispositionAction;

    /**
     * @param dispositionAction
     *            disposition action
     */
    public void setDispositionAction(String dispositionAction)
    {
        this.dispositionAction = dispositionAction;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;

        DispositionAction nextDispositionAction = dispositionService.getNextDispositionAction(nodeRef);
        if (nextDispositionAction != null)
        {
            // Get the disposition actions name
            String actionName = nextDispositionAction.getName();
            if (actionName.equals(dispositionAction) &&
                    dispositionService.isNextDispositionActionEligible(nodeRef))
            {
                result = true;
            }
        }

        return result;
    }
}
