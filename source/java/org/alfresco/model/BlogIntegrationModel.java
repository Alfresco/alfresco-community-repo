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
