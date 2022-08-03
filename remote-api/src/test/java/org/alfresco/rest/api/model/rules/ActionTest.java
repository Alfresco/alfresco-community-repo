/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.model.rules;

import static org.alfresco.repo.action.executer.SetPropertyValueActionExecuter.PARAM_PROPERTY;
import static org.alfresco.repo.action.executer.SetPropertyValueActionExecuter.PARAM_VALUE;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.action.ActionImpl;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Test;

@Experimental
public class ActionTest
{

    private static final String ACTION_DEFINITION_NAME = "actionDefName";
    private static final Map<String, Serializable> parameters = new HashMap<>();

    static
    {
        parameters.put(PARAM_PROPERTY, "propertyName");
        parameters.put(PARAM_VALUE, "propertyValue");
    }

    @Test
    public void testFrom()
    {
        final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ruleId");
        final org.alfresco.service.cmr.action.Action actionModel = new ActionImpl(nodeRef, "actionId", ACTION_DEFINITION_NAME, parameters);
        final Action expectedAction = Action.builder().actionDefinitionId(ACTION_DEFINITION_NAME).params(parameters).create();

        final Action actualAction = Action.from(actionModel);

        assertThat(actualAction).isNotNull().usingRecursiveComparison().isEqualTo(expectedAction);
    }

    @Test
    public void testFromActionModelWithNullValues()
    {
        final org.alfresco.service.cmr.action.Action actionModel = new ActionImpl(null, null, null);
        final Action expectedAction = Action.builder().params(Collections.emptyMap()).create();

        final Action actualAction = Action.from(actionModel);

        assertThat(actualAction).isNotNull().usingRecursiveComparison().isEqualTo(expectedAction);
    }
}
