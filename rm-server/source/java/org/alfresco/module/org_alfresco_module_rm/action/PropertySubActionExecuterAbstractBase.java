/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.action;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.action.parameter.ParameterProcessorComponent;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Extension to action implementation hierarchy to insert parameter substitution processing.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public abstract class PropertySubActionExecuterAbstractBase extends ActionExecuterAbstractBase
{
    private ParameterProcessorComponent parameterProcessorComponent;
    
    protected boolean allowParameterSubstitutions = false;
   
    public void setParameterProcessorComponent(ParameterProcessorComponent parameterProcessorComponent)
    {
        this.parameterProcessorComponent = parameterProcessorComponent;
    }
    
    public void setAllowParameterSubstitutions(boolean allowParameterSubstitutions)
    {
        this.allowParameterSubstitutions = allowParameterSubstitutions;
    }
    
    @Override
    public void execute(Action action, NodeRef actionedUponNodeRef)
    {
        if (allowParameterSubstitutions == true)
        {
           parameterProcessorComponent.process(action, getActionDefinition(), actionedUponNodeRef);
        }
        
        super.execute(action, actionedUponNodeRef);
    }
}
