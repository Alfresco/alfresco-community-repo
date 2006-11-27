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
}
