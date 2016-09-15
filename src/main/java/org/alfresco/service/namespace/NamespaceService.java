/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.namespace;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.Auditable;

/**
 * Namespace Service.
 * 
 * The Namespace Service provides access to and definition of namespace
 * URIs and Prefixes. 
 * 
 * @author David Caruana
 */
@AlfrescoPublicApi
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
    
    /** Audio Model URI */
    static final String AUDIO_MODEL_1_0_URI = "http://www.alfresco.org/model/audio/1.0";
    
    /** Audio Model Prefix */
    static final String AUDIO_MODEL_PREFIX = "audio";
    
    /** EXIF Model URI */
    static final String EXIF_MODEL_1_0_URI = "http://www.alfresco.org/model/exif/1.0";

    /** WebDAV Model Prefix */
    static final String WEBDAV_MODEL_PREFIX = "webdav";
    
    /** WebDAV Model URI */
    static final String WEBDAV_MODEL_1_0_URI = "http://www.alfresco.org/model/webdav/1.0";

    /** EXIF Model Prefix */
    static final String EXIF_MODEL_PREFIX = "exif";
    
    /** DataList Model URI */
    static final String DATALIST_MODEL_1_0_URI = "http://www.alfresco.org/model/datalist/1.0";
    
    /** DataList Model Prefix */
    static final String DATALIST_MODEL_PREFIX = "dl";

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
    
    /** Alfresco Links URI */
    static final String LINKS_MODEL_1_0_URI = "http://www.alfresco.org/model/linksmodel/1.0";

    /** Alfresco Links Prefix */
    static final String LINKS_MODEL_PREFIX = "lnk";
    
    /** Rendition Model URI */
    static final String RENDITION_MODEL_1_0_URI = "http://www.alfresco.org/model/rendition/1.0";
    
    /** Rendition Model Prefix */
    static final String RENDITION_MODEL_PREFIX = "rn";

    /** Alfresco View Namespace URI */
    static final String REPOSITORY_VIEW_1_0_URI = "http://www.alfresco.org/view/repository/1.0";
    
    /** Alfresco View Namespace Prefix */
    static final String REPOSITORY_VIEW_PREFIX = "view";
    
    /** Alfresco security URI */
    static final String SECURITY_MODEL_1_0_URI = "http://www.alfresco.org/model/security/1.0";
    
    /** Alfresco security Prefix */
    static final String SECURITY_MODEL_PREFIX = "security";
    
    /** Email Server Application Model URI */
    static final String EMAILSERVER_MODEL_URI = "http://www.alfresco.org/model/emailserver/1.0";

    /** Email Server Application Model Prefix */
    static final String EMAILSERVER_MODEL_PREFIX = "emailserver";
    
    /**
     * Register a prefix for namespace uri. 
     * 
     * @param prefix String
     * @param uri String
     */
    @Auditable(parameters = {"prefix", "uri"})
    public void registerNamespace(String prefix, String uri);
    

    /**
     * Unregister a prefix.
     * 
     * @param prefix String
     */
    @Auditable(parameters = {"prefix"})
    public void unregisterNamespace(String prefix);
    
}
