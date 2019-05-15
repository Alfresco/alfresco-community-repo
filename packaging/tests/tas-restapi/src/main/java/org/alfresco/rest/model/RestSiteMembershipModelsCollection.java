package org.alfresco.rest.model;

import org.alfresco.rest.core.RestModels;

/**
 * 
 * Handle collection of <RestSiteMembershipModel>
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
          "site": {
            "id": "string",
            "guid": "string",
            "title": "string",
            "description": "string",
            "visibility": "PRIVATE",
            "role": "SiteConsumer"
          },
          "id": "string",
          "guid": "string",
          "role": "SiteConsumer"
        }
      }
    ]
  }
}
 *
 */
public class RestSiteMembershipModelsCollection extends RestModels<RestSiteEntry,RestSiteMembershipModelsCollection>
{
}    