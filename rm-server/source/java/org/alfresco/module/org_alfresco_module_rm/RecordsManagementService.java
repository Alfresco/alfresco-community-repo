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
     * Indicates whether the given node is a file plan component or not.
     * 
     * @param  nodeRef   node reference
     * @return boolean   true if a file plan component, false otherwise
     */
    boolean isFilePlanComponent(NodeRef nodeRef);
    
    /**
     * Returns the 'kind' of file plan component the node reference is.
     * <p>
     * Returns null if the given node reference is not a
     * file plan component.
     *
     * @param nodeRef   node reference
     * @return FilePlanComponentKind    the kind of file plan component the
     *                                  node is
     *
     * @since 2.0
     */
    FilePlanComponentKind getFilePlanComponentKind(NodeRef nodeRef);
    
    /**
     * Returns the file plan component 'kind' that relates to the passed 
     * content type.
     * <p>
     * Returns null if the type does not relate to a file plan component.
     * 
     * @param type  qualified name of content type 
     * @return FilePlanComponentKind    the kind relating to the passed type
     * 
     * @since 2.0
     */
    FilePlanComponentKind getFilePlanComponentKindFromType(QName type);
    
    /**
     * Indicates whether the given node is file plan node or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if node is a file plan node
     */
    boolean isFilePlan(NodeRef nodeRef);
    
    /**
     * Indicates whether the given node is a record category or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if records category, false otherwise
     */
    boolean isRecordCategory(NodeRef nodeRef);
    
    /**
     * Indicates whether the given node is a record folder or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if record folder, false otherwise
     */
    boolean isRecordFolder(NodeRef nodeRef);
    
    /**
     * Indicates whether the given node is a record or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if record, false otherwise
     */
    boolean isRecord(NodeRef nodeRef);    
    
    /**
     * Indicates whether the given node is a hold (container) or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if hold, false otherwise
     * 
     * @since 2.0
     */
    boolean isHold(NodeRef nodeRef);
    
    /**
     * Indicates whether the given node is a transfer (container) or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if transfer, false otherwise
     * 
     * @since 2.0
     */
    boolean isTransfer(NodeRef nodeRef);
    
    /**
     * Indicates whether the given node (record or record folder) is a metadata stub or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if a metadata stub, false otherwise
     * 
     * @since 
     */
    boolean isMetadataStub(NodeRef nodeRef);    
    
    /**
     * Indicates whether the item is frozen or not.
     * 
     * @param   nodeRef     node reference
     * @return  boolean     true if record is frozen, false otherwise
     * 
     * @since 2.0
     */
    boolean isFrozen(NodeRef nodeRef);
    
    
    /**
     * Indicates whether the item has frozen children or not.
     * 
     * NOTE: this only checks the immediate children and does not check the frozen
     *       state of the node being passed
     * 
     * @param nodeRef   node reference 
     * @return boolean  true if record folder has frozen children, false otherwise
     * 
     * @since 2.0
     */
    boolean hasFrozenChildren(NodeRef nodeRef);    
    
    /**
     * Indicates whether the item is cutoff or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if the item is cutoff, false otherwise
     * 
     * @since 2.0
     */
    boolean isCutoff(NodeRef nodeRef);
    
    /**
     * Gets the <b>NodeRef</b> sequence from the {@link #getFilePlan(NodeRef) root}
     * down to the fileplan component given.  The array will start with the <b>NodeRef</b> of the root
     * and end with the name of the fileplan component node given.
     * 
     * @param nodeRef           a fileplan component
     * @return                  Returns a <b>NodeRef</b> path starting with the name of the
     *                          records management root
     */
    List<NodeRef> getNodeRefPath(NodeRef nodeRef);
    
    /**
     * Gets the file plan the node is in.
     * 
     * @return  {@link NodeRef} file node reference, null if none 
     */
    NodeRef getFilePlan(NodeRef nodeRef);
    
    /********** File Plan Methods **********/
    
    /**
     * Gets all the file plan nodes.
     * Searches the SpacesStore by default. 
     * 
     * @return  List<NodeRef>    list of file plan nodes
     */
    List<NodeRef> getFilePlans();
    
