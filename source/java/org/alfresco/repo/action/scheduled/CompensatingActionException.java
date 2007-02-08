/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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

    public CompensatingActionException(String msgId)
    {
        super(msgId);
    }

    public CompensatingActionException(String msgId, Throwable cause, List<Pair<Action, NodeRef>> compensatingActions)
    {
        super(msgId, cause);
        this.compensatingActions = compensatingActions;
    }

    public List<Pair<Action, NodeRef>> getCompensatingActions()
    {
        return compensatingActions;
    }

    public CompensatingActionException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public CompensatingActionException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public CompensatingActionException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
