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

    /**
     * Exception generated from scheduled actions
     * 
     * @param msgId
     */
    public ScheduledActionException(String msgId)
    {
        super(msgId);
    }

    /**
     * Exception generated from scheduled actions
     * 
     * @param msgId
     * @param msgParams
     */
    public ScheduledActionException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * Exception generated from scheduled actions
     * 
     * @param msgId
     * @param cause
     */
    public ScheduledActionException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * Exception generated from scheduled actions
     * 
     * @param msgId
     * @param msgParams
     * @param cause
     */
    public ScheduledActionException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
