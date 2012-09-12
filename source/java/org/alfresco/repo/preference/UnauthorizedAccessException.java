/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.preference;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Runtime exception thrown if the user is not authorized to read, write or
 * remove other user's data.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class UnauthorizedAccessException extends AlfrescoRuntimeException
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -546260299439699139L;

    /**
     * Constructor
     * 
     * @param msgId message id
     */
    public UnauthorizedAccessException(String msgId)
    {
        super(msgId);
    }

    /**
     * Constructor
     * 
     * @param msgId message id
     * @param msgParams message params
     */
    public UnauthorizedAccessException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * Constructor
     * 
     * @param msgId message id
     * @param cause causing exception
     */
    public UnauthorizedAccessException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * Constructor
     * 
     * @param msgId message id
     * @param msgParams message params
     * @param cause causing exception
     */
    public UnauthorizedAccessException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }
}
