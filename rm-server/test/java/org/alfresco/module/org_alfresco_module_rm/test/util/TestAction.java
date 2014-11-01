/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.util;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

public class TestAction extends RMActionExecuterAbstractBase
{
    public static final String NAME = "testAction";
    public static final String PARAM = "testActionParam";
    public static final String PARAM_VALUE = "value";

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (!action.getParameterValue(PARAM).equals(PARAM_VALUE))
        {
            throw new RuntimeException("Unexpected parameter value.  Expected " + PARAM_VALUE + " actual " + action.getParameterValue(PARAM));
        }
        this.getNodeService().addAspect(actionedUponNodeRef, ASPECT_RECORD, null);
    }

    @Override
    public boolean isDispositionAction()
    {
        return true;
    }
}
