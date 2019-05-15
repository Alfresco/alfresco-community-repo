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
 * {
 *   "syncId": "8096",
 *   "url": "/api/-default-/private/alfresco/versions/1/subscribers/925a2663-bc86-466d-9a49-95e9e8df7cde/subscriptions/7cfbd62d-2f9d-4c4e-bc3d-14234ae53d9e/sync/8096",
 *   "status": "ok",
 *   "message": null,
 *   "error": null
 * }
 */

public class RestSyncSetRequestModel extends TestModel
{
    
    public RestSyncSetRequestModel()
    {
    }

    @JsonProperty(required = false)
    private String message;

    @JsonProperty(required = false)
    private String error;

    private String syncId;

    private String status;
    
    private String url;

    public String getSyncId()
    {
        return syncId;
    }

    public String getUrl()
    {
        return url;
    }

    public String getStatus()
    {
        return status;
    }

    public String getMessage()
    {
        return message;
    }

    public String getError()
    {
        return error;
    }

}
