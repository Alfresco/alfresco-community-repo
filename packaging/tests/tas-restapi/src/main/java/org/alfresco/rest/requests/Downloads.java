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
import org.alfresco.rest.model.RestDownloadsModel;
import org.springframework.http.HttpMethod;


/**
 * Methods for Rest API under the /downloads path
 */

public class Downloads extends ModelRequest<Downloads> {

    RestDownloadsModel downloadsModel;

    public Downloads(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    public Downloads(RestDownloadsModel downloadsModel, RestWrapper restWrapper)
    {
        super(restWrapper);
        this.downloadsModel = downloadsModel;
    }

    /**
     * Get download details using POST call on "downloads"
     */
    public RestDownloadsModel createDownload(String postBody)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "downloads");
        return restWrapper.processModel(RestDownloadsModel.class, request);
    }

    /**
     * Get download details using GET call on "downloads/{downloadId}"
     */
    public RestDownloadsModel getDownload()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "downloads/{downloadId}", downloadsModel.getId());
        return restWrapper.processModel(RestDownloadsModel.class, request);
    }

    /**
     * Cancel download using DELETE call on "downloads/{downloadId}"
     */
    public void cancelDownload()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "downloads/{downloadId}", downloadsModel.getId());
        restWrapper.processEmptyModel(request);;
    }
}
