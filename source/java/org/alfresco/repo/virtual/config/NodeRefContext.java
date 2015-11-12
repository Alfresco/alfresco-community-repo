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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.config;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * A repository context in which a {@link NodeRefPathExpression} should resolve
 * to a {@link NodeRef} using a relative name or qname path.
 */
public interface NodeRefContext
{
    /**
     * @param namePath
     * @param resolver
     * @return the {@link NodeRef} the given name path resolves to using the
     *         supplied resolver.
     */
    NodeRef resolveNamePath(String[] namePath, NodeRefResolver resolver);

    /**
     * @param qNamePath
     * @param resolver
     * @return the {@link NodeRef} the given {@link QName} prefixed string path
     *         resolves to using the supplied resolver.
     */
    NodeRef resolveQNamePath(String[] qNamePath, NodeRefResolver resolver);

    /**
     * @return the name of this context
     */
    String getContextName();

    NodeRef createNamePath(String[] namePath, NodeRefResolver resolver);

    NodeRef createQNamePath(String[] qNamePath, String[] names, NodeRefResolver resolver);

}
