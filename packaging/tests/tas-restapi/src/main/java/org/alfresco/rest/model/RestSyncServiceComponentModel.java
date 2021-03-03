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
 * Base Path {@linkplain /alfresco/healthcheck}
 * 
 * @author Meenal Bhave
 * Example:
 * 
    "healthCheckComponent": {
        "healthy": true,
        "message": "ActiveMQ connection Ok"
    }
}
 */
public class RestSyncServiceComponentModel extends TestModel
{

    public RestSyncServiceComponentModel()
    {
    }

    @JsonProperty(required = true)
    private Boolean healthy;

    @JsonProperty
    private String message;

    @JsonProperty
    private int duration;

    @JsonProperty
    private String timestamp;

    public Boolean getHealthy()
    {
        return healthy;
    }

    public String getMessage()
    {
        return message;
    }

    public int getDuration()
    {
        return duration;
    }

    public String getTimestamp()
    {
        return timestamp;
    }

}
