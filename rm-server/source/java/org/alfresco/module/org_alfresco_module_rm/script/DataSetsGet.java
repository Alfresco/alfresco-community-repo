package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.dataset.DataSet;
import org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.model.RmSiteType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class DataSetsGet extends DeclarativeWebScript implements RecordsManagementCustomModel
{

   /** Data set service */
   private DataSetService dataSetService;

   /** Node service */
   private NodeService nodeService;

   /** Site service */
   private SiteService siteService; 

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
    * Set node service
    * 
    * @param nodeService the node service
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * Set site service
    * 
    * @param siteService the site service
    */
   public void setSiteService(SiteService siteService)
   {
      this.siteService = siteService;
   }

   /**
    * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
    *      org.springframework.extensions.webscripts.Status,
    *      org.springframework.extensions.webscripts.Cache)
    */
   @Override
   protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
   {
      // Get the list of data sets
      Map<String, DataSet> dataSets = dataSetService.getDataSets();
      // Filter the loaded data sets so that they won't show up in the UI
      Map<String, DataSet> filteredDataSets = filterLoadedDataSets(dataSets);

      List<Map<String, String>> dataSetList = new ArrayList<Map<String, String>>(filteredDataSets.size());

      for (Map.Entry<String, DataSet> entry : filteredDataSets.entrySet())
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

   /**
    * Helper method for filtering the data sets which already have been loaded
    * 
    * @param dataSets A map of available data sets
    * @return Map<String, DataSet> The new map of data sets which do not include the already loaded data sets
    */
   private Map<String, DataSet> filterLoadedDataSets(Map<String, DataSet> dataSets)
   {
      // FIXME: SiteId
      NodeRef filePlan = siteService.getContainer(RmSiteType.DEFAULT_SITE_NAME, RmSiteType.COMPONENT_DOCUMENT_LIBRARY);
      Serializable dataSetIds = nodeService.getProperty(filePlan, PROP_LOADED_DATA_SET_IDS);

      // Check if the property of the aspect has a value. If not return the original map
      if (dataSetIds != null)
      {
         @SuppressWarnings("unchecked")
         ArrayList<String> loadedDataSetIds = (ArrayList<String>) dataSetIds;
         for (String loadedDataSetId : loadedDataSetIds)
         {
            if (dataSets.containsKey(loadedDataSetId))
            {
               dataSets.remove(loadedDataSetId);
            }
         }
      }

      return dataSets;
   }

}
