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
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.Test;

public class UpdateCategoriesTests extends CategoriesRestTest
{
    private static final String CATEGORY_NEW_NAME_PREFIX = "NewCategoryName";
    private static final String IGNORE_FIELD_NAME = FIELD_NAME;

    /**
     * Update a category (direct child of root category)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testUpdateCategory_asAdmin()
    {
        STEP("Prepare as admin a category under root category");
        final RestCategoryModel createdCategory = prepareCategoryUnderRoot();

        STEP("Update as admin newly created category");
        final String categoryNewName = getRandomName(CATEGORY_NEW_NAME_PREFIX);
        final RestCategoryModel fixedCategoryModel = createCategoryModelWithName(categoryNewName);
        final RestCategoryModel updatedCategory = restClient.authenticateUser(dataUser.getAdminUser())
            .withCoreAPI()
            .usingCategory(createdCategory)
            .updateCategory(fixedCategoryModel);

        restClient.assertStatusCodeIs(OK);
        updatedCategory.assertThat().isEqualTo(createdCategory, IGNORE_FIELD_NAME);
        updatedCategory.assertThat().field(FIELD_NAME).isNot(createdCategory.getName());
        updatedCategory.assertThat().field(FIELD_NAME).is(categoryNewName);
    }

    /**
     * Update a subcategory of root's child category
     */
    @Test(groups = { TestGroup.REST_API})
    public void testUpdateSubcategory_asAdmin()
    {
        STEP("Prepare as admin a category under root category");
        final RestCategoryModel createdCategory = prepareCategoryUnderRoot();

        STEP("Prepare as admin a subcategory of root's child category");
        final RestCategoryModel createdSubcategory = prepareCategoryUnder(createdCategory);

        STEP("Update as admin newly created subcategory");
        final String categoryNewName = getRandomName(CATEGORY_NEW_NAME_PREFIX);
        final RestCategoryModel fixedCategoryModel = createCategoryModelWithName(categoryNewName);
        final RestCategoryModel updatedCategory = restClient.authenticateUser(dataUser.getAdminUser())
            .withCoreAPI()
            .usingCategory(createdSubcategory)
            .updateCategory(fixedCategoryModel);

        restClient.assertStatusCodeIs(OK);
        updatedCategory.assertThat().isEqualTo(createdSubcategory, IGNORE_FIELD_NAME);
        updatedCategory.assertThat().field(FIELD_NAME).is(categoryNewName);
    }

    /**
     * Try to update a category with a name, which is already present within the parent category
     */
    @Test(groups = { TestGroup.REST_API})
    public void testUpdateCategory_usingRecurringName()
    {
        STEP("Prepare as admin two categories under root category");
        final RestCategoryModel createdCategory = prepareCategoryUnderRoot();
        final RestCategoryModel secondCreatedCategory = prepareCategoryUnderRoot();

        STEP("Try to update as admin newly created category using name of already present, different category");
        final String categoryNewName = secondCreatedCategory.getName();
        final RestCategoryModel fixedCategoryModel = createCategoryModelWithName(categoryNewName);
        restClient.authenticateUser(dataUser.getAdminUser())
            .withCoreAPI()
            .usingCategory(createdCategory)
            .updateCategory(fixedCategoryModel);

        restClient.assertStatusCodeIs(CONFLICT);
    }

    /**
     * Try to update a category as a user and expect 403 (Forbidden) in response
     */
    @Test(groups = { TestGroup.REST_API})
    public void testUpdateCategory_asUserAndExpect403()
    {
        STEP("Prepare as admin a category under root category");
        final RestCategoryModel createdCategory = prepareCategoryUnderRoot();

        STEP("Try to update as user newly created category");
        final RestCategoryModel fixedCategoryModel = createCategoryModelWithName(getRandomName(CATEGORY_NEW_NAME_PREFIX));
        restClient.authenticateUser(user)
            .withCoreAPI()
            .usingCategory(createdCategory)
            .updateCategory(fixedCategoryModel);

        restClient.assertStatusCodeIs(FORBIDDEN);
    }

