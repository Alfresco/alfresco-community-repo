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

package org.alfresco.rest.api.categories;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import org.alfresco.rest.api.Categories;
import org.alfresco.rest.api.model.Category;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.ListPage;
import org.alfresco.rest.framework.resource.parameters.Parameters;

@RelationshipResource(name = "category-links", entityResource = NodesEntityResource.class, title = "Category links")
public class NodesCategoryLinksRelation implements RelationshipResourceAction.Create<Category>,
                                                    RelationshipResourceAction.Read<Category>,
                                                    RelationshipResourceAction.Delete
{

    private final Categories categories;

    public NodesCategoryLinksRelation(Categories categories)
    {
        this.categories = categories;
    }

    /**
     * GET /nodes/{nodeId}/category-links
     */
    @WebApiDescription(
        title = "Get categories linked to by node",
        description = "Get categories linked to by node",
        successStatus = HttpServletResponse.SC_OK
    )
    @Override
    public CollectionWithPagingInfo<Category> readAll(String nodeId, Parameters parameters)
    {
        return ListPage.of(categories.listCategoriesForNode(nodeId, parameters), parameters.getPaging());
    }

    /**
     * POST /nodes/{nodeId}/category-links
     */
    @WebApiDescription(
        title = "Link node to categories",
        description = "Creates a link between a node and categories",
        successStatus = HttpServletResponse.SC_CREATED
    )
    @Override
    public List<Category> create(String nodeId, List<Category> categoryLinks, Parameters parameters)
    {
        return categories.linkNodeToCategories(nodeId, categoryLinks, parameters);
    }

    /**
     * DELETE /nodes/{nodeId}/category-links/{categoryId}
     */
    @WebApiDescription(
            title = "Unlink content node from category",
            description = "Removes the link between a content node and a category",
            successStatus = HttpServletResponse.SC_NO_CONTENT
    )
    @Override
    public void delete(String nodeId, String categoryId, Parameters parameters)
    {
        categories.unlinkNodeFromCategory(nodeId, categoryId, parameters);
    }
}
