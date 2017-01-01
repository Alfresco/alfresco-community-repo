/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
import static org.alfresco.rest.rm.community.base.TestData.DEFAULT_EMAIL;
import static org.alfresco.rest.rm.community.base.TestData.DEFAULT_PASSWORD;
import static org.alfresco.rest.rm.community.model.site.RMSiteCompliance.DOD5015;
import static org.alfresco.rest.rm.community.model.site.RMSiteCompliance.STANDARD;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.social.alfresco.api.entities.Site.Visibility.PUBLIC;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.alfresco.dataprep.UserService;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.rm.community.base.BaseRestTest;
import org.alfresco.rest.rm.community.model.site.RMSite;
import org.alfresco.rest.rm.community.requests.RMSiteAPI;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * This class contains the tests for
 * the RM site CRUD API
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class RMSiteTests extends BaseRestTest
{
    @Autowired
    private UserService userService;

    @Autowired
    private RMSiteAPI rmSiteAPI;

    @Autowired
    private DataUser dataUser;

    /**
     * Given that RM module is installed
     * When I want to create the RM site with specific title, description and compliance
     * Then the RM site is created
     */
    @Test
    (
        description = "Create RM site with Standard Compliance as admin user"
    )
    public void createRMSiteAsAdminUser() throws Exception
    {
        // Authenticate with admin user
        rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Check if the RM site exists
        if (rmSiteAPI.existsRMSite())
        {
            // Delete the RM site
            rmSiteAPI.deleteRMSite();
        }

        // Create the RM site
        RMSite rmSite =RMSite.builder().compliance(STANDARD).build();
        rmSite.setTitle(RM_TITLE);
        rmSite.setDescription(RM_DESCRIPTION);

        RMSite rmSiteResponse = rmSiteAPI.createRMSite(rmSite);

        // Verify the status code
        rmSiteAPI.usingRestWrapper().assertStatusCodeIs(CREATED);

        // Verify the returned file plan component
        assertEquals(rmSiteResponse.getId(), RM_ID);
        assertEquals(rmSiteResponse.getTitle(), RM_TITLE);
        assertEquals(rmSiteResponse.getDescription(), RM_DESCRIPTION);
        assertEquals(rmSiteResponse.getCompliance(), STANDARD);
        assertEquals(rmSiteResponse.getVisibility(), PUBLIC);
        assertEquals(rmSiteResponse.getRole(), UserRole.SiteManager.toString());
    }

    /**
     * Given that RM site exists
     * When I want to  create the RM site
     * Then the response code 409 (Site with the given identifier already exists) is return
     */
    @Test
    (
        description = "Create RM site when site already exist with admin user"
    )
    public void createRMSiteWhenSiteExists() throws Exception
    {
        // Create the RM site if it does not exist
        createRMSiteIfNotExists();

        // Authenticate with admin user
        RestWrapper restWrapper = rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Construct new properties
        String newTitle = RM_TITLE + "createRMSiteWhenSiteExists";
        String newDescription = RM_DESCRIPTION + "createRMSiteWhenSiteExists";

        // Create the RM site
        RMSite rmSite = RMSite.builder().compliance(STANDARD).build();
        rmSite.setTitle(newTitle);
        rmSite.setDescription(newDescription);

        rmSiteAPI.createRMSite(rmSite);

        // Verify the status code
        restWrapper.assertStatusCodeIs(CONFLICT);
    }

    /**
     * Given that RM site exists
     * When I want to delete the RM site
     * Then RM site is successfully deleted
     */
    @Test
    (
        description = "Delete RM site as admin user"
    )
    public void deleteRMSite() throws Exception
    {
        // Authenticate with admin user
        RestWrapper restWrapper = rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Delete the RM site
        rmSiteAPI.deleteRMSite();

        // Verify the status code
        restWrapper.assertStatusCodeIs(NO_CONTENT);
    }

    /**
     * Given that RM site exists
     * When I GET the retrieve the RM site details
     * Then RM site details are returned
     */
    @Test
    (
        description = "GET the RM site as admin user"
    )
    public void getRMSite() throws Exception
    {
        // Authenticate with admin user
        RestWrapper restWrapper = rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Check if RM site exists
        if (!rmSiteAPI.existsRMSite())
        {
            // Verify the status code when RM site  doesn't exist
            restWrapper.assertStatusCodeIs(NOT_FOUND);
            createRMSiteIfNotExists();
        }
        else
        {
            // Get the RM site
            RMSite rmSite = rmSiteAPI.getSite();

            // Verify the status code
            restWrapper.assertStatusCodeIs(OK);
            assertEquals(rmSite.getId(), RM_ID);
            assertEquals(rmSite.getDescription(), RM_DESCRIPTION);
            assertEquals(rmSite.getCompliance(), STANDARD);
            assertEquals(rmSite.getVisibility(), PUBLIC);
        }
    }

    /**
     * Given that an user is created and RM site doesn't exist
     * When the user wants to create a RM site with DOD compliance
     * Then RM site is created
     */
    @Test
    (
        description = "Create RM site with DOD compliance as an another admin user"
    )
    @Bug (id="RM-4289")
    public void createRMSiteAsAnotherAdminUser() throws Exception
    {
        // Authenticate with admin user
        rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Check if the RM site exists
        if (rmSiteAPI.existsRMSite())
        {
            // Delete the RM site
            rmSiteAPI.deleteRMSite();
        }

        // Disconnect the current user from the API session
        rmSiteAPI.usingRestWrapper().disconnect();

        // Create user
        userService.create(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(),
                ANOTHER_ADMIN,
                DEFAULT_PASSWORD,
                DEFAULT_EMAIL,
                ANOTHER_ADMIN,
                ANOTHER_ADMIN);

        // Build the user model
        UserModel userModel = new UserModel(ANOTHER_ADMIN,DEFAULT_PASSWORD);

        // Authenticate as that new user
        rmSiteAPI.usingRestWrapper().authenticateUser(userModel);

        // Create the RM site
        RMSite rmSite = RMSite.builder().compliance(DOD5015).build();
        rmSite.setTitle(RM_TITLE);
        rmSite.setDescription(RM_DESCRIPTION);
        rmSite=rmSiteAPI.createRMSite(rmSite);

        // Verify the status code
        rmSiteAPI.usingRestWrapper().assertStatusCodeIs(CREATED);

        // Verify the returned file plan component
        assertEquals(rmSite.getId(), RM_ID);
        assertEquals(rmSite.getTitle(), RM_TITLE);
        assertEquals(rmSite.getDescription(), RM_DESCRIPTION);
        assertEquals(rmSite.getCompliance(), DOD5015);
        assertEquals(rmSite.getVisibility(), PUBLIC);
        assertEquals(rmSite.getRole(), UserRole.SiteManager.toString());
    }

    /**
     * Given that RM site exist
     * When a non-RM user wants to update the RM site details (title or description)
     * Then 403 response status code is return
     * When the admin user wants to update the RM site details (title or description)
     * Then RM site details are updated
     */
    @Test
    public void updateRMSiteDetails()throws Exception
    {
        String NEW_TITLE = RM_TITLE + RandomData.getRandomAlphanumeric();
        String NEW_DESCRIPTION = RM_DESCRIPTION+ RandomData.getRandomAlphanumeric();

        // Authenticate with admin user
        rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Create the site if it does not exist
        createRMSiteIfNotExists();

        //Create RM site model
        RMSite rmSiteToUpdate = new RMSite();
        rmSiteToUpdate.setTitle(NEW_TITLE);
        rmSiteToUpdate.setDescription(NEW_DESCRIPTION);

        // Disconnect the user from the API session
        rmSiteAPI.usingRestWrapper().disconnect();

        // Create a random user
        UserModel nonRMuser = dataUser.createRandomTestUser("testUser");

        // Authenticate as that random user
        rmSiteAPI.usingRestWrapper().authenticateUser(nonRMuser);

        // Create the RM site
        rmSiteAPI.updateRMSite(rmSiteToUpdate);

        // Verify the status code
        rmSiteAPI.usingRestWrapper().assertStatusCodeIs(FORBIDDEN);

        // Disconnect the user from the API session
        rmSiteAPI.usingRestWrapper().disconnect();

        // Authenticate with admin user
        rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Update the RM Site
        RMSite rmSite = rmSiteAPI.updateRMSite(rmSiteToUpdate);

        // Verify the response status code
        rmSiteAPI.usingRestWrapper().assertStatusCodeIs(OK);

        // Verify the returned file plan component
        assertEquals(rmSite.getId(), RM_ID);
        assertEquals(rmSite.getTitle(), NEW_TITLE);
        assertEquals(rmSite.getDescription(), NEW_DESCRIPTION);
        assertNotNull(rmSite.getCompliance());
        assertEquals(rmSite.getVisibility(), PUBLIC);
    }

    /**
     * Given that RM site exist
     * When the admin user wants to update the RM site compliance
     * Then RM site compliance is not updated
     */
    @Test
    public void updateRMSiteComplianceAsAdmin() throws Exception
    {
        // Authenticate with admin user
        rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Create the RM site if it does not exist
        createRMSiteIfNotExists();

        // Build the RM site properties
        RMSite rmSiteToUpdate =  RMSite.builder().compliance(DOD5015).build();

        // Update the RM site
        rmSiteAPI.updateRMSite(rmSiteToUpdate);

        // Verify the response status code
        rmSiteAPI.usingRestWrapper().assertStatusCodeIs(BAD_REQUEST);
    }
}
