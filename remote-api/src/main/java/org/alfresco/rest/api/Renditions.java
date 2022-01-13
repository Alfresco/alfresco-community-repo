/*
 * #%L
 * Alfresco Remote API
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

package org.alfresco.rest.api;

import org.alfresco.rest.api.model.Rendition;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.DirectAccessUrl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

import java.util.List;

/**
 * Renditions API
 *
 * @author Jamal Kaabi-Mofrad, janv
 */
public interface Renditions
{
    String PARAM_STATUS = "status";

    String PARAM_ATTACHMENT = "attachment";
    String PARAM_PLACEHOLDER = "placeholder";

    /**
     * Lists all available renditions includes those that have been created and those that are yet to be created.
     *
     * @param nodeRef    the source/live nodeRef
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return the rendition results
     */
    CollectionWithPagingInfo<Rendition> getRenditions(NodeRef nodeRef, Parameters parameters);

    /**
     * Lists all available renditions includes those that have been created and those that are yet to be created.
     *
     * @param nodeRef     the source/live nodeRef
     * @param versionId   the version id (aka version label)
     * @param parameters  the {@link Parameters} object to get the parameters passed into the request
     * @return the rendition results
     */
    CollectionWithPagingInfo<Rendition> getRenditions(NodeRef nodeRef, String versionId, Parameters parameters);

    /**
     * Gets information about a rendition of a node in the repository.
     * If there is no rendition, then returns the available/registered rendition.
     *
     * @param nodeRef     the source nodeRef, ie. live node
     * @param renditionId the rendition id
     * @param parameters  the {@link Parameters} object to get the parameters passed into the request
     * @return the {@link Rendition} object
     */
    Rendition getRendition(NodeRef nodeRef, String renditionId, Parameters parameters);

    /**
     * Gets information about a rendition of a node in the repository.
     * If there is no rendition, then returns the available/registered rendition.
     *
     * @param nodeRef     the source nodeRef, ie. live node
     * @param versionId   the version id (aka version label)
     * @param renditionId the rendition id
     * @param parameters  the {@link Parameters} object to get the parameters passed into the request
     * @return the {@link Rendition} object
     */
    Rendition getRendition(NodeRef nodeRef, String versionId, String renditionId, Parameters parameters);

    /**
     * Creates a rendition for the given node asynchronously.
     *
     * @param nodeRef    the source nodeRef, ie. live node
     * @param rendition  the {@link Rendition} request
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     */
    void createRendition(NodeRef nodeRef, Rendition rendition, Parameters parameters);

    /**
     * Creates a rendition for the given node - either async r sync
     *
     * @param nodeRef   the source nodeRef, ie. live node
     * @param rendition the {@link Rendition} request
     * @param executeAsync
     * @param parameters
     */
    void createRendition(NodeRef nodeRef, Rendition rendition, boolean executeAsync, Parameters parameters);

    /**
     * Creates a rendition for the given node - either async r sync
     *
     * @param nodeRef   the source nodeRef, ie. live node
     * @param versionId the version id (aka version label)
     * @param rendition the {@link Rendition} request
     * @param executeAsync
     * @param parameters
     */
    void createRendition(NodeRef nodeRef, String versionId, Rendition rendition, boolean executeAsync, Parameters parameters);

    /**
     * Creates renditions that don't already exist for the given node asynchronously.
     *
     * @param nodeRef    the source nodeRef, ie. live node
     * @param renditions the list of {@link Rendition} requests
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @throws NotFoundException if any of the rendition id do not exist.
     * @throws ConstraintViolatedException if all of the renditions already exist.
     */
    void createRenditions(NodeRef nodeRef, List<Rendition> renditions, Parameters parameters)
            throws NotFoundException, ConstraintViolatedException;

    /**
     * Creates renditions that don't already exist for the given node asynchronously.
     *
     * @param nodeRef    the source nodeRef, ie. live node
     * @param versionId the version id (aka version label)
     * @param renditions the list of {@link Rendition} requests
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @throws NotFoundException if any of the rendition id do not exist.
     * @throws ConstraintViolatedException if all of the renditions already exist.
     */
    void createRenditions(NodeRef nodeRef, String versionId, List<Rendition> renditions, Parameters parameters)
            throws NotFoundException, ConstraintViolatedException;

    /**
     * Delete the rendition node.
     *
     * @param nodeRef       the source nodeRef, ie. live node
     * @param renditionId   the rendition id
     * @param parameters    the {@link Parameters} object to get the parameters passed into the request
     */
    void deleteRendition(NodeRef nodeRef, String renditionId, Parameters parameters);

