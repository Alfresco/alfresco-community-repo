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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.impl;

import org.alfresco.rest.api.ContentStorageClasses;
import org.alfresco.rest.api.model.StorageClass;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.cmr.repository.ContentService;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Centralises access to storage classes functionality
 */
public class ContentStorageClassesImpl implements ContentStorageClasses
{
    private ContentService contentService;
    
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    @Override
    public CollectionWithPagingInfo<StorageClass> getStorageClasses(Paging paging)
    {
        Set<String> storageClasses = contentService.getSupportedStorageClasses();
        return CollectionWithPagingInfo.asPaged(paging, storageClasses.stream().map(StorageClass::new).collect(Collectors.toList()));
    }
}
