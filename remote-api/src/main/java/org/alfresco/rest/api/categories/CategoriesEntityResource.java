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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.categories;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.rest.api.Categories;
import org.alfresco.rest.api.model.Category;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Entity Resource for Categories
 *
 * @author mpichura
 */
@EntityResource(name = "categories", title = "Categories")
public class CategoriesEntityResource implements EntityResourceAction.ReadById<Category>,
                                                 EntityResourceAction.Update<Category>,
                                                 EntityResourceAction.Delete
{

    private final Categories categories;

    public CategoriesEntityResource(Categories categories)
    {
        this.categories = categories;
    }

    /**
     * GET /categories/{categoryId}
     */
    @WebApiDescription(
        title = "Get category by its ID",
        description = "Retrieves a category given category node id",
        successStatus = HttpServletResponse.SC_OK
    )
    @Override
    public Category readById(String id, Parameters parameters) throws EntityNotFoundException
    {
        return categories.getCategoryById(id, parameters);
    }

    /**
     * PUT /categories/{categoryId}
     */
    @WebApiDescription(
        title = "Update category",
        description = "Update a single category by its ID",
        successStatus = HttpServletResponse.SC_OK
    )
    @Override
    public Category update(String id, Category categoryModel, Parameters parameters)
    {
        return categories.updateCategoryById(id, categoryModel, parameters);
    }

    /**
     * DELETE /categories/{categoryId}
     */
    @WebApiDescription(
        title = "Delete category",
        description = "Delete a category given its node ID",
        successStatus = HttpServletResponse.SC_NO_CONTENT
    )
    @Override
    public void delete(String id, Parameters parameters)
    {
        categories.deleteCategoryById(id, parameters);
    }
}
