/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
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
    QName ASPECT_VERSIONABLE = QName.createQName(RMV_URI, "versionable");
    QName PROP_RECORDABLE_VERSION_POLICY = QName.createQName(RMV_URI, "recordableVersionPolicy");
    QName PROP_FILE_PLAN = QName.createQName(RMV_URI, "filePlan");

    /** recorded version aspect */
    QName ASPECT_RECORDED_VERSION = QName.createQName(RMV_URI, "recordedVersion");
    QName PROP_RECORD_NODE_REF = QName.createQName(RMV_URI, "recordNodeRef");
    QName PROP_FROZEN_OWNER = QName.createQName(RMV_URI, "frozenOwner");
    QName PROP_DESTROYED = QName.createQName(RMV_URI, "destroyed");
    
    /** version record aspect */
    QName ASPECT_VERSION_RECORD = QName.createQName(RMV_URI, "versionRecord");
    QName PROP_VERSIONED_NODEREF = QName.createQName(RMV_URI, "versionedNodeRef");
    QName PROP_VERSION_LABEL = QName.createQName(RMV_URI, "versionLabel");
    QName PROP_VERSION_DESCRIPTION = QName.createQName(RMV_URI, "versionDescription");
}
