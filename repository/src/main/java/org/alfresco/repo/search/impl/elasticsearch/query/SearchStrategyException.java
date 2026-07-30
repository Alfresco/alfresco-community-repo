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
 * Base class for recoverable exceptions thrown by {@link SearchStrategy} implementations.
 *
 * <p>
 * {@link ElasticsearchQueryExecutor} lets these exceptions propagate unchanged so the REST layer can map them to appropriate HTTP responses instead of returning a generic 500 error.
 *
 * <p>
 * This also provides an extension point for Enterprise-only search strategies without introducing compile-time dependencies on Enterprise-specific exception types.
 */
public abstract class SearchStrategyException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1L;

    protected SearchStrategyException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
