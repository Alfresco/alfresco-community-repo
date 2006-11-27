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
public interface WCMAppModel
{
    // AVM web folder
    static final QName TYPE_AVMWEBFOLDER = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "webfolder");
    static final QName PROP_AVMSTORE = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "avmstore");
    static final QName PROP_DEFAULTWEBAPP = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "defaultwebapp");
    static final QName ASSOC_WEBUSER = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "webuser");
    static final QName ASSOC_WEBFORM = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "webform");
    static final QName ASSOC_WEBWORKFLOWDEFAULTS = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "webworkflowdefaults");
    
    // AVM web user reference
    static final QName TYPE_WEBUSER = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "webuser");
    static final QName PROP_WEBUSERNAME = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "username");
    static final QName PROP_WEBUSERROLE = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "role");
    
    // AVM web form
    static final QName TYPE_WEBFORM = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "webform");
    static final QName PROP_FORMNAME = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "formname");
    static final QName ASSOC_WEBFORMTEMPLATE = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "webformtemplate");
    static final QName ASSOC_WORKFLOWDEFAULTS = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "workflowdefaults");
    
    // AVM web form template
    static final QName TYPE_WEBFORMTEMPLATE = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "webformtemplate");
    static final QName PROP_ENGINE = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "engine");
    
    // AVM workflow defaults
    static final QName TYPE_WORKFLOWDEFAULTS = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "workflowdefaults");
    static final QName PROP_WORKFLOW_NAME = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "workflowname");
    static final QName PROP_WORKFLOWDEFAULTS = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "defaults");
    
    // AVM web workflow defaults
    static final QName TYPE_WEBWORKFLOWDEFAULTS = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "webworkflowdefaults");
    
    // AVM filename pattern aspect
    static final QName ASPECT_FILENAMEPATTERN = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "filenamepattern");
    static final QName PROP_FILENAMEPATTERN = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "filenamepattern");
    
    // The XForms data capture form aspect.
    static final QName ASPECT_FORM = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "form");
    static final QName PROP_XML_SCHEMA = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "schema");
    static final QName PROP_XML_SCHEMA_ROOT_ELEMENT_NAME = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "xmlschemarootelementname");
    static final QName PROP_OUTPUT_PATH_PATTERN_FORM_INSTANCE_DATA = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "outputpathpatternforminstancedata");
    static final QName PROP_DEFAULT_WORKFLOW_NAME = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "defaultworkflowname");
    static final QName ASSOC_RENDERING_ENGINE_TEMPLATES = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "renderingenginetemplates");
    
    // An XML to something else tranformer aspect.
    static final QName ASPECT_RENDERING_ENGINE_TEMPLATE = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "renderingenginetemplate");
    static final QName PROP_PARENT_RENDERING_ENGINE_NAME = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "parentrenderingenginename");
    static final QName PROP_FORM_SOURCE = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "formsource");
    static final QName ASSOC_RENDITION_PROPERTIES = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "renditionproperties");
    
    static final QName ASPECT_FORM_INSTANCE_DATA = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "forminstancedata");
    static final QName PROP_PARENT_FORM = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "parentform");
    static final QName PROP_PARENT_FORM_NAME = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "parentformname");
    
    static final QName ASPECT_RENDITION = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "rendition");
    static final QName PROP_PARENT_RENDERING_ENGINE_TEMPLATE = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "parentrenderingenginetemplate");
    static final QName PROP_PARENT_RENDITION_PROPERTIES = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "parentrenditionproperties");
    static final QName PROP_PRIMARY_FORM_INSTANCE_DATA = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "primaryforminstancedata");
    
    static final QName TYPE_RENDITION_PROPERTIES = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "renditionproperties");
    static final QName PROP_OUTPUT_PATH_PATTERN_RENDITION = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "outputpathpatternrendition");
    static final QName PROP_MIMETYPE_FOR_RENDITION = QName.createQName(NamespaceService.WCMAPP_MODEL_1_0_URI, "mimetypeforrendition");
}
