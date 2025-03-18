/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.workflow.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;

/**
 * Representation of a variable used in an Activiti task.
 * 
 * @author Frederik Heremans
 */
public class TaskVariable extends Variable
{
    protected String scope;

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    public String getScope()
    {
        return scope;
    }

    @JsonIgnore
    public VariableScope getVariableScope()
    {
        VariableScope result = null;
        if (scope != null)
        {
            result = VariableScope.getScopeForValue(scope);
            if (result == null)
            {
                throw new InvalidArgumentException("Illegal value for variable scope: '" + scope + "'.");
            }
        }
        return result;
    }

    public void setVariableScope(VariableScope variableScope)
    {
        if (variableScope != null)
        {
            this.scope = variableScope.getValue();
        }
        else
        {
            this.scope = null;
        }
    }
}
