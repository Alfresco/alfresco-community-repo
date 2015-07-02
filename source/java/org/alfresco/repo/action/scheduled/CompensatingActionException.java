/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.action.scheduled;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.scheduled.AbstractScheduledAction.Pair;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Error that triggers the execution of compensating actions.
 * 
 * The required compensating actions are contained by the exception thrown.
 * 
 * @author Andy Hind
 */
public class CompensatingActionException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2144573075007116603L;

    List<Pair<Action, NodeRef>> compensatingActions;

    
    /**
     * @param msgId String
     */
    public CompensatingActionException(String msgId)
    {
        super(msgId);
    }

    /**
     * 
     * @param msgId String
     * @param cause Throwable
     * @param compensatingActions List<Pair<Action, NodeRef>>
     */
    public CompensatingActionException(String msgId, Throwable cause, List<Pair<Action, NodeRef>> compensatingActions)
    {
        super(msgId, cause);
        this.compensatingActions = compensatingActions;
    }

    /**
     * Get the compensationg actions
     * 
     * @return - the compensating actions
     */
    public List<Pair<Action, NodeRef>> getCompensatingActions()
    {
        return compensatingActions;
    }

    /**
     * 
     * @param msgId String
     * @param msgParams Object[]
     */
    public CompensatingActionException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * 
     * @param msgId String
     * @param cause Throwable
     */
    public CompensatingActionException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * 
     * @param msgId String
     * @param msgParams Object[]
     * @param cause Throwable
     */
    public CompensatingActionException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
