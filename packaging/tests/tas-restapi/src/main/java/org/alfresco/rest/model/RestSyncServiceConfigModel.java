/*
 * Copyright 2017 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */

package org.alfresco.rest.model;

import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base Path {@linkplain /alfresco/api/-default-/private/alfresco/versions/1}
 * 
 * @author Meenal Bhave
 * Example:
 *    "config": {
        "filters": {
            "nodeAspects": [
                "rma:filePlanComponent",
                "sf:*",
                "smf:*",
                "cm:workingcopy"
             ],
            "smartFolderNodeAspects": [
                "sf:*",
                "smf:*"
            ],
            "nodeTypesWhitelist": [
                "dod:filePlan",
                "hwf:rejectedCloudTask",
                "imap:imapBody",
                "st:site"
            ],
            "nodeTypes": [
                "bpm:package",
                "cm:systemfolder",
                "cm:failedThumbnail"
            ]
        },
        "dsyncClientVersionMin": "1.0.1",
        "repoInfo": {
            "versionLabel": "5.2.2",
            "edition": "Enterprise"
        }
 }
 */
public class RestSyncServiceConfigModel extends TestModel
{

    public RestSyncServiceConfigModel()
    {
    }

    @JsonProperty(required = true)
    private String dsyncClientVersionMin;

    @JsonProperty(required = true)
    private RestSyncServiceConfigFiltersModel filters;
    
    @JsonProperty(required = true)
    private RestSyncServiceRepoInfoModel repoInfo;
    
    private RestIdentityServiceConfigurationModel identityServiceConfig;
    
    public String getDsyncClientVersionMin()
    {
        return dsyncClientVersionMin;
    }

    public RestSyncServiceConfigFiltersModel getFilters()
    {
        return filters;
    }

    public RestSyncServiceRepoInfoModel getRepoInfo()
    {
        return repoInfo;
    }

	public RestIdentityServiceConfigurationModel getIdentityServiceConfig() {
		return identityServiceConfig;
	}
}
