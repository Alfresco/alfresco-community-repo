package org.alfresco.rest.framework.webscripts;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.alfresco.repo.web.scripts.BufferedRequest;
import org.alfresco.repo.web.scripts.BufferedResponse;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.exceptions.DefaultExceptionResolver;
import org.alfresco.rest.framework.core.exceptions.ErrorResponse;
import org.alfresco.rest.framework.core.exceptions.ExceptionResolver;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper.Writer;
import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.alfresco.rest.framework.resource.content.ContentInfoImpl;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.TempFileProvider;
import org.apache.chemistry.opencmis.server.shared.ThresholdOutputStreamFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Description.RequiredCache;
import org.springframework.extensions.webscripts.Format;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

/**
 * Entry point for API webscript.  Supports version/scope as well
 * as discovery.
 *
 * @author Gethin James
 */
public abstract class ApiWebScript extends AbstractWebScript
{
    protected JacksonHelper jsonHelper;
    ExceptionResolver<Exception> defaultResolver = new DefaultExceptionResolver();
    ExceptionResolver<Exception> resolver;

    protected boolean encryptTempFiles = false;
    protected String tempDirectoryName = null;
    protected int memoryThreshold = 4 * 1024 * 1024; // 4mb
    protected long maxContentSize = (long) 4 * 1024 * 1024 * 1024; // 4gb
    protected ThresholdOutputStreamFactory streamFactory = null;
    protected TransactionService transactionService;

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setDefaultResolver(ExceptionResolver<Exception> defaultResolver)
    {
        this.defaultResolver = defaultResolver;
    }

    public void setTempDirectoryName(String tempDirectoryName)
    {
        this.tempDirectoryName = tempDirectoryName;
    }

    public void setEncryptTempFiles(boolean encryptTempFiles)
    {
        this.encryptTempFiles = encryptTempFiles;
    }

    public void setMemoryThreshold(int memoryThreshold)
    {
        this.memoryThreshold = memoryThreshold;
    }

    public void setMaxContentSize(long maxContentSize)
    {
        this.maxContentSize = maxContentSize;
    }

    public void setStreamFactory(ThresholdOutputStreamFactory streamFactory)
    {
        this.streamFactory = streamFactory;
    }
    
    public void init()
    {
        File tempDirectory = new File(TempFileProvider.getTempDir(), tempDirectoryName);
        this.streamFactory = ThresholdOutputStreamFactory.newInstance(tempDirectory, memoryThreshold, maxContentSize, encryptTempFiles);
    }

    public final static String UTF8 = "UTF-8";
    public final static Cache CACHE_NEVER = new Cache(new RequiredCache()
    {
        @Override
        public boolean getNeverCache()
        {
            return true;
        }

        @Override
        public boolean getIsPublic()
        {
            return false;
        }

        @Override
        public boolean getMustRevalidate()
        {
            return true;
        }
    });
    final static ContentInfo DEFAULT_JSON_CONTENT = new ContentInfoImpl(Format.JSON.mimetype(),UTF8, 0, null);
    
    @Override
    public void execute(final WebScriptRequest req, final WebScriptResponse res) throws IOException
    {
		Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
		Api api = determineApi(templateVars);
		
		final BufferedRequest bufferedReq = getRequest(req);
		final BufferedResponse bufferedRes = getResponse(res);

		try
		{
		    execute(api, bufferedReq, bufferedRes);
		}
		finally
		{
            // Get rid of any temporary files
            if (bufferedReq != null)
            {
                bufferedReq.close();
            }
		}

        // Ensure a response is always flushed after successful execution
        if (bufferedRes != null)
        {
            bufferedRes.writeResponse();
        }
    }

    private Api determineApi(Map<String, String> templateVars)
    {
        String apiScope = templateVars.get("apiScope");
        String apiVersion = templateVars.get("apiVersion");
        String apiName = templateVars.get("apiName");
        return Api.valueOf(apiName,apiScope,apiVersion);
    }

    protected ErrorResponse resolveException(Exception ex)
    {
        ErrorResponse error = resolver.resolveException(ex);
        if (error == null)
        {
            error = defaultResolver.resolveException(ex);
        }
        return error;
    }

    protected BufferedRequest getRequest(final WebScriptRequest req)
    {
        // create buffered request and response that allow transaction retrying
        final BufferedRequest bufferedReq = new BufferedRequest(req, streamFactory);
        return bufferedReq;
    }

    protected BufferedResponse getResponse(final WebScriptResponse resp)
    {
        // create buffered request and response that allow transaction retrying
        final BufferedResponse bufferedRes = new BufferedResponse(resp, memoryThreshold);
        return bufferedRes;
    }

    public abstract void execute(final Api api, WebScriptRequest req, WebScriptResponse res) throws IOException;

    /**
     * Renders a JSON error response
     * @param errorResponse The error
     * @param res web script response
     * @throws IOException
     */
    public void renderErrorResponse(final ErrorResponse errorResponse, final WebScriptResponse res) throws IOException {
        
        errorResponse.setDescriptionURL("http://developer.alfresco.com/ErrorsExplained.html#"+errorResponse.getErrorKey());

        setContentInfoOnResponse(res, DEFAULT_JSON_CONTENT);
        
        // Status must be set before the response is written by Jackson (which will by default close and commit the response).
        // In a r/w txn, web script buffered responses ensure that it doesn't really matter but for r/o txns this is important.
        res.setStatus(errorResponse.getStatusCode());

        jsonHelper.withWriter(res.getOutputStream(), new Writer()
        {
            @SuppressWarnings("unchecked")
            @Override
            public void writeContents(JsonGenerator generator, ObjectMapper objectMapper)
                        throws JsonGenerationException, JsonMappingException, IOException
            {
                JSONObject obj = new JSONObject();
                obj.put("error", errorResponse);
                objectMapper.writeValue(generator, obj);
            }
        });
    }


    /**
     * Sets the response headers with any information we know about the content
     * @param res WebScriptResponse
     * @param contentInfo Content Information
     */
    protected void setContentInfoOnResponse(WebScriptResponse res, ContentInfo contentInfo)
    {
        if (contentInfo != null)
        {
            //Set content info on the response
            res.setContentType(contentInfo.getMimeType());
            res.setContentEncoding(contentInfo.getEncoding());
            if (res instanceof WebScriptServletResponse)
            {
                WebScriptServletResponse servletResponse = (WebScriptServletResponse) res;
                if (contentInfo.getLength() > 0)
                {
                	if (contentInfo.getLength()>0 && contentInfo.getLength() < Integer.MAX_VALUE)
                	{
                      servletResponse.getHttpServletResponse().setContentLength((int)contentInfo.getLength());
                	}
                }
                if (contentInfo.getLocale() != null)
                {
                    servletResponse.getHttpServletResponse().setLocale(contentInfo.getLocale());
                }
            }
        }

    }
    
    public void setResolver(ExceptionResolver<Exception> resolver)
    {
        this.resolver = resolver;
    }

    public void setJsonHelper(JacksonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }
}
