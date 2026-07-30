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
package org.alfresco.repo.search.impl.elasticsearch.query;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Raised when a {@code search_after} cursor is invalid, malformed, or refers to a Point-In-Time (PIT) snapshot that the search backend has already released (e.g. because its {@code keep_alive} elapsed or the search backend was restarted). This is a client-recoverable condition, not a server failure: the caller should start a new pagination session by omitting {@code searchAfter}.
 */
public class InvalidSearchAfterCursorException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1L;

    public InvalidSearchAfterCursorException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
