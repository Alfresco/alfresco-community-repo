/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
     * @param msgId String
     */
    public AuditException(String msgId)
    {
        super(msgId);
    }

    /**
     * I18n message
     * 
     * @param msgId String
     * @param msgParams Object[]
     */
    public AuditException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * Simple message ad nested exception
     * 
     * @param msgId String
     * @param cause Throwable
     */
    public AuditException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * I18n message and exception.
     * 
     * @param msgId String
     * @param msgParams Object[]
     * @param cause Throwable
     */
    public AuditException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
