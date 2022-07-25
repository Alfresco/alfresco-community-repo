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
package org.alfresco.rest.requests;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestNodeModelsCollection;
import org.alfresco.rest.model.RestPersonModelsCollection;
import org.alfresco.rest.model.RestSiteModelsCollection;
import org.springframework.http.HttpMethod;

public class Queries extends ModelRequest<Queries>
{

    public Queries(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    /**
     * GET on queries/nodes
     * 
     * @return
     */
    public RestNodeModelsCollection findNodes()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "queries/nodes?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestNodeModelsCollection.class, request);
    }
    
    /**
     * GET on queries/people
     * 
     * @return
     */
    public RestPersonModelsCollection findPeople()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "queries/people?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestPersonModelsCollection.class, request);
    }
    
    /**
     * GET on queries/people
     * 
     * @return
     */
    public RestSiteModelsCollection findSites()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "queries/sites?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestSiteModelsCollection.class, request);
    }
    
    
    
    
}
