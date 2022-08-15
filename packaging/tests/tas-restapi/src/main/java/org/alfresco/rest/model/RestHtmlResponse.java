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
package org.alfresco.rest.model;

import static org.alfresco.utility.report.log.Step.STEP;

import io.restassured.http.Headers;
import io.restassured.response.ResponseBody;
import org.alfresco.utility.Utility;
import org.testng.Assert;

/**
 * Created by Claudia Agache on 10/13/2016.
 */
@SuppressWarnings("rawtypes")
public class RestHtmlResponse
{
    private Headers headers;
    private ResponseBody body;

    /**
     * @return the {@link ResponseBody}
     */
    public ResponseBody getBody()
    {
        return body;
    }

    public RestHtmlResponse(Headers headers, ResponseBody body)
    {
        this.headers = headers;
        this.body = body;
    }

    public void assertResponseContainsImage()
    {
        STEP("REST API: Assert that response has an image.");
        Utility.checkObjectIsInitialized(headers, "Headers");
        Utility.checkObjectIsInitialized(body, "Body");
        Assert.assertTrue(headers.getValue("content-type").contains("image/png"),
                String.format("Content type is not an image. Actual content type is %s", headers.getValue("content-type")));
        Assert.assertNotEquals(headers.getValue("content-length"), "0", "Content length should be greater than 0 bytes.");
        Assert.assertFalse(body.toString().isEmpty(), "Body should not be empty.");
    }

    /**
     * Assetion that a html element with specific html path has the specified value
     * 
     * @param elementHtmlPath the HTML path of the HTML element
     * @param value value of the field 
     */
    public void assertPathInHtmlBodyEquals(String elementHtmlPath, String value)
    {
        Assert.assertEquals(body.htmlPath().get(elementHtmlPath), value);
    }
}
