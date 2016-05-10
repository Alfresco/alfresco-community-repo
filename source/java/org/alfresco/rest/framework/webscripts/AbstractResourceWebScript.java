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
package org.alfresco.rest.framework.webscripts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.web.scripts.content.ContentStreamer;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.HttpMethodSupport;
import org.alfresco.rest.framework.core.ResourceInspector;
import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceOperation;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.resource.actions.ActionExecutor;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.alfresco.rest.framework.resource.content.FileBinaryResource;
import org.alfresco.rest.framework.resource.content.NodeBinaryResource;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.HttpMethod;

/**
 * Webscript that handles the request for and execution of a Resource
 * 
 * 1) Finds a resource
 * 2) Extracts params
 * 3) Executes params on a resource
 * 4) Post processes the response to add embeds or projected relationship
 * 5) Renders the response
 * 
 * @author Gethin James
 * @author janv
 */
// TODO for requests that pass in input streams e.g. binary content for workflow, this is going to need a way to re-read the input stream a la
// code in RepositoryContainer due to retrying transaction logic
public abstract class AbstractResourceWebScript extends ApiWebScript implements HttpMethodSupport, ActionExecutor
{
    private static Log logger = LogFactory.getLog(AbstractResourceWebScript.class);

    protected ResourceLocator locator;
    private HttpMethod httpMethod;
    private ParamsExtractor paramsExtractor;
    private ContentStreamer streamer;
    protected ResourceWebScriptHelper helper;

    public final static String HDR_NAME_CONTENT_DISPOSITION = "Content-Disposition";

    @SuppressWarnings("rawtypes")
    @Override
    public void execute(final Api api, final WebScriptRequest req, final WebScriptResponse res) throws IOException
    {
        try
        {
            final Map<String, Object> respons = new HashMap<String, Object>();
            final Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
            final ResourceWithMetadata resource = locator.locateResource(api,templateVars, httpMethod);
            final Params params = paramsExtractor.extractParams(resource.getMetaData(),req);
            final boolean isReadOnly = HttpMethod.GET==httpMethod;

            //This execution usually takes place in a Retrying Transaction (see subclasses)
            final Object toSerialize = execute(resource, params, res, isReadOnly);
            
            //Outside the transaction.
            if (toSerialize != null)
            {
                if (toSerialize instanceof BinaryResource)
                {
                    // TODO review (experimental) - can we move earlier & wrap complete execute ? Also for QuickShare (in MT/Cloud) needs to be tenant for the nodeRef (TBC).
                    boolean noAuth = false;

                    if (BinaryResourceAction.Read.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        noAuth = resource.getMetaData().isNoAuth(BinaryResourceAction.Read.class);
                    }
                    else if (RelationshipResourceBinaryAction.Read.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        noAuth = resource.getMetaData().isNoAuth(RelationshipResourceBinaryAction.Read.class);
                    }
                    else
                    {
                        logger.warn("Unexpected");
                    }

                    if (noAuth)
                    {
                        String networkTenantDomain = TenantUtil.getCurrentDomain();

                        TenantUtil.runAsSystemTenant(new TenantUtil.TenantRunAsWork<Void>()
                        {
                            public Void doWork() throws Exception
                            {
                                streamResponse(req, res, (BinaryResource) toSerialize);
                                return null;
                            }
                        }, networkTenantDomain);
                    }
                    else
                    {
                        streamResponse(req, res, (BinaryResource) toSerialize);
                    }
                }
                else
                {
                    renderJsonResponse(res, toSerialize);
                }
            }

        }
        catch (IntegrityException intException)
        {
            renderErrorResponse(resolveException(intException), res);
        }
        catch (ApiException apiException)
        {
            renderErrorResponse(resolveException(apiException), res);
        }
        catch (WebScriptException webException)
        {
            renderErrorResponse(resolveException(webException), res);
        }
        catch (RuntimeException runtimeException)
        {
            renderErrorResponse(resolveException(runtimeException), res);
        }
    }

