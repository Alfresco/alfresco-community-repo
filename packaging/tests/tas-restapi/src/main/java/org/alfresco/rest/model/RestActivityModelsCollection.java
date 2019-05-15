package org.alfresco.rest.model;

import org.alfresco.rest.core.RestModels;
/**
 * 
 * @author Cristina Axinte
 * 
 * /**
 * Handle collection of <RestActivityModel>
 * Example:
{
  "list": {
    "pagination": {
      "count": 3,
      "hasMoreItems": false,
      "skipCount": 0,
      "maxItems": 100
    },
    "entries": [
      {
        "entry": {
          "postedAt": "2016-09-30T12:31:55.923+0000",
          "feedPersonId": "user-vtcaquckbq",
          "postPersonId": "user-vtcaquckbq",
          "siteId": "site-mNygPRuKka",
          "activitySummary": {
            "firstName": "User-vtCaqUCKBq FirstName",
            "lastName": "LN-User-vtCaqUCKBq",
            "parentObjectId": "e982c6b7-e16e-4a25-ae77-5ed96614e871",
            "title": "file-bWbaaGycDm.txt",
            "objectId": "3a647ca9-cef9-45d0-ae4a-49cffa131154"
          },
          "id": 14692,
          "activityType": "org.alfresco.documentlibrary.file-added"
        }
      }      
    ]
  }
}
 *
 */
public class RestActivityModelsCollection extends RestModels<RestActivityModel, RestActivityModelsCollection>
{   
}
