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