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

import static org.alfresco.utility.report.log.Step.STEP;

import java.util.List;

import org.alfresco.rest.core.RestModels;

/**
 * Handle collection of <RestSiteMemberModel>
 * Example:
 * {
 * {
 * "list": {
 * "pagination": {
 * "count": 2,
 * "hasMoreItems": false,
 * "skipCount": 0,
 * "maxItems": 100
 * },
 * "entries": [
 * {
 * "entry": {
 * "role": "SiteManager",
 * "isMemberOfGroup": false,
 * "person": {
 * "firstName": "Administrator",
 * "emailNotificationsEnabled": true,
 * "company": {},
 * "id": "admin",
 * "enabled": true,
 * "email": "admin@alfresco.com"
 * },
 * "id": "admin"
 * }
 * },
 * {
 * "entry": {
 * "role": "SiteConsumer",
 * "isMemberOfGroup": false,
 * "person": {
 * "firstName": "CqeKxvPHBd FirstName",
 * "lastName": "LN-CqeKxvPHBd",
 * "emailNotificationsEnabled": true,
 * "company": {},
 * "id": "CqeKxvPHBd",
 * "enabled": true,
 * "email": "CqeKxvPHBd"
 * },
 * "id": "CqeKxvPHBd"
 * }
 * }
 * ]
 * }
 * }
 */
public class RestSiteMemberModelsCollection extends RestModels<RestSiteMemberModel, RestSiteMemberModelsCollection>
{

    /**
     * Get member from site members list
     * 
     * @param memberId
     * @return
     */
    public RestSiteMemberModel getSiteMember(String memberId)
    {
        STEP(String.format("REST API: Get site member with id '%s'", memberId));
        RestSiteMemberModel siteMemberEntry = null;
        List<RestSiteMemberModel> siteMembers = getEntries();

        for (int i = 1; i < siteMembers.size(); i++)
        {
            if (siteMembers.get(i).onModel().getId().equals(memberId))
            {
                siteMemberEntry = siteMembers.get(i).onModel();
            }
        }

        return siteMemberEntry;
    }

}    
