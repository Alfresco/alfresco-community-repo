package org.alfresco.module.org_alfresco_module_rm.test.webscript;

import java.io.IOException;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

public class DataSetsGetTest extends BaseRMWebScriptTestCase
{
   /** URL for the REST API */
   private static final String GET_DATASETS_URL = "/api/rma/datasets";

   /**
    * Test the REST API to retrieve details of available RM data sets 
    * 
    * @throws IOException
    * @throws JSONException
    */
   public void testGetDataSetsAction() throws IOException, JSONException
   {
      // Send request
      Response response = sendRequest(new GetRequest(GET_DATASETS_URL), Status.STATUS_OK);

      // Check the content from the response
      String contentAsString = response.getContentAsString();
      assertNotNull(contentAsString);

      // Convert the response to json and check the data
      JSONObject contentAsJson = new JSONObject(contentAsString);
      JSONObject data = contentAsJson.getJSONObject("data");
      assertNotNull(data);

      // Get the data sets and check them
      JSONArray dataSets = data.getJSONArray("datasets");
      assertNotNull(dataSets);

      // Check the label and the id of the data sets
      for (int i = 0; i < dataSets.length(); i++)
      {
         JSONObject dataSet = (JSONObject) dataSets.get(i);
         assertTrue(dataSet.length() == 2);
         assertNotNull(dataSet.get("label"));
         assertNotNull(dataSet.get("id"));
      }
   }
}