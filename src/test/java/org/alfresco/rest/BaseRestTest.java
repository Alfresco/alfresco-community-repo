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
package org.alfresco.rest;

import static java.lang.Integer.parseInt;

import static org.alfresco.com.site.RMSiteCompliance.STANDARD;
import static org.alfresco.com.site.RMSiteFields.COMPLIANCE;
import static org.alfresco.com.site.RMSiteFields.DESCRIPTION;
import static org.alfresco.com.site.RMSiteFields.TITLE;
import static org.jglue.fluentjson.JsonBuilderFactory.buildObject;
import static org.springframework.http.HttpStatus.CREATED;

import com.google.gson.JsonObject;
import com.jayway.restassured.RestAssured;

import org.alfresco.dataprep.SiteService;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.requests.RMSiteAPI;
import org.alfresco.utility.data.DataUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;

/**
 * Base class for all IG REST API Tests
 *
 * @author Kristijan Conkas
 * @author Tuna Aksoy
 * @since 1.0
 */
@Configuration
@PropertySource("classpath:config.properties")
@PropertySource(value = "classpath:local.properties", ignoreResourceNotFound = true)
public class BaseRestTest extends RestTest
{
    @Value ("${alfresco.rm.scheme}")
    private String scheme;

    @Value ("${alfresco.rm.host}")
    private String host;

    @Value ("${alfresco.rm.port}")
    private String port;

    @Value ("${alfresco.rm.basePath}")
    private String basePath;

    @Autowired
    public RMSiteAPI rmSiteAPI;

    @Autowired
    public DataUser dataUser;

    private static final String RM_ID = "rm";

    @Autowired
    private SiteService siteService;

    /**
     * @see org.alfresco.rest.RestTest#checkServerHealth()
     */
    @Override
    @BeforeClass (alwaysRun = true)
    public void checkServerHealth() throws Exception
    {
        RestAssured.baseURI = scheme + "://" + host;
        RestAssured.port = parseInt(port);
        RestAssured.basePath = basePath;
        //create RM Site if not exist
        createRMSiteIfNotExists();
    }
    /*
    * Helper method to create the RM Site via the POST request
    * if the site doesn't exist
     */

    public void createRMSiteIfNotExists() throws Exception
    {
        final String RM_TITLE = "Records Management";
        final String RM_DESCRIPTION = "Records Management Site";
        //check RM site doesn't exist
        if (!siteRMExist())
        {
            rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
            // Build the RM site properties
            JsonObject rmSiteProperties = buildObject().
                     add(TITLE, RM_TITLE).
                     add(DESCRIPTION, RM_DESCRIPTION).
                     add(COMPLIANCE, STANDARD.toString()).
                     getJson();
            // Create the RM site
            rmSiteAPI.createRMSite(rmSiteProperties);

            // Verify the status code
            rmSiteAPI.usingRestWrapper().assertStatusCodeIs(CREATED);
        }
    }


    /*
    * Check the RM site exist via the GET request
    *
    */
    public boolean siteRMExist() throws Exception
    { /*
        return siteService.exists(RM_ID, dataUser.getAdminUser().getUsername(),
                                            dataUser.getAdminUser().getPassword());
        */
        RestWrapper restWrapper=rmSiteAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        rmSiteAPI.getSite();
        if (restWrapper.getStatusCode().equals(HttpStatus.OK.toString()))
        {
            return true;
        } else
        {
            return false;
        }
    }
}