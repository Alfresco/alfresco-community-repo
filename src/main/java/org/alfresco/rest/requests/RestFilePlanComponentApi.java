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

import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.springframework.http.HttpMethod.GET;

import org.alfresco.rest.core.RestAPI;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestFilePlanComponentModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * FIXME: Document me :)
 *
 * @author Tuna Aksoy
 * @since 1.0
 */
@Component
@Scope(value = "prototype")
public class RestFilePlanComponentApi extends RestAPI
{
    public RestFilePlanComponentModel getFilePlanComponent(String filePlanComponentId) throws Exception
    {
        RestRequest request = simpleRequest(GET, "fileplan-components/{fileplanComponentId}", filePlanComponentId);
        return usingRestWrapper().processModel(RestFilePlanComponentModel.class, request);
    }
}
