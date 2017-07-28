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

    private ExceptionResolver<Exception> defaultResolver = new DefaultExceptionResolver();
    private ExceptionResolver<WebScriptException> webScriptExceptionResolver;
    private ExceptionResolver<Exception> resolver;
    private JacksonHelper jsonHelper;

    /**
     * Determines the api being used from the templateVars
     * @param templateVars
     * @return Api
     */
    public static Api determineApi(Map<String, String> templateVars)
    {
        String apiScope = templateVars.get("apiScope");
        String apiVersion = templateVars.get("apiVersion");
        String apiName = templateVars.get("apiName");
        return Api.valueOf(apiName,apiScope,apiVersion);
    }

    /**
     * Resolves an exception as a json error.
     * @param exception
     * @return ErrorResponse
     */
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
