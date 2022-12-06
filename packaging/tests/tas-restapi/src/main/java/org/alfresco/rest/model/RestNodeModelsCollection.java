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

/**
 * Handles collection of nodes.
 * Example: 
 *  {
      "list": {
        "pagination": {
          "count": 2,
          "hasMoreItems": false,
          "totalItems": 2,
          "skipCount": 0,
          "maxItems": 100
        },
        "entries": [
          {
            "entry": {
              "createdAt": "2017-01-26T14:34:13.781+0000",
              "isFolder": true,
              "isFile": false,
              "createdByUser": {
                "id": "admin",
                "displayName": "Administrator"
              },
              "modifiedAt": "2017-01-26T14:34:13.781+0000",
              "modifiedByUser": {
                "id": "admin",
                "displayName": "Administrator"
              },
              "name": "my folder",
              "id": "ad79ba3f-daf2-4446-be9b-9686a020a94a",
              "nodeType": "cm:folder",
              "parentId": "ab7eb66a-cfc4-4325-b0a5-a43a0a6ffecd"
            }
          },
          {
            "entry": {
              "createdAt": "2017-01-26T14:31:59.821+0000",
              "isFolder": false,
              "isFile": true,
              "createdByUser": {
                "id": "admin",
                "displayName": "Administrator"
              },
              "modifiedAt": "2017-01-26T14:31:59.821+0000",
              "modifiedByUser": {
                "id": "admin",
                "displayName": "Administrator"
              },
              "name": "my file",
              "id": "6c93bff3-2174-4737-9c02-162df70a8549",
              "nodeType": "cm:content",
              "content": {
                "mimeType": "text/plain",
                "mimeTypeName": "Plain Text",
                "sizeInBytes": 17,
                "encoding": "UTF-8"
              },
              "parentId": "ab7eb66a-cfc4-4325-b0a5-a43a0a6ffecd"
            }
          }
        ]
      }
    }
 */
public class RestNodeModelsCollection extends RestModels<RestNodeModel, RestNodeModelsCollection>
{

}
