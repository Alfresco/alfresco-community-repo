/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.rm.requests;

import static org.alfresco.rest.core.RestRequest.requestWithBody;
import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.alfresco.rest.rm.util.ParameterCheck.mandatoryObject;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import com.google.gson.JsonObject;

import org.alfresco.rest.core.RestAPI;
import org.alfresco.rest.rm.model.site.RMSite;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *  File plan component REST API Wrapper
 *
 * @author Tuna Aksoy
 * @author Rodica Sutu
 * @since 1.0
 */
@Component
@Scope (value = "prototype")
public class RMSiteAPI extends RestAPI<RMSiteAPI>
{
    /**
     * Get the RM site
     *
     * @return The {@link RMSite} for the given file plan component id
     * @throws Exception for the following cases:
     * <ul>
     *  <li>Api Response code 400 Invalid parameter: GET request is supported only for the RM site</li>
     *  <li>Api Response code 401 If authentication failed</li>
     *  <li>Api Response code 409 If RM Site does not exist</li>
     *  <li>Api Response code default Unexpected error</li>
     * </ul>
     */
    public RMSite getSite() throws Exception
    {
        return usingRestWrapper().processModel(RMSite.class, simpleRequest(
                GET,
                "sites/rm"
        ));
    }

    /**
     * Create the RM site
     *
     * @param rmSiteProperties The properties of the rm site to be created
     * @return The {@link RMSite} with the given properties
     * @throws Exception for the following cases:
     * <ul>
     *  <li>Api Response code 400 Invalid parameter: title, or description exceed the maximum length; or siteBodyCreate invalid</li>
     *  <li>Api Response code 401 If authentication failed</
     *  <li>Api Response code 409 RM Site already exists</li>
     *  <li>Api Response code default Unexpected error</li>
     * </ul>
     */
    public RMSite createRMSite(JsonObject rmSiteProperties) throws Exception
    {
        mandatoryObject("rmSiteProperties", rmSiteProperties);

        return usingRestWrapper().processModel(RMSite.class, requestWithBody(
                POST,
                rmSiteProperties.toString(),
                "sites"
        ));
    }

    /**
     * Delete RM site
     * @throws Exception for the following cases:
     * <ul>
     *  <li>Api Response code 400 Invalid parameter: DELETE request is supported only for the RM site</li>
     *  <li>Api Response code 401 If authentication failed</
     *  <li>Api Response code 403 Current user does not have permission to delete the site that is visible to them.</li>
     *  <li>Api Response code 404 RM site does not exist</li>
     *  <li>Api Response code default Unexpected error</li>
     * </ul>
     */
    public void deleteRMSite() throws Exception
    {
        usingRestWrapper().processEmptyModel(simpleRequest(
                DELETE,
                "sites/rm"
        ));
    }

    /**
     * Update RM site
     *
     * @param rmSiteProperties The properties to be updated
     * @return The updated {@link RMSite}
     * @throws Exception for the following cases:
     * <ul>
     *  <li>Api Response code 400 the update request is invalid {@code rmSiteProperties} is invalid</li>
     *  <li>Api Response code 401 If authentication fails</li>
     *  <li>Api Response code 403 does not have permission to update {@code RMSite}</li>
     *  <li>Api Response code 404 {@code RMSite} does not exist</li>
     *  <li>Api Response code default Unexpected error,model integrity exception</li>
     * </ul>
     */
    public RMSite updateRMSite(JsonObject rmSiteProperties) throws Exception
    {
        mandatoryObject("rmSiteProperties", rmSiteProperties);

        return usingRestWrapper().processModel(RMSite.class, requestWithBody(
                PUT,
                rmSiteProperties.toString(),
                "sites/rm"
        ));
    }
}
