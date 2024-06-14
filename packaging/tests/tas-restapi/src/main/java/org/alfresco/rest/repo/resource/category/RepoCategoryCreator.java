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

import static org.alfresco.rest.repo.resource.Categories.CATEGORY_NAME_PREFIX;
import static org.alfresco.rest.repo.resource.Categories.ROOT_CATEGORY;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Map;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.rest.repo.resource.general.Creator;
import org.alfresco.rest.repo.resource.general.MultiCreator;
import org.alfresco.rest.repo.resource.general.ResourceCreator;
import org.alfresco.rest.repo.resource.general.Specifier;

public class RepoCategoryCreator
    extends ResourceCreator<RestCategoryModel, Creator.CategoryCreator>
    implements Creator.CategoryCreator, Specifier.CategoriesSpecifier
{

    private final RestWrapper restClient;
    private RestCategoryModel parent = ROOT_CATEGORY;
    private final Map<String, RestCategoryModel> categoriesCache;

    public RepoCategoryCreator(RestWrapper restClient, Map<String, RestCategoryModel> categoriesCache)
    {
        super();
        this.restClient = restClient;
        this.categoriesCache = categoriesCache;
    }

    @Override
    protected CategoryCreator self()
    {
        return this;
    }

    @Override
    public CategoryCreator category(String name)
    {
        return this.withName(name);
    }

    @Override
    public CategoryCreator randomCategory()
    {
        return this.withRandomName();
    }

    @Override
    public CategoryCreator randomCategory(String prefix)
    {
        return this.withRandomName(prefix);
    }

    @Override
    public MultiCreator.CategoriesCreator categories(String... names)
    {
        return new SerialCategoriesCreator(restClient, categoriesCache).withNames(names).underCategory(parent).asUser(user);
    }

    @Override
    public MultiCreator.CategoriesCreator randomCategories(String... prefixes)
    {
        return new SerialCategoriesCreator(restClient, categoriesCache).withRandomNames(prefixes).underCategory(parent).asUser(user);
    }

    @Override
    public MultiCreator.CategoriesCreator randomCategories(int quantity)
    {
        return new SerialCategoriesCreator(restClient, categoriesCache).withRandomNames(quantity).underCategory(parent).asUser(user);
    }

    @Override
    public MultiCreator.CategoriesCreator nestedCategories(String... names)
    {
        return new NestedCategoriesCreator(restClient, categoriesCache).withNames(names).underCategory(parent).asUser(user);
    }

    @Override
    public MultiCreator.CategoriesCreator nestedRandomCategories(String... prefixes)
    {
        return new NestedCategoriesCreator(restClient, categoriesCache).withRandomNames(prefixes).underCategory(parent).asUser(user);
    }

    @Override
    public MultiCreator.CategoriesCreator nestedRandomCategories(int depth)
    {
        return new NestedCategoriesCreator(restClient, categoriesCache).withRandomNames(depth).underCategory(parent).asUser(user);
    }

    @Override
    public CategoryCreator withRandomName(String prefix)
    {
        withAlias(prefix);
        return super.withRandomName(prefix);
    }
    @Override
    public CategoryCreator underCategory(RestCategoryModel parent)
    {
        this.parent = parent;
        return this;
    }

    @Override
    public RestCategoryModel create()
    {
        RestCategoryModel category = restClient.authenticateUser(user).withCoreAPI().usingCategory(parent)
            .createSingleCategory(RestCategoryModel.builder().name(name).create());

        categoriesCache.put(alias, category);

        return category;
    }

    @Override
    protected String generateRandomName()
    {
        return this.generateRandomNameWith(EMPTY);
    }

    @Override
    protected String generateRandomNameWith(String prefix)
    {
        return super.generateRandomNameWith(CATEGORY_NAME_PREFIX + prefix + "_");
    }
}
