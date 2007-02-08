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
package org.alfresco.service.cmr.repository;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * @author Kevin Roast
 */
public class TemplateException extends AlfrescoRuntimeException
{
    /**
     * @param msgId
     */
    public TemplateException(String msgId)
    {
        super(msgId);
    }

    /**
     * @param msgId
     * @param cause
     */
    public TemplateException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
    
    /**
     * @param msgId
     * @param params
     */
    public TemplateException(String msgId, Object[] params)
    {
        super(msgId, params);
    }
    
    /**
     * @param msgId
     * @param msgParams
     * @param cause
     */
    public TemplateException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }
}
