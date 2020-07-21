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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.rest.api.tests.util.MultiPartBuilder;
import org.alfresco.rest.api.tests.util.MultiPartBuilder.FileData;
import org.alfresco.rest.api.tests.util.MultiPartBuilder.MultiPartRequest;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.jacksonextensions.ExecutionResult;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper.Writer;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction.Read;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction.ReadById;
import org.alfresco.rest.framework.resource.actions.interfaces.MultiPartResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.content.BinaryProperty;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.tests.api.mocks.Farmer;
import org.alfresco.rest.framework.tests.api.mocks.Goat;
import org.alfresco.rest.framework.tests.api.mocks.Grass;
import org.alfresco.rest.framework.tests.api.mocks.Sheep;
import org.alfresco.rest.framework.tests.api.mocks3.Flock;
import org.alfresco.rest.framework.tests.api.mocks3.SlimGoat;
import org.alfresco.rest.framework.tools.RecognizedParamsExtractor;
import org.alfresco.rest.framework.webscripts.AbstractResourceWebScript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import org.springframework.extensions.webscripts.Format;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SerializeTests extends AbstractContextTest implements RecognizedParamsExtractor
{

    @Test
    public void testInvokeEntity() throws IOException
    {
        ResourceWithMetadata entityResource = locator.locateEntityResource(api,"sheep", HttpMethod.GET);
        assertNotNull(entityResource);
        EntityResourceAction.ReadById<?> getter = (ReadById<?>) entityResource.getResource();

        String out = writeResponse(helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null, NOT_USED, getter.readById("1234A3", NOT_USED)));
        assertTrue("There must be json output", StringUtils.startsWith(out, "{\"entry\":"));
        
        EntityResourceAction.Read<?> getAll = (Read<?>) entityResource.getResource();
        CollectionWithPagingInfo<?> resources = getAll.readAll(null);
        out = writeResponse(helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null, Params.valueOf("notUsed", null, null), resources));
        assertTrue("There must be json output as List", StringUtils.startsWith(out, "{\"list\":"));
    }

    @Test
    public void testInvokeMultiPartEntity() throws IOException
    {
        ResourceWithMetadata entityResource = locator.locateEntityResource(api,"multiparttest", HttpMethod.POST);
        assertNotNull(entityResource);
        MultiPartResourceAction.Create<?> resource = (MultiPartResourceAction.Create<?>) entityResource.getResource();

        File file = TempFileProvider.createTempFile("ParamsExtractorTests-", ".txt");
        PrintWriter writer = new PrintWriter(file);
        writer.println("Multipart Mock test2.");
        writer.close();

        MultiPartRequest reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(file.getName(), file, MimetypeMap.MIMETYPE_TEXT_PLAIN))
                    .build();

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "");
        mockRequest.setContent(reqBody.getBody());
        mockRequest.setContentType(reqBody.getContentType());

        String out = writeResponse(helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null, NOT_USED, resource.create(new FormData(mockRequest), NOT_USED, callBack)));
        assertTrue("There must be json output", StringUtils.startsWith(out, "{\"entry\":"));
    }

    @Test
    public void testSerializeResponse() throws IOException
    {
        ResourceWithMetadata relationResource = locator.locateRelationResource(api,"sheep", "baaahh", HttpMethod.GET);
        assertNotNull(relationResource);
        RelationshipResourceAction.Read<?> getter = (RelationshipResourceAction.Read<?>) relationResource.getResource();
        CollectionWithPagingInfo<?> resources = getter.readAll("123",Params.valueOf("", null, null));
        String out = writeResponse(helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null, Params.valueOf("notUsed", null, null), resources));
        assertTrue("There must be json output as List", StringUtils.startsWith(out, "{\"list\":"));
    }
    
    @Test
    public void testPagedCollection() throws IOException
    {
        ResourceWithMetadata relationResource = locator.locateRelationResource(api,"sheep", "baaahh", HttpMethod.GET);
        assertNotNull(relationResource);
        RelationshipResourceAction.Read<?> getter = (RelationshipResourceAction.Read<?>) relationResource.getResource();
        CollectionWithPagingInfo<?> resources = getter.readAll("123",Params.valueOf("", null, null));
        assertNotNull(resources);
        assertTrue(resources.getTotalItems().intValue() == 3);
        assertFalse(resources.hasMoreItems());
        String out = writeResponse(helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null, Params.valueOf("notUsed", null, null), resources));
        assertTrue("There must be json output as List with pagination", StringUtils.startsWith(out, "{\"list\":{\"pagination\":{\"count\":3,"));
        
        resources = getter.readAll("123",ParamsExtender.valueOf(Paging.valueOf(0, 1),"123"));
        assertTrue(resources.getCollection().size() == 1);
        assertTrue(resources.hasMoreItems());
        out = writeResponse(helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null, Params.valueOf("notUsed", null, null), resources));
        assertTrue("There must be json output as List with pagination", StringUtils.startsWith(out, "{\"list\":{\"pagination\":{\"count\":1,"));

    }
       
    @Test
    public void testExpandEmbedded() throws IOException
    {
        assertNotNull(helper);
        Farmer aFarmer = new Farmer("180");
        aFarmer.setGoatId("1111");
        aFarmer.setSheepId("2222");
        ExecutionResult res = (ExecutionResult) helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null,Params.valueOf("notUsed", null, null),aFarmer);
        assertNotNull(res);
        assertTrue(Farmer.class.equals(res.getRoot().getClass()));
        Map<String,Object> embeds = res.getEmbedded();
        assertNotNull(embeds);
        assertTrue(embeds.size()>0);
        ExecutionResult goat = (ExecutionResult) embeds.get("goat");
        assertTrue(Goat.class.equals(goat.getRoot().getClass()));
        ExecutionResult grassEmbed = (ExecutionResult) goat.getEmbedded().get("grass");
        Grass grass = (Grass) grassEmbed.getRoot();
        assertNotNull(grass);
        assertTrue("Goat1111".equals(grass.getId()));
        ExecutionResult sheep = (ExecutionResult) embeds.get("sheep");
        assertTrue(Sheep.class.equals(sheep.getRoot().getClass()));
        Sheep aSheep = (Sheep) sheep.getRoot();
        assertTrue("2222".equals(aSheep.getId()));
        String out = writeResponse(res);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
    }
    
    @Test
    public void testExpandRelations() throws IOException
    {
        assertNotNull(helper);
        Map<String, BeanPropertiesFilter> rFilter = getRelationFilter("blacksheep,baaahh");
        ExecutionResult res = (ExecutionResult) helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,"sheep",ParamsExtender.valueOf(rFilter,"1"),new Farmer("180"));
        assertNotNull(res);
        String out = writeResponse(res);
        assertTrue(Farmer.class.equals(res.getRoot().getClass()));
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
        Paging pageRequest = Paging.valueOf(1, 2);
        
        Object resultCollection =  helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,"sheep",ParamsExtender.valueOf(rFilter,"1"),CollectionWithPagingInfo.asPaged(pageRequest,Arrays.asList(new Farmer("180"), new Farmer("190"), new Farmer("280"))));
        assertNotNull(resultCollection);
        out = writeResponse(resultCollection);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
    }

    @Test
    public void testIncludeSource() throws IOException
    {
        ExecutionResult exec1 = new ExecutionResult(new Farmer("180"),null);
        ExecutionResult exec2 = new ExecutionResult(new Farmer("456"), null);
        CollectionWithPagingInfo<ExecutionResult> coll = CollectionWithPagingInfo.asPaged(null, Arrays.asList(exec1, exec2));

        Object resultCollection =  helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,"sheep",ParamsExtender.valueOf(true,"1"),coll);
        assertNotNull(resultCollection);
        String out = writeResponse(resultCollection);
        assertTrue("There must 'source' json output", StringUtils.contains(out, "\"source\":{\"name\":\"Dolly\",\"age\":3,\"sheepGuid\":\"1\"}"));

        resultCollection =  helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,"sheep",ParamsExtender.valueOf(false,"1"),coll);
        assertNotNull(resultCollection);
        out = writeResponse(resultCollection);
        assertFalse("There must not 'source' json output", StringUtils.contains(out, "\"source\":{\"name\":\"Dolly\",\"age\":3,\"sheepGuid\":\"1\"}"));

        coll = CollectionWithPagingInfo.asPaged(null, Arrays.asList(exec1, exec2), false, 2, new Sheep("barbie"));
        resultCollection =  helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,"sheep",ParamsExtender.valueOf(true,"1"),coll);
        assertNotNull(resultCollection);
        out = writeResponse(resultCollection);
        assertTrue("There must 'source' json output", StringUtils.contains(out, "\"source\":{\"name\":\"Dolly\",\"age\":3,\"sheepGuid\":\"barbie\""));
    }

    @Test
    public void testExpandRecursiveRelations() throws IOException
    {
        ExecutionResult exec1 = new ExecutionResult(new Farmer("180"),null);
        ExecutionResult exec2 = new ExecutionResult(new Farmer("456"),getFilter("age"));
        CollectionWithPagingInfo<ExecutionResult> coll = CollectionWithPagingInfo.asPaged(null, Arrays.asList(exec1, exec2));
        ExecutionResult execResult = new ExecutionResult(new Sheep("ssheep"),null);
        Map<String,Object> related = new HashMap<String,Object>();
        related.put("farmers", coll);
        execResult.addRelated(related);
        String out = writeResponse(execResult);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
        assertFalse("collections should be serialized correctly.  There may be a problem with this one.",StringUtils.contains(out, "\"collection\":["));
        assertTrue("collections should be serialized correctly.  There should be embed relations",StringUtils.contains(out, "\"relations\":{\"farmers\":{\"list\":"));
    }
    
    @Test
    public void testSerializeExecutionResult() throws IOException
    {
        assertNotNull(helper);
        Object res = helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null,Params.valueOf("notUsed", null, null),new Farmer("180"));
        String out = writeResponse(res);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
       
    }
   
    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testSerializePagedCollection() throws IOException
    {
        assertNotNull(helper);
        CollectionWithPagingInfo paged = CollectionWithPagingInfo.asPaged(null,null);
        String out = writeResponse(helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null, Params.valueOf("notUsed", null, null), paged));
        assertTrue("There must be json output as List with pagination", StringUtils.startsWith(out, "{\"list\":{\"pagination\":{\"count\":0,"));
        Paging pageRequest = Paging.valueOf(1, 2);
        
        paged = CollectionWithPagingInfo.asPaged(pageRequest,Arrays.asList(new Goat(), new Sheep("ABCD"), new Sheep("XYZ")));
        out = writeResponse(helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null, Params.valueOf("notUsed", null, null), paged));
        assertTrue("There must be json output as List with pagination", StringUtils.startsWith(out, "{\"list\":{\"pagination\":{\"count\":3,"));
        
        paged = CollectionWithPagingInfo.asPaged(pageRequest,Arrays.asList(new Goat(), new Sheep("ABCD"), new Sheep("XYZ")),true,5000);
        out = writeResponse(helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null, Params.valueOf("notUsed", null, null), paged));
        assertTrue("There must be json output as List with pagination", StringUtils.startsWith(out, "{\"list\":{\"pagination\":{\"count\":3,\"hasMoreItems\":true,\"totalItems\":5000"));
       
    }
  
    @Test
    public void testSerializeMap() throws IOException
    {
        assertNotNull(helper);
        Map<String,Object> aMap = new HashMap<String,Object>();
        aMap.put("goatie", new Goat());
        aMap.put("sheepie", new Sheep("ABCD"));
        aMap.put("sheepy", new Sheep("XYZ"));
        Object res = helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null,Params.valueOf("notUsed", null, null),aMap);
        String out = writeResponse(res);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
       
    }
    
    @Test
    public void testSerializeSet() throws IOException
    {
        assertNotNull(helper);
        Set<Object> aSet = new HashSet<Object>();
        aSet.add(new Goat());
        aSet.add(new Sheep("ABCD"));
        aSet.add(new Sheep("XYZ"));
        Object res = helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null,Params.valueOf("notUsed", null, null),aSet);
        String out = writeResponse(res);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
       
    }
    
    @Test
    public void testSerializeListOfSimpleType() throws IOException
    {
        assertNotNull(helper);
        CollectionWithPagingInfo<String> pString = CollectionWithPagingInfo.asPaged(null,Arrays.asList("goat", "sheep", "horse")); 
        Object res = helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null,Params.valueOf("notUsed", null, null),pString);
        String out = writeResponse(res);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
        CollectionWithPagingInfo<Integer> pInts = CollectionWithPagingInfo.asPaged(null,Arrays.asList(234, 45, 890, 3456)); 
        res = helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null,Params.valueOf("notUsed", null, null),pInts);
        out = writeResponse(res);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
    } 
        
    @Test
    public void testSerializeList() throws IOException
    {
        assertNotNull(helper);
        Object res = helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null,Params.valueOf("notUsed", null, null),Arrays.asList(new Goat(), new Sheep("ABCD"), new Sheep("XYZ")));
        String out = writeResponse(res);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
       
    }

    @Test
    public void testSerializeCustom() throws IOException
    {
        assertNotNull(helper);
        String uuid = GUID.generate();
        String out = writeResponse(uuid);
        NodeRef n = jsonHelper.construct(new StringReader(out),NodeRef.class);
        assertNotNull(n);
        assertEquals(uuid, n.getId());
    }

    @Test
    public void testSerializeUniqueId() throws IOException
    {
        assertNotNull(helper);
        Object res = helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null,Params.valueOf("notUsed", null, null), new Sheep("ABCD"));
        String out = writeResponse(res);
        assertTrue("Id field must be called sheepGuid.",  StringUtils.contains(out, "\"sheepGuid\":\"ABCD\""));      
    }
    
    @Test
    public void testInvokeRelation() throws IOException
    {
        ResourceWithMetadata relationResource = locator.locateRelationResource(api,"sheep", "baaahh", HttpMethod.GET);
        assertNotNull(relationResource);
        RelationshipResourceAction.Read<?> getter = (RelationshipResourceAction.Read<?>) relationResource.getResource();
        String out = writeResponse(helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), api,null, Params.valueOf("notUsed", null, null), getter.readAll("1234A3", Params.valueOf("notUsed", null, null))));
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
    }

    @Test
    public void testSerializeBinaryProperty() throws IOException
    {
        BinaryProperty prop = new BinaryProperty(Format.JSON.mimetype(),"UTF-8");      
        String out = writeResponse(prop);
        assertTrue("Only show the mimeType",  out.equals("{\"mimeType\":\"application/json\"}"));
        prop = new BinaryProperty(Format.XML.mimetype(),"UTF-8", 45, null);      
        out = writeResponse(prop);
        assertTrue("Type is xml.",  StringUtils.contains(out, "\"mimeType\":\"text/xml\""));
        assertTrue("Size must be serialized.",  StringUtils.contains(out, "\"sizeInBytes\":45"));
        
        prop = new BinaryProperty(Format.XML.mimetype(),"UTF-8", 99, Locale.CANADA_FRENCH);      
        out = writeResponse(prop);
        assertFalse("Locale should not be serialized",  StringUtils.contains(out, "\"locale\""));
        
        //Check binary properties serialize ok as part of a bean.
        Flock flock = new Flock("myflock", 50, null, prop);
        out = writeResponse(flock);
        assertFalse("Locale should not be serialized",  StringUtils.contains(out, "\"locale\""));
        assertTrue("Type is xml.",  StringUtils.contains(out, "\"mimeType\":\"text/xml\""));
        assertTrue("Size must be serialized.",  StringUtils.contains(out, "\"sizeInBytes\":99"));
    }
    
    @Test
    public void testInvokeProperty() throws IOException
    {        
        Api api3 = Api.valueOf("alfrescomock", "private", "3");
        ResourceWithMetadata propResource = locator.locateRelationResource(api3,"flock", "photo", HttpMethod.GET);
        AbstractResourceWebScript executor = getExecutor();
        Object result = executor.execute(propResource,  Params.valueOf("234", null, null),  mock(WebScriptResponse.class), true);
        assertNotNull(result);
    }

        @Test
    public void testInvokeVersions() throws IOException
    {
        final Map<String, Object> respons = new HashMap<String, Object>();
        ResourceWithMetadata entityResource = locator.locateEntityResource(api,"goat", HttpMethod.GET);
        assertNotNull(entityResource);
        EntityResourceAction.ReadById<?> getter = (ReadById<?>) entityResource.getResource();
        Object readById = getter.readById("1234A3", NOT_USED);
        assertTrue("Version 1 must be a goat.", Goat.class.equals(readById.getClass()));
        String out = writeResponse(readById);
        assertNotNull(out);
        
        Api v3 = Api.valueOf(api.getName(), api.getScope().toString(), "3");
        entityResource = locator.locateEntityResource(v3,"goat", HttpMethod.GET);
        assertNotNull(entityResource);
        getter = (ReadById<?>) entityResource.getResource();
        Object readByIdForNewVersion = getter.readById("1234A3", NOT_USED);
        assertTrue("Version 3 must be a slim goat.", SlimGoat.class.equals(readByIdForNewVersion.getClass()));
        respons.put("v3Goat",readByIdForNewVersion);
        out = writeResponse(readByIdForNewVersion);
        
        entityResource = locator.locateEntityResource(api,"grass", HttpMethod.GET);
        assertNotNull(entityResource);  //ok for version 1
        
        try
        {
            entityResource = locator.locateEntityResource(v3,"grass", HttpMethod.GET);
            fail("Should throw an UnsupportedResourceOperationException");
        }
        catch (UnsupportedResourceOperationException error)
        {
            //this is correct
        }
    }

    // note: exposed as "properties" query param
    @Test
    public void testFilter() throws IOException, JSONException
    {
        assertNotNull(helper);
        BeanPropertiesFilter theFilter  = getFilter("age");
        Object res = new ExecutionResult(new Sheep("bob"),theFilter);      
        String out = writeResponse(res);
        assertTrue("Filter must only return the age.",  StringUtils.contains(out, "{\"age\":3}"));
       
        theFilter = getFilter("age,name");
        res = new ExecutionResult(new Sheep("bob"),theFilter);  
        out = writeResponse(res);
        JSONObject jsonRsp = new JSONObject(new JSONTokener(out));
        assertEquals(1, jsonRsp.length());
        JSONObject entry = jsonRsp.getJSONObject("entry");
        assertEquals(2, entry.length());
        assertEquals("The name should be 'Dolly'", "Dolly", entry.getString("name"));
        assertTrue("The age should be 3", entry.getInt("age") == 3);

        // unit test filter with "include" taking precendence over "fields" filter
        List<String> theInclude = getIncludeClause("name");
        theFilter  = getFilter("age", theInclude);
        res = new ExecutionResult(new Sheep("bob"),theFilter);
        out = writeResponse(res);
        jsonRsp = new JSONObject(new JSONTokener(out));
        assertEquals(1, jsonRsp.length());
        entry = jsonRsp.getJSONObject("entry");
        assertEquals(2, entry.length());
        assertEquals("The name should be 'Dolly'", "Dolly", entry.getString("name"));
        assertTrue("The age should be 3", entry.getInt("age") == 3);
        
        Api v3 = Api.valueOf(api.getName(), api.getScope().toString(), "3");
        Map<String, BeanPropertiesFilter> relFiler = getRelationFilter("herd");
        res = helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), v3,"goat",ParamsExtender.valueOf(relFiler, "notUsed"),new SlimGoat());
        out = writeResponse(res);
        jsonRsp = new JSONObject(new JSONTokener(out));
        entry = jsonRsp.getJSONObject("relations")
                .getJSONObject("herd")
                .getJSONObject("list")
                .getJSONArray("entries").getJSONObject(0)
                .getJSONObject("entry");
        assertEquals("The name should be 'bigun'", "bigun", entry.getString("name"));
        assertTrue("The quantity should be 56", entry.getInt("quantity") == 56);

        relFiler = getRelationFilter("herd(name)");
        res = helper.processAdditionsToTheResponse(mock(WebScriptResponse.class), v3,"goat",ParamsExtender.valueOf(relFiler, "notUsed"),new SlimGoat());
        out = writeResponse(res);
        assertTrue("Must return only the herd name.", StringUtils.contains(out, "{\"name\":\"bigun\"}"));
    }
    
    @Test
    public void schema() throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonSchema schema = objectMapper.generateJsonSchema(Farmer.class);
        assertNotNull(schema);
        System.out.println("\nschema:");
        System.out.println(schema.toString());
    }
    
    private String writeResponse(final Object respons) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        jsonHelper.withWriter(out, new Writer()
        {
            @Override
            public void writeContents(JsonGenerator generator, ObjectMapper objectMapper)
                        throws JsonGenerationException, JsonMappingException, IOException
            {
                objectMapper.writeValue(generator, respons);
            }
        });
        System.out.println(out.toString());
        return out.toString();
    }
}
