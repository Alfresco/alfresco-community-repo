 
package org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript;

import java.io.IOException;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

public class EmailMapScriptTest extends BaseRMWebScriptTestCase
{
    /** URLs for the REST APIs */
    public final static String URL_RM_EMAILMAP = "/api/rma/admin/emailmap";
    public final static String URL_RM_EMAILMAP_DELETE = "/api/rma/admin/emailmap/%s/%s";

    /** Constant for the content type */
    private static final String APPLICATION_JSON = "application/json";

    /**
     * Tests the REST APIs for a custom mapping
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testEmailMap() throws IOException, JSONException
    {
        /** Test GET */
        Response getResponse = sendRequest(new GetRequest(URL_RM_EMAILMAP), Status.STATUS_OK);

        JSONObject getResponseContent = new JSONObject(getResponse.getContentAsString());
        JSONObject getData = getResponseContent.getJSONObject("data");
        JSONArray getMappings = getData.getJSONArray("mappings");
        assertTrue(getMappings.length() == 20);

        /** Test POST */
        JSONObject newMapping = new JSONObject();
        newMapping.put("from", "messageTo");
        newMapping.put("to", "rmc:Wibble");

        Response postResponse = sendRequest(new PostRequest(URL_RM_EMAILMAP, newMapping.toString(), APPLICATION_JSON), Status.STATUS_OK);
        JSONObject postResponseContent = new JSONObject(postResponse.getContentAsString());
        JSONObject postData = postResponseContent.getJSONObject("data");
        JSONArray postMappings = postData.getJSONArray("mappings");

        assertTrue(postMappings.length() == 21);
        assertTrue(existsMapping(postMappings));

        /** Test DELETE */
        Response deleteResponse = sendRequest(new DeleteRequest(String.format(URL_RM_EMAILMAP_DELETE, "messageTo", "rmc:Wibble")), Status.STATUS_OK);
        JSONObject deleteResponseContent = new JSONObject(deleteResponse.getContentAsString());
        JSONObject deleteData = deleteResponseContent.getJSONObject("data");
        JSONArray deleteMappings = deleteData.getJSONArray("mappings");

        assertTrue(deleteMappings.length() == 20);
        assertFalse(existsMapping(deleteMappings));
    }

    /**
     * Helper method for checking if a custom mapping exists
     *
     * @param mappings The list of available mappings
     * @return true if the custom mapping exists in the list of available mappings, false otherwise
     * @throws JSONException
     */
    private boolean existsMapping(JSONArray mappings) throws JSONException
    {
        boolean result = false;
        for (int i = 0; i < mappings.length(); i++)
        {
            String from = mappings.getJSONObject(i).getString("from");
            String to = mappings.getJSONObject(i).getString("to");
            if (from.equalsIgnoreCase("messageTo") && to.equalsIgnoreCase("rmc:Wibble"))
            {
                result = true;
                break;
            }
        }
        return result;
    }
}