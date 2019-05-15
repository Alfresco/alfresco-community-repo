package org.alfresco.rest.model;

import org.alfresco.rest.core.RestModels;

/**
 * Handle collection of <RestSitePersonMembershipRequestModel>
 * 
 *{"list": {
    "entries": [{"entry": {
       "createdAt": "2018-03-08T07:51:54.948+0000",
       "site": {
          "visibility": "MODERATED",
          "guid": "9d633bed-de72-414e-b188-c8e0ca42235a",
          "description": "siteModerated-sHrfyEhnJcBqElEMODERATED",
          "id": "siteModerated-sHrfyEhnJcBqElE",
          "preset": "site-dashboard",
          "title": "siteModerated-sHrfyEhnJcBqElE"
       },
       "person": {
          "firstName": "testUser-jvagwleqUCYSLeG FirstName",
          "lastName": "LN-testUser-jvagwleqUCYSLeG",
          "capabilities": {
             "isMutable": true,
             "isGuest": false,
             "isAdmin": false
          },
          "emailNotificationsEnabled": true,
          "company": {},
          "id": "testUser-jvagwleqUCYSLeG",
          "enabled": true,
          "email": "testUser-jvagwleqUCYSLeG@tas-automation.org"
       },
       "id": "siteModerated-sHrfyEhnJcBqElE",
       "message": "Please accept me"
    }}],
    "pagination": {
       "maxItems": 100,
       "hasMoreItems": false,
       "totalItems": 1,
       "count": 1,
       "skipCount": 0
    }
 }}
 */
public class RestSitePersonMembershipRequestModelsCollection extends RestModels<RestSitePersonMembershipRequestModel, RestSitePersonMembershipRequestModelsCollection>
{

}
