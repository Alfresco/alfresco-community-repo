/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
import org.alfresco.rest.rm.community.requests.FilePlanComponents;
import org.alfresco.rest.rm.community.requests.RMModelRequest;
import org.alfresco.rest.rm.community.requests.RMSite;

/**
 * Defines the entire IG Core API
 * {@link http://host:port/ig-api-explorer} select "IG Core API"
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class RestIGCoreAPI extends RMModelRequest
{
    @SuppressWarnings("unused")
    private RMRestProperties rmRestProperties;

    /**
     * FIXME!!!
     *
     * @param rmRestWrapper FIXME!!!
     * @param rmRestProperties FIXME!!!
     */
    public RestIGCoreAPI(RMRestWrapper rmRestWrapper, RMRestProperties rmRestProperties)
    {
        super(rmRestWrapper);
        this.rmRestProperties = rmRestProperties;
        RestAssured.baseURI = format("%s://%s", rmRestProperties.getScheme(), rmRestProperties.getServer());
        RestAssured.port = parseInt(rmRestProperties.getPort());
        RestAssured.basePath = rmRestProperties.getRestRmPath();
    }

    /**
     * Provides DSL on all REST calls under <code>ig-sites/rm/...</code> API path
     *
     * @return {@link RMSite}
     */
    public RMSite usingRMSite()
    {
      return new RMSite(getRMRestWrapper());
    }

    /**
     * FIXME!!!
     *
     * @return FIXME!!!
     */
    public FilePlanComponents usingFilePlanComponents()
    {
        return new FilePlanComponents(getRMRestWrapper());
    }
}
