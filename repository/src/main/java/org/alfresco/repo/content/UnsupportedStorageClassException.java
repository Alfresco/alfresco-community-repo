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

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception produced when a storage class is not supported by a particular {@link ContentStore} implementation.
 *
 * @since 7.1
 * @see ContentStore#getWriter(ContentContext)
 */
public class UnsupportedStorageClassException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1349903839801739376L;

    private final ContentStore contentStore;
    private final String storageClass;

    /**
     * @param contentStore the originating content store
     * @param storageClass the offending storage class
     */
    public UnsupportedStorageClassException(ContentStore contentStore, String storageClass)
    {
        this(contentStore, storageClass,
                "The storage class is not supported by the content store: \n" +
                "   Store:       " + contentStore.getClass().getName() + "\n" +
                "   Storage class: " + storageClass);
    }

    /**
     * @param contentStore the originating content store
     * @param storageClass the offending storage class
     */
    public UnsupportedStorageClassException(ContentStore contentStore, String storageClass, String msg)
    {
        super(msg);
        this.contentStore = contentStore;
        this.storageClass = storageClass;
    }

    public ContentStore getContentStore()
    {
        return contentStore;
    }

    public String getStorageClass()
    {
        return storageClass;
    }
}
