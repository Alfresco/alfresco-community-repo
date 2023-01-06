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

import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.rest.model.RestCategoryModelsCollection;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.testng.annotations.Test;

public class GetCategoriesTests extends CategoriesRestTest
{
    private static final List<String> DEFAULT_ROOT_CATEGORIES = List.of("Software Document Classification", "Languages", "Regions", "Tags");
    private static final String NON_EXISTING_CATEGORY_ID = "non-existing-category-id";

    /**
     * Check we can get a category which we just created in as direct child of root category
     */
    @Test(groups = {TestGroup.REST_API})
    public void testGetCategoryById()
    {
        STEP("Create a category under root category (as admin)");
        final RestCategoryModel rootCategory = createCategoryModelWithId(ROOT_CATEGORY_ID);
        final RestCategoryModel aCategory = createCategoryModelWithName(RandomData.getRandomName("Category"));
        final RestCategoryModel createdCategory = restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI()
                .usingCategory(rootCategory)
                .createSingleCategory(aCategory);
        restClient.assertStatusCodeIs(CREATED);

        createdCategory.assertThat().field("name").is(aCategory.getName());
        createdCategory.assertThat().field("parentId").is(rootCategory.getId());
        createdCategory.assertThat().field("hasChildren").is(false);

        STEP("Get the created category (as regular user)");
        final RestCategoryModel categoryFromGet =
                restClient.authenticateUser(user).withCoreAPI().usingCategory(createdCategory).getCategory();
        restClient.assertStatusCodeIs(OK);
        categoryFromGet.assertThat().isEqualTo(createdCategory);
    }

    /**
     * Check we get an error when passing -root- as category id
     */
    @Test(groups = {TestGroup.REST_API})
    public void testGetCategoryByIdProvidingRootAsId()
    {
        STEP("Get category with -root- as id");
        final RestCategoryModel rootCategory = createCategoryModelWithId(ROOT_CATEGORY_ID);
        restClient.authenticateUser(user).withCoreAPI().usingCategory(rootCategory).getCategory();
        restClient.assertStatusCodeIs(BAD_REQUEST).assertLastError().containsSummary("Node id does not refer to a valid category");
    }

    /**
     * Check we get an error when passing folder node id as category id
     */
    @Test(groups = {TestGroup.REST_API})
    public void testGetCategoryByIdProvidingFolderAsId()
    {
        STEP("Create a site and a folder inside it");
        final SiteModel site = dataSite.usingUser(user).createPublicRandomSite();
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Get category with folder id passed as id");
        final RestCategoryModel rootCategory = createCategoryModelWithId(folder.getNodeRef());
        restClient.authenticateUser(user).withCoreAPI().usingCategory(rootCategory).getCategory();
        restClient.assertStatusCodeIs(BAD_REQUEST).assertLastError().containsSummary("Node id does not refer to a valid category");
    }

    /**
     * Check we get an error when passing non existing as category id
     */
    @Test(groups = {TestGroup.REST_API})
    public void testGetCategoryByIdProvidingNonExistingId()
    {
        STEP("Get category with id which does not exist");
        final String id = NON_EXISTING_CATEGORY_ID;
        final RestCategoryModel rootCategory = createCategoryModelWithId(id);
        restClient.authenticateUser(user).withCoreAPI().usingCategory(rootCategory).getCategory();
        restClient.assertStatusCodeIs(NOT_FOUND).assertLastError().containsSummary(id);
    }

    /**
     * Check we can get children category of a root category
     */
    @Test(groups = {TestGroup.REST_API})
    public void testGetCategoryChildren()
    {
        STEP("Get category children with -root- as parent id");
        final RestCategoryModel rootCategory = createCategoryModelWithId(ROOT_CATEGORY_ID);
        RestCategoryModelsCollection childCategoriesList =
                restClient.authenticateUser(user).withCoreAPI().usingCategory(rootCategory).getCategoryChildren();
        restClient.assertStatusCodeIs(OK);

        childCategoriesList.assertThat().entriesListIsNotEmpty();
        assertTrue(childCategoriesList.getEntries().stream()
                .map(RestCategoryModel::onModel)
                .map(RestCategoryModel::getName)
                .collect(Collectors.toList())
                .containsAll(DEFAULT_ROOT_CATEGORIES));
        STEP("Create a new category under root and make sure it is returned as one of root's children");
        final RestCategoryModel aCategory = createCategoryModelWithName(RandomData.getRandomName("newCategoryUnderRoot"));
        final RestCategoryModel createdCategory = restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI()
                .usingCategory(rootCategory)
                .createSingleCategory(aCategory);
        restClient.assertStatusCodeIs(CREATED);

        childCategoriesList = restClient.authenticateUser(user).withCoreAPI().usingCategory(rootCategory).getCategoryChildren();
        restClient.assertStatusCodeIs(OK);
        assertTrue(childCategoriesList.getEntries().stream()
                .map(RestCategoryModel::onModel)
                .map(RestCategoryModel::getId)
                .collect(Collectors.toList())
                .contains(createdCategory.getId()));

        STEP("Create 2 more categories under newCategoryUnderRoot and make sure they are returned as children");
        final int categoriesCount = 2;
        final List<RestCategoryModel> categoriesToCreate = CreateCategoriesTests.getCategoriesToCreate(categoriesCount);
        final RestCategoryModelsCollection createdSubCategories = restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI()
                .usingCategory(createdCategory)
                .createCategoriesList(categoriesToCreate);
        restClient.assertStatusCodeIs(CREATED);
        childCategoriesList = restClient.authenticateUser(user).withCoreAPI().usingCategory(rootCategory).getCategoryChildren();
        restClient.assertStatusCodeIs(OK);
        childCategoriesList.getEntries().containsAll(createdSubCategories.getEntries());
    }

    /**
     * Check we get an error when passing folder node id as parent category id when getting children.
     */
    @Test(groups = {TestGroup.REST_API})
    public void testGetCategoryChildrenProvidingFolderAsId()
    {
        STEP("Create a site and a folder inside it");
        final SiteModel site = dataSite.usingUser(user).createPublicRandomSite();
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Get category children with folder id passed as parent id");
        final RestCategoryModel parentCategory = createCategoryModelWithId(folder.getNodeRef());
        restClient.authenticateUser(user).withCoreAPI().usingCategory(parentCategory).getCategoryChildren();
        restClient.assertStatusCodeIs(BAD_REQUEST).assertLastError().containsSummary("Node id does not refer to a valid category");
    }

    /**
     * Check we get an error when passing a non-existent node id as parent category id when getting children.
     */
    @Test(groups = {TestGroup.REST_API})
    public void testGetCategoryChildrenProvidingNonExistingParent()
    {

        STEP("Get category with folder id passed as id");
        final String parentId = NON_EXISTING_CATEGORY_ID;
        final RestCategoryModel parentCategory = createCategoryModelWithId(parentId);
        restClient.authenticateUser(user).withCoreAPI().usingCategory(parentCategory).getCategoryChildren();
        restClient.assertStatusCodeIs(NOT_FOUND).assertLastError().containsSummary(parentId);
    }
}
