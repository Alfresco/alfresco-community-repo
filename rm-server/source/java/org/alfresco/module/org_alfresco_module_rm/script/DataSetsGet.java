package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.dataset.DataSet;
import org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService;
import org.alfresco.module.org_alfresco_module_rm.model.rma.type.RmSiteType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class DataSetsGet extends DeclarativeWebScript
{

   /** Constant for the site name parameter */
   private static final String ARG_SITE_NAME = "site";

   /** Constant for the unloadedonly parameter */
   private static final String ARG_UNLOADED_ONLY = "unloadedonly";

   /** Data set service */
   private DataSetService dataSetService;

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
      // Get the site name from the URL and find out the file plan
      String siteName = req.getParameter(ARG_SITE_NAME);
      if (StringUtils.isBlank(siteName))
      {
         siteName = RmSiteType.DEFAULT_SITE_NAME;
      }
      NodeRef filePlan = siteService.getContainer(siteName, RmSiteType.COMPONENT_DOCUMENT_LIBRARY);

      // Check if only unloaded data sets should be returned - default value is false
      String unloadedOnlyParam = req.getParameter(ARG_UNLOADED_ONLY);
      boolean unloadedOnly = false;
      if (StringUtils.isNotBlank(unloadedOnlyParam))
      {
         unloadedOnly = Boolean.valueOf(unloadedOnlyParam).booleanValue();
      }

      // Get the loaded/unloaded data sets depending on the "unloadedOnly" parameter
      Map<String, DataSet> dataSets = dataSetService.getDataSets(filePlan, unloadedOnly);
      List<Map<String, String>> dataSetList = new ArrayList<Map<String, String>>(dataSets.size());

      for (Map.Entry<String, DataSet> entry : dataSets.entrySet())
      {
         Map<String, String> dataSet = new HashMap<String, String>(3);
         DataSet value = entry.getValue();

         // Data set details
         String dataSetId = value.getId();
         String isLoaded = String.valueOf(dataSetService.isLoadedDataSet(filePlan, dataSetId));

         dataSet.put("label", value.getLabel());
         dataSet.put("id", dataSetId);
         dataSet.put("isLoaded", isLoaded);

         // Add data set to the list
         dataSetList.add(dataSet);
      }

      Map<String, Object> model = new HashMap<String, Object>(1);
      model.put("datasets", dataSetList);

      return model;
   }
}
