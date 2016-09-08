/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Records management service interface.
 * 
 * Allows simple creation, manipulation and querying of records management components.
 * 
 * @author Roy Wetherall
 */
public interface RecordsManagementService
{
    /********** RM Component methods **********/
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#isFilePlan(NodeRef)}
     */
    @Deprecated
    boolean isFilePlanComponent(NodeRef nodeRef);
    
    /**
     * @since 2.0
     * @deprecated as of 2.1, see {@link FilePlanService#getFilePlanComponentKind(NodeRef)}
     */
    @Deprecated
    FilePlanComponentKind getFilePlanComponentKind(NodeRef nodeRef);
    
    /**
     * @since 2.0
     * @deprecated as of 2.1, see {@link FilePlanService#getFilePlanComponentKindFromType(QName)}
     */
    @Deprecated
    FilePlanComponentKind getFilePlanComponentKindFromType(QName type);
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#isFilePlanContainer(NodeRef)}
     */
    @Deprecated
    boolean isRecordsManagementContainer(NodeRef nodeRef);
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#isFilePlan(NodeRef)}
     */
    @Deprecated
    boolean isFilePlan(NodeRef nodeRef);
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#isRecordCategory(NodeRef)}
     */
    @Deprecated
    boolean isRecordCategory(NodeRef nodeRef); 
    
    /**
     * Indicates whether the given node is a record folder or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if record folder, false otherwise
     */
    boolean isRecordFolder(NodeRef nodeRef);	// record folder service
    
    /**
     * Indicates whether the given node is a transfer (container) or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if transfer, false otherwise
     * 
     * @since 2.0
     */
    boolean isTransfer(NodeRef nodeRef);	// transfer service
    
    /**
     * Indicates whether the given node (record or record folder) is a metadata stub or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if a metadata stub, false otherwise
     * 
     * @since 2.0
     */
    boolean isMetadataStub(NodeRef nodeRef);  // record service
    
