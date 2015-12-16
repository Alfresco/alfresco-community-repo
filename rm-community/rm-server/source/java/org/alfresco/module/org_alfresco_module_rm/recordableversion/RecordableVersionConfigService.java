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
package org.alfresco.module.org_alfresco_module_rm.recordableversion;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.script.slingshot.Version;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Recordable version config service interface
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public interface RecordableVersionConfigService
{
    /**
     * Gets the recordable versions
     *
     * @param nodeRef The node reference for which the recordable versions should be retrieved
     * @return The list of recordable versions
     */
    List<Version> getVersions(NodeRef nodeRef);

    /**
     * Sets the recordable version for the given node
     *
     * @param nodeRef The node reference for which the recorable version should be set
     * @param version The version to be set
     */
    void setVersion(NodeRef nodeRef, String version);
}
