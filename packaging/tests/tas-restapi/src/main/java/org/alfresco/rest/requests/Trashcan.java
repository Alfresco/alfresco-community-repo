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
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestNodeModelsCollection;
import org.alfresco.rest.model.RestRenditionInfoModel;
import org.alfresco.rest.model.RestRenditionInfoModelCollection;
import org.alfresco.utility.model.RepoTestModel;
import org.springframework.http.HttpMethod;

public class Trashcan extends ModelRequest<Trashcan>
{
    public Trashcan(RestWrapper restWrapper) 
    {
      super(restWrapper);
    }

    /**
     * GET on deleted-nodes
     * 
     * @return RestNodeModelsCollection
     */
    public RestNodeModelsCollection findDeletedNodes()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "deleted-nodes?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestNodeModelsCollection.class, request);
    }

    /**
     * Gets a node from trashcan using GET call on "deleted-nodes/{nodeId}"
     * 
     * @param repoModel
     * @return RestNodeModel
     */
    public RestNodeModel findDeletedNode(RepoTestModel repoModel)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "deleted-nodes/{nodeId}?{parameters}", repoModel.getNodeRefWithoutVersion(), restWrapper.getParameters());
        return restWrapper.processModel(RestNodeModel.class, request);
    }

    /**
     * Delete node from trashcan using DELETE call on "deleted-nodes/{nodeId}"
     *
     * @param repoModel
     * @return
     */
    public void deleteNodeFromTrashcan(RepoTestModel repoModel)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "deleted-nodes/{nodeId}", repoModel.getNodeRefWithoutVersion());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Restore node from trashcan using POST call on "deleted-nodes/{nodeId}/restore"
     *
     * @param repoModel
     * @return RestNodeModel
     */
    public RestNodeModel restoreNodeFromTrashcan(RepoTestModel repoModel)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.POST, "deleted-nodes/{nodeId}/restore?{parameters}", repoModel.getNodeRefWithoutVersion(), restWrapper.getParameters());
        return restWrapper.processModel(RestNodeModel.class, request);
    }

    /**
     * Gets a trashcan node content using GET call on "deleted-nodes/{nodeId}/content"
     * 
     * @param repoModel
     * @return RestResponse
     */
    public RestResponse getDeletedNodeContent(RepoTestModel repoModel)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "deleted-nodes/{nodeId}/content?{parameters}", repoModel.getNodeRefWithoutVersion(), restWrapper.getParameters());
        return restWrapper.process(request);
    }

    /**
     * Gets all trashcan node renditions using GET call on "/deleted-nodes/{nodeId}/renditions"
     * 
     * @param repoModel
     * @return RestRenditionInfoModelCollection
     */
    public RestRenditionInfoModelCollection getDeletedNodeRenditions(RepoTestModel repoModel)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "deleted-nodes/{nodeId}/renditions?{parameters}", repoModel.getNodeRefWithoutVersion(), restWrapper.getParameters());
        return restWrapper.processModels(RestRenditionInfoModelCollection.class, request);
    }

    /**
     * Gets a single trashcan node rendition using GET call on "/deleted-nodes/{nodeId}/renditions/{renditionId}"
     * 
     * @param repoModel
     * @param renditionId
     * @return RestRenditionInfoModel
     */
    public RestRenditionInfoModel getDeletedNodeRendition(RepoTestModel repoModel, String renditionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "deleted-nodes/{nodeId}/renditions/{renditionId}?{parameters}", repoModel.getNodeRefWithoutVersion(), renditionId, restWrapper.getParameters());
        return restWrapper.processModel(RestRenditionInfoModel.class, request);
    }

    /**
     * Gets a single trashcan node rendition content using GET call on "/deleted-nodes/{nodeId}/renditions/{renditionId}/content"
     * 
     * @param repoModel
     * @param renditionId
     * @return RestResponse
     */
    public RestResponse getDeletedNodeRenditionContent(RepoTestModel repoModel, String renditionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "deleted-nodes/{nodeId}/renditions/{renditionId}/content?{parameters}", repoModel.getNodeRefWithoutVersion(), renditionId, restWrapper.getParameters());
        return restWrapper.process(request);
    }
}
