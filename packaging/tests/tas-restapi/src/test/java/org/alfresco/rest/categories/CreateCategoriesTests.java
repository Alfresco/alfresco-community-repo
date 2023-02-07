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

package org.alfresco.rest.categories;

import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.rest.model.RestCategoryModelsCollection;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

public class CreateCategoriesTests extends CategoriesRestTest
{

    /**
     * Check we can create a category as direct child of root category
     */
    @Test(groups = {TestGroup.REST_API})
    public void testCreateCategoryUnderRoot()
    {
        STEP("Create a category under root category (as admin)");
        final RestCategoryModel rootCategory = createCategoryModelWithId(ROOT_CATEGORY_ID);
        final RestCategoryModel aCategory = createCategoryModelWithName(getRandomName("Category"));
        final RestCategoryModel createdCategory = restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI()
                .usingCategory(rootCategory)
                .createSingleCategory(aCategory);
        restClient.assertStatusCodeIs(CREATED);

        createdCategory.assertThat().field(FIELD_NAME).is(aCategory.getName());
        createdCategory.assertThat().field(FIELD_PARENT_ID).is(rootCategory.getId());
        createdCategory.assertThat().field(FIELD_HAS_CHILDREN).is(false);
    }

    /**
     * Check we get 400 error when attempting to create a category with empty name
     */
    @Test(groups = {TestGroup.REST_API})
    public void testCreateCategoryWithoutName_andFail()
    {
        STEP("Create a category under root category (as admin)");
        final RestCategoryModel rootCategory = createCategoryModelWithId(ROOT_CATEGORY_ID);
        final RestCategoryModel aCategory = createCategoryModelWithName(StringUtils.EMPTY);
        restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI()
                .usingCategory(rootCategory)
                .createSingleCategory(aCategory);
        restClient.assertStatusCodeIs(BAD_REQUEST).assertLastError().containsSummary("Category name must not be null or empty");
    }

    /**
     * Check we can create several categories as children of a created category
     */
    @Test(groups = {TestGroup.REST_API})
    public void testCreateSeveralSubCategories()
    {
        STEP("Create a category under root category (as admin)");
        final RestCategoryModel rootCategory = createCategoryModelWithId(ROOT_CATEGORY_ID);
        final RestCategoryModel aCategory = createCategoryModelWithName(getRandomName("Category"));
        final RestCategoryModel createdCategory = restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI()
                .usingCategory(rootCategory)
                .createSingleCategory(aCategory);
        restClient.assertStatusCodeIs(CREATED);

        createdCategory.assertThat().field(FIELD_NAME).is(aCategory.getName())
                .assertThat().field(FIELD_PARENT_ID).is(rootCategory.getId())
                .assertThat().field(FIELD_HAS_CHILDREN).is(false)
                .assertThat().field(FIELD_ID).isNotEmpty();

        STEP("Create two categories under the previously created (as admin)");
        final int categoriesNumber = 2;
        final List<RestCategoryModel> categoriesToCreate = getCategoriesToCreate(categoriesNumber);
        final RestCategoryModelsCollection createdSubCategories = restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI()
                .usingCategory(createdCategory)
                .createCategoriesList(categoriesToCreate);
        restClient.assertStatusCodeIs(CREATED);

        createdSubCategories.assertThat()
                .entriesListCountIs(categoriesToCreate.size());
        IntStream.range(0, categoriesNumber)
                .forEach(i -> createdSubCategories.getEntries().get(i).onModel()
                    .assertThat().field(FIELD_NAME).is(categoriesToCreate.get(i).getName())
                    .assertThat().field(FIELD_PARENT_ID).is(createdCategory.getId())
                    .assertThat().field(FIELD_HAS_CHILDREN).is(false)
                    .assertThat().field(FIELD_ID).isNotEmpty()
                );

        STEP("Get the parent category and check if it now has children (as regular user)");
        final RestCategoryModel parentCategoryFromGet = restClient.authenticateUser(user)
                .withCoreAPI()
                .usingCategory(createdCategory)
                .getCategory();

        parentCategoryFromGet.assertThat().field(FIELD_HAS_CHILDREN).is(true);
    }

