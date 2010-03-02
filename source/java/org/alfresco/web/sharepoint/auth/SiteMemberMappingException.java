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
package org.alfresco.web.sharepoint.auth;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * An exception thrown by a {@link SiteMemberMapper}.
 * 
 * @author dward
 */
public class SiteMemberMappingException extends AlfrescoRuntimeException
{
    
    private static final long serialVersionUID = -7235067946629381543L;

    /**
     * Constructs a <code>SiteMemberMappingException</code>.
     * 
     * @param msgId
     *            the message id
     */
    public SiteMemberMappingException(String msgId)
    {
        super(msgId);
    }

    /**
     * Constructs a <code>SiteMemberMappingException</code>.
     * 
     * @param msgId
     *            the message id
     * @param msgParams
     *            the message parameters
     */
    public SiteMemberMappingException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * Constructs a <code>SiteMemberMappingException</code>.
     * 
     * @param msgId
     *            the message id
     * @param cause
     *            the cause
     */
    public SiteMemberMappingException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * Constructs a <code>SiteMemberMappingException</code>.
     * 
     * @param msgId
     *            the message id
     * @param msgParams
     *            the message parameters
     * @param cause
     *            the cause
     */
    public SiteMemberMappingException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