    /**
     * Indicates whether the item is cutoff or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if the item is cutoff, false otherwise
     * 
     * @since 2.0
     */
    boolean isCutoff(NodeRef nodeRef);		// disposition service ??
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#getNodeRefPath(NodeRef)}
     */
    @Deprecated
    List<NodeRef> getNodeRefPath(NodeRef nodeRef);
    
    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlan(NodeRef)}
     */
    @Deprecated
    NodeRef getFilePlan(NodeRef nodeRef);
    
    /********** File Plan Methods **********/
    
    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlans()}
     */
    @Deprecated
    List<NodeRef> getFilePlans();
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, QName)}
     */
    @Deprecated
    NodeRef createFilePlan(NodeRef parent, String name, QName type);
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, QName, Map)}
     */
    @Deprecated
    NodeRef createFilePlan(NodeRef parent, String name, QName type, Map<QName, Serializable> properties);
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String)}
     */
    @Deprecated
    NodeRef createFilePlan(NodeRef parent, String name);
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, Map)}
     */
    @Deprecated
    NodeRef createFilePlan(NodeRef parent, String name, Map<QName, Serializable> properties);
    
    
    /********** Record Category Methods **********/
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#getAllContained(NodeRef, boolean)}
     */
    @Deprecated
    List<NodeRef> getAllContained(NodeRef recordCategory, boolean deep);
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#getAllContained(NodeRef)}
     */
    @Deprecated
    List<NodeRef> getAllContained(NodeRef recordCategory);    
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#getContainedRecordCategories(NodeRef, boolean)}
     */
    @Deprecated
    List<NodeRef> getContainedRecordCategories(NodeRef recordCategory, boolean deep);
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#getContainedRecordCategories(NodeRef)}
     */
    @Deprecated
    List<NodeRef> getContainedRecordCategories(NodeRef recordCategory);
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#getContainedRecordCategories(NodeRef, boolean)}
     */
    @Deprecated
    List<NodeRef> getContainedRecordFolders(NodeRef container, boolean deep);
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#getContainedRecordFolders(NodeRef)}
     */
    @Deprecated
    List<NodeRef> getContainedRecordFolders(NodeRef container);
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String, QName)}
     */
    @Deprecated
    NodeRef createRecordCategory(NodeRef parent, String name, QName type);
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String, QName, Map)}
     */
    @Deprecated
    NodeRef createRecordCategory(NodeRef parent, String name, QName type, Map<QName, Serializable> properties);
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String)}
     */
    @Deprecated
    NodeRef createRecordCategory(NodeRef parent, String name);
    
    /**
     * @deprecated as of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String, Map)}
     */
    @Deprecated
    NodeRef createRecordCategory(NodeRef parent, String name, Map<QName, Serializable> properties);
    
    
    /********** Record Folder methods **********/    
    
    /**
     * Indicates whether the contents of a record folder are all declared.
     * 
     * @param nodeRef   node reference (record folder)
     * @return boolean  true if record folder contents are declared, false otherwise
     */
    boolean isRecordFolderDeclared(NodeRef nodeRef);
    
    /**
     * Indicates whether a record folder is closed or not.
     * 
     * @param nodeRef   node reference (record folder)
     * @return boolean  true if record folder is closed, false otherwise
     * 
     * @since 2.0
     */
    boolean isRecordFolderClosed(NodeRef nodeRef);
    
    // TODO NodeRef getRecordFolderByPath(String path);
    
    // TODO NodeRef getRecordFolderById(String id);
    
    // TODO NodeRef getRecordFolderByName(NodeRef parent, String name);
    
    
    /**
     * Create a record folder in the rm container.  The record folder with take the name and type 
     * provided.
     * 
     * @param  rmContainer   records management container
     * @param  name          name
     * @param  type          type
     * @return NodeRef       node reference of record folder
     */
    NodeRef createRecordFolder(NodeRef rmContainer, String name, QName type);
    
    /**
     * 
     * @param rmContainer
     * @param name
     * @param type
     * @param properties
     * @return
     */
    NodeRef createRecordFolder(NodeRef rmContainer, String name, QName type, Map<QName, Serializable> properties);
    
    /**
     * Type defaults to rm:recordFolder
     * 
     * @see RecordsManagementService#createRecordCategory(NodeRef, String, QName)
     * 
     * @param  rmContainer   records management container
     * @param  name          name
     * @return NodeRef       node reference of record folder
     */
    NodeRef createRecordFolder(NodeRef parent, String name);
    
    /**
     * 
     * @param parent
     * @param name
     * @param properties
     * @return
     */
    NodeRef createRecordFolder(NodeRef parent, String name, Map<QName, Serializable> properties);
    
    // TODO void deleteRecordFolder(NodeRef recordFolder);
    
    // TODO List<NodeRef> getParentRecordsManagementContainers(NodeRef container); // also applicable to record folders
    
    /**
     * Gets a list of all the records within a record folder
     * 
     * @param recordFolder      record folder
     * @return List<NodeRef>    list of records in the record folder
     */
    // TODO rename to getContainedRecords(NodeRef recordFolder);
    List<NodeRef> getRecords(NodeRef recordFolder);
    
    // TODO move? copy? link?
    
    /**
     * Get all the record folders that a record is filed into.
     * 
     * @param record            the record node reference
     * @return List<NodeRef>    list of folder record node references
     */
    // TODO rename to List<NodeRef> getParentRecordFolders(NodeRef record);
    List<NodeRef> getRecordFolders(NodeRef record); 
    
    /********** Deprecated **********/

    /** 
     * @deprecated As of 2.1, replaced by {@link RecordService#getRecordMetaDataAspects()}
     */
    @Deprecated
    Set<QName> getRecordMetaDataAspects();

    /**
     * @deprecated As of 2.1, replaced by {@link RecordService#isDeclared(NodeRef)}
     */
    @Deprecated
    boolean isRecordDeclared(NodeRef nodeRef);
    
    /**
     * @since 2.0
     * @deprecated As of 2.1, replaced by {@link FreezeService#isHold(NodeRef)}
     */
    @Deprecated
    boolean isHold(NodeRef nodeRef);
    
    /**
     * @since 2.0
     * @deprecated As of 2.1, replaced by {@link FreezeService#isFrozen(NodeRef)}
     */
    @Deprecated 
    boolean isFrozen(NodeRef nodeRef);
    
    /**
     * @since 2.0
     * @deprecated As of 2.1, replaced by {@link FreezeService#hasFrozenChildren(NodeRef)}
     */
    @Deprecated
    boolean hasFrozenChildren(NodeRef nodeRef);
    
    /**
     * @deprecated As of 2.1, replaced by {@link RecordService#isRecord(NodeRef)}
     */
    @Deprecated
    boolean isRecord(NodeRef nodeRef);
}
