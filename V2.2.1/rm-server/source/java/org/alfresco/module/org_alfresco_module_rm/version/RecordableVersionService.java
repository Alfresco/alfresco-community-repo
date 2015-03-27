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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;

/**
 * Recordable version service interface.
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public interface RecordableVersionService 
{
    /**
     * Indicates whether the current version of a node is recorded or not.
     * <p>
     * Returns false if not versionable or no version.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if latest version recorded, false otherwise
     */
    boolean isCurrentVersionRecorded(NodeRef nodeRef);
    
    /**
     * Indicates whether a version is recorded or not.
     * 
     * @param version   version
     * @return boolean  true if recorded version, false otherwise
     */
    boolean isRecordedVersion(Version version);
    
    /**
     * Creates a record from the latest version, marking it as recorded.
     * <p>
     * Does not create a record if the node is not versionable or the latest
     * version is already recorded.
     * 
     * @param nodeRef   node reference
     * @return NodeRef  node reference to the crated record.
     */
    NodeRef createRecordFromLatestVersion(NodeRef filePlan, NodeRef nodeRef);

}
