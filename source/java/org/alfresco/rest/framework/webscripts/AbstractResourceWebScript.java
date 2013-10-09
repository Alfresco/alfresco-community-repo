package org.alfresco.rest.framework.webscripts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.web.scripts.content.ContentStreamer;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.HttpMethodSupport;
import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.resource.actions.ActionExecutor;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.alfresco.rest.framework.resource.content.FileBinaryResource;
import org.alfresco.rest.framework.resource.content.NodeBinaryResource;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.extensions.webscripts.Status;
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
            final ActionExecutor executor = findExecutor(httpMethod, params, resource, req.getContentType());

            //This execution usually takes place in a Retrying Transaction (see subclasses)
            executor.execute(resource, params, new ExecutionCallback()
            {
                @Override
                public void onSuccess(Object result, ContentInfo contentInfo)
                {
                    respons.put("toSerialize", result); 
                    respons.put("contentInfo", contentInfo);
                    setSuccessResponseStatus(res);
                }
            });
            
            //Outside the transaction.
            Object toSerialize = respons.get("toSerialize");
            ContentInfo contentInfo = (ContentInfo) respons.get("contentInfo");
            
            setContentInfoOnResponse(res, contentInfo);
            
            if (toSerialize != null)
            {
                if (toSerialize instanceof BinaryResource)
                {
                    streamResponse(req, res, (BinaryResource) toSerialize);
                }
                else
                {
                    renderJsonResponse(res, toSerialize);
                }
            }

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

    protected void streamResponse(final WebScriptRequest req, final WebScriptResponse res, BinaryResource resource) throws IOException
    {
        if (resource instanceof FileBinaryResource)
        {
            FileBinaryResource fileResource = (FileBinaryResource) resource;
            streamer.streamContent(req, res, fileResource.getFile(), null, false, null, null);
        }
        else if (resource instanceof NodeBinaryResource)
        {
            NodeBinaryResource nodeResource = (NodeBinaryResource) resource;
            streamer.streamContent(req, res, nodeResource.getNodeRef(), nodeResource.getPropertyQName(), false, null, null);        
        }

    }
    
    /**
     * Renders the result of an execution.
     * 
     * @param res WebScriptResponse
     * @param respons result of an execution
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

    /**
     * The response status must be set before the response is written by Jackson (which will by default close and commit the response).
     * In a r/w txn, web script buffered responses ensure that it doesn't really matter but for r/o txns this is important.
     * @param res
     */
    protected void setSuccessResponseStatus(final WebScriptResponse res)
    {
        // default for GET, HEAD, OPTIONS, PUT, TRACE
        res.setStatus(Status.STATUS_OK);
    }
    
    /**
     * Finds the action executor to execute actions on.
     * @param httpMethod - the http method
     * @param params Params
     * @param resource 
     * @param contentType Request content type
     * @return ActionExecutor the action executor
     */
    public ActionExecutor findExecutor(HttpMethod httpMethod, Params params, ResourceWithMetadata resource, String contentType)
    {
        //Ignore all params and return this
        return this;
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