//    /**
//     * Specify the store which should be searched.
//     * 
//     * @see RecordsManagementService#getFilePlans()
//     * 
//     * @param  storeRef         store reference
//     * @return List<NodeRef>    list of record management root nodes
//     */
//    @Deprecated
//    List<NodeRef> getRecordsManagementRoots(StoreRef storeRef);
    
    // TODO NodeRef getFilePlanById(String id);
    
    /**
     * Creates a file plan as a child of the given parent node, with the name
     * provided.
     * 
     * @param   parent  parent node reference
     * @param   name    name of the root
     * @param   type    type of root created (must be sub-type of rm:filePlan)
     * @return  NodeRef node reference to the newly create RM root
     */
    NodeRef createFilePlan(NodeRef parent, String name, QName type);
    
    /**
     * @see #createFilePlan(NodeRef, String, QName)
     * 
     * @param parent
     * @param name
     * @param type
     * @param properties
     * @return
     */
    NodeRef createFilePlan(NodeRef parent, String name, QName type, Map<QName, Serializable> properties);
    
    /**
     * Creates a file plan with the default type.
     * 
     * @see RecordsManagementService#createFilePlan(NodeRef, String, QName)
     */
    NodeRef createFilePlan(NodeRef parent, String name);
    
    /**
     * 
     * @param parent
     * @param name
     * @param properties
     * @return
     */
    NodeRef createFilePlan(NodeRef parent, String name, Map<QName, Serializable> properties);
    
    // TODO void deleteRecordsManagementRoot(NodeRef root);
    
    /********** Record Category Methods **********/
    
    // TODO NodeRef getRecordCategoryByPath(String path);
    
    // TODO NodeRef getRecordCategoryById(String id);
    
    // TODO NodeRef getRecordCategoryByName(NodeRef parent, String id); ??
    
    /**
     * Get all the items contained within a container.  This will include record folders and other record categories.
     * 
     * @param recordCategory record category node reference
     * @param deep if true then return all children including sub-categories and their children in turn, if false then just
     *             return the immediate children
     * @return {@link List}<{@link NodeRef>} list of contained node references
     */
    List<NodeRef> getAllContained(NodeRef recordCategory, boolean deep);
    
    /**
     * Only return the immediate children.
     * 
     * @see RecordsManagementService#getAllContained(NodeRef, boolean)
     * 
     * @param recordCategory record category node reference
     * @return {@link List}<{@link NodeRef>} list of contained node references
     */
    List<NodeRef> getAllContained(NodeRef recordCategory);    
    
    /**
     * Get all the record categories within a record category.
     * 
     * @param recordCategory record category node reference
     * @param deep if true then return all children including sub-categories and their children in turn, if false then just
     *             return the immediate children
     * @return {@link List}<{@link NodeRef>} list of container node references
     */
    List<NodeRef> getContainedRecordCategories(NodeRef recordCategory, boolean deep);
    
    /**
     * Only return immediate children.
     * 
     * @see RecordsManagementService#getContainedRecordCategories(NodeRef, boolean)
     * 
     * @param recordCategory container node reference
     * @return {@link List}<{@link NodeRef>} list of container node references
     */
    List<NodeRef> getContainedRecordCategories(NodeRef recordCategory);
    
    /**
     * Get all the record folders contained within a container
     * 
     * @param container container node reference
     * @param deep if true then return all children including sub-containers and their children in turn, if false then just
     *             return the immediate children
     * @return {@link List}<{@link NodeRef>} list of record folder node references
     */
    List<NodeRef> getContainedRecordFolders(NodeRef container, boolean deep);
    
    /**
     * Only return immediate children.
     * 
     * @see RecordsManagementService#getContainedRecordFolders(NodeRef, boolean)
     * 
     * @param container container node reference
     * @return {@link List}<{@link NodeRef>} list of record folder node references
     */
    List<NodeRef> getContainedRecordFolders(NodeRef container);
    
    // TODO List<NodeRef> getParentRecordCategories(NodeRef container); // also applicable to record folders 
    
    /**
     * Create a record category.
     * 
     * @param  parent    parent node reference, must be a record category or file plan.
     * @param  name      name of the new record category
     * @param  type      type of container to create, must be a sub-type of rm:recordCategory
     * @return NodeRef   node reference of the created record category
     */
    NodeRef createRecordCategory(NodeRef parent, String name, QName type);
    
    /**
     * 
     * @param parent
     * @param name
     * @param type
     * @param properties
     * @return
     */
    NodeRef createRecordCategory(NodeRef parent, String name, QName type, Map<QName, Serializable> properties);
    
    /**
     * Creates a record category of type rma:recordCategory
     * 
     * @see RecordsManagementService#createRecordCategory(NodeRef, String, QName)
     * 
     * @param  parent    parent node reference, must be a record category or file plan.
     * @param  name      name of the record category
     * @return NodeRef   node reference of the created record category
     */
    NodeRef createRecordCategory(NodeRef parent, String name);
    
    /**
     * 
     * @param parent
     * @param name
     * @param properties
     * @return
     */
    NodeRef createRecordCategory(NodeRef parent, String name, Map<QName, Serializable> properties);
    
    // TODO void deleteRecordCategory(NodeRef container);
    
    // TODO move, copy, link ??
    
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
    
    /********** Record methods **********/
    
    /**
     * Get a list of all the record meta-data aspects
     * 
     * @return {@link Set}<{@link QName}>	list of record meta-data aspects
     */
    Set<QName> getRecordMetaDataAspects();
    
    /**
     * Get all the record folders that a record is filed into.
     * 
     * @param record            the record node reference
     * @return List<NodeRef>    list of folder record node references
     */
    // TODO rename to List<NodeRef> getParentRecordFolders(NodeRef record);
    List<NodeRef> getRecordFolders(NodeRef record); 
        
    /**
     * Indicates whether the record is declared
     * 
     * @param nodeRef   node reference (record)
     * @return boolean  true if record is declared, false otherwise
     */
    boolean isRecordDeclared(NodeRef nodeRef);  
}
