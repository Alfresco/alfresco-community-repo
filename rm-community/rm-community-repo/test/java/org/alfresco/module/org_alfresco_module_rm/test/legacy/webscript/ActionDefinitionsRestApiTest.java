package org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * REST API Tests for Action Definitions
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class ActionDefinitionsRestApiTest extends BaseRMWebScriptTestCase
{
    /** URL for the REST APIs */
    private static final String RM_ACTIONDEFINITIONS_URL = "/api/rm/rm-actiondefinitions";    

    /**
     * Test the REST API to retrieve the list of rm action definitions
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testRmGetActionDefinitions() throws IOException, JSONException
    {
        // Send request
        Response response = sendRequest(new GetRequest(RM_ACTIONDEFINITIONS_URL), Status.STATUS_OK);

        // Check the content from the response
        String contentAsString = response.getContentAsString();
        assertNotNull(contentAsString);

        // Convert the response to json and check the data
        JSONObject contentAsJson = new JSONObject(contentAsString);
        JSONArray data = contentAsJson.getJSONArray("data");
        assertNotNull(data);

        // Get a (sub)list of available dm action definitions
        List<String> dmActionDefinitions = getDmActionDefinitions();

        // Get the list of rm action definitions from the response and check it
        List<String> rmActionDefinitions = new ArrayList<String>();
        for (int i = 0; i < data.length(); i++)
        {
            String name = data.getJSONObject(i).getString("name");
            assertNotNull(name);
            rmActionDefinitions.add(name);
            assertFalse(dmActionDefinitions.contains(name));
        }
        assertTrue(rmActionDefinitions.containsAll(getRmActionDefinitions()));
    }

    /**
     * Returns a (sub)list of rm action definitions
     *
     * @return A (sub)list of rm action definitions
     */
    private List<String> getRmActionDefinitions()
    {
        return Arrays.asList(new String[]
        {
            "reject",
            "fileTo",
            "declareRecord"
        });
    }

    /**
     * Returns a (sub)list of dm action definitions
     *
     * @return A (sub)list of dm action definitions
     */
    private List<String> getDmActionDefinitions()
    {
        return Arrays.asList(new String[]
        {
            "check-in",
            "check-out",
            "mail",
            "move",
            "transform"
        });
    }
}
