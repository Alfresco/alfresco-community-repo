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
    private static final String BASE_PATH = "nodes/{nodeId}/storage-info/{contentPropName}";
    private static final String VERSIONS_BASE_PATH = "nodes/{nodeId}/versions/{versionId}/storage-info/{contentPropName}";
    private static final String ARCHIVE_PATH_SUFFIX = "/archive";
    private static final String ARCHIVE_RESTORE_PATH_SUFFIX = "/archive-restore";

    private String nodeId;
    private String contentPropName;
    private String versionId;

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

    public ContentStorageInformation withVersionId(String versionId)
    {
        this.versionId = versionId;
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
     * Get Content Version Storage Properties using GET call on "nodes/{nodeId}/versions/{versionId}/storage-info/{contentPropName}"
     * @return
     */
    public RestContentStorageInfoModel getVersionStorageInfo()
    {
        return getVersionStorageInfo(nodeId, versionId, contentPropName);
    }

    /**
     * Get Content Storage Properties using GET call on "nodes/{nodeId}/storage-info/{contentPropName}"
     * @param nodeId The nodeId
     * @param contentPropName The content property QNAME ie. "cm:content"
     * @return
     */
    public RestContentStorageInfoModel getStorageInfo(String nodeId, String contentPropName)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, BASE_PATH, nodeId, contentPropName);
        return restWrapper.processModel(RestContentStorageInfoModel.class, request);
    }

    /**
     * Get Content Version Storage Properties using GET call on "nodes/{nodeId}/versions/{versionId}/storage-info/{contentPropName}"
     * @param nodeId The nodeId
     * @param versionId The versionId
     * @param contentPropName The content property QNAME ie. "cm:content"
     * @return object of {@link RestContentStorageInfoModel}
     */
    public RestContentStorageInfoModel getVersionStorageInfo(String nodeId, String versionId, String contentPropName)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, VERSIONS_BASE_PATH, nodeId, versionId, contentPropName);
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
     * Send version content to archive using POST call on "nodes/{nodeId}/versions/{versionId}/storage-info/{contentPropName}/archive"
     *
     * @param archiveContentRequest The request body
     * @return
     */
    public RestResponse requestArchiveVersionContent(RestArchiveContentRequestModel archiveContentRequest)
    {
        return requestArchiveVersionContent(nodeId, contentPropName, versionId, archiveContentRequest);
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
        RestRequest request = createRestRequestWithBody(HttpMethod.POST, archiveContentRequest, BASE_PATH + ARCHIVE_PATH_SUFFIX, nodeId, contentPropName);
        return restWrapper.process(request);
    }

    /**
     * Send version content to archive using POST call on "nodes/{nodeId}/versions/{versionId}/storage-info/{contentPropName}/archive"
     *
     * @param nodeId The nodeId
     * @param contentPropName The content property QNAME ie. "cm:content"
     * @param versionId  The versionId
     * @param archiveContentRequest The request body
     * @return
     */
    public RestResponse requestArchiveVersionContent(String nodeId, String contentPropName, String versionId, RestArchiveContentRequestModel archiveContentRequest)
    {
        RestRequest request = createRestRequestWithBody(HttpMethod.POST, archiveContentRequest, VERSIONS_BASE_PATH + ARCHIVE_PATH_SUFFIX, nodeId, versionId, contentPropName);
        return restWrapper.process(request);
    }

    /**
     * Restore content from archive using POST call on "nodes/{nodeId}/storage-info/{contentPropName}/archive-restore"
     * 
     * @param restoreArchivedContentRequest The request body
     * @return
     */
    public RestResponse requestRestoreContentFromArchive(RestRestoreArchivedContentRequestModel restoreArchivedContentRequest)
    {
        return requestRestoreContentFromArchive(nodeId, contentPropName, restoreArchivedContentRequest);
    }

    /**
     * Restore version content from archive using POST call on "nodes/{nodeId}/storage-info/versions/{versionId}/{contentPropName}/archive-restore"
     *
     * @param restoreArchivedContentRequest The request body
     * @return
     */
    public RestResponse requestRestoreVersionContentFromArchive(RestRestoreArchivedContentRequestModel restoreArchivedContentRequest)
    {
        return requestRestoreVersionContentFromArchive(nodeId, contentPropName, versionId, restoreArchivedContentRequest);
    }

    /**
     * Restore content from archive using POST call on "nodes/{nodeId}/storage-info/{contentPropName}/archive-restore"
     * 
     * @param nodeId The nodeId
     * @param contentPropName The content property QNAME ie. "cm:content"
     * @param restoreArchivedContentRequest The request body
     * @return
     */
    public RestResponse requestRestoreContentFromArchive(String nodeId, String contentPropName, RestRestoreArchivedContentRequestModel restoreArchivedContentRequest)
    {
        RestRequest request = createRestRequestWithBody(HttpMethod.POST, restoreArchivedContentRequest, BASE_PATH +
                ARCHIVE_RESTORE_PATH_SUFFIX, nodeId, contentPropName);
        return restWrapper.process(request);
    }

    /**
     * Restore version content from archive using POST call on "nodes/{nodeId}/storage-info/{contentPropName}/archive-restore"
     *
     * @param nodeId The nodeId
     * @param contentPropName The content property QNAME ie. "cm:content"
     * @param versionId  The versionId
     * @param restoreArchivedContentRequest The request body
     * @return
     */
    public RestResponse requestRestoreVersionContentFromArchive(String nodeId, String contentPropName, String versionId,
                                                                RestRestoreArchivedContentRequestModel restoreArchivedContentRequest)
    {
        RestRequest request = createRestRequestWithBody(HttpMethod.POST, restoreArchivedContentRequest, VERSIONS_BASE_PATH +
                ARCHIVE_RESTORE_PATH_SUFFIX, nodeId, versionId, contentPropName);
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
