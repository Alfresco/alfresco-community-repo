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

package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Freeze indicator.
 * <p>
 * Only shows as frozen if the user can 'read' the holds 
 * that hold the nodeRef.
 * 
 * @author Roy Wetherall
 */
public class FrozenEvaluator extends BaseEvaluator
{
    /** hold service */
    private HoldService holdService;
    
    /**
     * @param holdService   hold service
     */
    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }
    
    /**
     * Only indicate the node is frozen if the user can 'read' at least one of the holds
     * 
     * @see org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator#evaluateImpl(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        List<NodeRef> heldBy = holdService.heldBy(nodeRef, true);
        return !heldBy.isEmpty();
    }
}
