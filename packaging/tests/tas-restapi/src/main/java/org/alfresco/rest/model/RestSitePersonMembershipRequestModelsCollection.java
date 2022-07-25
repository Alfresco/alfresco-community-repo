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
