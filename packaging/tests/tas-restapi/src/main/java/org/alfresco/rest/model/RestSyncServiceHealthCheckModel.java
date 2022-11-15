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
 * Base Path {@linkplain /alfresco/healthcheck}
 * 
 * @author Meenal Bhave
 * Example:
 *    {
    "activeMQConnection": {
        "healthy": true,
        "message": "ActiveMQ connection Ok"
    },
    "databaseConnection": {
        "healthy": true,
        "message": "Database connection Ok"
    },
    "deadlocks": {
        "healthy": true
    },
    "eventsHealthCheck": {
        "healthy": true,
        "message": "Ok"
    },
    "minimumClientVersion": {
        "healthy": true,
        "message": "1.0.1"
    },
    "repositoryConnection": {
        "healthy": true,
        "message": "Repository connection Ok"
    },
    "syncServiceIdCheck": {
        "healthy": true,
        "message": "41ca6903-3b40-3154-b9ae-a406d83e02c9"
    },
    "versionCheck": {
        "healthy": true,
        "message": "2.2-SNAPSHOT (2017-10-04T08:41:58Z)"
    }
}
 */
public class RestSyncServiceHealthCheckModel extends TestModel
{

    public RestSyncServiceHealthCheckModel()
    {
    }

    @JsonProperty(required = true)
    private RestSyncServiceComponentModel activeMQConnection;

    @JsonProperty(required = true)
    private RestSyncServiceComponentModel databaseConnection;

    @JsonProperty(required = true)
    private RestSyncServiceComponentModel deadlocks;

    @JsonProperty(required = true)
    private RestSyncServiceComponentModel eventsHealthCheck;

    @JsonProperty(required = true)
    private RestSyncServiceComponentModel minimumClientVersion;

    @JsonProperty(required = true)
    private RestSyncServiceComponentModel repositoryConnection;

    @JsonProperty(required = true)
    private RestSyncServiceComponentModel syncServiceIdCheck;

    @JsonProperty(required = true)
    private RestSyncServiceComponentModel versionCheck;

    public RestSyncServiceComponentModel getActiveMQConnection()
    {
        return activeMQConnection;
    }

    public RestSyncServiceComponentModel getDatabaseConnection()
    {
        return databaseConnection;
    }

    public RestSyncServiceComponentModel getDeadlocks()
    {
        return deadlocks;
    }

    public RestSyncServiceComponentModel getEventsHealthCheck()
    {
        return eventsHealthCheck;
    }

    public RestSyncServiceComponentModel getMinimumClientVersion()
    {
        return minimumClientVersion;
    }

    public RestSyncServiceComponentModel getRepositoryConnection()
    {
        return repositoryConnection;
    }

    public RestSyncServiceComponentModel getSyncServiceIdCheck()
    {
        return syncServiceIdCheck;
    }

    public RestSyncServiceComponentModel getVersionCheck()
    {
        return versionCheck;
    }

}
