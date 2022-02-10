/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.util;

import static java.util.Collections.emptyMap;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.jscript.People;
import org.alfresco.repo.jscript.ScriptNode;
import org.json.JSONObject;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Container;
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
    protected abstract AbstractWebScript getWebScript();
    
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
        Map<String, String> result = new HashMap<>(values.length / 2);
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
        return executeWebScript(parameters, null);
    }
    
    /**
     * Execute web script and return result as a string.
     * 
     * @param parameters            map of all parameter values
     * @return {@link String}       result of web script
     */
    protected String executeWebScript(Map<String, String> parameters, String content) throws Exception
    {
        AbstractWebScript webScript = getWebScript();
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
    protected WebScriptRequest getMockedWebScriptRequest(AbstractWebScript webScript, final Map<String, String> parameters, String content) throws Exception
    {
        Match match = new Match(null, parameters, null, webScript);
        org.springframework.extensions.webscripts.Runtime mockedRuntime = mock(org.springframework.extensions.webscripts.Runtime.class);        
        
        WebScriptRequest mockedRequest = mock(WebScriptRequest.class);
        lenient().doReturn(match).when(mockedRequest).getServiceMatch();
        lenient().doReturn(mockedRuntime).when(mockedRequest).getRuntime();
        
        if (content != null && !content.isEmpty())
        {
            Content mockedContent = mock(Content.class);
            lenient().doReturn(content).when(mockedContent).getContent();
            lenient().doReturn(mockedContent).when(mockedRequest).getContent();
        }
        
        String [] paramNames = (String[])parameters.keySet().toArray(new String[parameters.size()]);
        lenient().doReturn(paramNames).when(mockedRequest).getParameterNames();
        lenient().doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                String paramName = (String)invocation.getArguments()[0];
                return parameters.get(paramName);
            }
            
        }).when(mockedRequest).getParameter(anyString());

        lenient().doReturn(new String[0]).when(mockedRequest).getHeaderNames();
        lenient().doReturn("json").when(mockedRequest).getFormat();
        
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
        lenient().doReturn(writer).when(mockedResponse).getWriter();
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
        lenient().doReturn("application/json").when(mockedFormatRegistry).getMimeType(nullable(String.class), nullable(String.class));
        
        ScriptProcessorRegistry mockedScriptProcessorRegistry = mock(ScriptProcessorRegistry.class);
        lenient().doReturn(null).when(mockedScriptProcessorRegistry).findValidScriptPath(anyString());
        
        TemplateProcessorRegistry mockedTemplateProcessorRegistry = mock(TemplateProcessorRegistry.class);
        lenient().doReturn(template).when(mockedTemplateProcessorRegistry).findValidTemplatePath(anyString());
        
        FTLTemplateProcessor ftlTemplateProcessor = new FTLTemplateProcessor()
        {
            @Override
            protected TemplateLoader getTemplateLoader()
            {
                return new ClassTemplateLoader(getClass(), "/");
            }
        };
        ftlTemplateProcessor.init();        
        
        lenient().doReturn(ftlTemplateProcessor).when(mockedTemplateProcessorRegistry).getTemplateProcessor(anyString());
        
        Container mockedContainer = mock(Container.class);
        lenient().doReturn(mockedFormatRegistry).when(mockedContainer).getFormatRegistry();
        lenient().doReturn(mockedScriptProcessorRegistry).when(mockedContainer).getScriptProcessorRegistry();
        lenient().doReturn(mockedTemplateProcessorRegistry).when(mockedContainer).getTemplateProcessorRegistry();
        
        Map<String, Object> containerTemplateParameters = new HashMap<>(5);
        containerTemplateParameters.put("jsonUtils", new JSONUtils());
        containerTemplateParameters.put("people", getMockedPeopleObject());
        lenient().doReturn(containerTemplateParameters).when(mockedContainer).getTemplateParameters();

        SearchPath mockedSearchPath = mock(SearchPath.class);
        lenient().doReturn(false).when(mockedSearchPath).hasDocument(anyString());
        lenient().doReturn(mockedSearchPath).when(mockedContainer).getSearchPath();
        
        // setup description
        Description mockDescription = mock(Description.class);
        lenient().doReturn(mock(RequiredCache.class)).when(mockDescription).getRequiredCache();
        
        return mockedContainer;
    }

    /**
     * Creates a mock {@code people} object for use as a root object within FTL.
     * This {@code people} object will return person nodes as specified in {@link #getMockedPeople()}.
     */
    protected People getMockedPeopleObject()
    {
        People p = mock(People.class);
        getMockedPeople().forEach((name, person) -> when(p.getPerson(eq(name))).thenReturn(person) );
        return p;
    }

    /**
     * Creates a map of person ScriptNodes for use within FTL.
     * The default implementation is an empty map, but this can be overridden by subclasses.
     * @return a map of usernames to mocked ScriptNode objects representing person nodes.
     */
    protected Map<String, ScriptNode> getMockedPeople()
    {
        return emptyMap();
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
