/*-
 * #%L
 * alfresco-tas-restapi
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
package org.alfresco.rest.repo.resource.category;

import java.util.Map;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.rest.repo.resource.general.Modifier;
import org.alfresco.rest.repo.resource.general.ResourceModifier;
import org.alfresco.rest.repo.resource.general.Specifier;
import org.alfresco.utility.model.UserModel;

public class RepoCategoryModifier
    extends ResourceModifier<RestCategoryModel, Modifier.CategoryModifier>
    implements Modifier.CategoryModifier
{

    private final RestWrapper restClient;
    private final RestCategoryModel category;
    private final Map<String, RestCategoryModel> categoriesCache;

    public RepoCategoryModifier(RestWrapper restClient, RestCategoryModel category, Map<String, RestCategoryModel> categoriesCache)
    {
        super();
        this.restClient = restClient;
        this.category = category;
        this.categoriesCache = categoriesCache;
    }

    @Override
    protected CategoryModifier self()
    {
        return this;
    }

    @Override
    public Specifier.CategoriesSpecifier add()
    {
        return (Specifier.CategoriesSpecifier) new RepoCategoryCreator(restClient, categoriesCache)
            .underCategory(category).asUser(user);
    }

    @Override
    public RestCategoryModel get(String id)
    {
        return buildCategoryRestRequest(restClient, user, RestCategoryModel.builder().id(id).create())
            .getCategory();
    }

    @Override
    public void delete()
    {
        categoriesCache.remove(category.getId());
        buildCategoryRestRequest(restClient, user, category).deleteCategory();
    }

    private static org.alfresco.rest.requests.Categories buildCategoryRestRequest(RestWrapper restClient, UserModel user, RestCategoryModel category)
    {
        return restClient.authenticateUser(user).withCoreAPI().usingCategory(category);
    }
}
