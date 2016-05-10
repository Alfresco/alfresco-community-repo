/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * @author Jamal Kaabi-Mofrad
 */
public class RequestEntityTooLargeException extends ApiException
{
    private static final long serialVersionUID = 3196212354672333823L;

    public static String DEFAULT_MESSAGE_ID = "framework.exception.RequestEntityTooLarge";

    public RequestEntityTooLargeException()
    {
        super(DEFAULT_MESSAGE_ID);
    }

    public RequestEntityTooLargeException(String msgId)
    {
        super(msgId);
    }
}
