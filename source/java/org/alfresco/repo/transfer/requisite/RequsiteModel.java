package org.alfresco.repo.transfer.requisite;

import org.alfresco.repo.transfer.TransferModel;

/**
 * The transfer model - extended for XML Manifest Model
 */
public interface RequsiteModel extends TransferModel
{
    static final String REQUSITE_MODEL_1_0_URI = "http://www.alfresco.org/model/requsite/1.0";
    
    static final String LOCALNAME_TRANSFER_REQUSITE = "transferRequsite";
    
    static final String LOCALNAME_ELEMENT_NODES = "nodes";
    static final String LOCALNAME_ELEMENT_NODE = "node";
    static final String LOCALNAME_ELEMENT_CONTENT = "requiredContent";
    static final String LOCALNAME_ELEMENT_GROUPS = "groups";
    static final String LOCALNAME_ELEMENT_GROUP = "group";
    static final String LOCALNAME_ELEMENT_USERS = "users";
    static final String LOCALNAME_ELEMENT_USER = "user";

    
    // Manifest file prefix
    static final String REQUSITE_PREFIX = "xferr";
}
