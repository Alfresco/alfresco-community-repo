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
package org.alfresco.repo.audit;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Audit related exceptions.
 * 
 * @author Andy Hind
 */
public class AuditException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -7947190775692164588L;

    /**
     * Simple message
     * 
     * @param msgId
     */
    public AuditException(String msgId)
    {
        super(msgId);
    }

    /**
     * I18n message
     * 
     * @param msgId
     * @param msgParams
     */
    public AuditException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * Simple message ad nested exception
     * 
     * @param msgId
     * @param cause
     */
    public AuditException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * I18n message and exception.
     * 
     * @param msgId
     * @param msgParams
     * @param cause
     */
    public AuditException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
