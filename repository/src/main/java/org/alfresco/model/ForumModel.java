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

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;


/**
 * Forums Model Constants
 * 
 * @author gavinc
 */
@AlfrescoPublicApi
public interface ForumModel
{
    //
    // Forums Model Definitions
    //
   
    static final QName TYPE_FORUMS = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "forums");
    static final QName TYPE_FORUM = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "forum");
    static final QName TYPE_TOPIC = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "topic");
    static final QName TYPE_POST = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "post");

    static final QName ASPECT_DISCUSSABLE = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussable");
    
    static final QName ASSOC_DISCUSSION = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussion");
    
    static final QName ASPECT_COMMENTS_ROLLUP = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "commentsRollup");
    static final QName PROP_COMMENT_COUNT = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "commentCount");
}
