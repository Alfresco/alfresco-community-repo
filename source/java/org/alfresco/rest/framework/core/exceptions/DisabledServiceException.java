/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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

package org.alfresco.rest.framework.core.exceptions;

/**
 * Thrown when the required service is disabled.
 * Default status is <i>Not Implemented</i> server error = 501.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class DisabledServiceException extends ApiException
{
    private static final long serialVersionUID = 1286704207931025507L;

    public DisabledServiceException(String msgId)
    {
        super(msgId);
    }

    public DisabledServiceException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }
}
