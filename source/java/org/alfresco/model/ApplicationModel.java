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
 * @author Kevin Roast
 */
public interface ApplicationModel
{
    // workflow
    static final QName ASPECT_SIMPLE_WORKFLOW = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "simpleworkflow");
    static final QName PROP_APPROVE_STEP = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "approveStep");
    static final QName PROP_APPROVE_FOLDER = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "approveFolder");
    static final QName PROP_APPROVE_MOVE = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "approveMove");
    static final QName PROP_REJECT_STEP = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "rejectStep");
    static final QName PROP_REJECT_FOLDER = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "rejectFolder");
    static final QName PROP_REJECT_MOVE = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "rejectMove");
    
    // ui facets aspect
    static final QName ASPECT_UIFACETS = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "uifacets");
    static final QName PROP_ICON = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "icon");
    
    // inlineeditable aspect
    static final QName ASPECT_INLINEEDITABLE = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "inlineeditable");
    static final QName PROP_EDITINLINE = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "editInline");
    
    // configurable aspect
    static final QName ASPECT_CONFIGURABLE = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "configurable");
    static final QName TYPE_CONFIGURATIONS = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "configurations");
    static final QName ASSOC_CONFIGURATIONS = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "configurations");
    
    // object links
    static final QName TYPE_FILELINK = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "filelink");
    static final QName TYPE_FOLDERLINK = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "folderlink");
    
    // feed source aspect
    static final QName ASPECT_FEEDSOURCE = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "feedsource");
    static final QName PROP_FEEDTEMPLATE = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "template");
}
