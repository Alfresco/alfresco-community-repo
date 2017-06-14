/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api;

import org.alfresco.rest.api.model.Group;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Groups API
 *
 * @author cturlica
 */
public interface Groups
{
    String PARAM_ID = "id";
    String PARAM_DISPLAY_NAME = "displayName";
    String PARAM_INCLUDE_PARENT_IDS = "parentIds";
    String PARAM_INCLUDE_ZONES = "zones";
    String PARAM_IS_ROOT = "isRoot";

    /**
     * Gets a list of groups.
     * 
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *        including:
     *        - filter, sort & paging params (where, orderBy, skipCount, maxItems)
     *        - incFiles, incFolders (both true by default)
     * @return a paged list of {@code org.alfresco.rest.api.model.Group} objects
     */
    CollectionWithPagingInfo<Group> getGroups(Parameters parameters);

}
