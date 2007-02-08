/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.model;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;


/**
 * Forums Model Constants
 * 
 * @author gavinc
 */
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
}
