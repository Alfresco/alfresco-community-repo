/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.version;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.version.VersionService;

/**
 * Base Version Model interface containing the common local names (and other constants) 
 * used by the lightWeightVersionStore and version2Store implementations
 */
public interface VersionBaseModel
{
    /**
    * The store protocol
    */
    public static final String STORE_PROTOCOL = VersionService.VERSION_STORE_PROTOCOL;
    
    public static final String PROP_DESCRIPTION = "description";
    
    public static final String PROP_VERSION_DESCRIPTION = "versionDescription";
    
    public static final String PROP_VERSION_LABEL = "versionLabel";
    public static final String PROP_CREATED_DATE = ContentModel.PROP_CREATED.getLocalName();
    public static final String PROP_CREATOR = ContentModel.PROP_CREATOR.getLocalName();
    public static final String PROP_VERSION_TYPE = "versionType";
    
    /**
     * @deprecated since 3.3
     */
    public static final String PROP_VERSION_NUMBER = "versionNumber";

    /** The version store root aspect localname*/
    public static final String ASPECT_LOCALNAME_VERSION_STORE_ROOT = "versionStoreRoot";

    /**
     * Version history type
     */
    public static final String TYPE_VERSION_HISTORY = "versionHistory";

    /**
     * Version history properties and associations
     */
    public static final String PROP_VERSIONED_NODE_ID = "versionedNodeId";
    public static final String ASSOC_LOCALNAME_ROOT_VERSION = "rootVersion";


    /**
     * Child relationship names
     */
    public static final String CHILD_VERSION_HISTORIES = "versionHistory";
    public static final String CHILD_VERSIONS = "version";

    // Used by ML service
    
    /**
     * Created version associated to the deleted translations of an mlContainer
     */
    public static final String PROP_TRANSLATION_VERSIONS = "translationVersions";
}
