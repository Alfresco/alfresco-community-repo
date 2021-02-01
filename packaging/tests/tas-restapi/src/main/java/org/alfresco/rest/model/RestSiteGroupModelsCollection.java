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