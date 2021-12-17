/*
 * Copyright 2021 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.alfresco.rest.requests;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestArchiveContentRequestModel;
import org.alfresco.rest.model.RestContentStorageInfoModel;
import org.alfresco.rest.model.RestRestoreArchivedContentRequestModel;
import org.alfresco.utility.model.TestModel;
import org.springframework.http.HttpMethod;

public class ContentStorageInformation extends ModelRequest<ContentStorageInformation> 
{
    private static final String basePath = "nodes/{nodeId}/storage-info/{contentPropName}";
    RestContentStorageInfoModel storageInfoModel;

    private String nodeId;
    private String contentPropName;

    public ContentStorageInformation(RestWrapper restWrapper) 
    {
        super(restWrapper);
    }


    public ContentStorageInformation withNodeId(String nodeId)
    {
        this.nodeId = nodeId;
        return this;
    }

    public ContentStorageInformation withContentPropName(String contentPropName)
    {
        this.contentPropName = contentPropName;
        return this;
    }

    /**
     * Get Content Storage Properties using GET call on "nodes/{nodeId}/storage-info/{contentPropName}"
     * @return
     */
    public RestContentStorageInfoModel getStorageInfo()
    {
        return getStorageInfo(nodeId, contentPropName);
    }

    /**
     * Get Content Storage Properties using GET call on "nodes/{nodeId}/storage-info/{contentPropName}"
     * @param nodeId The nodeId
     * @param contentPropName The content property QNAME ie. "cm:content"
     * @return
     */
    public RestContentStorageInfoModel getStorageInfo(String nodeId, String contentPropName)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, basePath, nodeId, contentPropName);
        return restWrapper.processModel(RestContentStorageInfoModel.class, request);
    }

    /**
     * Send content to archive using POST call on "nodes/{nodeId}/storage-info/{contentPropName}/archive"
     * 
     * @param archiveContentRequest The request body
     * @return
     */
    public RestResponse requestArchiveContent(RestArchiveContentRequestModel archiveContentRequest)
    {
        return requestArchiveContent(nodeId, contentPropName, archiveContentRequest);
    }

    /**
     * Send content to archive using POST call on "nodes/{nodeId}/storage-info/{contentPropName}/archive"
     * 
     * @param nodeId The nodeId
     * @param contentPropName The content property QNAME ie. "cm:content"
     * @param archiveContentRequest The request body
     * @return
     */
    public RestResponse requestArchiveContent(String nodeId, String contentPropName, RestArchiveContentRequestModel archiveContentRequest)
    {
        RestRequest request = createRestRequestWithBody(HttpMethod.POST, archiveContentRequest, basePath + "/archive", nodeId, contentPropName);
        return restWrapper.process(request);
    }

    /**
     * Send content to archive using POST call on "nodes/{nodeId}/storage-info/{contentPropName}/archive-restore"
     * 
     * @param restoreArchivedContentRequest The request body
     * @return
     */
    public RestResponse requestRestoreContentFromArchive(RestRestoreArchivedContentRequestModel restoreArchivedContentRequest)
    {
        return requestRestoreContentFromArchive(nodeId, contentPropName, restoreArchivedContentRequest);
    }

    /**
     * Send content to archive using POST call on "nodes/{nodeId}/storage-info/{contentPropName}/archive-restore"
     * 
     * @param nodeId The nodeId
     * @param contentPropName The content property QNAME ie. "cm:content"
     * @param restoreArchivedContentRequest The request body
     * @return
     */
    public RestResponse requestRestoreContentFromArchive(String nodeId, String contentPropName, RestRestoreArchivedContentRequestModel restoreArchivedContentRequest)
    {
        RestRequest request = createRestRequestWithBody(HttpMethod.POST, restoreArchivedContentRequest, basePath + "/archive-restore", nodeId, contentPropName);
        return restWrapper.process(request);
    }
    
    private RestRequest createRestRequestWithBody(HttpMethod httpMethod, TestModel request, String path, String... pathParams)
    {
        if (request != null)
        {
            return RestRequest.requestWithBody(httpMethod, request.toJson(), path, pathParams);
        }
        else
        {
            return RestRequest.simpleRequest(httpMethod, path, pathParams);
        }
    }
}
