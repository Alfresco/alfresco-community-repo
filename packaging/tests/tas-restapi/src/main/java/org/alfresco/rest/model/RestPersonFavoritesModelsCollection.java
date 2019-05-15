package org.alfresco.rest.model;

import org.alfresco.rest.core.RestModels;

/**
 * Handle collection of <RestFavoriteModel>
 * Example:
{
  "list": {
    "pagination": {
      "count": 1,
      "hasMoreItems": false,
      "totalItems": 1,
      "skipCount": 0,
      "maxItems": 100
    },
    "entries": [
      {
        "entry": {
          "targetGuid": "096babce-9f28-40d2-a38b-84b2ae41d71f",
          "createdAt": "2016-09-26T13:04:42.066+0000",
          "target": {
            "site": {
              "id": "site",
              "guid": "096babce-9f28-40d2-a38b-84b2ae41d71f",
              "title": "site",
              "visibility": "PUBLIC",
              "role": "SiteManager"
        }
      }
    ]
  }
}
 */

public class RestPersonFavoritesModelsCollection extends RestModels<RestPersonFavoritesModel, RestPersonFavoritesModelsCollection>
{
}    