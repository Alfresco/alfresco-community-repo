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
 * "resets": [
 *      {
 *          "subscriptionId": "7787ac59-ec71-43c0-b97a-ebdba0254759",
 *          "resetAll": false,
 *          "resetReason": "Stale",
 *          "timestamp": 1502811227880
 *      }
 *  ]
 */

public class RestSyncSetResetsModel extends TestModel
{

    public RestSyncSetResetsModel()
    {
    }

    @JsonProperty(required = true)
    private String subscriptionId;

    @JsonProperty(required = true)
    private String resetReason;

    @JsonProperty(required = true)
    private Boolean resetAll;

    @JsonProperty(required = true)
    private String timeStamp;

    public String getSubscriptionId()
    {
        return subscriptionId;
    }

    public String getResetReason()
    {
        return resetReason;
    }

    public Boolean getIsResetAll()
    {
        return resetAll;
    }

    public String getTimeStamp()
    {
        return timeStamp;
    }

}
