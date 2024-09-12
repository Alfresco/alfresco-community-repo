/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.alfresco.rest.framework.core.ResourceMetadata.RESOURCE_TYPE.RELATIONSHIP;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.rest.api.tests.util.MultiPartBuilder;
import org.alfresco.rest.api.tests.util.MultiPartBuilder.FileData;
import org.alfresco.rest.api.tests.util.MultiPartBuilder.MultiPartRequest;
import org.alfresco.rest.framework.core.ResourceDictionary;
import org.alfresco.rest.framework.core.ResourceInspector;
import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceLookupDictionary;
import org.alfresco.rest.framework.core.ResourceMetadata;
import org.alfresco.rest.framework.core.ResourceOperation;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.jacksonextensions.RestJsonModule;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.tests.api.mocks.Farmer;
import org.alfresco.rest.framework.tests.api.mocks.GrassEntityResource;
import org.alfresco.rest.framework.tools.ApiAssistant;
import org.alfresco.rest.framework.webscripts.ParamsExtractor;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptDelete;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptGet;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptPost;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptPut;
import org.alfresco.util.TempFileProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;


/**
 * Tests extracting of params from req
 * 
 * @author Gethin James
 */
public class ParamsExtractorTests
{
    static JacksonHelper jsonHelper = null;
    static ApiAssistant assistant = null;

    static ResourceLocator locator;

    @BeforeClass
    public static void setupTests() throws Exception
    {
        jsonHelper = new JacksonHelper();
        RestJsonModule module = new RestJsonModule();
        jsonHelper.setModule(module);
        jsonHelper.afterPropertiesSet();

        assistant = new ApiAssistant();
        assistant.setJsonHelper(jsonHelper);

        locator = new ResourceLookupDictionary();
    }

    @Test
    public void testGetExtractor()
    {
        ResourceWebScriptGet extractor = new ResourceWebScriptGet();
        extractor.setLocator(locator);

        Map<String, String> templateVars = new HashMap<String, String>();
        WebScriptRequest request = mock(WebScriptRequest.class);
        when(request.getServiceMatch()).thenReturn(new Match(null, templateVars, null));
        
        Params params = extractor.extractParams(mockEntity(), request);
        assertNull("For getting a Collection there should be no entity params.",params.getEntityId());
        assertNull("For getting a Collection there should be no passed params.",params.getPassedIn());
        assertNull("For getting a Collection there should be no relationshipId params.",params.getRelationshipId());
        assertEquals(Paging.DEFAULT_SKIP_COUNT, params.getPaging().getSkipCount());
        assertEquals(Paging.DEFAULT_MAX_ITEMS, params.getPaging().getMaxItems());
        assertNotNull(params.getFilter());
        assertTrue("Default filter is BeanPropertiesFilter.AllProperties", BeanPropertiesFilter.AllProperties.class.equals(params.getFilter().getClass()));
        
        templateVars.put(ResourceLocator.ENTITY_ID, "1234");
        params = extractor.extractParams(mockEntity(), request);
        assertNotNull(params);
        assertNotNull(params.getRelationsFilter());
        assertFalse(params.includeSource());

        templateVars.put(ResourceLocator.RELATIONSHIP_RESOURCE, "codfish");
        params = extractor.extractParams(mockRelationship(), request);
        assertNotNull(params);
        assertNull("For getting a Collection there should be no relationshipId params.",params.getRelationshipId());

        templateVars.put(ResourceLocator.RELATIONSHIP_ID, "45678");
        params = extractor.extractParams(mockRelationship(), request);
        assertNotNull(params);
        assertEquals("1234", params.getEntityId());
        assertEquals("45678", params.getRelationshipId());
        assertFalse(params.includeSource());

        testExtractAddressedParams(templateVars, request, extractor);
    }

