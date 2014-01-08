/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Records management action who's implementation is delegated to an existing Action.
 * <p>
 * Useful for creating a RM version of an existing action implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class DelegateAction extends RMActionExecuterAbstractBase
{
    /** Delegate action executer*/
    private ActionExecuter delegateActionExecuter;
    
    /** should we check whether the node is frozen */
    private boolean checkFrozen = false;
    
    /**
     * @param delegateActionExecuter    delegate action executer
     */
    public void setDelegateAction(ActionExecuter delegateActionExecuter)
    {
        this.delegateActionExecuter = delegateActionExecuter;
    }
    
    /**
     * @param checkFrozen   true if we check whether the actioned upon node reference is frozen, false otherwise
     */
    public void setCheckFrozen(boolean checkFrozen)
    {
        this.checkFrozen = checkFrozen;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (nodeService.exists(actionedUponNodeRef) == true &&
            (checkFrozen == false || freezeService.isFrozen(actionedUponNodeRef) == false))
        {
            // do the property subs (if any exist)
            if (allowParameterSubstitutions == true)
            {
               parameterProcessorComponent.process(action, delegateActionExecuter.getActionDefinition(), actionedUponNodeRef);
            }
            
            delegateActionExecuter.execute(action, actionedUponNodeRef);
        }
    }
    
    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#getParameterDefintions()
     */
    @Override
    protected List<ParameterDefinition> getParameterDefintions()
    {
        return delegateActionExecuter.getActionDefinition().getParameterDefinitions();
    }
}
