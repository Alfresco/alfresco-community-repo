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
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.testng.annotations.Test;

public class DeleteCategoriesTests extends CategoriesRestTest
{

    /**
     * Check we can delete a category.
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteCategory()
    {
        STEP("Create a category and send a request to delete it.");
        RestCategoryModel aCategory = prepareCategoryUnderRoot();
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingCategory(aCategory).deleteCategory();
        restClient.assertStatusCodeIs(NO_CONTENT);

        STEP("Ensure that the category has been deleted by sending a GET request and receiving 404.");
        restClient.authenticateUser(user).withCoreAPI().usingCategory(aCategory).getCategory();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /**
     * Check we get an error when trying to delete a category as a non-admin user.
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteCategoryAsRegularUser_andFail()
    {
        RestCategoryModel aCategory = prepareCategoryUnderRoot();
        restClient.authenticateUser(user).withCoreAPI().usingCategory(aCategory).deleteCategory();
        restClient.assertStatusCodeIs(FORBIDDEN).assertLastError().containsSummary("Current user does not have permission to manage a category");
    }

    /**
     * Check we receive 404 error when trying to delete a category with a non-existent node id.
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteNonExistentCategory()
    {
        STEP("Get category with non-existent id");
        final String id = "non-existing-dummy-id";
        final RestCategoryModel rootCategory = createCategoryModelWithId(id);

        STEP("Attempt to delete category with non-existent id and receive 404");
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingCategory(rootCategory).deleteCategory();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /**
     * Attempt to delete a category when providing a node id that doesn't belong to a category
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteCategory_givenNonCategoryNodeId()
    {
        STEP("Create a site and a folder inside it");
        final SiteModel site = dataSite.usingUser(user).createPublicRandomSite();
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        String id = folder.getNodeRef();

        STEP("Create a category, set its id to the folder id and attempt to delete it");
        final RestCategoryModel aCategory = createCategoryModelWithId(id);
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingCategory(aCategory).deleteCategory();
        restClient.assertStatusCodeIs(BAD_REQUEST).assertLastError().containsSummary("Node id does not refer to a valid category");
    }
}
