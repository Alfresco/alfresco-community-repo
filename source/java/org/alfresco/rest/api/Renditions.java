/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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

package org.alfresco.rest.api;

import org.alfresco.rest.api.model.Rendition;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Renditions API
 *
 * @author Jamal Kaabi-Mofrad
 */
public interface Renditions
{
    /**
     * Lists all available renditions includes those that have been created and those that are yet to be created.
     *
     * @param nodeId     the source node id
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return the rendition results
     */
    CollectionWithPagingInfo<Rendition> getRenditions(String nodeId, Parameters parameters);

    /**
     * Gets information about a rendition of a node in the repository.
     * If there is no rendition, then returns the available/registered rendition.
     *
     * @param nodeId      the source node id
     * @param renditionId the rendition id
     * @param parameters  the {@link Parameters} object to get the parameters passed into the request
     * @return the {@link Rendition} object
     */
    Rendition getRendition(String nodeId, String renditionId, Parameters parameters);

    /**
     * Creates a rendition for the given node asynchronously.
     *
     * @param nodeId     the source node id
     * @param rendition  the {@link Rendition} request
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     */
    void createRendition(String nodeId, Rendition rendition, Parameters parameters);

    /**
     * Downloads rendition.
     *
     * @param nodeId      the source node id
     * @param renditionId the rendition id
     * @param parameters  the {@link Parameters} object to get the parameters passed into the request
     * @return the rendition stream
     */
    BinaryResource getContent(String nodeId, String renditionId, Parameters parameters);

    String PARAM_STATUS = "status";
}
