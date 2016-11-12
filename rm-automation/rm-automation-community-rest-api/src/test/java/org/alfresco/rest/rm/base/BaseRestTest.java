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
package org.alfresco.rest.rm.base;

import static java.lang.Integer.parseInt;

import static org.alfresco.rest.rm.model.site.RMSiteCompliance.STANDARD;
import static org.alfresco.rest.rm.model.site.RMSiteFields.COMPLIANCE;
import static org.alfresco.rest.rm.model.site.RMSiteFields.DESCRIPTION;
import static org.alfresco.rest.rm.model.site.RMSiteFields.TITLE;
import static org.jglue.fluentjson.JsonBuilderFactory.buildObject;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.google.gson.JsonObject;
import com.jayway.restassured.RestAssured;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.rm.requests.RMSiteAPI;
import org.alfresco.utility.data.DataUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.testng.annotations.BeforeClass;

/**
 * Base class for all IG REST API Tests
 *
 * @author Kristijan Conkas
 * @author Tuna Aksoy
 * @since 1.0
 */
@Configuration
@PropertySource("classpath:default.properties")
@PropertySource(value = "classpath:local.properties", ignoreResourceNotFound = true)
public class BaseRestTest extends RestTest
{
    @Value ("${alfresco.scheme}")
    private String scheme;

    @Value ("${alfresco.server}")
    private String server;

    @Value ("${alfresco.port}")
    private String port;

    @Value ("${rest.rmPath}")
    private String restRmPath;

    @Autowired
    private RMSiteAPI rmSiteAPI;

    @Autowired
    private DataUser dataUser;

    // Constants
    public static final String RM_ID = "rm";
    public static final String RM_TITLE = "Records Management";
    public static final String RM_DESCRIPTION = "Records Management Site";

    /**
     * @see org.alfresco.rest.RestTest#checkServerHealth()
     */
    @Override
    @BeforeClass(alwaysRun = true)
    public void checkServerHealth() throws Exception
    {
        RestAssured.baseURI = scheme + "://" + server;
        RestAssured.port = parseInt(port);
        RestAssured.basePath = restRmPath;

        // Create RM Site if not exist
        createRMSiteIfNotExists();
    }

    /**
     * Helper method to create the RM Site via the POST request
     * if the site doesn't exist
     */
    public void createRMSiteIfNotExists() throws Exception
    {
        // Check RM site doesn't exist
        if (!siteRMExists())
        {
            rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

            // Build the RM site properties
            JsonObject rmSiteProperties = buildObject()
                    .add(TITLE, RM_TITLE)
                    .add(DESCRIPTION, RM_DESCRIPTION)
                    .add(COMPLIANCE, STANDARD.toString())
                    .getJson();

            // Create the RM site
            rmSiteAPI.createRMSite(rmSiteProperties);

            // Verify the status code
            rmSiteAPI.usingRestWrapper().assertStatusCodeIs(CREATED);
        }
    }

    /**
     * Check if the RM site exists via the GET request
     */
    public boolean siteRMExists() throws Exception
    {
        RestWrapper restWrapper = rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        rmSiteAPI.getSite();
        return restWrapper.getStatusCode().equals(OK.toString());
    }
}