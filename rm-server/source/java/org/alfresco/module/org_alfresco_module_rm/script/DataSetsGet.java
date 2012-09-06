package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.dataset.DataSet;
import org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
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
      Map<String, DataSet> dataSets = dataSetService.getDataSets();
      List<Map<String, String>> dataSetList = new ArrayList<Map<String, String>>(dataSets.size());

      for (Map.Entry<String, DataSet> entry : dataSets.entrySet())
      {
         Map<String, String> dataSet = new HashMap<String, String>(2);
         DataSet value = entry.getValue();

         dataSet.put("label", value.getLabel());
         dataSet.put("id", value.getId());

         dataSetList.add(dataSet);
      }

      Map<String, Object> model = new HashMap<String, Object>(1);
      model.put("datasets", dataSetList);

      return model;
   }
}
