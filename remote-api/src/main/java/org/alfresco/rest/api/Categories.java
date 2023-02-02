/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api;

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

import java.util.List;

import org.alfresco.rest.api.model.Category;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.StoreRef;

@Experimental
public interface Categories
{
    Category getCategoryById(StoreRef storeRef, String id, boolean includeCount);

    default Category getCategoryById(String id, boolean includeCount)
    {
        return getCategoryById(STORE_REF_WORKSPACE_SPACESSTORE, id, includeCount);
    }

    List<Category> createSubcategories(StoreRef storeRef, String parentCategoryId, List<Category> categories, boolean includeCount);

    default List<Category> createSubcategories(String parentCategoryId, List<Category> categories, boolean includeCount)
    {
        return createSubcategories(STORE_REF_WORKSPACE_SPACESSTORE, parentCategoryId, categories, includeCount);
    }

    List<Category> getCategoryChildren(StoreRef storeRef, String parentCategoryId, boolean includeCount);

    default List<Category> getCategoryChildren(String parentCategoryId, boolean includeCount)
    {
        return getCategoryChildren(STORE_REF_WORKSPACE_SPACESSTORE, parentCategoryId, includeCount);
    }

    /**
     * Update category by ID. Currently, it's possible only to update the name of category.
     *
     * @param storeRef Reference to node store.
     * @param id Category ID.
     * @param fixedCategoryModel Fixed category model.
     * @param includeCount Include category usage count in response.
     * @return Updated category.
     */
    Category updateCategoryById(StoreRef storeRef, String id, Category fixedCategoryModel, boolean includeCount);

    default Category updateCategoryById(String id, Category fixedCategoryModel, boolean includeCount)
    {
        return updateCategoryById(STORE_REF_WORKSPACE_SPACESSTORE, id, fixedCategoryModel, includeCount);
    }

    void deleteCategoryById(StoreRef storeRef, String id);

    default void deleteCategoryById(String id)
    {
        deleteCategoryById(STORE_REF_WORKSPACE_SPACESSTORE, id);
    }

    /**
     * Get categories linked from node. Read permission on node is required.
     * Node type is restricted to specified vales from: {@link org.alfresco.util.TypeConstraint}.
     *
     * @param nodeId Node ID.
     * @return Categories linked from node.
     */
    List<Category> listCategoriesForNode(String nodeId);

    /**
     * Link node to categories. Change permission on node is required.
     * Node types allowed for categorization are specified within {@link org.alfresco.util.TypeConstraint}.
     *
     * @param storeRef Reference to node store.
     * @param nodeId Node ID.
     * @param categoryLinks Category IDs to which content should be linked to.
     * @return Linked to categories.
     */
    List<Category> linkNodeToCategories(StoreRef storeRef, String nodeId, List<Category> categoryLinks);

    default List<Category> linkNodeToCategories(String nodeId, List<Category> categoryLinks)
    {
        return linkNodeToCategories(STORE_REF_WORKSPACE_SPACESSTORE, nodeId, categoryLinks);
    }

    /**
     * Unlink node from a category.
     *
     * @param storeRef Reference to node store.
     * @param nodeId Node ID.
     * @param categoryId Category ID from which content node should be unlinked from.
     */
    void unlinkNodeFromCategory(StoreRef storeRef, String nodeId, String categoryId);

    default void unlinkNodeFromCategory(String nodeId, String categoryId)
    {
        unlinkNodeFromCategory(STORE_REF_WORKSPACE_SPACESSTORE, nodeId, categoryId);
    }
}
