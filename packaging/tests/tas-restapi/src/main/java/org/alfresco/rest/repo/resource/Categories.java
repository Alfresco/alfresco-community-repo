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

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.rest.repo.resource.cache.MultiKeyResourceMap;
import org.alfresco.rest.repo.resource.general.Creator;
import org.alfresco.rest.repo.resource.general.Modifier;
import org.alfresco.rest.repo.resource.general.MultiCreator;
import org.alfresco.rest.repo.resource.general.MultipleResourcesCreator;
import org.alfresco.rest.repo.resource.general.ResourceCreator;
import org.alfresco.rest.repo.resource.general.ResourceModifier;
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
    private static final String CATEGORY_NAME_PREFIX = "category";
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

    private static org.alfresco.rest.requests.Categories buildCategoryRestRequest(RestWrapper restClient, UserModel user, RestCategoryModel category)
    {
        return restClient.authenticateUser(user).withCoreAPI().usingCategory(category);
    }

    public static class RepoCategoryCreator
        extends ResourceCreator<RestCategoryModel, Creator.CategoryCreator>
        implements Creator.CategoryCreator, Specifier.CategoriesSpecifier
    {
        private final RestWrapper restClient;
        private RestCategoryModel parent;
        private final Map<String, RestCategoryModel> categoriesCache;

        protected RepoCategoryCreator(RestWrapper restClient, Map<String, RestCategoryModel> categoriesCache)
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
        public CategoryCreator randomCategory(String nameSuffix)
        {
            return this.withRandomName(nameSuffix);
        }

        @Override
        public MultiCreator.CategoriesCreator categories(String... names)
        {
            return new SerialCategoriesCreator(restClient, categoriesCache).withNames(names).underCategory(parent).asUser(user);
        }

        @Override
        public MultiCreator.CategoriesCreator randomCategories(String... nameSuffixes)
        {
            return new SerialCategoriesCreator(restClient, categoriesCache).withRandomNames(nameSuffixes).underCategory(parent).asUser(user);
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
        public MultiCreator.CategoriesCreator nestedRandomCategories(String... nameSuffixes)
        {
            return new NestedCategoriesCreator(restClient, categoriesCache).withRandomNames(nameSuffixes).underCategory(parent).asUser(user);
        }

        @Override
        public MultiCreator.CategoriesCreator nestedRandomCategories(int depth)
        {
            return new NestedCategoriesCreator(restClient, categoriesCache).withRandomNames(depth).underCategory(parent).asUser(user);
        }

        @Override
        public CategoryCreator withRandomName()
        {
            return withRandomName(EMPTY);
        }

        @Override
        public CategoryCreator withRandomName(String nameSuffix)
        {
            withAlias(nameSuffix);
            return super.withRandomName(CATEGORY_NAME_PREFIX + nameSuffix + "_");
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
            RestCategoryModel category = buildCategoryRestRequest(restClient, user, parent)
                .createSingleCategory(RestCategoryModel.builder().name(name).create());

            categoriesCache.put(alias, category);

            return category;
        }
    }

    public static class SerialCategoriesCreator
        extends MultipleResourcesCreator<RestCategoryModel, MultiCreator.CategoriesCreator>
        implements MultiCreator.CategoriesCreator
    {
        private final RestWrapper restClient;
        protected RestCategoryModel parent;
        private final Map<String, RestCategoryModel> categoriesCache;

        protected SerialCategoriesCreator(RestWrapper restClient, Map<String, RestCategoryModel> categoriesCache)
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

    public static class NestedCategoriesCreator extends SerialCategoriesCreator
    {
        protected NestedCategoriesCreator(RestWrapper restClient, Map<String, RestCategoryModel> categories)
        {
            super(restClient, categories);
        }

        @Override
        public List<RestCategoryModel> create()
        {
            return createNestedCategories(parent, names, 0);
        }

        private List<RestCategoryModel> createNestedCategories(RestCategoryModel parent, List<String> categoryNames, int index)
        {
            List<RestCategoryModel> createdCategories = new ArrayList<>();
            categoryNames.stream().findFirst().ifPresent(categoryName -> {
                RestCategoryModel createdCategory = createCategory(categoryName, getOrNull(aliases, index), parent);
                createdCategories.add(createdCategory);
                List<String> remainingNames = categoryNames.stream().skip(1).toList();
                if (!remainingNames.isEmpty())
                {
                    createdCategories.addAll(createNestedCategories(createdCategory, remainingNames, index + 1));
                }
            });

            return createdCategories;
        }
    }

    public static class RepoCategoryModifier
        extends ResourceModifier<RestCategoryModel, Modifier.CategoryModifier>
        implements Modifier.CategoryModifier
    {
        private final RestWrapper restClient;
        private final RestCategoryModel category;
        private final Map<String, RestCategoryModel> categoriesCache;

        protected RepoCategoryModifier(RestWrapper restClient, RestCategoryModel category, Map<String, RestCategoryModel> categoriesCache)
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
    }
}
