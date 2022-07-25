/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
