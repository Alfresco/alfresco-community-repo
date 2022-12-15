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
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;

abstract class CategoriesRestTest extends RestTest
{
    protected static final String ROOT_CATEGORY_ID = "-root-";
    protected static final String CATEGORY_NAME_PREFIX = "CategoryName";
    protected static final String FIELD_NAME = "name";
    protected static final String FIELD_ID = "id";
    protected static final String FIELD_PARENT_ID = "parentId";
    protected static final String FIELD_HAS_CHILDREN = "hasChildren";

    protected UserModel user;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        STEP("Create a user");
        user = dataUser.createRandomTestUser();
    }

    protected RestCategoryModel prepareCategoryUnderRoot()
    {
        return prepareCategoryUnder(ROOT_CATEGORY_ID);
    }

    protected RestCategoryModel prepareCategoryUnder(final String parentId)
    {
        final RestCategoryModel parentCategory = createCategoryModelWithId(parentId);
        final RestCategoryModel categoryModel = createCategoryModelWithName(getRandomName(CATEGORY_NAME_PREFIX));
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
        return RestCategoryModel.builder()
            .id(id)
            .name(name)
            .create();
    }
}
