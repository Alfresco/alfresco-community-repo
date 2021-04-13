/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.content;

import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception produced when a storage classes is not supported by a particular {@link ContentStore} implementation.
 *
 * @since 7.1
 * @see ContentStore#getWriter(ContentContext)
 */
public class UnsupportedStorageClassException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1349903839801739376L;

    private final ContentStore contentStore;
    private final Set<String> storageClasses;

    /**
     * @param contentStore the originating content store
     * @param storageClasses the offending storage classes
     */
    public UnsupportedStorageClassException(ContentStore contentStore, Set<String> storageClasses)
    {
        this(contentStore, storageClasses,
                "The storage class is not supported by the content store: \n" +
                "   Store:       " + contentStore.getClass().getName() + "\n" +
                "   Storage class: " + storageClasses);
    }

    /**
     * @param contentStore the originating content store
     * @param storageClasses the offending storage classes
     */
    public UnsupportedStorageClassException(ContentStore contentStore, Set<String> storageClasses, String msg)
    {
        super(msg);
        this.contentStore = contentStore;
        this.storageClasses = storageClasses;
    }

    public ContentStore getContentStore()
    {
        return contentStore;
    }

    public Set<String> getStorageClass()
    {
        return storageClasses;
    }
}
