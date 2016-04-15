/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.version;

import org.alfresco.service.namespace.QName;

/**
 * Version1 Model Constants used by lightWeightVersionStore implementation
 *
 * @author Roy Wetherall, janv
 * 
 * NOTE: deprecated since 3.1 (migrate and useVersion2 Model)
 */
public interface VersionModel extends VersionBaseModel
{
    /**
     * Namespace
     */
    public static final String NAMESPACE_URI = "http://www.alfresco.org/model/versionstore/1.0";

    /**
     * The store id
     */
    public static final String STORE_ID = "lightWeightVersionStore";

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
     * Verison type
     */
    public static final String TYPE_VERSION = "version";
    public static final QName TYPE_QNAME_VERSION = QName.createQName(NAMESPACE_URI, TYPE_VERSION);

    /**
     * Version type properties and associations
     */
    
    /**
     * @deprecated since 3.1
     */
    public static final String PROP_FROZEN_NODE_ID = "frozenNodeId";
    /**
     * @deprecated since 3.1
     */
    public static final String PROP_FROZEN_NODE_STORE_PROTOCOL = "frozenNodeStoreProtocol";
    /**
     * @deprecated since 3.1
     */
    public static final String PROP_FROZEN_NODE_STORE_ID = "frozenNodeStoreId";
    /**
     * @deprecated since 3.1
     */
    public static final String PROP_FROZEN_NODE_TYPE = "frozenNodeType";
    /**
     * @deprecated since 3.1
     */
    public static final String PROP_FROZEN_ASPECTS = "frozenAspects";
    
    public static final QName PROP_QNAME_VERSION_LABEL = QName.createQName(NAMESPACE_URI, PROP_VERSION_LABEL);
    
    /**
     * @deprecated since 3.3
     */
    public static final QName PROP_QNAME_VERSION_NUMBER = QName.createQName(NAMESPACE_URI, PROP_VERSION_NUMBER);
    /**
     * @deprecated since 3.1
     */
    public static final QName PROP_QNAME_FROZEN_NODE_ID = QName.createQName(NAMESPACE_URI, PROP_FROZEN_NODE_ID);
    /**
     * @deprecated since 3.1
     */
    public static final QName PROP_QNAME_FROZEN_NODE_TYPE = QName.createQName(NAMESPACE_URI, PROP_FROZEN_NODE_TYPE);
    /**
     * @deprecated since 3.1
     */
    public static final QName PROP_QNAME_FROZEN_NODE_STORE_PROTOCOL = QName.createQName(NAMESPACE_URI, PROP_FROZEN_NODE_STORE_PROTOCOL);
    /**
     * @deprecated since 3.1
     */
    public static final QName PROP_QNAME_FROZEN_NODE_STORE_ID = QName.createQName(NAMESPACE_URI, PROP_FROZEN_NODE_STORE_ID);
    /**
     * @deprecated since 3.1
     */
    public static final QName PROP_QNAME_FROZEN_ASPECTS = QName.createQName(NAMESPACE_URI, PROP_FROZEN_ASPECTS);
    /**
     * @deprecated since 3.1
     */
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
    public static final QName TYPE_QNAME_VERSIONED_ASSOC = QName.createQName(NAMESPACE_URI, TYPE_VERSIONED_ASSOC);

    /**
     * Child relationship names
     */
    public static final String CHILD_VERSIONED_ATTRIBUTES = "versionedAttributes";
    public static final String CHILD_VERSIONED_CHILD_ASSOCS = "versionedChildAssocs";
    public static final String CHILD_VERSION_META_DATA = "versionMetaData";

    public static final QName CHILD_QNAME_VERSION_HISTORIES = QName.createQName(NAMESPACE_URI, CHILD_VERSION_HISTORIES);
    public static final QName CHILD_QNAME_VERSIONS = QName.createQName(NAMESPACE_URI, CHILD_VERSIONS);
    public static final QName CHILD_QNAME_VERSIONED_ATTRIBUTES = QName.createQName(NAMESPACE_URI, CHILD_VERSIONED_ATTRIBUTES);
    public static final QName CHILD_QNAME_VERSIONED_CHILD_ASSOCS = QName.createQName(NAMESPACE_URI, CHILD_VERSIONED_CHILD_ASSOCS);
    public static final QName CHILD_QNAME_VERSIONED_ASSOCS = QName.createQName(NAMESPACE_URI, CHILD_VERSIONED_ASSOCS);
    public static final QName CHILD_QNAME_VERSION_META_DATA = QName.createQName(NAMESPACE_URI, CHILD_VERSION_META_DATA);

    /**
     * Created version associated to the deleted translations of an mlContainer
     */
    public static final QName  PROP_QNAME_TRANSLATION_VERSIONS = QName.createQName(VersionModel.NAMESPACE_URI, PROP_TRANSLATION_VERSIONS);    
}
