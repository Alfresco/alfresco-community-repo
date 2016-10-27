/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.fileplancomponents;

import static org.alfresco.com.site.RMSiteCompliance.STANDARD;
import static org.alfresco.com.site.RMSiteFields.COMPLIANCE;
import static org.alfresco.com.site.RMSiteFields.DESCRIPTION;
import static org.alfresco.com.site.RMSiteFields.TITLE;
import static org.jglue.fluentjson.JsonBuilderFactory.buildObject;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.social.alfresco.api.entities.Site.Visibility.PUBLIC;
import static org.testng.Assert.assertEquals;

import com.google.gson.JsonObject;

import org.alfresco.rest.BaseRestTest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.site.RMSite;
import org.testng.annotations.Test;

/**
 * FIXME: Document me :)
 * FIXME: Should we use dependent tests or not?
 * They were removed here but there is no guarantee for the test execution order.
 * In {@link RecordCategoryTest} we create a record category first to delete it.
 * Probably something to think about again.
 *
 * @author Rodica Sutu
 * @since 1.0
 */
public class RMSiteTests extends BaseRestTest
{
    @Test
    (
            description = "Create RM site as admin user with standard Compliance"
    )
    public void createRMSiteAsAdminUser() throws Exception
    {
        RestWrapper restWrapper = rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        if (siteRMExist())
        {
            // Delete the RM site
            rmSiteAPI.deleteRMSite();
        }
        // Build the RM site properties
        JsonObject rmSiteProperties = buildObject().
                add(TITLE, RM_TITLE).
                add(DESCRIPTION, RM_DESCRIPTION).
                add(COMPLIANCE, STANDARD.toString()).
                getJson();

        // Create the RM site
        RMSite rmSite = rmSiteAPI.createRMSite(rmSiteProperties);

        // Verify the status code
        restWrapper.assertStatusCodeIs(CREATED);

        // Verify the returned file plan component
        assertEquals(rmSite.getId(), RM_ID);
        assertEquals(rmSite.getTitle(), RM_TITLE);
        assertEquals(rmSite.getDescription(), RM_DESCRIPTION);
        assertEquals(rmSite.getCompliance(), STANDARD);
        assertEquals(rmSite.getVisibility(), PUBLIC);
    }

    @Test
    (
            description = "Create RM site when site already exist with admin user"
    )
    public void createRMSiteWhenSiteExists() throws Exception
    {
        rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        createRMSiteIfNotExists();
        RestWrapper restWrapper = rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Construct new properties
        String newTitle = RM_TITLE + "createRMSiteWhenSiteExists";
        String newDescription = RM_DESCRIPTION + "createRMSiteWhenSiteExists";

        // Build the RM site properties
        JsonObject rmSiteProperties = buildObject().
                add(TITLE, newTitle).
                add(DESCRIPTION, newDescription).
                add(COMPLIANCE, STANDARD.toString()).
                getJson();

        // Create the RM site
        rmSiteAPI.createRMSite(rmSiteProperties);

        // Verify the status code
        restWrapper.assertStatusCodeIs(CONFLICT);
    }

    @Test
    (
            description = "Delete RM site as admin user"
    )
    public void deleteRMSite() throws Exception
    {
        RestWrapper restWrapper = rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Delete the RM site
        rmSiteAPI.deleteRMSite();

        // Verify the status code
        restWrapper.assertStatusCodeIs(NO_CONTENT);
    }

    @Test
    (
            description = "GET RM site as admin user"
    )
    public void getRMSite() throws Exception
    {
        RestWrapper restWrapper = rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Get the RM site
        RMSite rmSite = rmSiteAPI.getSite();

        if (!siteRMExist())
        {
            // Verify the status code
            restWrapper.assertStatusCodeIs(NOT_FOUND);
        }
        else
        {
            // Verify the status code
            restWrapper.assertStatusCodeIs(OK);
            assertEquals(rmSite.getId(), RM_ID);
            assertEquals(rmSite.getDescription(), RM_DESCRIPTION);
            assertEquals(rmSite.getCompliance(), STANDARD);
            assertEquals(rmSite.getVisibility(), PUBLIC);
        }
    }
}
