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
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertTrue;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.rest.model.RestCategoryModelsCollection;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CategoriesCountTests extends CategoriesRestTest
{

    private RestCategoryModel categoryLinkedWithFolder;
    private RestCategoryModel categoryLinkedWithFile;
    private RestCategoryModel categoryLinkedWithBoth;
    private RestCategoryModel notLinkedCategory;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        STEP("Create user and site");
        user = dataUser.createRandomTestUser();
        SiteModel site = dataSite.usingUser(user).createPublicRandomSite();

        STEP("Create a folder, file in it and few categories");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        FileModel file = dataContent.usingUser(user).usingResource(folder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        categoryLinkedWithFolder = prepareCategoryUnderRoot();
        categoryLinkedWithFile = prepareCategoryUnderRoot();
        categoryLinkedWithBoth = prepareCategoryUnder(prepareCategoryUnderRoot());
        notLinkedCategory = prepareCategoryUnderRoot();

        STEP("Link folder and file to categories");
        linkContentToCategories(folder, categoryLinkedWithFolder, categoryLinkedWithBoth);
        linkContentToCategories(file, categoryLinkedWithFile, categoryLinkedWithBoth);

        STEP("Wait for indexing to complete");
        Utility.sleep(1000, 60000, () -> restClient.authenticateUser(user)
            .withCoreAPI()
            .usingCategory(categoryLinkedWithFolder)
            .include(INCLUDE_COUNT_PARAM)
            .getCategory()
            .assertThat()
            .field(FIELD_COUNT)
            .isNot(0));
    }

    /**
     * Verify count for a category linked with file and folder.
     */
    @Test(groups = { TestGroup.REST_API })
    public void testGetCategoryById_includeCount()
    {
        STEP("Get linked category and verify if count is higher than 0");
        final RestCategoryModel actualCategory = restClient.authenticateUser(user)
            .withCoreAPI()
            .usingCategory(categoryLinkedWithBoth)
            .include(INCLUDE_COUNT_PARAM)
            .getCategory();

        restClient.assertStatusCodeIs(OK);
        actualCategory.assertThat().field(FIELD_ID).is(categoryLinkedWithBoth.getId());
        actualCategory.assertThat().field(FIELD_COUNT).is(2);
    }

    /**
     * Verify count for a category not linked with any content.
     */
    @Test(groups = { TestGroup.REST_API })
    public void testGetCategoryById_includeCountForNonLinkedCategory()
    {
        STEP("Get non-linked category and verify if count is 0");
        final RestCategoryModel actualCategory = restClient.authenticateUser(user)
            .withCoreAPI()
            .usingCategory(notLinkedCategory)
            .include(INCLUDE_COUNT_PARAM)
            .getCategory();

        restClient.assertStatusCodeIs(OK);
        actualCategory.assertThat().field(FIELD_ID).is(notLinkedCategory.getId());
        actualCategory.assertThat().field(FIELD_COUNT).is(0);
    }

    /**
     * Verify count for three categories: linked with file, linked with folder and third not linked to any content.
     */
    @Test(groups = { TestGroup.REST_API })
    public void testGetCategories_includeCount()
    {
        STEP("Get few categories and verify its counts");
        final RestCategoryModel parentCategory = createCategoryModelWithId(ROOT_CATEGORY_ID);
        final RestCategoryModelsCollection actualCategories = restClient.authenticateUser(user)
            .withCoreAPI()
            .usingCategory(parentCategory)
            .include(INCLUDE_COUNT_PARAM)
            .getCategoryChildren();

        restClient.assertStatusCodeIs(OK);
        assertTrue(actualCategories.getEntries().stream()
            .map(RestCategoryModel::onModel)
            .anyMatch(category -> category.getId().equals(categoryLinkedWithFolder.getId()) && category.getCount() == 1));
        assertTrue(actualCategories.getEntries().stream()
            .map(RestCategoryModel::onModel)
            .anyMatch(category -> category.getId().equals(categoryLinkedWithFile.getId()) && category.getCount() == 1));
        assertTrue(actualCategories.getEntries().stream()
            .map(RestCategoryModel::onModel)
            .anyMatch(category -> category.getId().equals(notLinkedCategory.getId()) && category.getCount() == 0));
    }

    /**
     * Create category and verify that its count is 0.
     */
    @Test(groups = { TestGroup.REST_API })
    public void testCreateCategory_includingCount()
    {
        STEP("Create a category under root and verify if count is 0");
        final String categoryName = getRandomName("Category");
        final RestCategoryModel rootCategory = createCategoryModelWithId(ROOT_CATEGORY_ID);
        final RestCategoryModel aCategory = createCategoryModelWithName(categoryName);
        final RestCategoryModel createdCategory = restClient.authenticateUser(dataUser.getAdminUser())
            .withCoreAPI()
            .include(INCLUDE_COUNT_PARAM)
            .usingCategory(rootCategory)
            .createSingleCategory(aCategory);

        STEP("Create a category under root category (as admin)");
        restClient.assertStatusCodeIs(CREATED);
        createdCategory.assertThat().field(FIELD_NAME).is(categoryName);
        createdCategory.assertThat().field(FIELD_COUNT).is(0);
    }

    /**
     * Update category linked to file and folder and verify that its count is 2.
     */
    @Test(groups = { TestGroup.REST_API })
    public void testUpdateCategory_includeCount()
    {
        STEP("Update linked category and verify if count is higher than 0");
        final String categoryNewName = getRandomName("NewCategoryName");
        final RestCategoryModel fixedCategoryModel = createCategoryModelWithName(categoryNewName);
        final RestCategoryModel updatedCategory = restClient.authenticateUser(dataUser.getAdminUser())
            .withCoreAPI()
            .usingCategory(categoryLinkedWithBoth)
            .include(INCLUDE_COUNT_PARAM)
            .updateCategory(fixedCategoryModel);

        restClient.assertStatusCodeIs(OK);
        updatedCategory.assertThat().field(FIELD_ID).is(categoryLinkedWithBoth.getId());
        updatedCategory.assertThat().field(FIELD_COUNT).is(2);
    }

    /**
     * Update category not linked to any content and verify that its count is 0.
     */
    @Test(groups = { TestGroup.REST_API })
    public void testUpdateCategory_includeCountForNonLinkedCategory()
    {
        STEP("Update non-linked category and verify if count is 0");
        final String categoryNewName = getRandomName("NewCategoryName");
        final RestCategoryModel fixedCategoryModel = createCategoryModelWithName(categoryNewName);
        final RestCategoryModel updatedCategory = restClient.authenticateUser(dataUser.getAdminUser())
            .withCoreAPI()
            .usingCategory(notLinkedCategory)
            .include(INCLUDE_COUNT_PARAM)
            .updateCategory(fixedCategoryModel);

        restClient.assertStatusCodeIs(OK);
        updatedCategory.assertThat().field(FIELD_ID).is(notLinkedCategory.getId());
        updatedCategory.assertThat().field(FIELD_COUNT).is(0);
    }
}
