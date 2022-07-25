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

import java.util.List;

import static org.alfresco.utility.report.log.Step.STEP;

public class RestSiteGroupModelsCollection extends RestModels<RestSiteGroupModel, RestSiteGroupModelsCollection>
{

    /**
     * Get groups from site groups list
     */
    public RestSiteGroupModel getSiteGroups(String groupId)
    {
        STEP(String.format("REST API: Get site group with id '%s'", groupId));
        RestSiteGroupModel siteGroupEntry = null;
        List<RestSiteGroupModel> siteGroups = getEntries();

        for (int i = 1; i < siteGroups.size(); i++)
        {
            if (siteGroups.get(i).onModel().getId().equals(groupId))
            {
                siteGroupEntry = siteGroups.get(i).onModel();
            }
        }

        return siteGroupEntry;
    }

}    
