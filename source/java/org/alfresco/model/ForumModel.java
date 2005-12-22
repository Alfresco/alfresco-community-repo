/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.model;

import org.alfresco.service.namespace.QName;


/**
 * Forums Model Constants
 */
public interface ForumModel
{
    //
    // Forums Model Definitions
    //
   
    static final String FORUMS_MODEL_URI = "http://www.alfresco.org/model/forum/1.0";
    static final String FORUMS_MODEL_PREFIX = "fm";
    
    static final QName TYPE_FORUMS = QName.createQName(FORUMS_MODEL_URI, "forums");
    static final QName TYPE_FORUM = QName.createQName(FORUMS_MODEL_URI, "forum");
    static final QName TYPE_TOPIC = QName.createQName(FORUMS_MODEL_URI, "topic");
    static final QName TYPE_POST = QName.createQName(FORUMS_MODEL_URI, "post");

    static final QName ASPECT_DISCUSSABLE = QName.createQName(FORUMS_MODEL_URI, "discussable");
    
    static final QName ASSOC_DISCUSSION = QName.createQName(FORUMS_MODEL_URI, "discussion");
}
