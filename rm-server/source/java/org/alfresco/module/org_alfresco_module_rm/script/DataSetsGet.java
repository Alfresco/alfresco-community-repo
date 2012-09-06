package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.dataset.DataSet;
import org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class DataSetsGet extends DeclarativeWebScript
{

   /** Data set service */
   private DataSetService dataSetService;

   /**
    * Set data set service
    * 
    * @param dataSetService the data set service
    */
   public void setDataSetService(DataSetService dataSetService)
   {
      this.dataSetService = dataSetService;
   }

   /**
    * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
    *      org.springframework.extensions.webscripts.Status,
    *      org.springframework.extensions.webscripts.Cache)
    */
   @Override
   protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
   {
      try
      {
         JSONObject data = new JSONObject();
         JSONArray dataSets = new JSONArray();

         for (Map.Entry<String, DataSet> entry : dataSetService.getDataSets().entrySet())
         {
            DataSet value = entry.getValue();
            JSONObject dataSet = new JSONObject();

            dataSet.put("label", value.getLabel());
            dataSet.put("id", value.getId());

            dataSets.put(dataSet);
         }

         data.put("datasets", dataSets);

         Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
         model.put("data", data.toString());

         return model;
      }
      catch (JSONException error)
      {
         throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                  "Cannot convert data set details into JSON.", error);
      }
   }
}
