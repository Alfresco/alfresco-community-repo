package org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript;

import java.io.IOException;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * REST API Test for Email mapping keys
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class EmailMapKeysRestApiTest extends BaseRMWebScriptTestCase
{
    /** URLs for the REST API */
    private static final String GET_EMAIL_MAP_KEYS_URL = "/api/rma/admin/emailmapkeys";

    /**
     * Tests the REST API to get the list of email mapping keys
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testGetCapabilitiesAction() throws IOException, JSONException
    {
        // Send request
        Response response = sendRequest(new GetRequest(GET_EMAIL_MAP_KEYS_URL), Status.STATUS_OK);

        // Check the content from the response
        String contentAsString = response.getContentAsString();
        assertNotNull(contentAsString);

        // Convert the response to json and check the data
        JSONObject contentAsJson = new JSONObject(contentAsString);
        JSONObject data = contentAsJson.getJSONObject("data");
        assertNotNull(data);

        // Get the email mapping keys and check them
        JSONArray dataSets = data.getJSONArray("emailmapkeys");
        assertNotNull(dataSets);

        // Check the number of email mapping keys
        assertTrue(dataSets.length() == 6);
    }
}
