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
    * @return Map<String, DataSet> details of all available data sets
    */
   Map<String, DataSet> getDataSets();

   /**
    * Loads the data set with the specified id into the specified file plan
    * 
    * @param dataSetId the id of the data set which will be imported
    * @param filePlan the file plan which the data set will load into
    */
   void loadDataSet(String dataSetId, NodeRef filePlan);

}
