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
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetCategoriesTests extends RestTest
{
    private UserModel user;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        STEP("Create a user");
        user = dataUser.createRandomTestUser();
    }

    /**
     * Check we can get a category which we just created in as direct child of root category
     */
    @Test(groups = {TestGroup.REST_API})
    public void testGetCategoryById()
    {
        STEP("Get category with -root- as id (which does not exist)");
        final RestCategoryModel rootCategory = new RestCategoryModel();
        rootCategory.setId("-root-");
        restClient.authenticateUser(user).withCoreAPI().usingCategory(rootCategory).getCategory();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /**
     * Check we get an error when passing -root- as category id
     */
    @Test(groups = {TestGroup.REST_API})
    public void testGetCategoryByIdProvidingRootAsId()
    {
        STEP("Get category with -root- as id (which does not exist)");
        final RestCategoryModel rootCategory = new RestCategoryModel();
        rootCategory.setId("-root-");
        restClient.authenticateUser(user).withCoreAPI().usingCategory(rootCategory).getCategory();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /**
     * Check we get an error when passing  as category id
     */
    @Test(groups = {TestGroup.REST_API})
    public void testGetCategoryByIdProvidingFolderAsId()
    {
        STEP("Create a site and a folder inside it");
        final SiteModel site = dataSite.usingUser(user).createPublicRandomSite();
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Get category with folder id passed as id");
        final RestCategoryModel rootCategory = new RestCategoryModel();
        rootCategory.setId(folder.getNodeRef());
        restClient.authenticateUser(user).withCoreAPI().usingCategory(rootCategory).getCategory();
        restClient.assertStatusCodeIs(BAD_REQUEST);
    }

}
