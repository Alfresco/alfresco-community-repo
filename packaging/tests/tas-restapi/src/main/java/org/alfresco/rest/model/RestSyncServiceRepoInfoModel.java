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
 * "repoInfo": {
 *                   "versionLabel": "5.2.2",
 *                   "edition": "Enterprise"
 *             }
 */
public class RestSyncServiceRepoInfoModel extends TestModel
{

    public RestSyncServiceRepoInfoModel()
    {
    }

    @JsonProperty(required = true)
    private String versionLabel;

    @JsonProperty(required = true)
    private String edition;

    @JsonProperty(required = true)
    private boolean clusterEnabled;

    public boolean isClusterEnabled()
    {
        return clusterEnabled;
    }

    public String getVersionLabel()
    {
        return versionLabel;
    }

    public void setVersionLabel(String versionLabel)
    {
        this.versionLabel = versionLabel;
    }

    public String getEdition()
    {
        return edition;
    }

}
