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
