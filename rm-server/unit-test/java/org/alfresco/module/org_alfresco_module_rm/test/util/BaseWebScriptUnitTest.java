/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.util;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Container;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Description;
import org.springframework.extensions.webscripts.Description.RequiredCache;
import org.springframework.extensions.webscripts.DescriptionExtension;
import org.springframework.extensions.webscripts.FormatRegistry;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.ScriptProcessorRegistry;
import org.springframework.extensions.webscripts.SearchPath;
import org.springframework.extensions.webscripts.TemplateProcessorRegistry;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.json.JSONUtils;
import org.springframework.extensions.webscripts.processor.FTLTemplateProcessor;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;

/**
 * Base Web Script Unit Test.
 * <p>
 * Provides helper methods that mock the nessesery classes needed to execute
 * a Java backed webscript that implements DeclarativeWebScript.
 * <p>
 * Note that execution of java script controllers is not currently supported.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public abstract class BaseWebScriptUnitTest extends BaseUnitTest
{
    /** web script root folder for RM webscripts */
    protected static final String WEBSCRIPT_ROOT_RM = "alfresco/templates/webscripts/org/alfresco/rma/";
    
    /**
     * @return  declarative webscript
     */
    protected abstract DeclarativeWebScript getWebScript();
    
    /**
     * @return  classpath location of webscript template
     */
    protected abstract String getWebScriptTemplate();
    
    /**
     * Helper method to build a map of web script parameter values
     * mimicking those provided on the URL
     * 
     * @param values    
     * @return
     */
    protected Map<String, String> buildParameters(String ... values)
    {
        Map<String, String> result = new HashMap<String, String>(values.length/2);
        for (int i = 0; i < values.length; i=i+2)
        {
            String key = values[i];
            String value = values[i+1];
            result.put(key, value);
        }
        return result;
    }
    
    /**
     * 
     * @param parameters
     * @return
     * @throws Exception
     */
    protected JSONObject executeJSONWebScript(Map<String, String> parameters) throws Exception
    {
        return executeJSONWebScript(parameters, null);
    }
    
    /**
     * Execute web script and convert result into a JSON object.
     * 
     * @param parameters            map of all parameter values
     * @return {@link JSONObject}   result, parsed into a JSON object
     */
    protected JSONObject executeJSONWebScript(Map<String, String> parameters, String content) throws Exception
    {
        String result = executeWebScript(parameters, content);        
        return new JSONObject(result);
    }
    
    /**
     * 
     * @param parameters
     * @return
     * @throws Exception
     */
    protected String executeWebScript(Map<String, String> parameters) throws Exception
    {
        return executeWebScript( parameters, null);
    }
    
    /**
     * Execute web script and return result as a string.
     * 
     * @param parameters            map of all parameter values
     * @return {@link String}       result of web script
     */
    protected String executeWebScript(Map<String, String> parameters, String content) throws Exception
    {
        DeclarativeWebScript webScript = getWebScript();
        String template = getWebScriptTemplate();
        
        // initialise webscript
        webScript.init(getMockedContainer(template), getMockedDescription());
        
        // execute webscript
        WebScriptResponse mockedResponse = getMockedWebScriptResponse();
        webScript.execute(getMockedWebScriptRequest(webScript, parameters, content), mockedResponse);
        
        // return results
        return mockedResponse.getWriter().toString();        
    }
    
    /**
     * Helper method to get the mocked web script request.
     * 
     * @param webScript                 declarative web script
     * @param parameters                web script parameter values
     * @return {@link WebScriptRequest} mocked web script request
     */
    @SuppressWarnings("rawtypes")
    protected WebScriptRequest getMockedWebScriptRequest(DeclarativeWebScript webScript, final Map<String, String> parameters, String content) throws Exception
    {
        Match match = new Match(null, parameters, null, webScript);
        org.springframework.extensions.webscripts.Runtime mockedRuntime = mock(org.springframework.extensions.webscripts.Runtime.class);        
        
        WebScriptRequest mockedRequest = mock(WebScriptRequest.class);
        doReturn(match).when(mockedRequest).getServiceMatch();
        doReturn(mockedRuntime).when(mockedRequest).getRuntime();
        
        if (content != null && !content.isEmpty())
        {
            Content mockedContent = mock(Content.class);
            doReturn(content).when(mockedContent).getContent();
            doReturn(mockedContent).when(mockedRequest).getContent();
        }
        
        String [] paramNames = (String[])parameters.keySet().toArray(new String[parameters.size()]);
        doReturn(paramNames).when(mockedRequest).getParameterNames();
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                String paramName = (String)invocation.getArguments()[0];
                return parameters.get(paramName);
            }
            
        }).when(mockedRequest).getParameter(anyString());
        
        doReturn(new String[0]).when(mockedRequest).getHeaderNames();
        doReturn("json").when(mockedRequest).getFormat();
        
        return mockedRequest;        
    }
    
    /**
     * Helper method to get mocked web script response
     * 
     * @return  {@link WebScriptResponse}   mocked web script response
     */
    protected WebScriptResponse getMockedWebScriptResponse() throws Exception
    {
        WebScriptResponse mockedResponse = mock(WebScriptResponse.class);
        StringWriter writer = new StringWriter();
        doReturn(writer).when(mockedResponse).getWriter();
        return mockedResponse;
    }
    
    /**
     * Helper method to get mocked container object.
     *  
     * @param template              classpath location of webscripts ftl template
     * @return {@link Container}    mocked container 
     */
    protected Container getMockedContainer(String template) throws Exception
    {
        FormatRegistry mockedFormatRegistry = mock(FormatRegistry.class);
        doReturn("application/json").when(mockedFormatRegistry).getMimeType(anyString(), anyString());
        
        ScriptProcessorRegistry mockedScriptProcessorRegistry = mock(ScriptProcessorRegistry.class);
        doReturn(null).when(mockedScriptProcessorRegistry).findValidScriptPath(anyString());
        
        TemplateProcessorRegistry mockedTemplateProcessorRegistry = mock(TemplateProcessorRegistry.class);
        doReturn(template).when(mockedTemplateProcessorRegistry).findValidTemplatePath(anyString());
        
        FTLTemplateProcessor ftlTemplateProcessor = new FTLTemplateProcessor()
        {
            @Override
            protected TemplateLoader getTemplateLoader()
            {
                return new ClassTemplateLoader(getClass(), "/");
            }
        };
        ftlTemplateProcessor.init();        
        
        doReturn(ftlTemplateProcessor).when(mockedTemplateProcessorRegistry).getTemplateProcessor(anyString());
        
        Container mockedContainer = mock(Container.class);
        doReturn(mockedFormatRegistry).when(mockedContainer).getFormatRegistry();
        doReturn(mockedScriptProcessorRegistry).when(mockedContainer).getScriptProcessorRegistry();
        doReturn(mockedTemplateProcessorRegistry).when(mockedContainer).getTemplateProcessorRegistry();
        
        Map<String, Object> containerTemplateParameters = new HashMap<String, Object>(5);
        containerTemplateParameters.put("jsonUtils", new JSONUtils());
        doReturn(containerTemplateParameters).when(mockedContainer).getTemplateParameters();
        
        SearchPath mockedSearchPath = mock(SearchPath.class);
        doReturn(false).when(mockedSearchPath).hasDocument(anyString());
        doReturn(mockedSearchPath).when(mockedContainer).getSearchPath();
        
        // setup description
        Description mockDescription = mock(Description.class);
        doReturn(mock(RequiredCache.class)).when(mockDescription).getRequiredCache();
        
        return mockedContainer;        
    }
    
    /**
     * Helper method to get mocked description class
     * 
     * @return  {@link DescriptionExtension}    mocked description class
     */
    protected Description getMockedDescription()
    {
        Description mockedDescription = mock(Description.class);
        doReturn(mock(RequiredCache.class)).when(mockedDescription).getRequiredCache();
        return mockedDescription;
    }
}
