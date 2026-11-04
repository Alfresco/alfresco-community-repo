/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
 * Raised when audit data could not be recorded for an invocation.
 *
 * <p>
 * Audit failures must abort the audited operation rather than let it complete unrecorded. This type exists so that a transaction interceptor can tell an audit failure apart from a failure of the service it advises, and keep rolling back for the former even where it suppresses rollback for the latter.
 * </p>
 *
 * @see AuditMethodInterceptor
 */
public class AuditWriteException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1314012874559443622L;

    public AuditWriteException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
}
