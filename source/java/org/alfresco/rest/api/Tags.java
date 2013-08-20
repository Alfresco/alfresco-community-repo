/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.api;

import java.util.List;

import org.alfresco.rest.api.model.Tag;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.StoreRef;

public interface Tags
{
    public List<Tag> addTags(String nodeId, List<Tag> tags);
    public Tag getTag(StoreRef storeRef, String tagId);
    public void deleteTag(String nodeId, String tagId);
    public CollectionWithPagingInfo<Tag> getTags(StoreRef storeRef, Paging paging);
    public Tag changeTag(StoreRef storeRef, String tagId, Tag tag);
    public CollectionWithPagingInfo<Tag> getTags(String nodeId, Parameters params);
}
