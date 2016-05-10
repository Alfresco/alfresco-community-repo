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

import org.alfresco.rest.api.model.QuickShareLink;
import org.alfresco.rest.api.model.QuickShareLinkEmailRequest;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

import java.util.List;

/**
 * Centralises access to quick share services and maps between representations.
 *
 * @author janv
 * @author Jamal Kaabi-Mofrad
 * 
 * @since publicapi1.0
 */
public interface QuickShareLinks
{
    /**
     * Returns limited metadata regarding the shared (content) link.
     *
     * Note: does *not* require authenticated access for (public) shared link.
     */
    QuickShareLink readById(String sharedId, Parameters parameters);

    /**
     * Download file content (or rendition content) via shared link.
     *
     * Note: does *not* require authenticated access for (public) shared link.
     *
     * @param sharedId
     * @param renditionId - optional
     * @param parameters {@link Parameters}
     * @return
     * @throws EntityNotFoundException
     */
    BinaryResource readProperty(String sharedId, String renditionId, Parameters parameters) throws EntityNotFoundException;

    /**
     * Delete the shared link.
     *
     * Once deleted, the shared link will no longer exist hence get/download will no longer work (ie. return 404).
     * If the link is later re-created then a new unique shared id will be generated.
     *
     * Requires authenticated access.
     *
     * @param sharedId String id of the quick share
     */
    void delete(String sharedId, Parameters parameters);

    /**
     * Create quick share.
     *
     * Requires authenticated access.
     *
     * @param nodeIds
     * @param parameters
     * @return
     */
    List<QuickShareLink> create(List<QuickShareLink> nodeIds, Parameters parameters);

    /**
     * Notifies users by email that a content has been shared with them.
     *
     * @param sharedId     The string id of the quick share
     * @param emailRequest The email details including its template details
     * @param parameters   The {@link Parameters} object to get the parameters passed into the request
     */
    void emailSharedLink(String sharedId, QuickShareLinkEmailRequest emailRequest, Parameters parameters);

    /**
     * Find (search) for shared links visible to current user.
     * Optionally filter by "sharedByUser/id" (if -me- then filter by current user).
     *
     * @param parameters
     * @return
     */
    CollectionWithPagingInfo<QuickShareLink> findLinks(Parameters parameters);

    /**
     * API Constants - query parameters, etc
     */
    String PARAM_SHAREDBY = "sharedByUser";

    String PARAM_INCLUDE_ALLOWABLEOPERATIONS = Nodes.PARAM_INCLUDE_ALLOWABLEOPERATIONS;
}