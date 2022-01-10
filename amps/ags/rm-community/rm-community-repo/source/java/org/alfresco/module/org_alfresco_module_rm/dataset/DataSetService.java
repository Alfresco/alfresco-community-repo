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

package org.alfresco.module.org_alfresco_module_rm.dataset;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

public interface DataSetService
{

   /**
    * Register a data set implementation with the service
    * 
    * @param dataSet the data set
    */
   void register(DataSet dataSet);

   /**
    * Gets the details of all available data sets.
    * 
    * @return the Map&lt;String, DataSet&gt; with details of all available data sets
    */
   Map<String, DataSet> getDataSets();

   /**
    * Gets the details of all available data sets for a file plan depending on
    * the parameter "excludeLoaded".
    * 
    * @param filePlan the file plan for which the details should be retrieved
    * @param excludeLoaded if true only data sets will be retrieved which has
    *           not been loaded
    * @return Map&lt;String, DataSet&gt; with details of the available data sets for a
    *         specified file plan depending on the parameter "excludeLoaded".
    *         The result could also be an empty map
    */
   Map<String, DataSet> getDataSets(NodeRef filePlan, boolean excludeLoaded);

   /**
    * Gets the details of all loaded data sets for a specified file plan
    * 
    * @param filePlan the file plan for which the loaded data sets should be
    *           retrieved
    * @return  Map&lt;String, DataSet&gt;  details of all loaded data sets or an empty
    *         map if there has not been any data sets loaded for the specified
    *         file plan
    */
   Map<String, DataSet> getLoadedDataSets(NodeRef filePlan);

   /**
    * Loads the data set with the specified id into the specified file plan
    * 
    * @param filePlan the file plan which the data set will load into
    * @param dataSetId the id of the data set which will be imported
    */
   void loadDataSet(NodeRef filePlan, String dataSetId);

   /**
    * Checks if a data set exists with the given data set id
    * 
    * @param dataSetId the id of the data set which will be checked
    * @return true if the data set exists, false otherwise
    */
   boolean existsDataSet(String dataSetId);

   /**
    * Checks if a data set with the id "dataSetId" has been loaded into the
    * specified file plan
    * 
    * @param filePlan the file plan for which the check should be done
    * @param dataSetId the id of the data set which should be checked if it has
    *           been loaded to the file plan
    * @return true if the data set with the specified id has been loaded into
    *         the specified file plan, false otherwise
    */
   boolean isLoadedDataSet(NodeRef filePlan, String dataSetId);

}