    /**
     * Delete the rendition node.
     *
     * @param nodeRef       the source nodeRef, ie. live node
     * @param versionId     the version id (aka version label)
     * @param renditionId   the rendition id
     * @param parameters    the {@link Parameters} object to get the parameters passed into the request
     */
    void deleteRendition(NodeRef nodeRef, String versionId, String renditionId, Parameters parameters);

    /**
     * Downloads rendition.
     *
     * @param nodeRef     the source nodeRef, ie. live node
     * @param renditionId the rendition id
     * @param parameters  the {@link Parameters} object to get the parameters passed into the request
     * @return the rendition stream
     */
    BinaryResource getContent(NodeRef nodeRef, String renditionId, Parameters parameters);

    /**
     * Downloads rendition.
     *
     * @param nodeRef     the source nodeRef, ie. live node
     * @param versionId   the version id (aka version label)
     * @param renditionId the rendition id
     * @param parameters  the {@link Parameters} object to get the parameters passed into the request
     * @return the rendition stream
     */
    BinaryResource getContent(NodeRef nodeRef, String versionId, String renditionId, Parameters parameters);

    /**
     * Downloads rendition.
     *
     * @param nodeRef     the source nodeRef, ie. live node
     * @param renditionId the rendition id
     * @param parameters  the {@link Parameters} object to get the parameters passed into the request
     * @return the rendition stream
     */
    BinaryResource getContentNoValidation(NodeRef nodeRef, String renditionId, Parameters parameters);

    /**
     * Downloads rendition.
     *
     * @param nodeRef     the source nodeRef, ie. live node
     * @param versionId   the version id (aka version label)
     * @param renditionId the rendition id
     * @param parameters  the {@link Parameters} object to get the parameters passed into the request
     * @return the rendition stream
     */
    BinaryResource getContentNoValidation(NodeRef nodeRef, String versionId, String renditionId, Parameters parameters);

    /**
     * Gets a presigned URL to directly access content.
     * @param nodeId      the node id for which to obtain the direct access {@code URL}
     * @param versionId   the version id (aka version label)
     * @param renditionId the rendition id
     * @param attachment  {@code true} if an attachment {@code URL} is requested, {@code false} for an embedded {@code URL}
     * @return            a direct access {@code URL} object for the content
     */
    default DirectAccessUrl requestContentDirectUrl(String nodeId, String versionId, String renditionId, boolean attachment)
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        return requestContentDirectUrl(nodeRef, versionId, renditionId, attachment);
    }

    /**
     * Gets a presigned URL to directly access content.
     * @param nodeId      the node id for which to obtain the direct access {@code URL}
     * @param versionId   the version id (aka version label)
     * @param renditionId the rendition id
     * @param attachment  {@code true} if an attachment {@code URL} is requested, {@code false} for an embedded {@code URL}
     * @param validFor    the time at which the direct access {@code URL} will expire
     * @return            a direct access {@code URL} object for the content
     */
    default DirectAccessUrl requestContentDirectUrl(String nodeId, String versionId, String renditionId, boolean attachment, Long validFor)
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        return requestContentDirectUrl(nodeRef, versionId, renditionId, attachment, validFor);
    }

    /**
     * Gets a presigned URL to directly access content.
     * @param nodeRef     the node reference for which to obtain the direct access {@code URL}
     * @param versionId   the version id (aka version label)
     * @param renditionId the rendition id
     * @param attachment  {@code true} if an attachment {@code URL} is requested, {@code false} for an embedded {@code URL}
     * @return            a direct access {@code URL} object for the content.
     */
    default DirectAccessUrl requestContentDirectUrl(NodeRef nodeRef, String versionId, String renditionId, boolean attachment)
    {
        return requestContentDirectUrl(nodeRef, versionId, renditionId, attachment, null);
    }

    /**
     * Gets a presigned URL to directly access content.
     * @param nodeRef     the node reference for which to obtain the direct access {@code URL}
     * @param versionId   the version id (aka version label)
     * @param renditionId the rendition id
     * @param attachment  {@code true} if an attachment {@code URL} is requested, {@code false} for an embedded {@code URL}
     * @param validFor    the time at which the direct access {@code URL} will expire
     * @return            a direct access {@code URL} object for the content.
     */
    DirectAccessUrl requestContentDirectUrl(NodeRef nodeRef, String versionId, String renditionId, boolean attachment, Long validFor);
}

