/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
package org.alfresco.model;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * QName definitions for WCM.
 * @author britt
 */
public interface WCMModel 
{
    // content
    static final QName TYPE_AVM_CONTENT = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmcontent");
    static final QName TYPE_AVM_PLAIN_CONTENT = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmplaincontent");
    static final QName TYPE_AVM_LAYERED_CONTENT = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmlayeredcontent");
    static final QName PROP_AVM_FILE_INDIRECTION =  QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmfileindirection");
    
    // folders
    static final QName TYPE_AVM_FOLDER = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmfolder");
    static final QName TYPE_AVM_PLAIN_FOLDER = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmplainfolder");
    static final QName TYPE_AVM_LAYERED_FOLDER = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmlayeredfolder");
    static final QName PROP_AVM_DIR_INDIRECTION = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmdirindirection");
    
    // Reverted Aspect.
    static final QName ASPECT_REVERTED = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "reverted");
    static final QName PROP_REVERTED_ID = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "revertedid");
}
