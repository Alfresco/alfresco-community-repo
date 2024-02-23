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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.rest.repo.resource.general.MultiCreator;
import org.alfresco.rest.repo.resource.general.MultipleResourcesCreator;

public class SerialCategoriesCreator
    extends MultipleResourcesCreator<RestCategoryModel, MultiCreator.CategoriesCreator>
    implements MultiCreator.CategoriesCreator
{

    private final RestWrapper restClient;
    protected RestCategoryModel parent = ROOT_CATEGORY;
    private final Map<String, RestCategoryModel> categoriesCache;

    public SerialCategoriesCreator(RestWrapper restClient, Map<String, RestCategoryModel> categoriesCache)
    {
        super();
        this.restClient = restClient;
        this.categoriesCache = categoriesCache;
    }

    @Override
    protected CategoriesCreator self()
    {
        return this;
    }

    @Override
    public CategoriesCreator underCategory(RestCategoryModel parent)
    {
        this.parent = parent;
        return this;
    }

    @Override
    public List<RestCategoryModel> create()
    {
        return createRawCategories(parent, names);
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

    private List<RestCategoryModel> createRawCategories(RestCategoryModel parent, List<String> categoryNames)
    {
        List<RestCategoryModel> createdCategories = new ArrayList<>();
        AtomicInteger i = new AtomicInteger();
        categoryNames.forEach(categoryName -> {
            RestCategoryModel createdCategory = createCategory(categoryName, getOrNull(aliases, i.getAndIncrement()), parent);
            createdCategories.add(createdCategory);
        });

        return createdCategories;
    }

    protected RestCategoryModel createCategory(String name, String alias, RestCategoryModel parent)
    {
        return new RepoCategoryCreator(restClient, this.categoriesCache)
            .withAlias(alias)
            .withName(name)
            .underCategory(parent)
            .asUser(user)
            .create();
    }
}
