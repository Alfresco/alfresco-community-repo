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
package org.alfresco.rest.requests.syncServiceAPI;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestSyncServiceHealthCheckModel;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.rest.requests.privateAPI.RestPrivateAPI;
import org.springframework.http.HttpMethod;

/**
 * Declares all Rest API under the /healthcheck path
 * 
 * @author Meenal Bhave
 */
public class Healthcheck extends ModelRequest<RestPrivateAPI>
{   
    public Healthcheck(RestWrapper restWrapper)
    {
        super(restWrapper);
        restWrapper.configureSyncServiceEndPoint();
        restWrapper.configureRequestSpec().setBasePath("alfresco/");
    }

    /**
     * Get Healthcheck using GET call on alfresco/healthcheck    
     * @return {@link RestSyncServiceHealthCheckModel}
     */
    public RestSyncServiceHealthCheckModel getHealthcheck()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "healthcheck?{parameters}", restWrapper.getParameters());
        return restWrapper.processModelWithoutEntryObject(RestSyncServiceHealthCheckModel.class, request);
    }

}
