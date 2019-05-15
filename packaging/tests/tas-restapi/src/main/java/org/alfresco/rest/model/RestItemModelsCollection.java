package org.alfresco.rest.model;

import org.alfresco.rest.core.RestModels;

import java.util.List;

import static org.alfresco.utility.report.log.Step.STEP;

/**
 * Handles collection of Processes
 *
 * Example:
 *  "entries": [
      {
        "entry": {
          "createdAt": "2016-10-13T11:21:34.621+0000",
          "size": 19,
          "createdBy": "admin",
          "modifiedAt": "2016-10-13T11:21:38.338+0000",
          "name": "file-yCQFYpLniWAzkcR.txt",
          "modifiedBy": "User-cchKFZoNIAfZXXn",
          "id": "ffb7178f-fc11-41c9-8c40-df6523ad917f",
          "mimeType": "text/plain"
        }
      }
    ]
 */
public class RestItemModelsCollection extends RestModels<RestItemModel, RestItemModelsCollection>
{
    public RestItemModel getProcessItemByName(String name)
    {
        STEP(String.format("REST API: Get process item entry with name '%s'", name));
        List<RestItemModel> processItemsList = getEntries();

        for (RestItemModel item: processItemsList)
        {
            if (item.onModel().getName().equals(name))
            {
                return item.onModel();
            }
        }
        return null;
    }
}    