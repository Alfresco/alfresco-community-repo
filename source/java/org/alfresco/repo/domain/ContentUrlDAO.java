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
 * http://www.alfresco.com/legal/licensing */

package org.alfresco.repo.domain;

import java.util.Set;

/**
 * Abstraction for manipulating <b>Content URL</b> entities.
 * 
 * @author Derek Hulley
 * @since 2.0
 */
public interface ContentUrlDAO
{
    /**
     * Create a new <b>Content URL</b> or get an existing instance.
     */
    ContentUrl createContentUrl(String contentUrl);
    
    /**
     * Enumerate all the available <b>Content URLs</b>, calling back to the given handler.
     * 
     * @param handler       the component that will be called with each URL
     */
    void getAllContentUrls(ContentUrlHandler handler);
    
    /**
     * Delete the <b>Content URL</b>.
     */
    void deleteContentUrl(String contentUrl);
    
    /**
     * Delete a set of <b>Content URL</b>.
     */
    void deleteContentUrls(Set<String> contentUrls);
    
    /**
     * Delete all <b>Content URL</b> entities.
     */
    void deleteAllContentUrls();
    
    /**
     * A callback interface to handle <b>Content URLS<b> produced by iteration.
     * 
     * @author Derek Hulley
     * @since 2.0
     */
    public interface ContentUrlHandler
    {
        void handle(String contentUrl);
    };
}
