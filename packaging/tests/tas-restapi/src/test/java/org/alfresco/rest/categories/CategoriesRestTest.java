/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.rest.categories;

import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.springframework.http.HttpStatus.CREATED;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestCategoryModel;

abstract class CategoriesRestTest extends RestTest
{

    public static final String ROOT_CATEGORY_ID = "-root-";
    public static final String FIELD_NAME = "name";

    protected RestCategoryModel prepareCategoryUnderRoot()
    {
        return prepareCategoryUnder(ROOT_CATEGORY_ID);
    }

    protected RestCategoryModel prepareCategoryUnder(final String parentId)
    {
        final RestCategoryModel parentCategory = createCategoryModelWithId(parentId);
        final RestCategoryModel categoryModel = createCategoryModelWithName(getRandomName("CategoryName"));
        final RestCategoryModel createdCategory = restClient.authenticateUser(dataUser.getAdminUser())
            .withCoreAPI()
            .usingCategory(parentCategory)
            .createSingleCategory(categoryModel);
        restClient.assertStatusCodeIs(CREATED);

        return createdCategory;
    }

    protected RestCategoryModel createCategoryModelWithId(final String id)
    {
        return createCategoryModelWithIdAndName(id, null);
    }

    protected RestCategoryModel createCategoryModelWithName(final String name)
    {
        return createCategoryModelWithIdAndName(null, name);
    }

    protected RestCategoryModel createCategoryModelWithIdAndName(final String id, final String name)
    {
        final RestCategoryModel categoryModel = new RestCategoryModel();
        categoryModel.setName(name);
        categoryModel.setId(id);

        return categoryModel;
    }

    protected RestCategoryModel createCategoryModelOf(final String name, final RestCategoryModel originalCategory)
    {
        final RestCategoryModel categoryModel = createCategoryModelWithName(name);
        categoryModel.setId(originalCategory.getId());
        categoryModel.setParentId(originalCategory.getParentId());
        categoryModel.setHasChildren(originalCategory.getHasChildren());

        return categoryModel;
    }
}
