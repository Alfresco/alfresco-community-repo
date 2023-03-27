/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

import java.util.List;

import org.alfresco.rest.api.model.Tag;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.StoreRef;

public interface Tags
{
    List<Tag> addTags(String nodeId, List<Tag> tags);
    Tag getTag(StoreRef storeRef, String tagId);
    void deleteTag(String nodeId, String tagId);
    CollectionWithPagingInfo<Tag> getTags(StoreRef storeRef, Parameters params);
    Tag changeTag(StoreRef storeRef, String tagId, Tag tag);
    CollectionWithPagingInfo<Tag> getTags(String nodeId, Parameters params);

    @Experimental
    List<Tag> createTags(StoreRef storeRef, List<Tag> tags, Parameters parameters);

    @Experimental
    default List<Tag> createTags(List<Tag> tags, Parameters parameters)
    {
        return createTags(STORE_REF_WORKSPACE_SPACESSTORE, tags, parameters);
    }

    void deleteTagById(StoreRef storeRef, String tagId);
}
