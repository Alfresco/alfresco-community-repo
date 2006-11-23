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
    public static final QName TYPE_AVM_CONTENT = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmcontent");
    public static final QName TYPE_AVM_PLAIN_CONTENT = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmplaincontent");
    public static final QName TYPE_AVM_LAYERED_CONTENT = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmlayeredcontent");
    public static final QName PROP_AVM_FILE_INDIRECTION =  QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmfileindirection");
    
    // folders
    public static final QName TYPE_AVM_FOLDER = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmfolder");
    public static final QName TYPE_AVM_PLAIN_FOLDER = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmplainfolder");
    public static final QName TYPE_AVM_LAYERED_FOLDER = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmlayeredfolder");
    public static final QName PROP_AVM_DIR_INDIRECTION = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "avmdirindirection");
    
    // The XForms data capture form aspect.
    public static final QName ASPECT_FORM = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "form");
    public static final QName PROP_XML_SCHEMA = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "schema");
    public static final QName PROP_XML_SCHEMA_ROOT_ELEMENT_NAME = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "xmlschemarootelementname");
    public static final QName PROP_OUTPUT_PATH_PATTERN_FOR_FORM_INSTANCE_DATA = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "outputpathpatternforforminstancedata");
    public static final QName PROP_DEFAULT_WORKFLOW_NAME = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "defaultworkflowname");
    public static final QName ASSOC_RENDERING_ENGINE_TEMPLATES = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "renderingenginetemplates");
    
    // An XML to something else tranformer aspect.
    public static final QName ASPECT_RENDERING_ENGINE_TEMPLATE = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "renderingenginetemplate");
    public static final QName PROP_PARENT_RENDERING_ENGINE_NAME = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "parentrenderingenginename");
    public static final QName PROP_FORM_SOURCE = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "formsource");
    public static final QName ASSOC_RENDITION_PROPERTIES = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "renditionproperties");

    public static final QName ASPECT_FORM_INSTANCE_DATA = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "forminstancedata");
    public static final QName PROP_PARENT_FORM = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "parentform");
    public static final QName PROP_PARENT_FORM_NAME = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "parentformname");

    public static final QName ASPECT_RENDITION = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "rendition");
    public static final QName PROP_PARENT_RENDERING_ENGINE_TEMPLATE = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "parentrenderingenginetemplate");
    public static final QName PROP_PARENT_RENDITION_PROPERTIES = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "parentrenditionproperties");
    public static final QName PROP_PRIMARY_FORM_INSTANCE_DATA = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "primaryforminstancedata");

    public static final QName TYPE_RENDITION_PROPERTIES = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "renditionproperties");
    public static final QName PROP_OUTPUT_PATH_PATTERN_FOR_RENDITION = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "outputpathpatternforrendition");
    public static final QName PROP_MIMETYPE_FOR_RENDITION = QName.createQName(NamespaceService.WCM_MODEL_1_0_URI, "mimetypeforrendition");
}
