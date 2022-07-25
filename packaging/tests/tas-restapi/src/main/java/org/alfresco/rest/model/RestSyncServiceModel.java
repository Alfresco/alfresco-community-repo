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
 * "syncService": {
 *  "id": "0",
 *  "uri": "https://localhost:9090/alfresco",
 *  "config": {
 *      "filters": {
 *          "nodeAspects": [
 *              "rma:filePlanComponent",
 *              "sf:*",
 *              "smf:*",
 *              "cm:workingcopy"
 *           ],
 *          "smartFolderNodeAspects": [
 *              "sf:*",
 *              "smf:*"
 *          ],
 *          "nodeTypesWhitelist": [
 *              "dod:filePlan",
 *              "hwf:rejectedCloudTask",
 *              "imap:imapBody",
 *              "st:site"
 *          ],
 *          "nodeTypes": [
 *              "bpm:package",
 *              "cm:systemfolder",
 *              "cm:failedThumbnail"
 *          ]
 *      },
 *      "dsyncClientVersionMin": "1.0.1",
 *      "repoInfo": {
 *          "versionLabel": "5.2.2",
 *          "edition": "Enterprise"
 *      }
 *  }
 * }
 */
public class RestSyncServiceModel extends TestModel
{

    public RestSyncServiceModel()
    {
    }

    @JsonProperty(required = true)
    private String id;

    @JsonProperty(required = true)
    private String uri;

    @JsonProperty(required = true)
    private RestSyncServiceConfigModel config;
    
    private RestIdentityServiceConfigurationModel identityServiceConfig;
    
    public String getId()
    {
        return id;
    }

    public String getUri()
    {
        return uri;
    }

    public RestSyncServiceConfigModel getConfig()
    {
        return config;
    }

	public RestIdentityServiceConfigurationModel getIdentityServiceConfig() {
		return identityServiceConfig;
	}

	public void setIdentityServiceConfig(RestIdentityServiceConfigurationModel identityServiceConfig) {
		this.identityServiceConfig = identityServiceConfig;
	}
}
