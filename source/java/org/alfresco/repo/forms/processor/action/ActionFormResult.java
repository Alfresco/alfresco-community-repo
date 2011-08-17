/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.forms.processor.action;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.util.ParameterCheck;

/**
 * Class used purely to represent the result of an action being 
 * executed via the {@link ActionFormProcessor}.
 * 
 * This class holds the {@link Action} executed and any optional
 * results stored by the action.
 *
 * @author Gavin Cornwell
 */
public class ActionFormResult
{
    private Action action;
    private Object result;
    
    /**
     * Default constructor.
     * 
     * @param action The action that was executed, can not be null
     * @param result The result from the action, can be null
     */
    public ActionFormResult(Action action, Object result)
    {
        ParameterCheck.mandatory("action", action);
        
        this.action = action;
        this.result = result;
    }

    /**
     * Returns the action that was executed
     * 
     * @return The executed Action
     */
    public Action getAction()
    {
        return this.action;
    }

    /**
     * Returns the result from the executed action
     * 
     * @return The result or null if there were no results
     */
    public Object getResult()
    {
        return this.result;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(this.action.toString());
        builder.append(" result=").append(this.result);
        return builder.toString();
    }
}
