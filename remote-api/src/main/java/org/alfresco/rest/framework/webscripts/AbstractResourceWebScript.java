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
package org.alfresco.rest.framework.webscripts;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.metrics.rest.RestMetricsReporter;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.web.scripts.BufferedRequest;
import org.alfresco.repo.web.scripts.content.ContentStreamer;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.HttpMethodSupport;
import org.alfresco.rest.framework.core.ResourceInspector;
import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceOperation;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.ArchivedContentException;
import org.alfresco.rest.framework.resource.actions.ActionExecutor;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.CacheDirective;
import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.alfresco.rest.framework.resource.content.FileBinaryResource;
import org.alfresco.rest.framework.resource.content.NodeBinaryResource;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.tools.ResponseWriter;
import org.alfresco.service.cmr.repository.ArchivedIOException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public abstract class AbstractResourceWebScript extends ApiWebScript implements HttpMethodSupport, ActionExecutor, ResponseWriter
{
    private static Log logger = LogFactory.getLog(AbstractResourceWebScript.class);

    protected ResourceLocator locator;
    private HttpMethod httpMethod;
    private ParamsExtractor paramsExtractor;
    private ContentStreamer streamer;
    protected ResourceWebScriptHelper helper;

    private static final String HEADER_CONTENT_LENGTH = "Content-Length";

    @SuppressWarnings("rawtypes")
    @Override
    public void execute(final Api api, final WebScriptRequest req, final WebScriptResponse res) throws IOException
    {
        long startTime = System.currentTimeMillis();
        
        try
        {
            final Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
            final ResourceWithMetadata resource = locator.locateResource(api,templateVars, httpMethod);
            final boolean isReadOnly = HttpMethod.GET==httpMethod;

            // MNT-20308 - allow write transactions for authentication api
            RetryingTransactionHelper transHelper = getTransactionHelper(resource.getMetaData().getApi().getName());

            // encapsulate script within transaction
            RetryingTransactionHelper.RetryingTransactionCallback<Object> work = new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
            {
                @Override
                public Object execute() throws Throwable
                {
                    try
                    {
                        final Params params = paramsExtractor.extractParams(resource.getMetaData(), req);
                        return AbstractResourceWebScript.this.execute(resource, params, res, isReadOnly);
                    }
                    catch (Exception e)
                    {
                        if (req instanceof BufferedRequest)
                        {
                            // Reset the request in case of a transaction retry
                            ((BufferedRequest) req).reset();
                        }

                        // re-throw original exception for retry
                        throw e;
                    }
                }
            };

            //This execution usually takes place in a Retrying Transaction (see subclasses)
            final Object toSerialize = transHelper.doInTransaction(work, isReadOnly, true);

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
                    renderJsonResponse(res, toSerialize, assistant.getJsonHelper());
                }
            }

        }
        catch (ContentIOException cioe)
        {
            handleContentIOException(res, cioe); 
        }
        catch (AlfrescoRuntimeException | ApiException | WebScriptException xception )
        {
            renderException(xception, res, assistant);
        }
        catch (RuntimeException runtimeException)
        {
            renderException(runtimeException, res, assistant);
        }
        finally
        {
            reportExecutionTimeMetric(startTime, req.getServicePath());
        }
    }

    public Object execute(final ResourceWithMetadata resource, final Params params, final WebScriptResponse res, boolean isReadOnly)
    {
        final String entityCollectionName = ResourceInspector.findEntityCollectionNameName(resource.getMetaData());
        final ResourceOperation operation = resource.getMetaData().getOperation(getHttpMethod());
        final WithResponse callBack = new WithResponse(operation.getSuccessStatus(), DEFAULT_JSON_CONTENT,CACHE_NEVER);

        // MNT-20308 - allow write transactions for authentication api
        RetryingTransactionHelper transHelper = getTransactionHelper(resource.getMetaData().getApi().getName());

        Object toReturn = transHelper.doInTransaction(
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
                }, isReadOnly, false);
        setResponse(res,callBack);
        return toReturn;
    }

    private void handleContentIOException(final WebScriptResponse res, ContentIOException exception) throws IOException
    {
        // If the Content-Length is not set back to -1 any client will expect to receive binary and will hang until it times out
        res.setHeader(HEADER_CONTENT_LENGTH, String.valueOf(-1));
        if (exception instanceof ArchivedIOException)
        {
            renderException(new ArchivedContentException(exception.getMsgId(), exception), res, assistant);
        }
        else
        {
            renderException(exception, res, assistant);
        }
    }

    protected RetryingTransactionHelper getTransactionHelper(String api)
    {
        RetryingTransactionHelper transHelper = transactionService.getRetryingTransactionHelper();
        if (api.equals("authentication"))
        {
            transHelper.setForceWritable(true);
        }
        return transHelper;
    }

    protected void streamResponse(final WebScriptRequest req, final WebScriptResponse res, BinaryResource resource) throws IOException
    {
        if (resource instanceof FileBinaryResource)
        {
            FileBinaryResource fileResource = (FileBinaryResource) resource;
            // if requested, set attachment
            boolean attach = StringUtils.isNotEmpty(fileResource.getAttachFileName());
            Map<String, Object> model = getModelForCacheDirective(fileResource.getCacheDirective());
            streamer.streamContent(req, res, fileResource.getFile(), null, attach, fileResource.getAttachFileName(), model);
        }
        else if (resource instanceof NodeBinaryResource)
        {
            NodeBinaryResource nodeResource = (NodeBinaryResource) resource;
            ContentInfo contentInfo = nodeResource.getContentInfo();
            setContentInfoOnResponse(res, contentInfo);
            // if requested, set attachment
            boolean attach = StringUtils.isNotEmpty(nodeResource.getAttachFileName());
            Map<String, Object> model = getModelForCacheDirective(nodeResource.getCacheDirective());
            streamer.streamContent(req, res, nodeResource.getNodeRef(), nodeResource.getPropertyQName(), attach, nodeResource.getAttachFileName(), model);
        }

    }

    private void reportExecutionTimeMetric(final long startTime, final String servicePath)
    {
        try
        {
            final RestMetricsReporter restMetricsReporter = assistant.getRestMetricsReporter();
            if (restMetricsReporter != null)
            {
                long delta = System.currentTimeMillis() - startTime;
                restMetricsReporter.reportRestRequestExecutionTime(delta, httpMethod.toString(), servicePath);
            }
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Could not report rest api metric:" + e.getMessage(), e);
            }
        }
    }

    private static Map<String, Object> getModelForCacheDirective(CacheDirective cacheDirective)
    {
        if (cacheDirective != null)
        {
            return Collections.singletonMap(ContentStreamer.KEY_CACHE_DIRECTIVE, (Object) cacheDirective);
        }
        return null;
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
