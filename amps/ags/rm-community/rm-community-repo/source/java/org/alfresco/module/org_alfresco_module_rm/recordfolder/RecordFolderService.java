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

package org.alfresco.module.org_alfresco_module_rm.recordfolder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Record folder service interface
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@AlfrescoPublicApi
public interface RecordFolderService
{
    /**
     * Sets up the a record folder from a standard folder.
     *
     * @param nodeRef   node reference of the folder to setup
     *
     * @since 2.2
     */
    void setupRecordFolder(NodeRef nodeRef);

    /**
     * Indicates whether the given node is a record folder or not.
     *
     * @param nodeRef   node reference
     * @return boolean  true if record folder, false otherwise
     *
     * @since 2.2
     */
    boolean isRecordFolder(NodeRef nodeRef);

    /**
     * Indicates whether the contents of a record folder are all declared.
     *
     * @param nodeRef   node reference (record folder)
     * @return boolean  true if record folder contents are declared, false otherwise
     *
     * @since 2.2
     */
    boolean isRecordFolderDeclared(NodeRef nodeRef);

    /**
     * Indicates whether a record folder is closed or not.
     *
     * @param nodeRef   node reference (record folder)
     * @return boolean  true if record folder is closed, false otherwise
     *
     * @since 2.2
     */
    boolean isRecordFolderClosed(NodeRef nodeRef);

    /**
     * Create a record folder in the rm container.  The record folder will take the name and type
     * provided.
     *
     * @param  rmContainer   records management container
     * @param  name          name
     * @param  type          type
     * @return NodeRef       node reference of record folder
     *
     * @since 2.2
     */
    NodeRef createRecordFolder(NodeRef rmContainer, String name, QName type);

    /**
     * Create a record folder in the rm container.  The record folder will take the name, type and
     * properties provided.
     *
     * @param rmContainer   records management container
     * @param name          name
     * @param type          type
     * @param properties    properties
     * @return NodeRef      node reference of record folder
     *
     * @since 2.2
     */
    NodeRef createRecordFolder(NodeRef rmContainer, String name, QName type, Map<QName, Serializable> properties);

    /**
     * Create a record folder in the rm container.  The record folder will take the name provided.
     * Type defaults to rm:recordFolder.
     *
     * @param  rmContainer   records management container
     * @param  name          name
     * @return NodeRef       node reference of record folder
     *
     * @since 2.2
     */
    NodeRef createRecordFolder(NodeRef rmContainer, String name);

    /**
     * Create a record folder in the rm container.  The record folder will take the name and
     * properties provided. Type defaults to rm:recordFolder.
     *
     * @param rmContainer   records management container
     * @param name          name
     * @param properties    properties
     * @return NodeRef      node reference of record folder
     *
     * @since 2.2
     */
    NodeRef createRecordFolder(NodeRef rmContainer, String name, Map<QName, Serializable> properties);

    /**
     * Get all the record folders that a record is filed into.
     *
     * @param record        the record node reference
     * @return List         list of folder record node references
     *
     * @since 2.2
     */
    // TODO rename to List<NodeRef> getParentRecordFolders(NodeRef record);
    List<NodeRef> getRecordFolders(NodeRef record);

    // TODO NodeRef getRecordFolderByPath(String path);

    // TODO NodeRef getRecordFolderById(String id);

    // TODO NodeRef getRecordFolderByName(NodeRef parent, String name);

    // TODO void deleteRecordFolder(NodeRef recordFolder);

    // TODO List<NodeRef> getParentRecordsManagementContainers(NodeRef container); // also applicable to record folders

    // TODO rename to getContainedRecords(NodeRef recordFolder);

    /**
     * Closes the record folder. If the given node reference is a record the parent will be retrieved and processed.
     *
     * @param nodeRef   the record folder node reference
     *
     * @since 2.2
     */
    void closeRecordFolder(NodeRef nodeRef);
}
