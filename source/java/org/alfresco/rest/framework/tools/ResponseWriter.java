/*-
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
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.jacksonextensions.ExecutionResult;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.resource.SerializablePagedCollection;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.alfresco.rest.framework.resource.content.ContentInfoImpl;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.*;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/*
 * Writes to the response
 *
 * @author Gethin James
 */
public interface ResponseWriter
{

    String UTF8 = "UTF-8";
    ContentInfo DEFAULT_JSON_CONTENT = new ContentInfoImpl(Format.JSON.mimetype(),UTF8, 0, null);
    Cache CACHE_NEVER = new Cache(new Description.RequiredCache()
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

    default Log resWriterLogger() {
        return LogFactory.getLog(this.getClass());
    }

    /**
     * Sets the response headers with any information we know about the content
     * @param res WebScriptResponse
     * @param contentInfo Content Information
     */
    default void setContentInfoOnResponse(WebScriptResponse res, ContentInfo contentInfo)
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
    default void setResponse(final WebScriptResponse res, int status, Cache cache, ContentInfo contentInfo,  Map<String, List<String>> headers)
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

    /**
     * Sets the response using the WithResponse object
     * @param res
     * @param withResponse
     */
    default void setResponse(final WebScriptResponse res, WithResponse withResponse)
    {
        setResponse(res, withResponse.getStatus(), withResponse.getCache(), withResponse.getContentInfo(), withResponse.getHeaders());
    }

    /**
     * Renders a JSON error response
     * @param errorResponse The error
     * @param res web script response
     * @throws IOException
     */
    default void renderErrorResponse(final ErrorResponse errorResponse, final WebScriptResponse res, final JacksonHelper jsonHelper) throws IOException {

        String logId = "";

        if (Status.STATUS_INTERNAL_SERVER_ERROR == errorResponse.getStatusCode() || resWriterLogger().isDebugEnabled())
        {
            logId = org.alfresco.util.GUID.generate();
            resWriterLogger().error(logId+" : "+errorResponse.getStackTrace());
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


    /**
     * Renders an exception to the output stream as Json.
     * @param exception
     * @param response
     * @throws IOException
     */
    default void renderException(final Exception exception, final WebScriptResponse response, final ApiAssistant assistant) throws IOException {
        renderErrorResponse(assistant.resolveException(exception), response, assistant.getJsonHelper());
    }

    /**
     * Renders the result of an execution.
     *
     * @param res WebScriptResponse
     * @param toSerialize result of an execution
     * @throws IOException
     */
    default void renderJsonResponse(final WebScriptResponse res, final Object toSerialize, final JacksonHelper jsonHelper)
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

}
