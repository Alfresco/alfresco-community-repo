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
package org.alfresco.rest.requests;

import static org.alfresco.rest.core.RestRequest.requestWithBody;
import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import com.google.gson.JsonObject;

import org.alfresco.rest.core.RestAPI;
import org.alfresco.rest.model.site.RMSite;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * FIXME: Document me :)
 */
@Component
@Scope (value = "prototype")
public class RMSiteAPI extends RestAPI
{
    /**
     * Get the RM site
     *
     * @return FIXME: Document me :)
     * @throws FIXME: Document me :)
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
     * @param rmSiteProperties FIXME: Document me :)
     * @return FIXME: Document me :)
     * @throws Exception FIXME: Document me :)
     */
    public RMSite createRMSite(JsonObject rmSiteProperties) throws Exception
    {
        return usingRestWrapper().processModel(RMSite.class, requestWithBody(
                POST,
                rmSiteProperties.toString(),
                "sites"
        ));
    }

    /**
     * Delete RM site
     *
     * @throws Exception FIXME: Document me :)
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
     * @param rmSiteProperties FIXME: Document me :)
     * @return FIXME: Document me :)
     * @throws Exception FIXME: Document me :)
     */
    public RMSite updateRMSite(JsonObject rmSiteProperties) throws Exception
    {
        return usingRestWrapper().processModel(RMSite.class, requestWithBody(
                PUT,
                rmSiteProperties.toString(),
                "sites/rm"
        ));
    }
}
