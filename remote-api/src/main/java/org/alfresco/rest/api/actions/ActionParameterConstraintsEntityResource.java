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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.actions;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.rest.api.Actions;
import org.alfresco.rest.api.model.ActionParameterConstraint;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;

@EntityResource(name="action-parameter-constraints", title = "Action parameter constraints")
@Experimental
public class ActionParameterConstraintsEntityResource implements EntityResourceAction.ReadById<ActionParameterConstraint>
{
    private final Actions actions;

    public ActionParameterConstraintsEntityResource(Actions actions)
    {
        this.actions = actions;
    }

    @WebApiDescription(title = "Get single action parameter constraint",
            description = "Retrieves a single action parameter constraint by constraint name",
            successStatus = HttpServletResponse.SC_OK)
    @Experimental
    @Override
    public ActionParameterConstraint readById(String constraintName, Parameters parameters) throws EntityNotFoundException
    {
        return actions.getActionConstraint(constraintName);
    }
}
