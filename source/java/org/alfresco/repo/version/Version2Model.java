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

import org.alfresco.service.namespace.QName;

/**
 * Version2 Model Constants used by version2Store implementation
 */
public interface Version2Model extends VersionBaseModel
{
    /**
     * Namespace
     */
    public static final String NAMESPACE_URI = "http://www.alfresco.org/model/versionstore/2.0";

    /**
     * The store id
     */
    public static final String STORE_ID = "version2Store";
    
    /** The version store root aspect */
    public static final QName ASPECT_VERSION_STORE_ROOT = QName.createQName(NAMESPACE_URI, ASPECT_LOCALNAME_VERSION_STORE_ROOT);

    /**
     * Version history type
     */
    public static final QName TYPE_QNAME_VERSION_HISTORY = QName.createQName(NAMESPACE_URI, TYPE_VERSION_HISTORY);

    /**
     * Version history properties and associations
     */
    public static final QName PROP_QNAME_VERSIONED_NODE_ID = QName.createQName(NAMESPACE_URI, PROP_VERSIONED_NODE_ID);
    public static final QName ASSOC_ROOT_VERSION = QName.createQName(NAMESPACE_URI, ASSOC_LOCALNAME_ROOT_VERSION);

    /**
     * Version aspect + aspect properties
     */
    public static final String ASPECT_LOCALNAME_VERSION = "version";
    public static final QName ASPECT_VERSION = QName.createQName(NAMESPACE_URI, ASPECT_LOCALNAME_VERSION);
    
    public static final String PROP_VERSION_DESCRIPTION = "versionDescription"; // maps from description
    
    public static final QName PROP_QNAME_VERSION_LABEL = QName.createQName(NAMESPACE_URI, PROP_VERSION_LABEL);
    
    /**
     * @deprecated
     */
    public static final QName PROP_QNAME_VERSION_NUMBER = QName.createQName(NAMESPACE_URI, PROP_VERSION_NUMBER);
    
    public static final QName PROP_QNAME_VERSION_DESCRIPTION = QName.createQName(NAMESPACE_URI, PROP_VERSION_DESCRIPTION);

    //  frozen sys:referenceable properties (x4)
    
    public static final String PROP_FROZEN_NODE_REF = "frozenNodeRef";
    public static final QName PROP_QNAME_FROZEN_NODE_REF = QName.createQName(NAMESPACE_URI, PROP_FROZEN_NODE_REF);
    
    public static final String PROP_FROZEN_NODE_DBID = "frozenNodeDbId";
    public static final QName PROP_QNAME_FROZEN_NODE_DBID = QName.createQName(NAMESPACE_URI, PROP_FROZEN_NODE_DBID);
    
    // frozen cm:auditable properties (x5)

    public static final String PROP_FROZEN_CREATOR = "frozenCreator";
    public static final QName PROP_QNAME_FROZEN_CREATOR = QName.createQName(NAMESPACE_URI, PROP_FROZEN_CREATOR);
    
    public static final String PROP_FROZEN_CREATED = "frozenCreated";
    public static final QName PROP_QNAME_FROZEN_CREATED = QName.createQName(NAMESPACE_URI, PROP_FROZEN_CREATED);
    
    public static final String PROP_FROZEN_MODIFIER = "frozenModifer";
    public static final QName PROP_QNAME_FROZEN_MODIFIER = QName.createQName(NAMESPACE_URI, PROP_FROZEN_MODIFIER);
    
    public static final String PROP_FROZEN_MODIFIED = "frozenModified";
    public static final QName PROP_QNAME_FROZEN_MODIFIED = QName.createQName(NAMESPACE_URI, PROP_FROZEN_MODIFIED);
    
    public static final String PROP_FROZEN_ACCESSED = "frozenAccessed";
    public static final QName PROP_QNAME_FROZEN_ACCESSED = QName.createQName(NAMESPACE_URI, PROP_FROZEN_ACCESSED);
    

    
    public static final QName ASSOC_SUCCESSOR = QName.createQName(NAMESPACE_URI, "successor");
 
    public static final String PROP_METADATA_PREFIX = "metadata-";
    
    public static final String PROP_VERSION_TYPE = "versionType";
    
    /**
     * Child relationship names
     */
    public static final QName CHILD_QNAME_VERSION_HISTORIES = QName.createQName(NAMESPACE_URI, CHILD_VERSION_HISTORIES);
    public static final QName CHILD_QNAME_VERSIONS = QName.createQName(NAMESPACE_URI, CHILD_VERSIONS);
    
    
    // Used by ML service
    
    /**
     * Created version associated to the deleted translations of an mlContainer
     */
    
    public static final QName PROP_QNAME_TRANSLATION_VERSIONS = QName.createQName(VersionModel.NAMESPACE_URI, PROP_TRANSLATION_VERSIONS);
}

