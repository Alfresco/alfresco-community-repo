/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.model;

import org.alfresco.service.namespace.QName;

/**
 * WebDAV Model Constants
 * 
 * @author Ivan Rybnikov
 */
public interface WebDAVModel
{
    static final String WEBDAV_MODEL_1_0_URI = "http://www.alfresco.org/model/webdav/1.0";

    // Exclusive lock token
    static final QName PROP_OPAQUE_LOCK_TOKEN = QName.createQName(WEBDAV_MODEL_1_0_URI, "opaquelocktoken");

    // Lock depth
    static final QName PROP_LOCK_DEPTH = QName.createQName(WEBDAV_MODEL_1_0_URI, "lockDepth");

    // Lock type
    static final QName PROP_LOCK_SCOPE = QName.createQName(WEBDAV_MODEL_1_0_URI, "lockScope");

    // Node's shared locks
    static final QName PROP_SHARED_LOCK_TOKENS = QName.createQName(WEBDAV_MODEL_1_0_URI, "sharedLockTokens");
    

}
