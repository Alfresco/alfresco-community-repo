/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.site;

import static org.alfresco.rest.rm.community.base.TestData.ANOTHER_ADMIN;
import static org.alfresco.rest.rm.community.base.TestData.DEFAULT_PASSWORD;
import static org.alfresco.rest.rm.community.model.site.RMSiteCompliance.DOD5015;
import static org.alfresco.rest.rm.community.model.site.RMSiteCompliance.STANDARD;
import static org.alfresco.rest.rm.community.utils.RMSiteUtil.RM_DESCRIPTION;
import static org.alfresco.rest.rm.community.utils.RMSiteUtil.RM_ID;
import static org.alfresco.rest.rm.community.utils.RMSiteUtil.RM_TITLE;
import static org.alfresco.rest.rm.community.utils.RMSiteUtil.createDOD5015RMSiteModel;
import static org.alfresco.rest.rm.community.utils.RMSiteUtil.createRMSiteModel;
import static org.alfresco.rest.rm.community.utils.RMSiteUtil.createStandardRMSiteModel;
import static org.alfresco.utility.constants.UserRole.SiteManager;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.alfresco.dataprep.SiteService.Visibility;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.base.TestData;
import org.alfresco.rest.rm.community.model.site.RMSite;
import org.alfresco.rest.rm.community.requests.gscore.api.RMSiteAPI;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.testng.annotations.Test;

