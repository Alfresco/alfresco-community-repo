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
 * Exception produced when a request is made to write content to a location
 * already in use, either by content being written or previously written.
 *
 * @see ContentStore#getWriter(ContentContext)
 * @since 2.1
 * @author Derek Hulley
 */
public class ContentExistsException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 5154068664249490612L;

    private ContentStore contentStore;
    private String contentUrl;
    
    /**
     * @param contentStore      the originating content store
     * @param contentUrl        the offending content URL
     */
    public ContentExistsException(ContentStore contentStore, String contentUrl)
    {
        this(contentStore, contentUrl,
                "Content with the given URL already exists in the store: \n" +
                "   Store:       " + contentStore.getClass().getName() + "\n" +
                "   Content URL: " + contentUrl);
    }

    /**
     * @param contentStore      the originating content store
     * @param contentUrl        the offending content URL
     */
    public ContentExistsException(ContentStore contentStore, String contentUrl, String msg)
    {
        super(msg);
        this.contentStore = contentStore;
        this.contentUrl = contentUrl;
    }

    public ContentStore getContentStore()
    {
        return contentStore;
    }

    public String getContentUrl()
    {
        return contentUrl;
    }
}
