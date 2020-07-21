/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.actions;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.Actions;
import org.alfresco.rest.api.model.Action;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.springframework.extensions.webscripts.Status;

@EntityResource(name = "action-executions", title = "Actions")
public class ActionExecutionsEntityResource implements EntityResourceAction.Create<Action>
{
    private Actions actions;

    public void setActions(Actions actions)
    {
        this.actions = actions;
    }

    @WebApiDescription(title = "Execute action", successStatus = Status.STATUS_ACCEPTED)
    @Override
    public List<Action> create(List<Action> entity, Parameters parameters)
    {
        if (entity == null || entity.size() != 1)
        {
            throw new InvalidArgumentException("Please specify one action request only.");
        }

        List<Action> result = new ArrayList<>(1);
        result.add(actions.executeAction(entity.get(0), parameters));
        return result;
    }
}
