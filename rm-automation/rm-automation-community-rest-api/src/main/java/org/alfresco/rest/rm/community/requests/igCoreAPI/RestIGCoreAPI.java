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
package org.alfresco.rest.rm.community.requests.igCoreAPI;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import com.jayway.restassured.RestAssured;

import org.alfresco.rest.core.RMRestProperties;
import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.requests.RMModelRequest;

/**
 * Defines the entire IG Core API
 * {@link http://host:port/ig-api-explorer} select "IG Core API"
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class RestIGCoreAPI extends RMModelRequest
{
    /**
     * Constructor
     *
     * @param rmRestWrapper RM REST Wrapper
     * @param rmRestProperties RM REST Properties
     */
    public RestIGCoreAPI(RMRestWrapper rmRestWrapper, RMRestProperties rmRestProperties)
    {
        super(rmRestWrapper);
        RestAssured.baseURI = format("%s://%s", rmRestProperties.getScheme(), rmRestProperties.getServer());
        RestAssured.port = parseInt(rmRestProperties.getPort());
        RestAssured.basePath = rmRestProperties.getRestRmPath();
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    /**
     * Provides DSL on all REST calls under <code>ig-sites/rm/...</code> API path
     *
     * @return {@link RMSiteAPI}
     */
    public RMSiteAPI usingRMSite()
    {
      return new RMSiteAPI(getRMRestWrapper());
    }

    /**
     * Provides DSL on all REST calls under <code>fileplan-components/...</code> API path
     *
     * @return {@link FilePlanComponentAPI}
     */
    public FilePlanComponentAPI usingFilePlanComponents()
    {
        return new FilePlanComponentAPI(getRMRestWrapper());
    }

    /**
     * Provides DSL on all REST calls under <code>records/...</code> API path
     *
     * @return {@link FilePlanComponentAPI}
     */
    public RecordsAPI usingRecords()
    {
        return new RecordsAPI(getRMRestWrapper());
    }
    
    /**
     * Provides DSL on all REST calls under <code>files/...</code> API path
     *
     * @return {@link FilesAPI}
     */
    public FilesAPI usingFiles()
    {
        return new FilesAPI(getRMRestWrapper());
    }
    
    /**
     * Provides DSL for RM user management API
     *
     * @return {@link RMUserAPI}
     */
    public RMUserAPI usingRMUser()
    {
        return new RMUserAPI(getRMRestWrapper());
    }
}