    /**
     * Try to update a non-existing category and receive 404 (Not Found)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testUpdateCategory_usingNonExistingCategoryAndExpect404()
    {
        STEP("Create a fake parent category");
        final RestCategoryModel nonExistingCategory = createCategoryModelWithIdAndName("non-existing-dummy-id", getRandomName(CATEGORY_NAME_PREFIX));

        STEP("Try to update as admin fake category");
        final RestCategoryModel fixedCategoryModel = createCategoryModelWithName(getRandomName(CATEGORY_NEW_NAME_PREFIX));
        restClient.authenticateUser(dataUser.getAdminUser())
            .withCoreAPI()
            .usingCategory(nonExistingCategory)
            .updateCategory(fixedCategoryModel);

        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /**
     * Try to update a non-category (folder) node and receive 400 (Bad Request)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testUpdateCategory_usingFolderNodeAndExpect400()
    {
        STEP("Prepare a site and a folder inside it");
        final UserModel user = dataUser.createRandomTestUser();
        final SiteModel site = dataSite.usingUser(user).createPublicRandomSite();
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        final RestCategoryModel categoryWithFolderId = createCategoryModelWithIdAndName(folder.getNodeRef(), getRandomName(CATEGORY_NAME_PREFIX));

        STEP("Try to update as admin folder node as category");
        final RestCategoryModel fixedCategoryModel = createCategoryModelWithName(getRandomName(CATEGORY_NEW_NAME_PREFIX));
        restClient.authenticateUser(dataUser.getAdminUser())
            .withCoreAPI()
            .usingCategory(categoryWithFolderId)
            .updateCategory(fixedCategoryModel);

        restClient.assertStatusCodeIs(BAD_REQUEST);
    }

    /**
     * Try to update a root category and receive 400 (Bad Request)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testUpdateCategory_usingRootCategoryAndExpect400()
    {
        STEP("Create root category model");
        final RestCategoryModel rootCategoryModel = createCategoryModelWithId(ROOT_CATEGORY_ID);

        STEP("Try to update as admin root category");
        final RestCategoryModel fixedCategoryModel = createCategoryModelWithName(getRandomName(CATEGORY_NEW_NAME_PREFIX));
        restClient.authenticateUser(dataUser.getAdminUser())
            .withCoreAPI()
            .usingCategory(rootCategoryModel)
            .updateCategory(fixedCategoryModel);

        restClient.assertStatusCodeIs(BAD_REQUEST);
    }

    /**
     * Try to update a category with an empty name and receive 400 (Bad Request)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testUpdateCategory_withEmptyNameAndExpect400()
    {
        STEP("Prepare as admin a category under root category");
        final RestCategoryModel createdCategory = prepareCategoryUnderRoot();

        STEP("Try to update as admin newly created category with a category without name");
        final RestCategoryModel fixedCategoryModel = createCategoryModelWithName(null);
        restClient.authenticateUser(dataUser.getAdminUser())
            .withCoreAPI()
            .usingCategory(createdCategory)
            .updateCategory(fixedCategoryModel);

        restClient.assertStatusCodeIs(BAD_REQUEST);
    }

    /**
     * Try to update a category with an invalid, but not important ID in body and receive 200 (OK)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testUpdateCategory_withIgnoredInvalidIdInBodyAndExpect200()
    {
        STEP("Prepare as admin a category under root category");
        final RestCategoryModel createdCategory = prepareCategoryUnderRoot();

        STEP("Try to update as admin newly created category with a category with invalid ID and receive 200");
        final String categoryNewName = getRandomName(CATEGORY_NEW_NAME_PREFIX);
        final RestCategoryModel fixedCategoryModel = createCategoryModelWithIdAndName("non-existing-dummy-id", categoryNewName);
        final RestCategoryModel updatedCategory = restClient.authenticateUser(dataUser.getAdminUser())
            .withCoreAPI()
            .usingCategory(createdCategory)
            .updateCategory(fixedCategoryModel);

        restClient.assertStatusCodeIs(OK);
        updatedCategory.assertThat().field(FIELD_NAME).is(categoryNewName);
    }

    /**
     * Check whether count present in update category request will be ignored.
     */
    @Test(groups = { TestGroup.REST_API })
    public void testUpdateCategory_verifyIfCountInRequestIsIgnored()
    {
        STEP("Prepare a category under root category");
        final RestCategoryModel createdCategory = prepareCategoryUnderRoot();

        STEP("Try to update newly created category providing new name and count number");
        final RestCategoryModel fixedCategoryModel = createCategoryModelWithName(getRandomName(CATEGORY_NEW_NAME_PREFIX));
        fixedCategoryModel.setCount(2);
        final RestCategoryModel updatedCategory = restClient.authenticateUser(dataUser.getAdminUser())
            .withCoreAPI()
            .usingCategory(createdCategory)
            .include(INCLUDE_COUNT_PARAM)
            .updateCategory(fixedCategoryModel);

        restClient.assertStatusCodeIs(OK);
        updatedCategory.assertThat().field(FIELD_ID).is(createdCategory.getId());
        updatedCategory.assertThat().field(FIELD_COUNT).is(0);
    }
}
