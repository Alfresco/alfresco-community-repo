/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.transfer.manifest;

import org.alfresco.repo.transfer.TransferModel;

/**
 * The transfer model - extended for XML Manifest Model
 */
public interface ManifestModel extends TransferModel
{
    static final String LOCALNAME_TRANSFER_MAINIFEST = "transferManifest";
    static final String LOCALNAME_TRANSFER_HEADER = "transferManifestHeader";
    static final String LOCALNAME_HEADER_CREATED_DATE = "createdDate";
    static final String LOCALNAME_HEADER_NODE_COUNT = "nodeCount";
    static final String LOCALNAME_HEADER_SYNC = "sync";
    static final String LOCALNAME_HEADER_RONLY = "readOnly";
    static final String LOCALNAME_HEADER_VERSION = "version";
    static final String LOCALNAME_HEADER_REPOSITORY_ID = "repositoryId";
    static final String LOCALNAME_ELEMENT_NODES = "nodes";
    static final String LOCALNAME_ELEMENT_NODE = "node";
    static final String LOCALNAME_ELEMENT_DELETED_NODE = "deletedNode";
    static final String LOCALNAME_ELEMENT_ASPECTS = "aspects";
    static final String LOCALNAME_ELEMENT_ASPECT = "aspect";
    static final String LOCALNAME_ELEMENT_PROPERTIES = "properties";
    static final String LOCALNAME_ELEMENT_PROPERTY = "property";
    static final String LOCALNAME_ELEMENT_PARENT_ASSOCS = "parentAssocs"; 
    static final String LOCALNAME_ELEMENT_CHILD_ASSOCS = "childAssocs";
    static final String LOCALNAME_ELEMENT_CHILD_ASSOC = "childAssoc";
    static final String LOCALNAME_ELEMENT_PARENT_ASSOC = "parentAssoc";
    static final String LOCALNAME_ELEMENT_TARGET_ASSOCS = "targetAssocs";
    static final String LOCALNAME_ELEMENT_SOURCE_ASSOCS = "sourceAssocs";
    static final String LOCALNAME_ELEMENT_ASSOC = "assoc";
    static final String LOCALNAME_ELEMENT_PRIMARY_PARENT = "primaryParent";
    static final String LOCALNAME_ELEMENT_PRIMARY_PATH = "primaryPath";
    static final String LOCALNAME_ELEMENT_VALUES = "values";
    static final String LOCALNAME_ELEMENT_VALUE_STRING = "value";
    static final String LOCALNAME_ELEMENT_VALUE_NULL = "nullValue";
    static final String LOCALNAME_ELEMENT_VALUE_SERIALIZED = "serializedValue";
    static final String LOCALNAME_ELEMENT_MLVALUE = "mlvalue";
    static final String LOCALNAME_ELEMENT_CONTENT_HEADER = "content";
    static final String LOCALNAME_ELEMENT_ACL = "acl";
    static final String LOCALNAME_ELEMENT_ACL_PERMISSION = "permission";
    
    // Manifest file prefix
    static final String MANIFEST_PREFIX = "xfer";
}
