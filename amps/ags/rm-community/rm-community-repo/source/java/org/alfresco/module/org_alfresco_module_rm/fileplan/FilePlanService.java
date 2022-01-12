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

package org.alfresco.module.org_alfresco_module_rm.fileplan;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * File plan service interface.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@AlfrescoPublicApi
public interface FilePlanService
{
    /**
     * Default RM site id.
     * Can be used with {@link FilePlanService#getFilePlanBySiteId(String)} to get the file plan node.
     * */
    String DEFAULT_RM_SITE_ID = "rm";

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
     * @return FilePlanComponentKind    the kind of file plan component the node is
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
     * Gets all the file plan nodes.
     * Looks in the SpacesStore by default.
     *
     * @return  Set<NodeRef>    set of file plan nodes
     */
    Set<NodeRef> getFilePlans();

    /**
     * Getse all the file plan nodes in a store.
     *
     * @param  storeRef     store reference
     * @return Set<NodeRef> set of file plan nodes
     */
    Set<NodeRef> getFilePlans(StoreRef storeRef);

    /**
     * Gets the file plan the node is in.
     *
     * @return  {@link NodeRef} file node reference, null if none
     */
    NodeRef getFilePlan(NodeRef nodeRef);

    /**
     * Gets a file plan by site id.  Assumes the site is a RM site and that the file plan node, ie
     * the document library container, has been already created.  Otherwise returns null.
     *
     * @param siteId    records management site id
     * @return NodeRef  file plan, null if can't be found
     */
    NodeRef getFilePlanBySiteId(String siteId);

    /**
     * Indicates whether the unfiled container exists for a given file plan or not.
     *
     * @param filePlan  file plan
     * @return boolean  true if unfiled container exists, false otherwise
     */
    boolean existsUnfiledContainer(NodeRef filePlan);

    /**
     * Gets the unfiled container for a given file plan.  Returns null if
     * none.
     *
     * @param filePlan          file plan
     * @return {@link NodeRef}  unfiled container, null if none
     */
    NodeRef getUnfiledContainer(NodeRef filePlan);

    /**
     * Creates, and returns, a unfiled container for a given file plan.
     *
     * @param filePlan      file plan
     * @return {@link NodeRef} unfiled container
     */
    NodeRef createUnfiledContainer(NodeRef filePlan);

    /**
     * Gets the hold container for a given file plan.  Returns
     * null if none.
     *
     * @param filePlan
     * @return
     */
    NodeRef getHoldContainer(NodeRef filePlan);

    /**
     *
     * @param filePlan
     * @return
     */
    NodeRef createHoldContainer(NodeRef filePlan);

    /**
     *
     * @param filePlan
     * @return
     */
    NodeRef getTransferContainer(NodeRef filePlan);

    /**
     *
     * @param filePlan
     * @return
     */
    NodeRef createTransferContainer(NodeRef filePlan);

    /**
     * Creates a file plan as a child of the given parent node, with the name
     * provided.
     *
     * @param   parent  parent node reference
     * @param   name    file plan name
     * @param   type    type, must be rma:filePlan or sub-type of
     * @return  NodeRef file plan node reference
     */
    NodeRef createFilePlan(NodeRef parent, String name, QName type);

    /**
     * Specifies the properties to be set on the created file plan.
     *
     * @see #createFilePlan(NodeRef, String, QName)
     *
     * @param  parent		parent node reference
     * @param  name			file plan name
     * @param  type			type, must be rma:filePlan or sub-type of
     * @param  properties	file plan properties
     * @return NodeRef		file plan node reference
     */
    NodeRef createFilePlan(NodeRef parent, String name, QName type, Map<QName, Serializable> properties);

    /**
     * Creates a file plan with the default type.
     *
     * @see #createFilePlan(NodeRef, String, QName)
     *
     * @param  parent	parent node reference
     * @param  name		file plan name
     * @return NodeRef	file plan node reference
     */
    NodeRef createFilePlan(NodeRef parent, String name);

    /**
     * Creates a file plan with the default type, specifying properties.
     *
     * @see #createFilePlan(NodeRef, String, QName)
     *
     * @param  parent		parent node reference
     * @param  name			file plan name
     * @param  properties	file plan properties
     * @return NodeRef		file plan node reference
     */
    NodeRef createFilePlan(NodeRef parent, String name, Map<QName, Serializable> properties);

    // TODO deleteFilePlan

    /**
     * Gets the <b>NodeRef</b> sequence from the {@link #getFilePlan(NodeRef) root}
     * down to the fileplan component given.  The array will start with the <b>NodeRef</b> of the root
     * and end with the name of the fileplan component node given.
     *
     * @param nodeRef           a fileplan component
     * @return                  Returns a <b>NodeRef</b> path starting with the file plan
     */
    List<NodeRef> getNodeRefPath(NodeRef nodeRef);

    /**
     * Indicates whether the given node is a file plan container or not.
     * <p>
     * This includes file plan and record category nodes.
     *
     * @param nodeRef   node reference
     * @return boolean  true if node is a file plan container, false otherwise.
     */
    boolean isFilePlanContainer(NodeRef nodeRef);

    /**
     * Indicates whether the given node is a record category or not.
     *
     * @param nodeRef   node reference
     * @return boolean  true if records category, false otherwise
     */
    boolean isRecordCategory(NodeRef nodeRef);

    /**
     * Get all the items contained within a container.  This will include record folders and other record categories.
     *
     * @param recordCategory record category node reference
     * @param deep if true then return all children including sub-categories and their children in turn, if false then just
     *             return the immediate children
     * @return {@link List}&lt;{@link NodeRef}&gt; list of contained node references
     */
    List<NodeRef> getAllContained(NodeRef recordCategory, boolean deep);

    /**
     * Only return the immediate children.
     *
     * @param recordCategory record category node reference
     * @return {@link List}&lt;{@link NodeRef}&gt; list of contained node references
     */
    List<NodeRef> getAllContained(NodeRef recordCategory);

    /**
     * Get all the record categories within a record category.
     *
     * @param recordCategory record category node reference
     * @param deep if true then return all children including sub-categories and their children in turn, if false then just
     *             return the immediate children
     * @return {@link List}&lt;{@link NodeRef}&gt; list of container node references
     */
    List<NodeRef> getContainedRecordCategories(NodeRef recordCategory, boolean deep);

    /**
     * Only return immediate children.
     *
     * @param recordCategory container node reference
     * @return {@link List}&lt;{@link NodeRef}&gt; list of container node references
     */
    List<NodeRef> getContainedRecordCategories(NodeRef recordCategory);

    /**
     * Get all the record folders contained within a container
     *
     * @param container container node reference
     * @param deep if true then return all children including sub-containers and their children in turn, if false then just
     *             return the immediate children
     * @return {@link List}&lt;{@link NodeRef}&gt; list of record folder node references
     */
    List<NodeRef> getContainedRecordFolders(NodeRef container, boolean deep);

    /**
     * Only return immediate children.
     *
     * @param container container node reference
     * @return {@link List}&lt;{@link NodeRef}&gt;  list of record folder node references
     */
    List<NodeRef> getContainedRecordFolders(NodeRef container);

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

}
