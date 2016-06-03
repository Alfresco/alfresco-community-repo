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
 
package org.alfresco.rest.framework.tests.core;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceLookupDictionary;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.*;
import org.alfresco.rest.framework.jacksonextensions.ExecutionResult;
import org.alfresco.rest.framework.resource.actions.ActionExecutor;
import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.tests.api.mocks.CowEntityResource;
import org.alfresco.rest.framework.tests.api.mocks.Goat;
import org.alfresco.rest.framework.tests.api.mocks.Grass;
import org.alfresco.rest.framework.tests.api.mocks.Sheep;
import org.alfresco.rest.framework.tests.api.mocks3.FlockEntityResource;
import org.alfresco.rest.framework.webscripts.AbstractResourceWebScript;
import org.alfresco.rest.framework.webscripts.ApiWebScript;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests the execution of resources
 */

public class ExecutionTests extends AbstractContextTest
{
    static final Api api3 = Api.valueOf("alfrescomock", "private", "3");

    @Autowired
    SimpleMappingExceptionResolver simpleMappingExceptionResolver;

    @Test
    public void testInvokeGet() throws IOException
    {
        ResourceWithMetadata entityResource = locator.locateEntityResource(api,"sheep", HttpMethod.GET);
        AbstractResourceWebScript executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfGets");
        Object result = executor.execute(entityResource, Params.valueOf((String)null, null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), true);
        assertNotNull(result);

        WebScriptResponse response = mock(WebScriptResponse.class);
        entityResource = locator.locateEntityResource(api,"cow", HttpMethod.GET);
        result = executor.execute(entityResource, Params.valueOf((String)null, null, mock(WebScriptRequest.class)), response, true);
        assertNotNull(result);
        verify(response, times(1)).setCache((Cache) ApiWebScript.CACHE_NEVER);

        response = mock(WebScriptResponse.class);
        result = executor.execute(entityResource, Params.valueOf("543", null, mock(WebScriptRequest.class)),  response, true);
        assertNotNull(result);
        verify(response, times(1)).setCache((Cache) CowEntityResource.CACHE_COW);

        ResourceWithMetadata baa = locator.locateRelationResource(api,"sheep", "baaahh", HttpMethod.GET);
        result = executor.execute(baa, Params.valueOf("4", null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), true);
        assertNotNull(result);

        executor.execute(baa, Params.valueOf("4", "45", mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), true);
        assertNotNull(result);

        ResourceWithMetadata cowResource = locator.locateRelationResource(api,"cow","photo", HttpMethod.GET);
        result = executor.execute(cowResource, Params.valueOf("4", null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), true);
        assertNull(result);

        ResourceWithMetadata calf = locator.locateRelationResource(api,"cow", "calf", HttpMethod.GET);
        result = executor.execute(calf, Params.valueOf("4", null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), true);
        assertNotNull(result);

        executor.execute(calf, Params.valueOf("4", "45", mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), true);
        assertNotNull(result);

        calf = locator.locateRelationResource(api,"cow/{entityId}/calf", "photo", HttpMethod.GET);
        executor.execute(calf, Params.valueOf("4", "45", mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), true);
        assertNotNull(result);

        ResourceWithMetadata baaPhoto = locator.locateRelationResource(api,"sheep/{entityId}/baaahh", "photo", HttpMethod.GET);
        executor.execute(baaPhoto, Params.valueOf("4", "45", mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), true);
        assertNotNull(result);
    }

