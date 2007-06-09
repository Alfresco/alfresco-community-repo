/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
