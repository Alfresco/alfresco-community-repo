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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.model;

import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public interface BlogIntegrationModel
{
    static final String MODEL_URL = "http://www.alfresco.org/model/blogintegration/1.0";
    static final String MODEL_PREFIX = "blg";
    
    static final QName ASPECT_BLOG_DETAILS = QName.createQName(MODEL_URL, "blogDetails");
    static final QName PROP_BLOG_IMPLEMENTATION = QName.createQName(MODEL_URL, "blogImplementation");
    static final QName PROP_ID = QName.createQName(MODEL_URL, "id");
    static final QName PROP_NAME = QName.createQName(MODEL_URL, "name");
    static final QName PROP_DESCRIPTION = QName.createQName(MODEL_URL, "description");
    static final QName PROP_URL = QName.createQName(MODEL_URL, "url");
    static final QName PROP_USER_NAME = QName.createQName(MODEL_URL, "userName");
    static final QName PROP_PASSWORD = QName.createQName(MODEL_URL, "password");
        
    static final QName ASPECT_BLOG_POST = QName.createQName(MODEL_URL, "blogPost");
    static final QName PROP_POST_ID = QName.createQName(MODEL_URL, "postId");
    static final QName PROP_PUBLISHED = QName.createQName(MODEL_URL, "published");
    static final QName PROP_LINK = QName.createQName(MODEL_URL, "link");
    static final QName PROP_POSTED = QName.createQName(MODEL_URL, "posted");
    static final QName PROP_LAST_UPDATE = QName.createQName(MODEL_URL, "lastUpdate");
    static final QName ASSOC_BLOG_DETAILS = QName.createQName(MODEL_URL, "blogDetails");
}
