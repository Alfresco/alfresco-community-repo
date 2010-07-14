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
