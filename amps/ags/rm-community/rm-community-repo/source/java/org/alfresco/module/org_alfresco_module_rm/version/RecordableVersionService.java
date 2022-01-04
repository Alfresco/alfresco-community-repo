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

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;

/**
 * Recordable version service interface.
 *
 * @author Roy Wetherall
 * @since 2.3
 */
@AlfrescoPublicApi
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
     * If the version is a recorded version, gets the related version record.
     *
     * @param  version   version
     * @return NodeRef   node reference of version record
     */
    NodeRef getVersionRecord(Version version);

    /**
     * Gets the version that relates to the version record
     *
     * @param record version record node reference
     * @return Version  version or null if not found
     */
    Version getRecordedVersion(NodeRef record);

    /**
     * Creates a record from the latest frozen version, marking it as recorded.
     * <p>
     * Does not create a record if the node is not versionable or the latest version is already recorded.
     *
     * @param nodeRef node reference
     * @return NodeRef node reference to the created record.
     */
    NodeRef createRecordFromLatestVersion(NodeRef filePlan, NodeRef nodeRef);

    /**
     * Creates a record from the latest version, marking it as recorded.
     * <p>
     * Does not create a record if the node is not versionable or the latest version is already recorded.
     *
     * @param filePlan parent node reference
     * @param nodeRef   node reference
     * @param autoVersion true, create new record version from latest version, false creates a record from the latest frozen version
     * @return NodeRef  node reference to the created record.
     */
    NodeRef createRecordFromLatestVersion(NodeRef filePlan, NodeRef nodeRef, boolean autoVersion);

    /**
     * Indicates whether a record version is destroyed or not.
     *
     * @param version   version
     * @return boolean  true if destroyed, false otherwise
     */
    boolean isRecordedVersionDestroyed(Version version);

    /**
     * Marks a recorded version as destroyed.
     * <p>
     * Note this method does not destroy the associated record, instead it marks the version as destroyed.
     *
     * @param version   version
     */
    void destroyRecordedVersion(Version version);

    /**
     * Flag that indicate to create new version on record creation if current node is modified
     *
     * @return boolean true to auto-version on record creation, false to use latest versioned version
     */
    boolean isEnableAutoVersionOnRecordCreation();

    /**
     * Create a snapshot version of current node
     *
     * @param nodeRef node reference
     */
    void createSnapshotVersion(NodeRef nodeRef);
}
