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
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.StoreRef;

@Experimental
public interface Categories
{
    Category getCategoryById(StoreRef storeRef, String id, Parameters parameters);

    default Category getCategoryById(String id, Parameters parameters)
    {
        return getCategoryById(STORE_REF_WORKSPACE_SPACESSTORE, id, parameters);
    }

    List<Category> createSubcategories(StoreRef storeRef, String parentCategoryId, List<Category> categories, Parameters parameters);

    default List<Category> createSubcategories(String parentCategoryId, List<Category> categories, Parameters parameters)
    {
        return createSubcategories(STORE_REF_WORKSPACE_SPACESSTORE, parentCategoryId, categories, parameters);
    }

    List<Category> getCategoryChildren(StoreRef storeRef, String parentCategoryId, Parameters parameters);

    default List<Category> getCategoryChildren(String parentCategoryId, Parameters parameters)
    {
        return getCategoryChildren(STORE_REF_WORKSPACE_SPACESSTORE, parentCategoryId, parameters);
    }

    /**
     * Update category by ID. Currently, it's possible only to update the name of category.
     *
     * @param storeRef Reference to node store.
     * @param id Category ID.
     * @param fixedCategoryModel Fixed category model.
     * @param parameters Additional parameters.
     * @return Updated category.
     */
    Category updateCategoryById(StoreRef storeRef, String id, Category fixedCategoryModel, Parameters parameters);

    default Category updateCategoryById(String id, Category fixedCategoryModel, Parameters parameters)
    {
        return updateCategoryById(STORE_REF_WORKSPACE_SPACESSTORE, id, fixedCategoryModel, parameters);
    }

    void deleteCategoryById(StoreRef storeRef, String id, Parameters parameters);

    default void deleteCategoryById(String id, Parameters parameters)
    {
        deleteCategoryById(STORE_REF_WORKSPACE_SPACESSTORE, id, parameters);
    }

    /**
     * Get categories linked from node. Read permission on node is required.
     * Node type is restricted to specified vales from: {@link org.alfresco.util.TypeConstraint}.
     *
     * @param nodeId Node ID.
     * @param parameters Additional parameters.
     * @return Categories linked from node.
     */
    List<Category> listCategoriesForNode(String nodeId, Parameters parameters);

    /**
     * Link node to categories. Change permission on node is required.
     * Node types allowed for categorization are specified within {@link org.alfresco.util.TypeConstraint}.
     *
     * @param storeRef Reference to node store.
     * @param nodeId Node ID.
     * @param categoryLinks Category IDs to which content should be linked to.
     * @param parameters Additional parameters.
     * @return Linked to categories.
     */
    List<Category> linkNodeToCategories(StoreRef storeRef, String nodeId, List<Category> categoryLinks, Parameters parameters);

    default List<Category> linkNodeToCategories(String nodeId, List<Category> categoryLinks, Parameters parameters)
    {
        return linkNodeToCategories(STORE_REF_WORKSPACE_SPACESSTORE, nodeId, categoryLinks, parameters);
    }

    /**
     * Unlink node from a category.
     *
     * @param storeRef Reference to node store.
     * @param nodeId Node ID.
     * @param categoryId Category ID from which content node should be unlinked from.
     * @param parameters Additional parameters.
     */
    void unlinkNodeFromCategory(StoreRef storeRef, String nodeId, String categoryId, Parameters parameters);

    default void unlinkNodeFromCategory(String nodeId, String categoryId, Parameters parameters)
    {
        unlinkNodeFromCategory(STORE_REF_WORKSPACE_SPACESSTORE, nodeId, categoryId, parameters);
    }
}
