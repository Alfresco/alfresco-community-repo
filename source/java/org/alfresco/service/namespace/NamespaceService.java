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
package org.alfresco.service.namespace;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;



/**
 * Namespace Service.
 * 
 * The Namespace Service provides access to and definition of namespace
 * URIs and Prefixes. 
 * 
 * @author David Caruana
 */
@PublicService
public interface NamespaceService extends NamespacePrefixResolver
{
    /** Default Namespace URI */
    static final String DEFAULT_URI = "";
    
    /** Default Namespace Prefix */
    static final String DEFAULT_PREFIX = "";

    /** Default Alfresco URI */
    static final String ALFRESCO_URI = "http://www.alfresco.org";
    
    /** Default Alfresco Prefix */
    static final String ALFRESCO_PREFIX = "alf";
    
    /** Dictionary Model URI */
    static final String DICTIONARY_MODEL_1_0_URI = "http://www.alfresco.org/model/dictionary/1.0";
    
    /** Dictionary Model Prefix */
    static final String DICTIONARY_MODEL_PREFIX = "d";

    /** System Model URI */
    static final String SYSTEM_MODEL_1_0_URI = "http://www.alfresco.org/model/system/1.0";

    /** System Model Prefix */
    static final String SYSTEM_MODEL_PREFIX = "sys";

    /** Content Model URI */
    static final String CONTENT_MODEL_1_0_URI = "http://www.alfresco.org/model/content/1.0";

    /** Content Model Prefix */
    static final String CONTENT_MODEL_PREFIX = "cm";

    /** Application Model URI */
    static final String APP_MODEL_1_0_URI = "http://www.alfresco.org/model/application/1.0";

    /** Application Model Prefix */
    static final String APP_MODEL_PREFIX = "app";

    /** Business Process Model URI */
    static final String BPM_MODEL_1_0_URI = "http://www.alfresco.org/model/bpm/1.0";

    /** Business Process Model Prefix */
    static final String BPM_MODEL_PREFIX = "bpm";

    /** Workflow Model URI */
    static final String WORKFLOW_MODEL_1_0_URI = "http://www.alfresco.org/model/workflow/1.0";

    /** Workflow Model Prefix */
    static final String WORKFLOW_MODEL_PREFIX = "wf";
    
    /** Alfresco Forums URI */
    static final String FORUMS_MODEL_1_0_URI = "http://www.alfresco.org/model/forum/1.0";

    /** Alfresco Forums Prefix */
    static final String FORUMS_MODEL_PREFIX = "fm";
    
    /** Alfresco View Namespace URI */
    static final String REPOSITORY_VIEW_1_0_URI = "http://www.alfresco.org/view/repository/1.0";
    
    /** Alfresco View Namespace Prefix */
    static final String REPOSITORY_VIEW_PREFIX = "view";
    
    /** Alfresco security URI */
    static final String SECURITY_MODEL_1_0_URI = "http://www.alfresco.org/model/security/1.0";
    
    /** Alfresco security Prefix */
    static final String SECURITY_MODEL_PREFIX = "security";
    
    /** Alfresco WCM URI */
    static final String WCM_MODEL_1_0_URI = "http://www.alfresco.org/model/wcmmodel/1.0";
    
    /** Alfresco WCM Prefix */
    static final String WCM_MODEL_PREFIX = "wcm";
    
    /** WCM Application Model URI */
    static final String WCMAPP_MODEL_1_0_URI = "http://www.alfresco.org/model/wcmappmodel/1.0";
    
    /** WCM Application Model Prefix */
    static final String WCMAPP_MODEL_PREFIX = "wca";
    
    
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
