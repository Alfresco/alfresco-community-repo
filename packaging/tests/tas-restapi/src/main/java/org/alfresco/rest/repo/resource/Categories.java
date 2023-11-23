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
package org.alfresco.rest.repo.resource;

import java.util.Map;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.rest.repo.resource.category.RepoCategoryCreator;
import org.alfresco.rest.repo.resource.category.RepoCategoryModifier;
import org.alfresco.rest.repo.resource.cache.MultiKeyResourceMap;
import org.alfresco.rest.repo.resource.general.Modifier;
import org.alfresco.rest.repo.resource.general.Specifier;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Helper class simplifying things related with repository categories management.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Categories implements ResourceManager<RestCategoryModel, Specifier.CategoriesSpecifier, Modifier.CategoryModifier>
{
    public static final String CATEGORY_NAME_PREFIX = "category";
    private static final String ROOT_CATEGORY_ID = "-root-";
    public static final RestCategoryModel ROOT_CATEGORY = RestCategoryModel.builder().id(ROOT_CATEGORY_ID).create();

    private final RestWrapper restClient;
    private final UserModel user;

    private final Map<String, RestCategoryModel> categoriesCache = new MultiKeyResourceMap<>(RestCategoryModel::getId, RestCategoryModel::getName);

    public Categories(RestWrapper restClient, UserModel user)
    {
        this.restClient = restClient;
        this.user = user;
    }

    @Autowired
    public Categories(RestWrapper restClient, DataUser dataUser)
    {
        this(restClient, dataUser.getAdminUser());
    }

    @Override
    public Specifier.CategoriesSpecifier add()
    {
        return (Specifier.CategoriesSpecifier) new RepoCategoryCreator(restClient, categoriesCache)
            .underCategory(ROOT_CATEGORY).asUser(user);
    }

    @Override
    public RestCategoryModel get(String key)
    {
        return categoriesCache.get(key);
    }

    @Override
    public Modifier.CategoryModifier modify(RestCategoryModel category)
    {
        return new RepoCategoryModifier(restClient, category, categoriesCache).asUser(user);
    }

    @Override
    public void delete(RestCategoryModel category)
    {
        new RepoCategoryModifier(restClient, category, categoriesCache).asUser(user).delete();
    }
}