    public Object execute(final ResourceWithMetadata resource, final Params params, final WebScriptResponse res, boolean isReadOnly)
    {
        final String entityCollectionName = ResourceInspector.findEntityCollectionNameName(resource.getMetaData());
        final ResourceOperation operation = resource.getMetaData().getOperation(getHttpMethod());
        final WithResponse callBack = new WithResponse(operation.getSuccessStatus(),DEFAULT_JSON_CONTENT,ApiWebScript.CACHE_NEVER);
        Object toReturn = transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {

                        Object result = executeAction(resource, params, callBack);
                        if (result instanceof BinaryResource)
                        {
                            return result; //don't postprocess it.
                        }
                        return helper.processAdditionsToTheResponse(res, resource.getMetaData().getApi(), entityCollectionName, params, result);
                    }
                }, isReadOnly, true);
        setResponse(res,callBack);
        return toReturn;
    }

    protected void streamResponse(final WebScriptRequest req, final WebScriptResponse res, BinaryResource resource) throws IOException
    {
        if (resource instanceof FileBinaryResource)
        {
            FileBinaryResource fileResource = (FileBinaryResource) resource;
            // if requested, set attachment
            setAttachment(res, fileResource.getAttachFileName());
            streamer.streamContent(req, res, fileResource.getFile(), null, false, null, null);
        }
        else if (resource instanceof NodeBinaryResource)
        {
            NodeBinaryResource nodeResource = (NodeBinaryResource) resource;
            ContentInfo contentInfo = nodeResource.getContentInfo();
            setContentInfoOnResponse(res,contentInfo);
            // if requested, set attachment
            setAttachment(res, nodeResource.getAttachFileName());
            streamer.streamContent(req, res, nodeResource.getNodeRef(), nodeResource.getPropertyQName(), false, null, null);        
        }
    }

    private void setAttachment(final WebScriptResponse res, final String attachFileName)
    {
        if (StringUtils.isNotEmpty(attachFileName))
        {
            String headerValue = "attachment; filename=\"" + attachFileName + "\"; filename*=UTF-8''" + URLEncoder.encode(attachFileName);
            res.setHeader(HDR_NAME_CONTENT_DISPOSITION, headerValue);
        }
    }

    /**
     * The response status must be set before the response is written by Jackson (which will by default close and commit the response).
     * In a r/w txn, web script buffered responses ensure that it doesn't really matter but for r/o txns this is important.
     *
     * If you set content information via the contentInfo object and ALSO the headers then "headers" will win because they are
     * set last.
     *
     * @param res
     * @param status
     * @param cache
     * @param contentInfo
     * @param headers
     */
    public void setResponse(final WebScriptResponse res, int status, Cache cache, ContentInfo contentInfo,  Map<String, List<String>> headers)
    {
        res.setStatus(status);
        if (cache != null) res.setCache(cache);
        setContentInfoOnResponse(res,contentInfo);
        if (headers != null && !headers.isEmpty())
        {
            for (Map.Entry<String, List<String>> header:headers.entrySet())
            {
                for (int i=0; i < header.getValue().size(); i++) {
                    if (i==0)
                    {
                        //If its the first one then set the header overwriting.
                        res.setHeader(header.getKey(), header.getValue().get(i));
                    }
                    else
                    {
                        //If its not the first one than update the header
                        res.addHeader(header.getKey(), header.getValue().get(i));
                    }
                }
            }
        }
    }

    protected void setResponse(final WebScriptResponse res, WithResponse withResponse)
    {
        setResponse(res, withResponse.getStatus(), withResponse.getCache(), withResponse.getContentInfo(), withResponse.getHeaders());
    }

    /**
     * Renders the result of an execution.
     * 
     * @param res WebScriptResponse
     * @param toSerialize result of an execution
     * @throws IOException
     */
    protected void renderJsonResponse(final WebScriptResponse res, final Object toSerialize)
                throws IOException
    {
        jsonHelper.withWriter(res.getOutputStream(), new JacksonHelper.Writer()
        {
            @Override
            public void writeContents(JsonGenerator generator, ObjectMapper objectMapper)
                        throws JsonGenerationException, JsonMappingException, IOException
            {
                objectMapper.writeValue(generator, toSerialize);
            }
        });
    }

    public void setLocator(ResourceLocator locator)
    {
        this.locator = locator;
    }

    public void setHttpMethod(HttpMethod httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    public void setParamsExtractor(ParamsExtractor paramsExtractor)
    {
        this.paramsExtractor = paramsExtractor;
    }

    public void setHelper(ResourceWebScriptHelper helper)
    {
        this.helper = helper;
    }

    public HttpMethod getHttpMethod()
    {
        return this.httpMethod;
    }

    public void setStreamer(ContentStreamer streamer)
    {
        this.streamer = streamer;
    }
}