    private Params testExtractAddressedParams(Map<String, String> templateVars, WebScriptRequest request, ParamsExtractor extractor)
    {
        templateVars.clear();
        templateVars.put(ResourceLocator.ENTITY_ID, "1234");
        templateVars.put(ResourceLocator.RELATIONSHIP_RESOURCE, "codfish");
        Params params = extractor.extractParams(mockProperty(), request);
        assertNotNull(params);
        assertTrue(params.hasBinaryProperty("codfish"));
        assertFalse(params.hasBinaryProperty("something"));
        assertEquals("codfish", params.getBinaryProperty());

        templateVars.put(ResourceLocator.RELATIONSHIP_ID, "9865");
        templateVars.put(ResourceLocator.PROPERTY, "monkFish");
        params = extractor.extractParams(mockProperty(), request);
        assertNotNull(params);
        assertEquals("1234", params.getEntityId());
        assertEquals("9865", params.getRelationshipId());
        assertTrue(params.hasBinaryProperty("monkFish"));
        return params;
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testPostExtractor() throws IOException
    {
        //Put together the stubs
        ResourceWebScriptPost extractor = new ResourceWebScriptPost();
        extractor.setAssistant(assistant);
        extractor.setLocator(locator);

        Map<String, String> templateVars = new HashMap<String, String>();

        Content content = mock(Content.class);
        when(content.getReader()).thenReturn(new StringReader(JsonJacksonTests.FARMER_JSON));
        WebScriptRequest request = mock(WebScriptRequest.class);
        when(request.getServiceMatch()).thenReturn(new Match(null, templateVars, null));
        when(request.getContent()).thenReturn(content);
        
        Params params = extractor.extractParams(mockEntity(), request);
        assertNotNull(params);
        
        assertNotNull(params.getFilter());
        assertTrue("Default filter is BeanPropertiesFilter.AllProperties", BeanPropertiesFilter.AllProperties.class.equals(params.getFilter().getClass()));
        
        Object passed =  params.getPassedIn();
        assertNotNull(passed);
        
        assertTrue(List.class.isAssignableFrom(passed.getClass()));
        List<Object> passedObjs = (List<Object>) passed;
        assertTrue(passedObjs.size() == 1);
        assertTrue("A Farmer was passed in.", Farmer.class.equals(passedObjs.get(0).getClass()));    
        
        //No entity id for POST
        templateVars.put(ResourceLocator.ENTITY_ID, "1234");
        try
        {
            params = extractor.extractParams(mockEntity(), request);
            fail("Should not get here. No entity id for POST");
        }
        catch (UnsupportedResourceOperationException uoe)
        {
            assertNotNull(uoe);  //Must throw this exception
        }
    
        when(content.getReader()).thenReturn(new StringReader(JsonJacksonTests.FARMER_JSON));  //reset the reader
        params = extractor.extractParams(mockRelationship(), request);
        assertNotNull(params);
        assertEquals("1234", params.getEntityId());
        passed =  params.getPassedIn();
        assertNotNull(passed);
        passedObjs = (List<Object>) passed;
        assertTrue(passedObjs.size() == 1);
        assertTrue("A Farmer was passed in.", Farmer.class.equals(passedObjs.get(0).getClass()));      
        
        try
        {
            when(content.getReader()).thenReturn(new StringReader(JsonJacksonTests.FARMER_JSON));  //reset the reader
            templateVars.put(ResourceLocator.RELATIONSHIP_ID, "45678");
            params = extractor.extractParams(mockRelationship(), request);
            fail("Should not get here.");
        }
        catch (UnsupportedResourceOperationException iae)
        { 
            assertNotNull("POSTING to a relationship collection by id is not correct.",iae);  //Must throw this exception
        }

        templateVars.clear();
        when(content.getReader()).thenReturn(new StringReader(JsonJacksonTests.FARMER_JSON));  //reset the reader
        templateVars.put(ResourceLocator.ENTITY_ID, "1234");
        templateVars.put(ResourceLocator.RELATIONSHIP_ID, "codfish");
        try
        {
            //POST does not support addressed parameters.
            params = extractor.extractParams(mockEntity(), request);
            fail("Should not get here.");
        }
        catch (UnsupportedResourceOperationException uoe)
        {
            assertNotNull(uoe);  //Must throw this exception
        }
        testExtractOperationParams(templateVars, request, extractor);
        
        templateVars.clear();
        Method aMethod = ResourceInspector.findMethod(EntityResourceAction.Create.class, GrassEntityResource.class);
        ResourceOperation op = ResourceInspector.inspectOperation(GrassEntityResource.class, aMethod, HttpMethod.POST);
        List<ResourceMetadata> metainfo = ResourceInspector.inspect(GrassEntityResource.class);
        assertNotNull(op);
        assertTrue("Create method should have two params", op.getParameters().size() == 2);
        ResourceParameter singleParam = op.getParameters().get(0);
        assertTrue(ResourceParameter.KIND.HTTP_BODY_OBJECT.equals(singleParam.getParamType()));
        assertFalse("Create grass does not support multiple grass creations", singleParam.isAllowMultiple());
        assertFalse(singleParam.isRequired());

        // Test context when the request body is null and 'required' webApiParam is false
        when(request.getHeader("content-length")).thenReturn("0");
        params = extractor.extractParams(metainfo.get(0), request);
        assertNotNull(params);

        // Test context when the request body is provided and 'required' property is false
        when(content.getReader()).thenReturn(new StringReader(JsonJacksonTests.GRASS_JSON));
        params = extractor.extractParams(metainfo.get(0), request);
        assertNotNull(params);
    }

    private Params testExtractOperationParams(Map<String, String> templateVars, WebScriptRequest request, ParamsExtractor extractor)
    {
        templateVars.clear();
        templateVars.put(ResourceLocator.ENTITY_ID, "1234");
        templateVars.put(ResourceLocator.RELATIONSHIP_RESOURCE, "codfish");
        Params params = extractor.extractParams(mockOperation(), request);
        assertNotNull(params);
        assertNull("For a Collection there should be no relationshipId params.",params.getRelationshipId());

        templateVars.put(ResourceLocator.RELATIONSHIP_ID, "9865");
        templateVars.put(ResourceLocator.PROPERTY, "monkFish");
        params = extractor.extractParams(mockOperation(), request);
        assertNotNull(params);
        assertEquals("1234", params.getEntityId());
        assertEquals("9865", params.getRelationshipId());
        return params;
    }

    @Test
    public void testMultiPartPostExtractor() throws Exception
    {
        ResourceWebScriptPost extractor = new ResourceWebScriptPost();
        extractor.setAssistant(assistant);
        extractor.setLocator(locator);

        Map<String, String> templateVars = new HashMap<String, String>();

        WebScriptRequest request = mock(WebScriptRequest.class);
        when(request.getServiceMatch()).thenReturn(new Match(null, templateVars, null));

        File file = TempFileProvider.createTempFile("ParamsExtractorTests-", ".txt");
        PrintWriter writer = new PrintWriter(file);
        writer.println("Multipart Mock test.");
        writer.close();

        MultiPartRequest reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(file.getName(), file, MimetypeMap.MIMETYPE_TEXT_PLAIN))
                    .build();

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "");
        mockRequest.setContent(reqBody.getBody());
        mockRequest.setContentType(reqBody.getContentType());

