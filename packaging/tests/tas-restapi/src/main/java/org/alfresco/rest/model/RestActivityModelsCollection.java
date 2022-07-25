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
