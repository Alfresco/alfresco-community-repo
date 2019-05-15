package org.alfresco.rest.model;

import org.alfresco.rest.core.RestModels;

/**
 * Handle collection of <RestSiteMembershipRequestModel>
 * 
 * {
  "list": {
    "pagination": {
      "count": 0,
      "hasMoreItems": true,
      "totalItems": 0,
      "skipCount": 0,
      "maxItems": 0
    },
    "entries": [
      {
        "entry": {
          "id": "string",
          "createdAt": "2016-10-06T08:24:04.910Z",
          "site": {
            "id": "string",
            "guid": "string",
            "title": "string",
            "description": "string",
            "visibility": "PRIVATE",
            "role": "SiteConsumer"
          },
          "message": "string"
        }
      }
    ]
  }
}
 * 
 * @author Cristina Axinte
 *
 */
public class RestSiteMembershipRequestModelsCollection extends RestModels<RestSiteMembershipRequestModel, RestSiteMembershipRequestModelsCollection>
{
}    