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
    
    // Default view config aspect
    static final QName ASPECT_DEFAULT_VIEW_CONFIG = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "defaultViewConfig");
    static final QName PROP_DEFAULT_VIEW_ID = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "defaultViewId");

    // Linked aspect
    static final QName ASPECT_LINKED = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "linked");
}
