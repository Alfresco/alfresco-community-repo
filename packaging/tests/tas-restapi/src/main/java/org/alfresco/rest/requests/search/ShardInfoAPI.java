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
 * Copyright (C) 2005-2018 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.rest.requests.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.rest.search.RestShardInfoModelCollection;
import org.springframework.http.HttpMethod;

/**
 * Wrapper for Shard Info API.
 *
 * @author Tuna Aksoy
 */
public class ShardInfoAPI extends ModelRequest<ShardInfoAPI>
{
    /**
     * Constructor 
     * 
     * @param restWrapper Rest Wrapper
     */
    public ShardInfoAPI(RestWrapper restWrapper)
    {
        super(restWrapper);
        restWrapper.configureAlfrescoEndpoint();
        RestAssured.basePath = "alfresco/api/-default-/private/search/versions/1";
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    /**
     * Get Info about the shard groups
     * 
     * @return {@link RestShardInfoModelCollection} Information about shard groups
     * @throws JsonProcessingException
     */
    public RestShardInfoModelCollection getInfo() throws JsonProcessingException
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "info");
        return restWrapper.processModels(RestShardInfoModelCollection.class, request);
    }
}
