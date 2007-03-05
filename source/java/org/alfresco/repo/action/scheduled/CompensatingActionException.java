/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
     * @param msgId
     */
    public CompensatingActionException(String msgId)
    {
        super(msgId);
    }

    /**
     * 
     * @param msgId
     * @param cause
     * @param compensatingActions
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
     * @param msgId
     * @param msgParams
     */
    public CompensatingActionException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * 
     * @param msgId
     * @param cause
     */
    public CompensatingActionException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * 
     * @param msgId
     * @param msgParams
     * @param cause
     */
    public CompensatingActionException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
