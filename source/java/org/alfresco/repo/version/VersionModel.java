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
package org.alfresco.repo.version;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;

/**
 * interface conating the constants used by the light weight 
 * version store implementation
 * 
 * @author Roy Wetherall
 */
public interface VersionModel
{
    /**
     * Namespace
     */
    public static final String NAMESPACE_URI = "http://www.alfresco.org/model/versionstore/1.0";
    
	 /**
     * The store protocol
     */
    public static final String STORE_PROTOCOL = VersionService.VERSION_STORE_PROTOCOL;
    
    /**
     * The store id
     */
    public static final String STORE_ID = "lightWeightVersionStore";
   
	
    public static final String PROP_VERSION_LABEL = "versionLabel";
    public static final String PROP_CREATED_DATE = ContentModel.PROP_CREATED.getLocalName(); 
    public static final String PROP_CREATOR = ContentModel.PROP_CREATOR.getLocalName();
    public static final String PROP_VERSION_TYPE = "versionType";
    public static final String PROP_VERSION_NUMBER = "versionNumber";
    public static final String PROP_FROZEN_NODE_ID = "frozenNodeId";
    public static final String PROP_FROZEN_NODE_TYPE = "frozenNodeType";
    public static final String PROP_FROZEN_NODE_STORE_PROTOCOL = "frozenNodeStoreProtocol";
    public static final String PROP_FROZEN_NODE_STORE_ID = "frozenNodeStoreId";
    public static final String PROP_FROZEN_ASPECTS = "frozenAspects";
    
    /**
     * Version history type
     */
    public static final String TYPE_VERSION_HISTORY = "versionHistory";
    public static final QName TYPE_QNAME_VERSION_HISTORY = QName.createQName(NAMESPACE_URI, TYPE_VERSION_HISTORY);
    
    /**
     * Version history properties and associations
     */
    public static final String PROP_VERSIONED_NODE_ID = "versionedNodeId";
    public static final QName PROP_QNAME_VERSIONED_NODE_ID = QName.createQName(NAMESPACE_URI, PROP_VERSIONED_NODE_ID);        
    public static final QName ASSOC_ROOT_VERSION = QName.createQName(NAMESPACE_URI, "rootVersion");
    
    /**
     * Verison type
     */
    public static final String TYPE_VERSION = "version";
    public static final QName TYPE_QNAME_VERSION = QName.createQName(NAMESPACE_URI, TYPE_VERSION);
    
    /**
     * Version type properties and associations
     */
    public static final QName PROP_QNAME_VERSION_LABEL = QName.createQName(NAMESPACE_URI, PROP_VERSION_LABEL);
    public static final QName PROP_QNAME_VERSION_NUMBER = QName.createQName(NAMESPACE_URI, PROP_VERSION_NUMBER);
    public static final QName PROP_QNAME_FROZEN_NODE_ID = QName.createQName(NAMESPACE_URI, PROP_FROZEN_NODE_ID);
    public static final QName PROP_QNAME_FROZEN_NODE_TYPE = QName.createQName(NAMESPACE_URI, PROP_FROZEN_NODE_TYPE);
    public static final QName PROP_QNAME_FROZEN_NODE_STORE_PROTOCOL = QName.createQName(NAMESPACE_URI, PROP_FROZEN_NODE_STORE_PROTOCOL);
    public static final QName PROP_QNAME_FROZEN_NODE_STORE_ID = QName.createQName(NAMESPACE_URI, PROP_FROZEN_NODE_STORE_ID);
    public static final QName PROP_QNAME_FROZEN_ASPECTS = QName.createQName(NAMESPACE_URI, PROP_FROZEN_ASPECTS);
    public static final QName ASSOC_SUCCESSOR = QName.createQName(NAMESPACE_URI, "successor");    
    
    /**
     * Version Meta Data Value type
     */
    public static final String TYPE_VERSION_META_DATA_VALUE = "versionMetaDataValue";
    public static final QName TYPE_QNAME_VERSION_META_DATA_VALUE = QName.createQName(NAMESPACE_URI, TYPE_VERSION_META_DATA_VALUE);
    
    /**
     * Version Meta Data Value attributes
     */
    public static final String PROP_META_DATA_NAME = "metaDataName";
    public static final QName PROP_QNAME_META_DATA_NAME = QName.createQName(NAMESPACE_URI, PROP_META_DATA_NAME);
    public static final String PROP_META_DATA_VALUE = "metaDataValue";
    public static final QName PROP_QNAME_META_DATA_VALUE = QName.createQName(NAMESPACE_URI, PROP_META_DATA_VALUE);
    
