/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.requests.gscore.api;

import static org.alfresco.rest.core.RestRequest.requestWithBody;
import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryObject;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;

import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.site.RMSite;
import org.alfresco.rest.rm.community.requests.RMModelRequest;

/**
 * RM Site REST API Wrapper
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class RMSiteAPI extends RMModelRequest
{
    /**
     * Constructor
     *
     * @param rmRestWrapper RM REST Wrapper
     */
    public RMSiteAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    /**
     * Get the RM site
     *
     * @return The {@link RMSite} for the given file plan component id
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>Api Response code 400 Invalid parameter: GET request is supported only for the RM site</li>
     *  <li>Api Response code 401 If authentication failed</li>
     *  <li>Api Response code 409 If RM Site does not exist</li>
     *  <li>Api Response code default Unexpected error</li>
     * </ul>
     */
    public RMSite getSite()
    {
        return getRmRestWrapper().processModel(RMSite.class, simpleRequest(
                GET,
                "gs-sites/rm"
        ));
    }

    /**
     * Create the RM site
     *
     * @param rmSiteModel The properties of the rm site to be created
     * @return The {@link RMSite} with the given properties
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>Api Response code 400 Invalid parameter: title, or description exceed the maximum length; or siteBodyCreate invalid</li>
     *  <li>Api Response code 401 If authentication failed</
     *  <li>Api Response code 409 RM Site already exists</li>
     *  <li>Api Response code default Unexpected error</li>
     * </ul>
     */
    public RMSite createRMSite(RMSite rmSiteModel)
    {
        mandatoryObject("rmSiteModel", rmSiteModel);

        return getRmRestWrapper().processModel(RMSite.class, requestWithBody(
                POST,
                toJson(rmSiteModel),
                "gs-sites"
        ));
    }

    /**
     * Delete RM site
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>Api Response code 400 Invalid parameter: DELETE request is supported only for the RM site</li>
     *  <li>Api Response code 401 If authentication failed</
     *  <li>Api Response code 403 Current user does not have permission to delete the site that is visible to them.</li>
     *  <li>Api Response code 404 RM site does not exist</li>
     *  <li>Api Response code default Unexpected error</li>
     * </ul>
     */
    public void deleteRMSite()
    {
        getRmRestWrapper().processEmptyModel(simpleRequest(
                DELETE,
                "gs-sites/rm"
        ));
    }

    /**
     * Update RM site
     *
     * @param rmSiteModel The properties to be updated
     * @return The updated {@link RMSite}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>Api Response code 400 the update request is invalid {@code rmSiteModel} is invalid</li>
     *  <li>Api Response code 401 If authentication fails</li>
     *  <li>Api Response code 403 does not have permission to update {@code RMSite}</li>
     *  <li>Api Response code 404 {@code RMSiteModel} does not exist</li>
     *  <li>Api Response code default Unexpected error,model integrity exception</li>
     * </ul>
     */
    public RMSite updateRMSite(RMSite rmSiteModel)
    {
        mandatoryObject("rmSiteProperties", rmSiteModel);

        return getRmRestWrapper().processModel(RMSite.class, requestWithBody(
                PUT,
                toJson(rmSiteModel),
                "gs-sites/rm"
        ));
    }

    /**
     * Checks if the RM site exists or not
     *
     * @return <code>true</code> if the RM site exists, <code>false</code> otherwise
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>Api Response code 400 Invalid parameter: GET request is supported only for the RM site</li>
     *  <li>Api Response code 401 If authentication failed</li>
     *  <li>Api Response code 409 If RM Site does not exist</li>
     *  <li>Api Response code default Unexpected error</li>
     * </ul>
     */
    public boolean existsRMSite()
    {
        getSite();
        return getRmRestWrapper().getStatusCode().equals(String.valueOf(OK.value()));
    }
}
