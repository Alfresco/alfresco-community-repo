package org.alfresco.module.org_alfresco_module_rm.test.webscript;

import java.io.IOException;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

public class DataSetRestApiTest extends BaseRMWebScriptTestCase
{
    /** URL for the REST APIs */
    private static final String GET_DATASETS_URL = "/api/rma/datasets?site=%s";
    //private static final String POST_DATASET_URL = "/api/rma/datasets/%s?site=%s";

    /** Constant for the content type */
    //private static final String APPLICATION_JSON = "application/json";

    /**
     * Test the REST API to retrieve details of available RM data sets
     * and to import an RM data set into a file plan
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testGetPostDataSetAction() throws IOException, JSONException
    {
        /** Test GET */

        // Format url and send request
        String getUrl = String.format(GET_DATASETS_URL, SITE_ID);
        Response getResponse = sendRequest(new GetRequest(getUrl), Status.STATUS_OK);

        // Check the content from the response
        String getContentAsString = getResponse.getContentAsString();
        assertNotNull(getContentAsString);

        // Convert the response to json and check the data
        JSONObject getContentAsJson = new JSONObject(getContentAsString);
        JSONObject getData = getContentAsJson.getJSONObject("data");
        assertNotNull(getData);

        // Get the data sets and check them
        JSONArray getDataSets = getData.getJSONArray("datasets");
        assertNotNull(getDataSets);

        // Check the label and the id of the data sets
        for (int i = 0; i < getDataSets.length(); i++)
        {
            JSONObject dataSet = (JSONObject) getDataSets.get(i);
            assertTrue(dataSet.length() == 3);
            assertNotNull(dataSet.get("label"));
            assertNotNull(dataSet.get("id"));
            assertNotNull(dataSet.get("isLoaded"));
        }

        /** Test POST */
//        String dataSetId = getDataSets.getJSONObject(0).getString("id");
//        if (StringUtils.isNotBlank(dataSetId))
//        {
//            // Format url and send request
//            String url = String.format(POST_DATASET_URL, dataSetId, SITE_ID);
//            Response response = sendRequest(new PostRequest(url, new JSONObject().toString(), APPLICATION_JSON), Status.STATUS_OK);
//
//            // Check the content from the response
//            String contentAsString = response.getContentAsString();
//            assertNotNull(contentAsString);
//
//            // Convert the response to json and check the result
//            JSONObject contentAsJson = new JSONObject(contentAsString);
//            String success = contentAsJson.getString("success");
//            assertNotNull(success);
//            assertTrue(success.equals("true"));
//
//            // It is not possible to import the same data set into the same file plan
//            response = sendRequest(new PostRequest(url, new JSONObject().toString(), APPLICATION_JSON), Status.STATUS_OK);
//
//            // Check the content from the response
//            contentAsString = response.getContentAsString();
//            assertNotNull(contentAsString);
//
//            // Convert the response to json and check the result
//            contentAsJson = new JSONObject(contentAsString);
//            success = contentAsJson.getString("success");
//            assertNotNull(success);
//            assertTrue(success.equals("false"));
//            assertNotNull(contentAsJson.getString("message"));
//        }
    }
}