    @Test
    public void testInvokePost() throws IOException
    {
        AbstractResourceWebScript executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfPost");

        ResourceWithMetadata resource = locator.locateRelationResource(api, "sheep", "blacksheep", HttpMethod.POST);
        final Sheep aSheep = new Sheep("xyz");

        Object result = executor.execute(resource, Params.valueOf("654", null, NULL_PARAMS, Arrays.asList(aSheep), mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNotNull(result);
        assertEquals(aSheep,((ExecutionResult)result).getRoot());

        ResourceWithMetadata grassResource = locator.locateEntityResource(api,"grass", HttpMethod.POST);
        final Grass grr = new Grass("grr");
        result = executor.execute(grassResource, Params.valueOf("654", null, NULL_PARAMS, Arrays.asList(grr), mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertEquals(grr,((ExecutionResult)result).getRoot());

        final Goat goat = new Goat("xyz");
        ResourceWithMetadata cowresource = locator.locateEntityResource(api,"cow", HttpMethod.POST);
        WebScriptResponse response = mock(WebScriptResponse.class);
        result = executor.execute(cowresource, Params.valueOf("654", null, NULL_PARAMS, Arrays.asList(goat), mock(WebScriptRequest.class)),  response, false);
        assertEquals(goat,((ExecutionResult)result).getRoot());
        verify(response, times(1)).setStatus(Status.STATUS_ACCEPTED);

        ResourceWithMetadata entityResource = locator.locateRelationResource(api,"grass", "grow", HttpMethod.POST);
        result = executor.execute(entityResource,  Params.valueOf("654", null, NULL_PARAMS, grr, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertEquals("Growing well",result);

        ResourceWithMetadata calfResource = locator.locateRelationResource(api,"cow", "calf", HttpMethod.POST);
        result = executor.execute(calfResource,  Params.valueOf("654", null, NULL_PARAMS, Arrays.asList(goat), mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertEquals(goat,((ExecutionResult)result).getRoot());

        Map<String, String> templateVars = new HashMap();
        templateVars.put(ResourceLocator.COLLECTION_RESOURCE, "sheep");
        templateVars.put(ResourceLocator.ENTITY_ID, "sheepId");
        templateVars.put(ResourceLocator.RELATIONSHIP_RESOURCE, "baaahh");
        templateVars.put(ResourceLocator.PROPERTY, "chew");
        ResourceWithMetadata collResource = locator.locateResource(api, templateVars, HttpMethod.POST);
        result = executor.execute(collResource, Params.valueOf("654", "345", NULL_PARAMS, null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertEquals("All done",result);
    }

    @Test
    public void testInvokeDelete() throws IOException
    {
        ResourceWithMetadata grassResource = locator.locateEntityResource(api,"grass", HttpMethod.DELETE);
        AbstractResourceWebScript executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfDelete");
        Object result = executor.execute(grassResource,  Params.valueOf("4", null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNull(result);

        ResourceWithMetadata cowResource = locator.locateEntityResource(api,"cow", HttpMethod.DELETE);
        result = executor.execute(cowResource,  Params.valueOf("4", null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNull(result);

        cowResource = locator.locateRelationResource(api,"cow","photo", HttpMethod.DELETE);
        result = executor.execute(cowResource,  Params.valueOf("4", null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNull(result);

        ResourceWithMetadata resource = locator.locateRelationResource(api, "sheep", "blacksheep", HttpMethod.DELETE);
        result = executor.execute(resource,  Params.valueOf("4", null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNull(result);

        ResourceWithMetadata calf = locator.locateRelationResource(api,"cow", "calf", HttpMethod.DELETE);
        result = executor.execute(calf,  Params.valueOf("4", null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNull(result);

        ResourceWithMetadata flockEntityResource = locator.locateRelationResource(api3,"flock","photo", HttpMethod.DELETE);
        result = executor.execute(flockEntityResource,  Params.valueOf("4", null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNull(result);

        calf = locator.locateRelationResource(api,"cow/{entityId}/calf", "photo", HttpMethod.DELETE);
        result = executor.execute(calf,  Params.valueOf("4", null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNull(result);

        ResourceWithMetadata goatDelete = locator.locateRelationResource(api3,"goat/{entityId}/herd", "content", HttpMethod.DELETE);
        result = executor.execute(goatDelete,  Params.valueOf("4", "56", mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNull(result);
    }


    @Test
    public void testInvokePut() throws IOException
    {
        ResourceWithMetadata entityResource = locator.locateEntityResource(api,"sheep", HttpMethod.PUT);
        AbstractResourceWebScript executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfPut");
        final Sheep aSheep = new Sheep("xyz");
        Object result = executor.execute(entityResource, Params.valueOf("654", null, NULL_PARAMS, aSheep, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNotNull(result);
        assertEquals(aSheep,((ExecutionResult)result).getRoot());

        final Goat goat = new Goat("xyz");
        ResourceWithMetadata cowResource = locator.locateEntityResource(api,"cow", HttpMethod.PUT);
        result = executor.execute(cowResource, Params.valueOf("654", null, NULL_PARAMS, goat, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNotNull(result);
        assertEquals(goat,((ExecutionResult)result).getRoot());

        ResourceWithMetadata resource = locator.locateRelationResource(api, "sheep", "blacksheep", HttpMethod.PUT);
        result = executor.execute(resource, Params.valueOf("654", null, NULL_PARAMS, aSheep, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNotNull(result);
        assertEquals(aSheep,((ExecutionResult)result).getRoot());

        ResourceWithMetadata baaPhoto = locator.locateRelationResource(api,"sheep/{entityId}/baaahh", "photo", HttpMethod.PUT);
        result = executor.execute(baaPhoto,  Params.valueOf("4", "56", mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNull(result);

        ResourceWithMetadata flockEntityResource = locator.locateRelationResource(api3,"flock","photo", HttpMethod.PUT);
        result = executor.execute(flockEntityResource, Params.valueOf("654", null, NULL_PARAMS, goat, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNull(result);

        ResourceWithMetadata calf = locator.locateRelationResource(api,"cow", "calf", HttpMethod.PUT);
        result = executor.execute(calf, Params.valueOf("654", null, NULL_PARAMS, goat, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNotNull(result);
        assertEquals(goat,((ExecutionResult)result).getRoot());

        cowResource = locator.locateRelationResource(api,"cow","photo", HttpMethod.PUT);
        result = executor.execute(cowResource, Params.valueOf("654", null, NULL_PARAMS, goat, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNull(result);

        calf = locator.locateRelationResource(api,"cow/{entityId}/calf", "photo", HttpMethod.PUT);
        result = executor.execute(calf,  Params.valueOf("4", "56", mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertNull(result);
    }

    @Test
    public void testInvokeAbstract() throws IOException
    {
        AbstractResourceWebScript executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfGets");
        executor.setLocator(locator);
        executor.setResolver(simpleMappingExceptionResolver);
        executor.setJsonHelper(jsonHelper);
        Map<String, String> templateVars = new HashMap();
        templateVars.put("apiScope", "private");
        templateVars.put("apiVersion", "1");
        templateVars.put("apiName", "alfrescomock");
        templateVars.put(ResourceLocator.COLLECTION_RESOURCE, "sheep");
        executor.execute(executor.determineApi(templateVars), mockRequest(templateVars,new HashMap<String, List<String>>(1)), mock(WebScriptResponse.class));

        WebScriptResponse response = mockResponse();
        templateVars.put(ResourceLocator.COLLECTION_RESOURCE, "bad");
        executor.execute(api, mockRequest(templateVars,new HashMap<String, List<String>>(1)), response);
        //throws a runtime exception so INTERNAL_SERVER_ERROR
        verify(response, times(1)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        response = mockResponse();
        templateVars.put(ResourceLocator.ENTITY_ID, "badId");
        executor.execute(api, mockRequest(templateVars,new HashMap<String, List<String>>(1)), response);
        //throws a IntegrityException so 422
        verify(response, times(1)).setStatus(422);

    }

    @Test
    public void testInvalidUrls() throws IOException
    {
        AbstractResourceWebScript executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfGets");
        executor.setLocator(locator);
        executor.setResolver(simpleMappingExceptionResolver);
        executor.setJsonHelper(jsonHelper);
        Map<String, String> templateVars = new HashMap();
        templateVars.put("apiScope", "private");
        templateVars.put("apiVersion", "1");
        templateVars.put("apiName", "alfrescomock");

        WebScriptResponse response = mockResponse();
        templateVars.put(ResourceLocator.COLLECTION_RESOURCE, "blah:");
        executor.execute(api, mockRequest(templateVars, new HashMap<String, List<String>>(1)), response);
        //Can't find it so a 404
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRenderError() throws IOException
    {
        AbstractResourceWebScript executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfGets");
        executor.setResolver(simpleMappingExceptionResolver);
        executor.setJsonHelper(jsonHelper);

        ErrorResponse defaultError = new DefaultExceptionResolver().resolveException(new NullPointerException());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        executor.renderErrorResponse(defaultError, mockResponse(out));
        String errorMessage = out.toString();
        //System.out.println(errorMessage);
        assertTrue(errorMessage.contains("\"errorKey\":\"framework.exception.ApiDefault\""));
        assertTrue(errorMessage.contains("\"statusCode\":500"));
        assertTrue(errorMessage.contains("\"stackTrace\":\""));
        assertTrue(errorMessage.contains("\"descriptionURL\":\""+DefaultExceptionResolver.ERROR_URL+"\""));

        ErrorResponse anError = simpleMappingExceptionResolver.resolveException(new ApiException("nothing"));
        out = new ByteArrayOutputStream();
        executor.renderErrorResponse(anError, mockResponse(out));
        errorMessage = out.toString();
        System.out.println(errorMessage);
        assertTrue(errorMessage.contains("\"errorKey\":\"nothing\""));
        assertTrue(errorMessage.contains("\"statusCode\":500"));
        assertTrue(errorMessage.contains("\"stackTrace\":\""));

        anError = simpleMappingExceptionResolver.resolveException(new EntityNotFoundException("2"));
        out = new ByteArrayOutputStream();
        executor.renderErrorResponse(anError, mockResponse(out));
        errorMessage = out.toString();
        System.out.println(errorMessage);
        assertTrue(errorMessage.contains("\"errorKey\":\"framework.exception.EntityNotFound\""));
        assertTrue(errorMessage.contains("\"statusCode\":404"));
        assertTrue("Only 500 errors should have a stracktrace", errorMessage.contains("\"stackTrace\":\" \""));
    }

    private WebScriptResponse mockResponse() throws IOException
    {
        return mockResponse(new ByteArrayOutputStream());
    }
    private WebScriptResponse mockResponse(ByteArrayOutputStream byteArrayOutputStream) throws IOException
    {
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(byteArrayOutputStream);
        return res;
    }

    private WebScriptRequest mockRequest(Map<String, String> templateVars, final Map<String, List<String>> params)
    {
        final String[] paramNames = params.keySet().toArray(new String[]{});
        WebScriptRequest request = mock(WebScriptRequest.class);
        when(request.getServiceMatch()).thenReturn(new Match(null, templateVars,null));
        when(request.getParameterNames()).thenReturn(paramNames);
        when(request.getParameterValues(anyString())).thenAnswer(new Answer<String[]>() {
            @Override
            public String[] answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return params.get((String) args[0]).toArray(new String[]{});
            }
        });
        return request;
    }
}
