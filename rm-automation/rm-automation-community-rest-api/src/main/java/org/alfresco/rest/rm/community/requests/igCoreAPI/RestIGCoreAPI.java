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
package org.alfresco.rest.rm.community.requests.igCoreAPI;

import com.jayway.restassured.RestAssured;

import org.alfresco.rest.core.ExtendedRestProperties;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.requests.ModelRequest;

/**
 * FIXME!!!
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class RestIGCoreAPI extends ModelRequest
{
    private ExtendedRestProperties extendedRestProperties;

    /**
     * FIXME!!!
     *
     * @param restWrapper FIXME!!!
     * @param restProperties FIXME!!!
     */
    public RestIGCoreAPI(RestWrapper restWrapper, ExtendedRestProperties extendedRestProperties)
    {
        super(restWrapper);
        this.extendedRestProperties = extendedRestProperties;
        // FIXME
        RestAssured.baseURI = extendedRestProperties.envProperty().getTestServerUrl();
        RestAssured.port = extendedRestProperties.envProperty().getPort();
        RestAssured.basePath = extendedRestProperties.getRestWorkflowPath();
    }
}
