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
package org.alfresco.rest.ig;

import static com.jayway.restassured.RestAssured.basePath;
import static com.jayway.restassured.RestAssured.baseURI;
import static com.jayway.restassured.RestAssured.port;

import org.alfresco.rest.RestTest;
import org.testng.annotations.BeforeClass;

/**
 * Information Governance specific RestTest
 * @author Kristijan Conkas
 * @since 2.6
 */
public class IgTest extends RestTest
{
    /**
     * @see org.alfresco.rest.RestTest#checkServerHealth()
     */
    @Override
    @BeforeClass(alwaysRun = true)
    public void checkServerHealth() throws Exception
    {
        // TODO: obtain these from property files
        baseURI = "http://192.168.33.10";
        port = 8080;
        basePath = "alfresco/api/-default-/public/ig/versions/1";
    }
}
