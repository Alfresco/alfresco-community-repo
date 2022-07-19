package org.alfresco.rest.model;

import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.Utility;
import org.testng.Assert;

import io.restassured.http.Headers;
import io.restassured.response.ResponseBody;

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
     * DSL for assertion on this rest model
     * 
     * @return
     */
    public ModelAssertion<RestHtmlResponse> assertThat()
    {
        return new ModelAssertion<RestHtmlResponse>(this);
    }

    public ModelAssertion<RestHtmlResponse> and()
    {
        return assertThat();
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