    /**
     * Check we can create over 100 categories as children of a created category and pagination information is proper.
     */
    @Test(groups = {TestGroup.REST_API})
    public void testCreateOver100SubCategories()
    {
        STEP("Create a category under root category (as admin)");
        final RestCategoryModel rootCategory = createCategoryModelWithId(ROOT_CATEGORY_ID);
        final RestCategoryModel aCategory = createCategoryModelWithName(getRandomName("Category"));
        final RestCategoryModel createdCategory = restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI()
                .usingCategory(rootCategory)
                .createSingleCategory(aCategory);
        restClient.assertStatusCodeIs(CREATED);

        createdCategory.assertThat().field(FIELD_NAME).is(aCategory.getName())
                .assertThat().field(FIELD_PARENT_ID).is(rootCategory.getId())
                .assertThat().field(FIELD_HAS_CHILDREN).is(false)
                .assertThat().field(FIELD_ID).isNotEmpty();

        STEP("Create more than a hundred categories under the previously created (as admin)");
        final int categoriesNumber = 120;
        final List<RestCategoryModel> categoriesToCreate = getCategoriesToCreate(categoriesNumber);
        final RestCategoryModelsCollection createdSubCategories = restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI()
                .usingCategory(createdCategory)
                .createCategoriesList(categoriesToCreate);
        restClient.assertStatusCodeIs(CREATED);

        createdSubCategories.assertThat()
                .entriesListCountIs(categoriesToCreate.size());
        IntStream.range(0, categoriesNumber)
                .forEach(i -> createdSubCategories.getEntries().get(i).onModel()
                        .assertThat().field(FIELD_NAME).is(categoriesToCreate.get(i).getName())
                        .assertThat().field(FIELD_PARENT_ID).is(createdCategory.getId())
                        .assertThat().field(FIELD_HAS_CHILDREN).is(false)
                        .assertThat().field(FIELD_ID).isNotEmpty()
                );
        createdSubCategories.getPagination().assertThat().field("count").is(categoriesNumber)
            .assertThat().field("totalItems").is(categoriesNumber)
            .assertThat().field("maxItems").is(categoriesNumber)
            .assertThat().field("skipCount").is(0)
            .assertThat().field("hasMoreItems").is(false);

    }

    /**
     * Check we cannot create a category as direct child of root category as non-admin user
     */
    @Test(groups = {TestGroup.REST_API})
    public void testCreateCategoryUnderRootAsRegularUser_andFail()
    {
        STEP("Create a category under root category (as user)");
        final RestCategoryModel rootCategory = createCategoryModelWithId(ROOT_CATEGORY_ID);
        final RestCategoryModel aCategory = createCategoryModelWithName(getRandomName("Category"));
        restClient.authenticateUser(user)
                .withCoreAPI()
                .usingCategory(rootCategory)
                .createSingleCategory(aCategory);
        restClient.assertStatusCodeIs(FORBIDDEN).assertLastError().containsSummary("Current user does not have permission to manage a category");
    }

    /**
     * Check we cannot create a category under non existing parent node
     */
    @Test(groups = {TestGroup.REST_API})
    public void testCreateCategoryUnderNonExistingParent_andFail()
    {
        STEP("Create a category under non existing category node (as admin)");
        final String id = "non-existing-node-id";
        final RestCategoryModel rootCategory = createCategoryModelWithId(id);
        final RestCategoryModel aCategory = createCategoryModelWithName(getRandomName("Category"));
        restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI()
                .usingCategory(rootCategory)
                .createSingleCategory(aCategory);
        restClient.assertStatusCodeIs(NOT_FOUND).assertLastError().containsSummary("The entity with id: " + id + " was not found");
    }

    /**
     * Check we cannot create a category under a node which is not a category
     */
    @Test(groups = {TestGroup.REST_API})
    public void testCreateCategoryUnderFolderNode_andFail()
    {
        STEP("Create a site and a folder inside it");
        final SiteModel site = dataSite.usingUser(user).createPublicRandomSite();
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Create a category under folder node (as admin)");
        final RestCategoryModel rootCategory = createCategoryModelWithId(folder.getNodeRef());
        final RestCategoryModel aCategory = createCategoryModelWithName(getRandomName("Category"));
        restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI()
                .usingCategory(rootCategory)
                .createSingleCategory(aCategory);
        restClient.assertStatusCodeIs(BAD_REQUEST).assertLastError().containsSummary("Node id does not refer to a valid category");
    }

    /**
     * Check weather count present in create category request will be ignored.
     */
    @Test(groups = { TestGroup.REST_API })
    public void testCreateCategoryUnderRoot_verifyIfCountInRequestIsIgnored()
    {
        STEP("Try to create a category with filled count under root");
        final RestCategoryModel rootCategory = createCategoryModelWithId(ROOT_CATEGORY_ID);
        final RestCategoryModel aCategory = createCategoryModelWithName(getRandomName("Category"));
        aCategory.setCount(2);
        final RestCategoryModel createdCategory = restClient.authenticateUser(dataUser.getAdminUser())
            .withCoreAPI()
            .usingCategory(rootCategory)
            .include(INCLUDE_COUNT_PARAM)
            .createSingleCategory(aCategory);

        restClient.assertStatusCodeIs(CREATED);
        createdCategory.assertThat().field(FIELD_NAME).is(aCategory.getName());
        createdCategory.assertThat().field(FIELD_COUNT).is(0);
    }

    static List<RestCategoryModel> getCategoriesToCreate(final int count)
    {
        return IntStream.range(0, count)
            .mapToObj(i -> RestCategoryModel.builder().name(getRandomName("SubCategory")).create())
            .collect(Collectors.toList());
    }
}
