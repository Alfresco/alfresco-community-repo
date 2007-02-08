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
package org.alfresco.web.api;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * API Service Exceptions.
 * 
 * @author David Caruana
 */
public class APIException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -7338963365877285084L;

    public APIException(String msgId)
    {
       super(msgId);
    }
    
    public APIException(String msgId, Throwable cause)
    {
       super(msgId, cause);
    }

    public APIException(String msgId, Object ... args)
    {
        super(msgId, args);
    }

    public APIException(String msgId, Throwable cause, Object ... args)
    {
        super(msgId, args, cause);
    }
}
