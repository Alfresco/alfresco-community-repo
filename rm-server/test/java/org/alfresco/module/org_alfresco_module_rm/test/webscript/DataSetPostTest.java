package org.alfresco.module.org_alfresco_module_rm.test.webscript;

import java.io.IOException;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest; 
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

public class DataSetPostTest extends BaseRMWebScriptTestCase
{
   /** URL for the REST APIs */
   private static final String GET_DATASETS_URL = "/api/rma/datasets";
   private static final String POST_DATASET_URL = "/api/rma/datasets/%s?site=%s";

   /** Constant for the content type */
   private static final String APPLICATION_JSON = "application/json";

   /**
    * Test the REST API to import a RM data set into a file plan
    * 
    * @throws IOException
    * @throws JSONException
    */
   public void testPostDataSetAction() throws IOException, JSONException
   {
      String dataSetId = getDataSetId();
      if (StringUtils.isNotBlank(dataSetId))
      {
         // Format url and send request
         String url = String.format(POST_DATASET_URL, dataSetId, SITE_ID);
         Response response = sendRequest(new PostRequest(url, new JSONObject().toString(), APPLICATION_JSON), Status.STATUS_OK);

         // Check the content from the response
         String contentAsString = response.getContentAsString();
         assertNotNull(contentAsString);

         // Convert the response to json and check the result
         JSONObject contentAsJson = new JSONObject(contentAsString);
         String success = contentAsJson.getString("success");
         assertNotNull(success);
         assertTrue(success.equals("true"));

         // It is not possible to import the same data set into the same file plan
         response = sendRequest(new PostRequest(url, new JSONObject().toString(), APPLICATION_JSON), Status.STATUS_INTERNAL_SERVER_ERROR);
      }
   }

   /**
    * Helper method for getting a data set id
    * 
    * @return A data set id from the list of available data sets or null if there is no data set
    * @throws IOException
    * @throws JSONException
    */
   private String getDataSetId() throws IOException, JSONException
   {
      // Send request
      Response response = sendRequest(new GetRequest(GET_DATASETS_URL), Status.STATUS_OK);

      // Get the response as string, convert to json and retrieve the data sets
      String contentAsString = response.getContentAsString();
      JSONObject contentAsJson = new JSONObject(contentAsString);
      JSONObject data = contentAsJson.getJSONObject("data");
      JSONArray dataSets = data.getJSONArray("datasets");

      // Check if there are some test data sets
      String dataSetId = null;
      if (dataSets.length() > 0)
      {
         JSONObject dataSet = (JSONObject) dataSets.get(0);
         dataSetId = (String) dataSet.get("id");
      }

      return dataSetId;
   }
}
