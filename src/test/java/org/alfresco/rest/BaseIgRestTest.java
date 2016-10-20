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

import com.jayway.restassured.RestAssured;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.testng.annotations.BeforeClass;

/**
 * Base class for all IG REST API Tests
 *
 * @author Kristijan Conkas
 * @author Tuna Aksoy
 * @since 2.6
 */
@Configuration
@PropertySource("classpath:config.properties")
@PropertySource(value = "classpath:local.properties", ignoreResourceNotFound = true)
public class BaseIgRestTest extends RestTest
{
    @Value("${alfresco.rm.scheme}")
    private String scheme;

    @Value("${alfresco.rm.host}")
    private String host;

    @Value("${alfresco.rm.port}")
    private String port;

    @Value("${alfresco.rm.basePath}")
    private String basePath;

    /**
     * @see org.alfresco.rest.RestTest#checkServerHealth()
     */
    @Override
    @BeforeClass(alwaysRun = true)
    public void checkServerHealth() throws Exception
    {
        RestAssured.baseURI = scheme + "://" + host;
        RestAssured.port = parseInt(port);
        RestAssured.basePath = basePath;
    }
}
