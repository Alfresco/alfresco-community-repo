/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Access denied exception thrown when a user tries to execute a method call on an uncleared node.
 *
 * @author Tuna Aksoy
 * @since 3.0.a
 */
public class AccessDeniedException extends AlfrescoRuntimeException
{
    /** Serial version uid */
    private static final long serialVersionUID = -1546218007029075883L;

    /**
     * Constructor
     *
     * @param key The key of the exception to be localized
     */
    public AccessDeniedException(String key)
    {
        super(key);
    }
}
