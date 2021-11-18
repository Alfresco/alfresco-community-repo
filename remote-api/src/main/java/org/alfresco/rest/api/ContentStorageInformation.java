/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api;

import org.alfresco.rest.api.model.ContentStorageInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Storage information for content API
 *
 * @author mpichura
 */

public interface ContentStorageInformation
{
    /**
     * @param nodeId          Identifier of the node
     * @param contentPropName Qualified name of content property (e.g. 'cm_content')
     * @param parameters      {@link Parameters} object to get the parameters passed into the request
     * @return {@link ContentStorageInfo} object consisting of qualified name of content property and a map of storage properties
     */
    ContentStorageInfo getStorageInfo(String nodeId, String contentPropName, Parameters parameters);
}
