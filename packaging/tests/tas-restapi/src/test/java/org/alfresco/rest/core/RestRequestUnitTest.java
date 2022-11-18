/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.core;

import static org.alfresco.rest.core.RestRequest.requestWithBody;
import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.testng.Assert.assertEquals;

import java.util.MissingFormatArgumentException;

import io.restassured.RestAssured;
import org.testng.annotations.Test;

/** Unit tests for {@link RestRequest}. */
public class RestRequestUnitTest
{
    @Test
    public void testSimpleRequest_emptyPathAndParams()
    {
        RestRequest restRequest = simpleRequest(GET, "");

        assertEquals(restRequest.getPath(), "", "Unexpected path");
    }

    @Test
    public void testSimpleRequest_pathWithEqualParamsAndGroups()
    {
        RestRequest restRequest = simpleRequest(GET, "nodes/{nodeId}", "nodeId");

        assertEquals(restRequest.getPath(), "nodes/{nodeId}", "Unexpected path");
    }

    @Test
    public void testSimpleRequest_pathWithMoreParamsThanGroups()
    {
        RestRequest restRequest = simpleRequest(GET, "nodes/{nodeId}", "nodeId", "key1=value1", "key2=value2");

        assertEquals(restRequest.getPath(), "nodes/{nodeId}?{parameter0}&{parameter1}", "Unexpected path");
    }

    @Test(expectedExceptions = MissingFormatArgumentException.class)
    public void testSimpleRequest_pathWithFewerParamsThanGroups()
    {
        simpleRequest(GET, "nodes/{nodeId}");
    }

    @Test
    public void testSetPath()
    {
        RestRequest restRequest = simpleRequest(GET, "nodes/{nodeId}", "nodeId");

        restRequest.setPath("nodes");

        assertEquals(restRequest.getPath(), "nodes?{parameter0}", "Unexpected path");
    }

    @Test
    public void testSetPathParams()
    {
        RestRequest restRequest = simpleRequest(GET, "nodes/{nodeId}", "nodeId");

        Object[] pathParams = {"nodeId", "key=value"};
        restRequest.setPathParams(pathParams);

        assertEquals(restRequest.getPath(), "nodes/{nodeId}?{parameter0}", "Unexpected path");
    }

    @Test
    public void testRequestWithBody_pathWithEqualParamsAndGroups()
    {
        RestRequest restRequest = requestWithBody(POST, "BODY", "nodes/{nodeId}", "nodeId");

        assertEquals(restRequest.getPath(), "nodes/{nodeId}", "Unexpected path");
    }

    @Test
    public void testRequestWithBody_pathWithMoreParamsThanGroups()
    {
        RestRequest restRequest = requestWithBody(GET, "BODY", "nodes/{nodeId}", "nodeId", "key1=value1", "key2=value2");

        assertEquals(restRequest.getPath(), "nodes/{nodeId}?{parameter0}&{parameter1}", "Unexpected path");
    }

    @Test (expectedExceptions = MissingFormatArgumentException.class)
    public void testRequestWithBody_pathWithFewerParamsThanGroups()
    {
        requestWithBody(POST, "BODY", "nodes/{nodeId}");
    }

    @Test
    public void testToString()
    {
        RestAssured.baseURI = "BASE";
        RestAssured.port = 1234;
        RestAssured.basePath = "BASE_PATH";

        RestRequest restRequest = requestWithBody(GET, "BODY", "nodes/{nodeId}", "nodeId", "key1=value1", "key2=value2");

        String expected = "Request: GET BASE:1234/BASE_PATH/nodes/nodeId?key1=value1&key2=value2\nbody:BODY\n";
        assertEquals(restRequest.toString(), expected, "Unexpected toString representation");

        RestAssured.reset();
    }
}
