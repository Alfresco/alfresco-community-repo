/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
import org.apache.commons.lang3.StringUtils;
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
      List<Map<String, String>> dataSetList = new ArrayList<>(dataSets.size());

      for (Map.Entry<String, DataSet> entry : dataSets.entrySet())
      {
         Map<String, String> dataSet = new HashMap<>(3);
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

      Map<String, Object> model = new HashMap<>(1);
      model.put("datasets", dataSetList);

      return model;
   }
}
