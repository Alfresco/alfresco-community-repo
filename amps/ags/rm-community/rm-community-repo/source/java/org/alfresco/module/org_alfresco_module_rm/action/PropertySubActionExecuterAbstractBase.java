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

package org.alfresco.module.org_alfresco_module_rm.action;

import org.alfresco.repo.action.parameter.ParameterProcessorComponent;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Extension to action implementation hierarchy to insert parameter substitution processing.
 *
 * NOTE:  this should eventually be pushed into the core.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public abstract class PropertySubActionExecuterAbstractBase extends AuditableActionExecuterAbstractBase
{
    /** Parameter processor component */
    private ParameterProcessorComponent parameterProcessorComponent;

    /** Indicates whether parameter substitutions are allowed */
    private boolean allowParameterSubstitutions = false;

    /**
     * @return Parameter processor component
     */
    protected ParameterProcessorComponent getParameterProcessorComponent()
    {
        return this.parameterProcessorComponent;
    }

    /**
     * @return True if parameter substitutions are allowed, false otherwise
     */
    protected boolean isAllowParameterSubstitutions()
    {
        return this.allowParameterSubstitutions;
    }

    /**
     * 	@param parameterProcessorComponent	parameter processor component
     */
    public void setParameterProcessorComponent(ParameterProcessorComponent parameterProcessorComponent)
    {
        this.parameterProcessorComponent = parameterProcessorComponent;
    }

    /**
     * @param allowParameterSubstitutions	true if property subs allowed, false otherwise
     */
    public void setAllowParameterSubstitutions(boolean allowParameterSubstitutions)
    {
        this.allowParameterSubstitutions = allowParameterSubstitutions;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#execute(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void execute(Action action, NodeRef actionedUponNodeRef)
    {
    	// do the property subs (if any exist)
        if (isAllowParameterSubstitutions())
        {
           getParameterProcessorComponent().process(action, getActionDefinition(), actionedUponNodeRef);
        }

        super.execute(action, actionedUponNodeRef);
    }
}