        when(request.getContentType()).thenReturn("multipart/form-data");
        when(request.parseContent()).thenReturn(new FormData(mockRequest));

        Params params = extractor.extractParams(mockEntity(), request);
        assertNotNull(params);
        Object passed = params.getPassedIn();
        assertNotNull(passed);
        assertTrue(FormData.class.isAssignableFrom(passed.getClass()));
        FormData formData = (FormData) passed;
        assertTrue(formData.getIsMultiPart());

        // No entity id for POST
        templateVars.put(ResourceLocator.ENTITY_ID, "1234");
        try
        {
            params = extractor.extractParams(mockEntity(), request);
            fail("Should not get here. No entity id for POST");
        }
        catch (UnsupportedResourceOperationException uoe)
        {
            assertNotNull(uoe);
        }

        params = extractor.extractParams(mockRelationship(), request);
        assertNotNull(params);
        assertEquals("1234", params.getEntityId());
        passed = params.getPassedIn();
        assertNotNull(passed);
        assertTrue(FormData.class.isAssignableFrom(passed.getClass()));
        formData = (FormData) passed;
        assertTrue(formData.getIsMultiPart());
    }

    @Test
    public void testPutExtractor() throws IOException
    {
        //Put together the stubs
        ResourceWebScriptPut extractor = new ResourceWebScriptPut();
        extractor.setAssistant(assistant);
        extractor.setLocator(locator);

        Map<String, String> templateVars = new HashMap<String, String>();

        Content content = mock(Content.class);
        when(content.getReader()).thenReturn(new StringReader(JsonJacksonTests.FARMER_JSON));
        WebScriptRequest request = mock(WebScriptRequest.class);
        when(request.getServiceMatch()).thenReturn(new Match(null, templateVars, null));
        when(request.getContent()).thenReturn(content);
        when(request.getContentType()).thenReturn("application/pdf; charset=UTF-16BE");

        Params params = null;
        try
        {
            params = extractor.extractParams(mockEntity(), request);
            fail("Should not get here. PUT is executed against the instance URL");
        }
        catch (UnsupportedResourceOperationException uoe)
        {
            assertNotNull(uoe);  //Must throw this exception
        }
        
        templateVars.put(ResourceLocator.ENTITY_ID, "1234");
        try
        {
            params = extractor.extractParams(mockRelationship(), request);
            fail("Should not get here. PUT is executed against the instance URL");
        }
        catch (UnsupportedResourceOperationException uoe)
        {
            assertNotNull(uoe);  //Must throw this exception
        }
        
        
        templateVars.put(ResourceLocator.ENTITY_ID, "1234");
        //Put single entity wrapped in array
        params = extractor.extractParams(mockEntity(), request);
        assertNotNull(params);
        Object passed =  params.getPassedIn();
        assertNotNull(passed);
        assertTrue("A Farmer was passed in.", Farmer.class.equals(passed.getClass()));
        
        assertNotNull(params.getFilter());
        assertTrue("Default filter is BeanPropertiesFilter.AllProperties", BeanPropertiesFilter.AllProperties.class.equals(params.getFilter().getClass()));
        
        when(content.getReader()).thenReturn(new StringReader(JsonJacksonTests.FARMER_JSON));  //reset the reader
        params = extractor.extractParams(mockEntity(), request);
        assertNotNull(params);
        assertEquals("1234", params.getEntityId());
        passed =  params.getPassedIn();
        assertNotNull(passed);
        
        when(content.getReader()).thenReturn(new StringReader(JsonJacksonTests.FARMER_JSON));  //reset the reader
        templateVars.put(ResourceLocator.RELATIONSHIP_ID, "67890");
        params = extractor.extractParams(mockRelationship(), request);
        assertNotNull(params);
        assertEquals("1234", params.getEntityId());
        passed =  params.getPassedIn();
        assertNotNull(passed);
        assertTrue("A Farmer was passed in.", Farmer.class.equals(passed.getClass()));
        Farmer aFarmer = (Farmer) passed;
        assertEquals("Relationship id should be automatically set on the object passed in.", aFarmer.getId(), "67890");
        
        when(content.getReader()).thenReturn(new StringReader(JsonJacksonTests.FARMER_JSON));  //reset the reader
        params = testExtractAddressedParams(templateVars, request, extractor);
        assertEquals("UTF-16BE", params.getContentInfo().getEncoding());
        assertEquals(MimetypeMap.MIMETYPE_PDF, params.getContentInfo().getMimeType());
    }
    
    @Test
    public void testDeleteExtractor() throws IOException
    {
        ResourceWebScriptDelete extractor = new ResourceWebScriptDelete();
        extractor.setLocator(locator);

        Map<String, String> templateVars = new HashMap<String, String>();
        
        WebScriptRequest request = mock(WebScriptRequest.class);
        when(request.getServiceMatch()).thenReturn(new Match(null, templateVars, null));

        Params params = null;
        params = extractor.extractParams(mockEntity(), request);
        assertNotNull(params);
        assertNull(params.getEntityId());
        assertNull(params.getRelationshipId());
        assertNotNull(params.getFilter());
        assertTrue("Default filter is BeanPropertiesFilter.AllProperties", BeanPropertiesFilter.AllProperties.class.equals(params.getFilter().getClass()));
        
        templateVars.put(ResourceLocator.ENTITY_ID, "1234");
        params = extractor.extractParams(mockRelationship(), request);
        assertNotNull(params);
        assertEquals("1234", params.getEntityId());
        assertNull(params.getRelationshipId());
        assertNotNull(params.getFilter());
        assertTrue("Default filter is BeanPropertiesFilter.AllProperties", BeanPropertiesFilter.AllProperties.class.equals(params.getFilter().getClass()));
        
        templateVars.put(ResourceLocator.RELATIONSHIP_ID, "45678");
        params = extractor.extractParams(mockRelationship(), request);
        assertNotNull(params);
        assertEquals("1234", params.getEntityId());
        assertEquals("45678", params.getRelationshipId());
        assertNotNull(params.getFilter());
        assertTrue("Default filter is BeanPropertiesFilter.AllProperties", BeanPropertiesFilter.AllProperties.class.equals(params.getFilter().getClass()));
        
        testExtractAddressedParams(templateVars, request, extractor);
    }

    @Test
    public void testSpecialChars() throws IOException
    {
       String specialChars = new String(new char[] { (char) '香' }) + " 香蕉";
       ResourceWebScriptPost extractor = new ResourceWebScriptPost();
       extractor.setAssistant(assistant);
       extractor.setLocator(locator);

       Map<String, String> templateVars = new HashMap<String, String>();
       String mockMe = "{\"name\":\""+specialChars+"\",\"created\":\"2012-03-23T15:56:18.552+0000\",\"age\":54,\"id\":\"1234A3\",\"farm\":\"LARGE\"}";
       Content content = mock(Content.class);
       when(content.getReader()).thenReturn(new StringReader(mockMe));
       WebScriptRequest request = mock(WebScriptRequest.class);
       when(request.getServiceMatch()).thenReturn(new Match(null, templateVars, null));
       when(request.getContent()).thenReturn(content);
       
       Params params = extractor.extractParams(mockEntity(), request);
       assertNotNull(params);
       Object passed =  params.getPassedIn();
       assertTrue(List.class.isAssignableFrom(passed.getClass()));
       @SuppressWarnings("unchecked")
       List<Object> passedObjs = (List<Object>) passed;
       assertTrue(passedObjs.size() == 1);
       assertTrue("A Farmer was passed in.", Farmer.class.equals(passedObjs.get(0).getClass()));   
       Farmer f = (Farmer)passedObjs.get(0);
       assertTrue(f.getName().equals("香 香蕉"));
       
       //Test passing in special characters as a param.
       ResourceWebScriptGet getExtractor = new ResourceWebScriptGet();
       getExtractor.setAssistant(assistant);
       getExtractor.setLocator(locator);

       Map<String, String> getTemplateVars = new HashMap<String, String>();
       WebScriptRequest getRequest = mock(WebScriptRequest.class);
       when(getRequest.getServiceMatch()).thenReturn(new Match(null, getTemplateVars, null));
       when(getRequest.getParameterNames()).thenReturn(new String[] { "aParam" });
       when(getRequest.getParameterValues("aParam")).thenReturn(new String[] { specialChars });
       Params pGet = getExtractor.extractParams(mockEntity(), getRequest);
       assertNotNull(pGet);
       String pVal = pGet.getParameter("aParam");
       assertTrue(pVal.equals("香 香蕉"));
       
    }

    /**
     * Mocks a Entity Resource
     * @return ResourceMetadata a Entity
     */
    private static ResourceMetadata mockEntity()
    {
        ResourceMetadata resourceMock = mock(ResourceMetadata.class);
        ResourceOperation resourceOperation = mock(ResourceOperation.class);
        when(resourceMock.getType()).thenReturn(ResourceMetadata.RESOURCE_TYPE.ENTITY);
        when(resourceMock.getOperation(notNull())).thenReturn(resourceOperation);
        when(resourceMock.getObjectType(notNull())).thenReturn(Farmer.class);
        return resourceMock;
    }
 
    /**
     * Mocks a Property Resource
     * @return ResourceMetadata a Entity
     */
    private static ResourceMetadata mockProperty()
    {
        ResourceMetadata resourceMock = mock(ResourceMetadata.class);
        when(resourceMock.getType()).thenReturn(ResourceMetadata.RESOURCE_TYPE.PROPERTY);
        //when(resourceMock.getObjectType(HttpMethod.PUT)).thenReturn(Farmer.class);
        //when(resourceMock.getObjectType(HttpMethod.POST)).thenReturn(Farmer.class);
        return resourceMock;
    }

    /**
     * Mocks an operation
     * @return ResourceMetadata a Entity
     */
    private static ResourceMetadata mockOperation()
    {
        ResourceMetadata resourceMock = mock(ResourceMetadata.class);
        when(resourceMock.getType()).thenReturn(ResourceMetadata.RESOURCE_TYPE.OPERATION);
        return resourceMock;
    }

    /**
     * Mocks a Relationship Resource
     * @return ResourceMetadata a Relationship
     */
    private static ResourceMetadata mockRelationship()
    {
        ResourceMetadata resourceMock = mock(ResourceMetadata.class);
        ResourceOperation resourceOperation = mock(ResourceOperation.class);
        when(resourceMock.getOperation(notNull())).thenReturn(resourceOperation);
        when(resourceMock.getType()).thenReturn(RELATIONSHIP);
        when(resourceMock.getObjectType(notNull())).thenReturn(Farmer.class);
        return resourceMock;
    }
    
}
