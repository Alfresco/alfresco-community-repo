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
package org.alfresco.rest.framework.tools;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.exceptions.DefaultExceptionResolver;
import org.alfresco.rest.framework.core.exceptions.ErrorResponse;
import org.alfresco.rest.framework.core.exceptions.ExceptionResolver;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.alfresco.rest.framework.resource.content.ContentInfoImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.*;
import org.springframework.extensions.webscripts.Description.RequiredCache;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Assists you in creating a great Rest API.
 *
 * @author Gethin James
 */
public class ApiAssistant {

    private static Log logger = LogFactory.getLog(ApiAssistant.class);

    public final static String UTF8 = "UTF-8";
    public final static ContentInfo DEFAULT_JSON_CONTENT = new ContentInfoImpl(Format.JSON.mimetype(),UTF8, 0, null);
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

    private ExceptionResolver<Exception> defaultResolver = new DefaultExceptionResolver();
    private ExceptionResolver<WebScriptException> webScriptExceptionResolver;
    private ExceptionResolver<Exception> resolver;
    private JacksonHelper jsonHelper;

    public static Api determineApi(Map<String, String> templateVars)
    {
        String apiScope = templateVars.get("apiScope");
        String apiVersion = templateVars.get("apiVersion");
        String apiName = templateVars.get("apiName");
        return Api.valueOf(apiName,apiScope,apiVersion);
    }

    public ErrorResponse resolveException(Exception ex)
    {
        ErrorResponse error = null;
        if (ex instanceof WebScriptException)
        {
            error = webScriptExceptionResolver.resolveException((WebScriptException) ex);
        }
        else
        {
            error = resolver.resolveException(ex);
        }
        if (error == null)
        {
            error = defaultResolver.resolveException(ex);
        }
        return error;
    }

    /**
     * Sets the response headers with any information we know about the content
     * @param res WebScriptResponse
     * @param contentInfo Content Information
     */
    public void setContentInfoOnResponse(WebScriptResponse res, ContentInfo contentInfo)
    {
        if (contentInfo != null)
        {
            //Set content info on the response
            res.setContentType(contentInfo.getMimeType());
            res.setContentEncoding(contentInfo.getEncoding());

            if (res instanceof WrappingWebScriptResponse)
            {
                WrappingWebScriptResponse wrappedRes = ((WrappingWebScriptResponse) res);
                res = wrappedRes.getNext();
            }

            if (res instanceof WebScriptServletResponse)
            {
                WebScriptServletResponse servletResponse = (WebScriptServletResponse) res;
                if (contentInfo.getLength() > 0)
                {
                    if (contentInfo.getLength() > 0 && contentInfo.getLength() < Integer.MAX_VALUE)
                    {
                        servletResponse.getHttpServletResponse().setContentLength((int) contentInfo.getLength());
                    }
                }
                if (contentInfo.getLocale() != null)
                {
                    servletResponse.getHttpServletResponse().setLocale(contentInfo.getLocale());
                }
            }
        }
    }

    /**
     * Renders an exception to the output stream as Json.
     * @param exception
     * @param response
     * @throws IOException
     */
    public void renderException(Exception exception, final WebScriptResponse response) throws IOException {
        renderErrorResponse(resolveException(exception), response);
    }

    /**
     * Renders a JSON error response
     * @param errorResponse The error
     * @param res web script response
     * @throws IOException
     */
    public void renderErrorResponse(ErrorResponse errorResponse, final WebScriptResponse res) throws IOException {

        String logId = "";

        if (Status.STATUS_INTERNAL_SERVER_ERROR == errorResponse.getStatusCode() || logger.isDebugEnabled())
        {
            logId = org.alfresco.util.GUID.generate();
            logger.error(logId+" : "+errorResponse.getStackTrace());
        }

        String stackMessage = I18NUtil.getMessage(DefaultExceptionResolver.STACK_MESSAGE_ID);

        final ErrorResponse errorToWrite = new ErrorResponse(errorResponse.getErrorKey(),
                errorResponse.getStatusCode(),
                errorResponse.getBriefSummary(),
                stackMessage,
                logId,
                errorResponse.getAdditionalState(),
                DefaultExceptionResolver.ERROR_URL);

        setContentInfoOnResponse(res, DEFAULT_JSON_CONTENT);

        // Status must be set before the response is written by Jackson (which will by default close and commit the response).
        // In a r/w txn, web script buffered responses ensure that it doesn't really matter but for r/o txns this is important.
        res.setStatus(errorToWrite.getStatusCode());

        jsonHelper.withWriter(res.getOutputStream(), new JacksonHelper.Writer()
        {
            @SuppressWarnings("unchecked")
            @Override
            public void writeContents(JsonGenerator generator, ObjectMapper objectMapper)
                    throws JsonGenerationException, JsonMappingException, IOException
            {
                JSONObject obj = new JSONObject();
                obj.put("error", errorToWrite);
                objectMapper.writeValue(generator, obj);
            }
        });
    }

    public JacksonHelper getJsonHelper() {
        return jsonHelper;
    }

    public void setDefaultResolver(ExceptionResolver<Exception> defaultResolver) {
        this.defaultResolver = defaultResolver;
    }

    public void setWebScriptExceptionResolver(ExceptionResolver<WebScriptException> webScriptExceptionResolver) {
        this.webScriptExceptionResolver = webScriptExceptionResolver;
    }

    public void setResolver(ExceptionResolver<Exception> resolver) {
        this.resolver = resolver;
    }

    public void setJsonHelper(JacksonHelper jsonHelper) {
        this.jsonHelper = jsonHelper;
    }
}
