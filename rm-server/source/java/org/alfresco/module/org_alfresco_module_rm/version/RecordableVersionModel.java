/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.version;

import org.alfresco.service.namespace.QName;

/**
 * Helper class containing recordable version model qualified names
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public interface RecordableVersionModel
{
    /** Namespace details */
    String RMV_URI = "http://www.alfresco.org/model/recordableversion/1.0";
    String RMV_PREFIX = "rmv";

    /** versionable aspect */
    public QName ASPECT_VERSIONABLE = QName.createQName(RMV_URI, "versionable");
    public QName PROP_RECORDABLE_VERSION_POLICY = QName.createQName(RMV_URI, "recordableVersionPolicy");
    public QName PROP_FILE_PLAN = QName.createQName(RMV_URI, "filePlan");
    
    /** recorded version aspect */
    public QName ASPECT_RECORDED_VERSION = QName.createQName(RMV_URI, "recordedVersion");
    public QName PROP_RECORD_NODE_REF = QName.createQName(RMV_URI, "recordNodeRef");
    public QName PROP_FROZEN_OWNER = QName.createQName(RMV_URI, "frozenOwner");
}