/**
 * This class contains the tests for
 * the RM site CRUD API
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class RMSiteTests extends BaseRMRestTest
{
    /**
     * Given that RM module is installed
     * When I want to create the RM site with specific title, description and compliance
     * Then the RM site is created
     */
    @Test (description = "Create RM site with Standard Compliance as admin user", priority = 2)
    // Run after createRMSiteAsAnotherAdminUser. In this way the Dod site is deleted and standard site is created for the rest of the tests
    public void createRMSiteAsAdminUser()
    {
        RMSiteAPI rmSiteAPI = getRestAPIFactory().getRMSiteAPI();

        // Check if the RM site exists
        if (rmSiteAPI.existsRMSite())
        {
            // Delete the RM site
            rmSiteAPI.deleteRMSite();
        }

        // Create the RM site
        RMSite rmSiteResponse = rmSiteAPI.createRMSite(createStandardRMSiteModel());

        // Verify the status code
        assertStatusCode(CREATED);

        // Verify the returned file plan component
        assertEquals(rmSiteResponse.getId(), RM_ID);
        assertEquals(rmSiteResponse.getTitle(), RM_TITLE);
        assertEquals(rmSiteResponse.getDescription(), RM_DESCRIPTION);
        assertEquals(rmSiteResponse.getCompliance(), STANDARD);
        assertEquals(rmSiteResponse.getVisibility(), Visibility.PUBLIC);
        assertEquals(rmSiteResponse.getRole(), SiteManager.toString());
    }

    /**
     * Given that RM site exists
     * When I want to  create the RM site
     * Then the response code 409 (Site with the given identifier already exists) is return
     */
    @Test (description = "Create RM site when site already exist with admin user", priority = 3)
    // Run test after the other tests with priority 0, 1 or 2
    public void createRMSiteWhenSiteExists()
    {
        // Create the RM site if it does not exist
        createRMSiteIfNotExists();

        // Construct new properties
        String newTitle = RM_TITLE + "createRMSiteWhenSiteExists";
        String newDescription = RM_DESCRIPTION + "createRMSiteWhenSiteExists";

        // Create the RM site
        RMSite rmSiteModel = createRMSiteModel(STANDARD, newTitle, newDescription);
        getRestAPIFactory().getRMSiteAPI().createRMSite(rmSiteModel);

        // Verify the status code
        assertStatusCode(CONFLICT);
    }

    /**
     * Given that RM site exists
     * When I want to delete the RM site
     * Then RM site is successfully deleted
     */
    @Test (description = "Delete RM site as admin user")
    public void deleteRMSite()
    {
        // Create the RM site if it does not exist
        createRMSiteIfNotExists();

        // Delete the RM site
        getRestAPIFactory().getRMSiteAPI().deleteRMSite();

        // Verify the status code
        assertStatusCode(NO_CONTENT);
    }

    /**
     * Given that RM site exists
     * When I GET the retrieve the RM site details
     * Then RM site details are returned
     */
    @Test (description = "GET the RM site as admin user", priority = 3)
    // Run test after the tests with priority 0, 1 or 2
    public void getRMSite()
    {
        RMSiteAPI rmSiteAPI = getRestAPIFactory().getRMSiteAPI();

        // Check if RM site exists
        if (!rmSiteAPI.existsRMSite())
        {
            // Verify the status code when RM site  doesn't exist
            assertStatusCode(NOT_FOUND);
            createRMSiteIfNotExists();
        }
        else
        {
            // Get the RM site
            RMSite rmSiteModel = rmSiteAPI.getSite();

            // Verify the status code
            assertStatusCode(OK);
            assertEquals(rmSiteModel.getId(), RM_ID);
            assertEquals(rmSiteModel.getDescription(), RM_DESCRIPTION);
            assertEquals(rmSiteModel.getCompliance(), STANDARD);
            assertEquals(rmSiteModel.getVisibility(), Visibility.PUBLIC);
        }
    }

    /**
     * Given that an user is created and RM site doesn't exist
     * When the user wants to create a RM site with DOD compliance
     * Then RM site is created
     */
    // Run test after deleteRMSite. In this way rmSiteAPI.deleteRMSite isn't called because site is already deleted
    @Test (description = "Create RM site with DOD compliance as an another admin user", priority = 1)
    @Bug (id="RM-4289")
    public void createRMSiteAsAnotherAdminUser()
    {
        RMSiteAPI rmSiteAPI = getRestAPIFactory().getRMSiteAPI();

        // Check if the RM site exists
        if (rmSiteAPI.existsRMSite())
        {
            // Delete the RM site
            rmSiteAPI.deleteRMSite();
        }

        // Create user
        getRestAPIFactory().getRMUserAPI().createUser(ANOTHER_ADMIN, TestData.DEFAULT_PASSWORD, TestData.DEFAULT_EMAIL);

        // Create the RM site
        RMSite rmSiteModel = getRestAPIFactory().getRMSiteAPI(new UserModel(ANOTHER_ADMIN, DEFAULT_PASSWORD)).createRMSite(createDOD5015RMSiteModel());

        // Verify the status code
        assertStatusCode(CREATED);

        // Verify the returned file plan component
        assertEquals(rmSiteModel.getId(), RM_ID);
        assertEquals(rmSiteModel.getTitle(), RM_TITLE);
        assertEquals(rmSiteModel.getDescription(), RM_DESCRIPTION);
        assertEquals(rmSiteModel.getCompliance(), DOD5015);
        assertEquals(rmSiteModel.getVisibility(), Visibility.PUBLIC);
        assertEquals(rmSiteModel.getRole(), SiteManager.toString());
    }

    /**
     * Given that RM site exist
     * When a non-RM user wants to update the RM site details (title or description)
     * Then 403 response status code is return
     * When the admin user wants to update the RM site details (title or description)
     * Then RM site details are updated
     */
    @Test(priority = 3) // Run test after the other tests with priority 0, 1 or 2
    public void updateRMSiteDetails()
    {
        String NEW_TITLE = RM_TITLE + RandomData.getRandomAlphanumeric();
        String NEW_DESCRIPTION = RM_DESCRIPTION + RandomData.getRandomAlphanumeric();

        // Create the site if it does not exist
        createRMSiteIfNotExists();

        // Create RM site model
        RMSite rmSiteToUpdate = RMSite.builder().title(NEW_TITLE).description(NEW_DESCRIPTION).build();

        // Create the RM site
        getRestAPIFactory().getRMSiteAPI(getDataUser().createRandomTestUser("testUser")).updateRMSite(rmSiteToUpdate);

        // Verify the status code
        assertStatusCode(FORBIDDEN);

        // Update the RM Site
        RMSite rmSiteModel = getRestAPIFactory().getRMSiteAPI().updateRMSite(rmSiteToUpdate);

        // Verify the response status code
        assertStatusCode(OK);

        // Verify the returned file plan component
        assertEquals(rmSiteModel.getId(), RM_ID);
        assertEquals(rmSiteModel.getTitle(), NEW_TITLE);
        assertEquals(rmSiteModel.getDescription(), NEW_DESCRIPTION);
        assertNotNull(rmSiteModel.getCompliance());
        assertEquals(rmSiteModel.getVisibility(), Visibility.PUBLIC);
    }

    /**
     * Given that RM site exist
     * When the admin user wants to update the RM site compliance
     * Then RM site compliance is not updated
     */
    @Test(priority = 3) // Run test after the other tests with priority 0, 1 or 2
    public void updateRMSiteComplianceAsAdmin()
    {
        // Create the RM site if it does not exist
        createRMSiteIfNotExists();

        // Build the RM site properties
        RMSite rmSiteToUpdate = RMSite.builder().compliance(DOD5015).build();

        // Update the RM site
        getRestAPIFactory().getRMSiteAPI().updateRMSite(rmSiteToUpdate);

        // Verify the response status code
        assertStatusCode(BAD_REQUEST);
    }
}
