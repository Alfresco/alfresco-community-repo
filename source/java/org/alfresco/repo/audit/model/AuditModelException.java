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
package org.alfresco.repo.audit.model;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exceptions from the audit model package.
 * 
 * @author Andy Hind
 */
public class AuditModelException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -2527034441058184109L;

    public AuditModelException(String msgId)
    {
        super(msgId);
    }

    public AuditModelException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public AuditModelException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public AuditModelException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