    /**
     * Versioned attribute type
     */
    public static final String TYPE_VERSIONED_PROPERTY = "versionedProperty";
    public static final QName TYPE_QNAME_VERSIONED_PROPERTY = QName.createQName(NAMESPACE_URI, TYPE_VERSIONED_PROPERTY);
    
    /**
     * Versioned attribute properties
     */
    public static final String PROP_QNAME = "qname";
    public static final String PROP_VALUE = "value";
    public static final String PROP_MULTI_VALUE = "multiValue";
    public static final String PROP_IS_MULTI_VALUE = "isMultiValue";
    public static final QName PROP_QNAME_QNAME = QName.createQName(NAMESPACE_URI, PROP_QNAME);
    public static final QName PROP_QNAME_VALUE = QName.createQName(NAMESPACE_URI, PROP_VALUE);
    public static final QName PROP_QNAME_MULTI_VALUE = QName.createQName(NAMESPACE_URI, PROP_MULTI_VALUE);
    public static final QName PROP_QNAME_IS_MULTI_VALUE = QName.createQName(NAMESPACE_URI, PROP_IS_MULTI_VALUE);
    
    /**
     * Versioned child assoc type
     */
    public static final String TYPE_VERSIONED_CHILD_ASSOC = "versionedChildAssoc";
    public static final QName TYPE_QNAME_VERSIONED_CHILD_ASSOC = QName.createQName(NAMESPACE_URI, TYPE_VERSIONED_CHILD_ASSOC);
    
    /**
     * Versioned child assoc properties
     */
    public static final String PROP_ASSOC_QNAME = "assocQName";
    public static final String PROP_ASSOC_TYPE_QNAME = "assocTypeQName";
    public static final String PROP_IS_PRIMARY = "isPrimary";
    public static final String PROP_NTH_SIBLING = "nthSibling";
    public static final QName PROP_QNAME_ASSOC_QNAME = QName.createQName(NAMESPACE_URI, PROP_ASSOC_QNAME);
    public static final QName PROP_QNAME_ASSOC_TYPE_QNAME = QName.createQName(NAMESPACE_URI, PROP_ASSOC_TYPE_QNAME);
    public static final QName PROP_QNAME_IS_PRIMARY = QName.createQName(NAMESPACE_URI, PROP_IS_PRIMARY);
    public static final QName PROP_QNAME_NTH_SIBLING = QName.createQName(NAMESPACE_URI, PROP_NTH_SIBLING);
    
    /**
     * Versioned assoc type
     */
    public static final String TYPE_VERSIONED_ASSOC = "versionedAssoc";
    public static final QName TYPE_QNAME_VERSIONED_ASSOC = QName.createQName(NAMESPACE_URI, TYPE_VERSIONED_ASSOC);
    
    /**
     * Child relationship names
     */
    public static final String CHILD_VERSION_HISTORIES = "versionHistory";
    public static final String CHILD_VERSIONS = "version";
    public static final String CHILD_VERSIONED_ATTRIBUTES = "versionedAttributes";
    public static final String CHILD_VERSIONED_CHILD_ASSOCS = "versionedChildAssocs";
    public static final String CHILD_VERSIONED_ASSOCS = "versionedAssocs";
    public static final String CHILD_VERSION_META_DATA = "versionMetaData";
    
    public static final QName CHILD_QNAME_VERSION_HISTORIES = QName.createQName(NAMESPACE_URI, CHILD_VERSION_HISTORIES);
    public static final QName CHILD_QNAME_VERSIONS = QName.createQName(NAMESPACE_URI, CHILD_VERSIONS);
    public static final QName CHILD_QNAME_VERSIONED_ATTRIBUTES = QName.createQName(NAMESPACE_URI, CHILD_VERSIONED_ATTRIBUTES);
    public static final QName CHILD_QNAME_VERSIONED_CHILD_ASSOCS = QName.createQName(NAMESPACE_URI, CHILD_VERSIONED_CHILD_ASSOCS);
    public static final QName CHILD_QNAME_VERSIONED_ASSOCS = QName.createQName(NAMESPACE_URI, CHILD_VERSIONED_ASSOCS);
    public static final QName CHILD_QNAME_VERSION_META_DATA = QName.createQName(NAMESPACE_URI, CHILD_VERSION_META_DATA);
}
