 
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
 * @author Mark Hibbins
 * @since 2.2
 */
public class SubstitutionSuggestionsRestApiTest extends BaseRMWebScriptTestCase
{
    /** URL for the REST APIs */
    private static final String RM_SUBSTITUTIONSUGGESTIONS_URL = "/api/rm/rm-substitutionsuggestions?fragment=month";

    /**
     * Test the REST API to retrieve the list of rm substitution suggestions
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testRmGetSubstitutionSuggestions() throws IOException, JSONException
    {
        // Send request
        Response response = sendRequest(new GetRequest(RM_SUBSTITUTIONSUGGESTIONS_URL), Status.STATUS_OK);

        // Check the content from the response
        String contentAsString = response.getContentAsString();
        assertNotNull(contentAsString);

        // Convert the response to json and check the data
        JSONObject contentAsJson = new JSONObject(contentAsString);
        JSONArray data = contentAsJson.getJSONArray("substitutions");
        assertNotNull(data);

        // Get the list of rm action definitions from the response and check it
        List<String> substitutionDefinitions = getSubstitutionDefinitions();
        List<String> rmSubstitutionDefinitions = new ArrayList<String>();
        for (int i = 0; i < data.length(); i++)
        {
            String value = data.getString(i);
            assertNotNull(value);
            rmSubstitutionDefinitions.add(value);
        }
        assertTrue(rmSubstitutionDefinitions.containsAll(substitutionDefinitions));
        assertTrue(substitutionDefinitions.containsAll(rmSubstitutionDefinitions));
    }

    /**
     * Returns a (sub)list of dm action definitions
     *
     * @return A (sub)list of dm action definitions
     */
    private List<String> getSubstitutionDefinitions()
    {
        return Arrays.asList(new String[]
        {
            "date.month.number",
            "date.month.long",
            "date.month.short",
            "date.month",
            "date.day.month",
        });
    }
}