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

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Base exception for sceduled actions.
 * 
 * @author Andy Hind
 */
public class ScheduledActionException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -543079391770744598L;

    public ScheduledActionException(String msgId)
    {
        super(msgId);
    }

    public ScheduledActionException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public ScheduledActionException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public ScheduledActionException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
