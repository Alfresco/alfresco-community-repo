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

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.model.RestTagModelsCollection;
import org.springframework.http.HttpMethod;

public class Tags extends ModelRequest<Tags>
{
    RestTagModel tag;

    public Tags(RestTagModel tag, RestWrapper restWrapper)
    {
        super(restWrapper);
        this.tag = tag;
    }

    /**
     * Retrieves 100 tags (this is the default size when maxItems is not specified) from Alfresco using GET call on "/tags"
     * 
     * @return
     * @throws JsonToModelConversionException
     */
    public RestTagModelsCollection getTags()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "tags?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestTagModelsCollection.class, request);
    }

    /**
     * Retrieves a tag with ID using GET call on using GET call on "/tags/{tagId}"
     * 
     * @param tag
     * @return
     */
    public RestTagModel getTag()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "tags/{tagId}?{parameters}", tag.getId(), restWrapper.getParameters());
        return restWrapper.processModel(RestTagModel.class, request);
    }

    /**
     * Update a tag using PUT call on tags/{tagId}
     *
     * @param tag
     * @param newTag
     * @return
     * @throws JsonToModelConversionException
     */
    public RestTagModel update(String newTag)
    {
        String postBody = JsonBodyGenerator.keyValueJson("tag", newTag);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tags/{tagId}", tag.getId());

        return restWrapper.processModel(RestTagModel.class, request);
    }

    /**
     * Delete tag.
     * - DELETE /tags/{tagId}
     */
    public void deleteTag()
    {
        RestRequest request = RestRequest.
                simpleRequest(HttpMethod.DELETE, "/tags/{tagId}", tag.getId());
        restWrapper.processEmptyModel(request);
    }
}
