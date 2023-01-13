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

import java.util.List;

import org.alfresco.rest.api.model.Category;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;

@Experimental
public interface Categories
{
    Category getCategoryById(String id, Parameters parameters);

    List<Category> createSubcategories(String parentCategoryId, List<Category> categories, Parameters parameters);

    CollectionWithPagingInfo<Category> getCategoryChildren(String parentCategoryId, Parameters parameters);

    /**
     * Update category by ID. Currently, it's possible only to update the name of category.
     *
     * @param id Category ID.
     * @param fixedCategoryModel Fixed category model.
     * @return Updated category.
     */
    Category updateCategoryById(String id, Category fixedCategoryModel);

    void deleteCategoryById(String id, Parameters parameters);

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
     * @param nodeId Node ID.
     * @param categoryLinks Category IDs to which content should be linked to.
     * @return Linked to categories.
     */
    List<Category> linkNodeToCategories(String nodeId, List<Category> categoryLinks);
}
