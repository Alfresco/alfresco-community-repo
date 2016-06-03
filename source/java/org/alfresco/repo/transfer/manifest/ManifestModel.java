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
    static final String LOCALNAME_ELEMENT_CATEGORIES = "categories";
    static final String LOCALNAME_ELEMENT_CATEGORY = "category";
    
    
    // Manifest file prefix
    static final String MANIFEST_PREFIX = "xfer";
}
