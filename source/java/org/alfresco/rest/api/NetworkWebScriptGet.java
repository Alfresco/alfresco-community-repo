/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import java.io.IOException;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.model.PersonNetwork;
import org.alfresco.rest.api.networks.NetworksEntityResource;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper.Writer;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.webscripts.ApiWebScript;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.extensions.webscripts.Format;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class NetworkWebScriptGet extends ApiWebScript
{
	private Networks networks;
    private ResourceWebScriptHelper helper;
    
	public void setHelper(ResourceWebScriptHelper helper)
	{
		this.helper = helper;
	}

	public void setNetworks(Networks networks)
	{
		this.networks = networks;
	}

    @Override
    public void execute(final Api api, final WebScriptRequest req, final WebScriptResponse res) throws IOException
    {
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    // apply content type
                    res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");

                    assistant.getJsonHelper().withWriter(res.getOutputStream(), new Writer()
                    {
                        @Override
                        public void writeContents(JsonGenerator generator, ObjectMapper objectMapper)
                                    throws JsonGenerationException, JsonMappingException, IOException
                        {
                            String personId = AuthenticationUtil.getFullyAuthenticatedUser();
                            String networkId = TenantUtil.getCurrentDomain();
            
                            PersonNetwork networkMembership = networks.getNetwork(personId, networkId);
                            if(networkMembership != null)
                            {
                                // TODO this is not ideal, but the only way to populate the embedded network entities (this would normally be
                                // done automatically by the api framework).
                                Object wrapped = helper.processAdditionsToTheResponse(res, Api.ALFRESCO_PUBLIC, NetworksEntityResource.NAME, Params.valueOf(personId, null, req), networkMembership);
                
                                objectMapper.writeValue(generator, wrapped);
                            }
                            else
                            {
                                throw new EntityNotFoundException(networkId);
                            }
                        }
                    });

                    return null;
                }
            }, true, true);
        }
        catch (ApiException | WebScriptException apiException)
        {
            assistant.renderException(apiException, res);
        }
        catch (RuntimeException runtimeException)
        {
            assistant.renderException(runtimeException, res);
        }
    }
}
