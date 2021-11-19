/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.framework.core.exceptions;

import org.alfresco.service.Experimental;

/**
 * Thrown when the content is archived and not readily accessible.
 * Status is <i>Precondition Failed</i> client error = 412.
 *
 * @author David Edwards
 */
@Experimental
public class ArchivedContentException  extends  ApiException
{

    public static String DEFAULT_MESSAGE_ID = "framework.exception.ArchivedContent";

    public ArchivedContentException() 
    {
        super(DEFAULT_MESSAGE_ID);
    }

    public ArchivedContentException(String message) 
    {
        this(DEFAULT_MESSAGE_ID, message);
    }

    private ArchivedContentException(String msgId, String message) 
    {
        super(msgId, message);
    }

    public ArchivedContentException(Throwable cause) 
    {
        this(DEFAULT_MESSAGE_ID, cause.getLocalizedMessage(), cause);
    }
    
    public ArchivedContentException(String message, Throwable cause) 
    {
        this(DEFAULT_MESSAGE_ID, message, cause);
    }
    
    private ArchivedContentException(String msgId, String message, Throwable cause) 
    {
        super(msgId, message, cause);
    }
    
}
