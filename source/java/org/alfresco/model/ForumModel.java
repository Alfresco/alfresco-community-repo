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
