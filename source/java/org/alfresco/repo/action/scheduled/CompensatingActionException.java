/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
