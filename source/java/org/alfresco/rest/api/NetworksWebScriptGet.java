package org.alfresco.rest.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.model.PersonNetwork;
import org.alfresco.rest.api.networks.NetworksEntityResource;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper.Writer;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
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

/**
 * A webscript that returns the authenticated user's network memberships.
 * 
 * @author steveglover
 *
 */
public class NetworksWebScriptGet extends ApiWebScript
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
                    final Paging paging = ResourceWebScriptHelper.findPaging(req);
            
                    // apply content type
                    res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");

                    assistant.getJsonHelper().withWriter(res.getOutputStream(), new Writer()
                    {
                        @Override
                        public void writeContents(JsonGenerator generator, ObjectMapper objectMapper)
                                    throws JsonGenerationException, JsonMappingException, IOException
                        {
                            List<Object> entities = new ArrayList<Object>();
            
                            String personId = AuthenticationUtil.getFullyAuthenticatedUser();
                            
                            CollectionWithPagingInfo<PersonNetwork> networkMemberships = networks.getNetworks(personId, paging);
                            for (PersonNetwork networkMember : networkMemberships.getCollection())
                            {
                                // TODO this is not ideal, but the only way to populate the embedded network entities (this would normally be
                                // done automatically by the api framework).
                                Object wrapped = helper.processAdditionsToTheResponse(res, Api.ALFRESCO_PUBLIC, NetworksEntityResource.NAME, Params.valueOf(personId, null, req), networkMember);
                                entities.add(wrapped);
                            }
            
                            objectMapper.writeValue(generator, CollectionWithPagingInfo.asPaged(paging, entities));
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