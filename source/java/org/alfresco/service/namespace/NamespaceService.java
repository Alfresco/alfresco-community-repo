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
package org.alfresco.service.namespace;

import org.alfresco.service.Auditable;



/**
 * Namespace Service.
 * 
 * The Namespace Service provides access to and definition of namespace
 * URIs and Prefixes. 
 * 
 * @author David Caruana
 */
public interface NamespaceService extends NamespacePrefixResolver
{
    /** Default Namespace URI */
    public static final String DEFAULT_URI = "";
    
    /** Default Namespace Prefix */
    public static final String DEFAULT_PREFIX = "";

    /** Default Alfresco URI */
    public static final String ALFRESCO_URI = "http://www.alfresco.org";
    
    /** Default Alfresco Prefix */
    public static final String ALFRESCO_PREFIX = "alf";
    
    /** Dictionary Model URI */
    public static final String DICTIONARY_MODEL_1_0_URI = "http://www.alfresco.org/model/dictionary/1.0";
    
    /** Dictionary Model Prefix */
    public static final String DICTIONARY_MODEL_PREFIX = "d";

    /** System Model URI */
    public static final String SYSTEM_MODEL_1_0_URI = "http://www.alfresco.org/model/system/1.0";

    /** System Model Prefix */
    public static final String SYSTEM_MODEL_PREFIX = "sys";

    /** Content Model URI */
    public static final String CONTENT_MODEL_1_0_URI = "http://www.alfresco.org/model/content/1.0";

    /** Content Model Prefix */
    public static final String CONTENT_MODEL_PREFIX = "cm";

    /** Application Model URI */
    public static final String APP_MODEL_1_0_URI = "http://www.alfresco.org/model/application/1.0";

    /** Application Model Prefix */
    public static final String APP_MODEL_PREFIX = "app";

    /** Business Process Model URI */
    public static final String BPM_MODEL_1_0_URI = "http://www.alfresco.org/model/bpm/1.0";

    /** Business Process Model Prefix */
    public static final String BPM_MODEL_PREFIX = "bpm";

    /** Workflow Model URI */
    public static final String WORKFLOW_MODEL_1_0_URI = "http://www.alfresco.org/model/workflow/1.0";

    /** Workflow Model Prefix */
    public static final String WORKFLOW_MODEL_PREFIX = "wf";

    /** Alfresco View Namespace URI */
    public static final String REPOSITORY_VIEW_1_0_URI = "http://www.alfresco.org/view/repository/1.0";
    
    /** Alfresco View Namespace Prefix */
    public static final String REPOSITORY_VIEW_PREFIX = "view";
    
    /** Alfresco security URI */
    public static final String SECURITY_MODEL_1_0_URI = "http://www.alfresco.org/model/security/1.0";
    
    /** Alfresco security Prefix */
    public static final String SECURITY_MODEL_PREFIX = "security";
    
    
    /**
     * Register a prefix for namespace uri. 
     * 
     * @param prefix
     * @param uri
     */
    @Auditable(parameters = {"prefix", "uri"})
    public void registerNamespace(String prefix, String uri);
    

    /**
     * Unregister a prefix.
     * 
     * @param prefix
     */
    @Auditable(parameters = {"prefix"})
    public void unregisterNamespace(String prefix);
    
}
