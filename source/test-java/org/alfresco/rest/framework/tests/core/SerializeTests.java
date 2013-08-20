package org.alfresco.rest.framework.tests.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceDictionaryBuilder;
import org.alfresco.rest.framework.core.ResourceLookupDictionary;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.jacksonextensions.ExecutionResult;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper.Writer;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.ActionExecutor.ExecutionCallback;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction.Read;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction.ReadById;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.content.BinaryProperty;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.tests.api.mocks.Farmer;
import org.alfresco.rest.framework.tests.api.mocks.Goat;
import org.alfresco.rest.framework.tests.api.mocks.Grass;
import org.alfresco.rest.framework.tests.api.mocks.Sheep;
import org.alfresco.rest.framework.tests.api.mocks3.Flock;
import org.alfresco.rest.framework.tests.api.mocks3.SlimGoat;
import org.alfresco.rest.framework.webscripts.AbstractResourceWebScript;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.schema.JsonSchema;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Format;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-rest-context.xml" })
public class SerializeTests
{
    @Autowired
    private ResourceLookupDictionary locator;

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private ResourceWebScriptHelper helper;
    
    private static Api api = Api.valueOf("alfrescomock", "private", "1");
    private static Params NOT_USED = Params.valueOf("notUsed", null);
    
    @Autowired
    protected JacksonHelper jsonHelper;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception
    {
        Map<String, Object> entityResourceBeans = applicationContext.getBeansWithAnnotation(EntityResource.class);
        Map<String, Object> relationResourceBeans = applicationContext.getBeansWithAnnotation(RelationshipResource.class);
        locator.setDictionary(ResourceDictionaryBuilder.build(entityResourceBeans.values(), relationResourceBeans.values()));
        AbstractResourceWebScript executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfGets");
        
        //Mock transaction service
        TransactionService transerv = mock(TransactionService.class);
        RetryingTransactionHelper tHelper = mock(RetryingTransactionHelper.class);
        when(transerv.getRetryingTransactionHelper()).thenReturn(tHelper);
        when(tHelper.doInTransaction(any(RetryingTransactionCallback.class), anyBoolean(), anyBoolean())).thenAnswer(new Answer<Object>() {
            @SuppressWarnings("rawtypes")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
              Object[] args = invocation.getArguments();
              RetryingTransactionCallback cb = (RetryingTransactionCallback) args[0];
              cb.execute();
              return null;
            }
          });
        executor.setTransactionService(transerv);
    }
    
    @Test
    public void testInvokeEntity() throws IOException
    {
        ResourceWithMetadata entityResource = locator.locateEntityResource(api,"sheep", HttpMethod.GET);
        assertNotNull(entityResource);
        EntityResourceAction.ReadById<?> getter = (ReadById<?>) entityResource.getResource();

        String out = writeResponse(helper.postProcessResponse(api,null, NOT_USED, getter.readById("1234A3", NOT_USED)));
        assertTrue("There must be json output", StringUtils.startsWith(out, "{\"entry\":"));
        
        EntityResourceAction.Read<?> getAll = (Read<?>) entityResource.getResource();
        CollectionWithPagingInfo<?> resources = getAll.readAll(null);
        out = writeResponse(helper.postProcessResponse(api,null, Params.valueOf("notUsed", null), resources));
        assertTrue("There must be json output as List", StringUtils.startsWith(out, "{\"list\":"));
    }
    @Test
    public void testSerializeResponse() throws IOException
    {
        ResourceWithMetadata relationResource = locator.locateRelationResource(api,"sheep", "baaahh", HttpMethod.GET);
        assertNotNull(relationResource);
        RelationshipResourceAction.Read<?> getter = (RelationshipResourceAction.Read<?>) relationResource.getResource();
        CollectionWithPagingInfo<?> resources = getter.readAll("123",Params.valueOf("", null));
        String out = writeResponse(helper.postProcessResponse(api,null, Params.valueOf("notUsed", null), resources));
        assertTrue("There must be json output as List", StringUtils.startsWith(out, "{\"list\":"));
    }
    
    @Test
    public void testPagedCollection() throws IOException
    {
        ResourceWithMetadata relationResource = locator.locateRelationResource(api,"sheep", "baaahh", HttpMethod.GET);
        assertNotNull(relationResource);
        RelationshipResourceAction.Read<?> getter = (RelationshipResourceAction.Read<?>) relationResource.getResource();
        CollectionWithPagingInfo<?> resources = getter.readAll("123",Params.valueOf("", null));
        assertNotNull(resources);
        assertTrue(resources.getTotalItems().intValue() == 3);
        assertFalse(resources.hasMoreItems());
        String out = writeResponse(helper.postProcessResponse(api,null, Params.valueOf("notUsed", null), resources));
        assertTrue("There must be json output as List with pagination", StringUtils.startsWith(out, "{\"list\":{\"pagination\":{\"count\":3,"));
        
        resources = getter.readAll("123",ParamsExtender.valueOf(Paging.valueOf(0, 1),"123"));
        assertTrue(resources.getCollection().size() == 1);
        assertTrue(resources.hasMoreItems());
        out = writeResponse(helper.postProcessResponse(api,null, Params.valueOf("notUsed", null), resources));
        assertTrue("There must be json output as List with pagination", StringUtils.startsWith(out, "{\"list\":{\"pagination\":{\"count\":1,"));

    }
       
    @Test
    public void testExpandEmbedded() throws IOException
    {
        assertNotNull(helper);
        Farmer aFarmer = new Farmer("180");
        aFarmer.setGoatId("1111");
        aFarmer.setSheepId("2222");
        ExecutionResult res = (ExecutionResult) helper.postProcessResponse(api,null,Params.valueOf("notUsed", null),aFarmer);
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
        Map<String, BeanPropertiesFilter> rFilter = ResourceWebScriptHelper.getRelationFilter("blacksheep,baaahh");
        ExecutionResult res = (ExecutionResult) helper.postProcessResponse(api,"sheep",ParamsExtender.valueOf(rFilter,"1"),new Farmer("180"));
        assertNotNull(res);
        String out = writeResponse(res);
        assertTrue(Farmer.class.equals(res.getRoot().getClass()));
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
        Paging pageRequest = Paging.valueOf(1, 2);
        
        Object resultCollection =  helper.postProcessResponse(api,"sheep",ParamsExtender.valueOf(rFilter,"1"),CollectionWithPagingInfo.asPaged(pageRequest,Arrays.asList(new Farmer("180"), new Farmer("190"), new Farmer("280"))));
        assertNotNull(resultCollection);
        out = writeResponse(resultCollection);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
    }
    
    @Test
    public void testExpandRecursiveRelations() throws IOException
    {
        ExecutionResult exec1 = new ExecutionResult(new Farmer("180"),null);
        ExecutionResult exec2 = new ExecutionResult(new Farmer("456"),ResourceWebScriptHelper.getFilter("age"));
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
        Object res = helper.postProcessResponse(api,null,Params.valueOf("notUsed", null),new Farmer("180"));        
        String out = writeResponse(res);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
       
    }
   
    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testSerializePagedCollection() throws IOException
    {
        assertNotNull(helper);
        CollectionWithPagingInfo paged = CollectionWithPagingInfo.asPaged(null,null);
        String out = writeResponse(helper.postProcessResponse(api,null, Params.valueOf("notUsed", null), paged));
        assertTrue("There must be json output as List with pagination", StringUtils.startsWith(out, "{\"list\":{\"pagination\":{\"count\":0,"));
        Paging pageRequest = Paging.valueOf(1, 2);
        
        paged = CollectionWithPagingInfo.asPaged(pageRequest,Arrays.asList(new Goat(), new Sheep("ABCD"), new Sheep("XYZ")));
        out = writeResponse(helper.postProcessResponse(api,null, Params.valueOf("notUsed", null), paged));
        assertTrue("There must be json output as List with pagination", StringUtils.startsWith(out, "{\"list\":{\"pagination\":{\"count\":3,"));
        
        paged = CollectionWithPagingInfo.asPaged(pageRequest,Arrays.asList(new Goat(), new Sheep("ABCD"), new Sheep("XYZ")),true,5000);
        out = writeResponse(helper.postProcessResponse(api,null, Params.valueOf("notUsed", null), paged));
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
        Object res = helper.postProcessResponse(api,null,Params.valueOf("notUsed", null),aMap);        
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
        Object res = helper.postProcessResponse(api,null,Params.valueOf("notUsed", null),aSet);        
        String out = writeResponse(res);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
       
    }
    
    @Test
    public void testSerializeListOfSimpleType() throws IOException
    {
        assertNotNull(helper);
        CollectionWithPagingInfo<String> pString = CollectionWithPagingInfo.asPaged(null,Arrays.asList("goat", "sheep", "horse")); 
        Object res = helper.postProcessResponse(api,null,Params.valueOf("notUsed", null),pString);        
        String out = writeResponse(res);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
        CollectionWithPagingInfo<Integer> pInts = CollectionWithPagingInfo.asPaged(null,Arrays.asList(234, 45, 890, 3456)); 
        res = helper.postProcessResponse(api,null,Params.valueOf("notUsed", null),pInts);
        out = writeResponse(res);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
    } 
        
    @Test
    public void testSerializeList() throws IOException
    {
        assertNotNull(helper);
        Object res = helper.postProcessResponse(api,null,Params.valueOf("notUsed", null),Arrays.asList(new Goat(), new Sheep("ABCD"), new Sheep("XYZ")));        
        String out = writeResponse(res);
        assertTrue("There must be json output", StringUtils.isNotBlank(out));
       
    }

    @Test
    public void testSerializeUniqueId() throws IOException
    {
        assertNotNull(helper);
        Object res = helper.postProcessResponse(api,null,Params.valueOf("notUsed", null), new Sheep("ABCD"));        
        String out = writeResponse(res);
        assertTrue("Id field must be called sheepGuid.",  StringUtils.contains(out, "\"sheepGuid\":\"ABCD\""));      
    }
    
    @Test
    public void testInvokeRelation() throws IOException
    {
        ResourceWithMetadata relationResource = locator.locateRelationResource(api,"sheep", "baaahh", HttpMethod.GET);
        assertNotNull(relationResource);
        RelationshipResourceAction.Read<?> getter = (RelationshipResourceAction.Read<?>) relationResource.getResource();
        String out = writeResponse(helper.postProcessResponse(api,null, Params.valueOf("notUsed", null), getter.readAll("1234A3", Params.valueOf("notUsed", null))));
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
        AbstractResourceWebScript executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfGets");
        executor.execute(propResource, Params.valueOf("234", null), new ExecutionCallback<BinaryResource>(){
            @Override
            public void onSuccess(BinaryResource result, ContentInfo contentInfo)
            {
                assertNotNull(result);
            }});
        
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
    
    @Test
    public void testFilter() throws IOException
    {
        assertNotNull(helper);
        BeanPropertiesFilter theFilter  = ResourceWebScriptHelper.getFilter("age");
        Object res = new ExecutionResult(new Sheep("bob"),theFilter);      
        String out = writeResponse(res);
        assertTrue("Filter must only return the age.",  StringUtils.contains(out, "{\"age\":3}"));
       
        theFilter = ResourceWebScriptHelper.getFilter("age,name");
        res = new ExecutionResult(new Sheep("bob"),theFilter);  
        out = writeResponse(res);
        assertTrue("Filter must return the age and name.", StringUtils.contains(out, "{\"age\":3,\"name\":\"Dolly\"}"));
        
        Api v3 = Api.valueOf(api.getName(), api.getScope().toString(), "3");
        Map<String, BeanPropertiesFilter> relFiler = ResourceWebScriptHelper.getRelationFilter("herd");
        res = helper.postProcessResponse(v3,"goat",ParamsExtender.valueOf(relFiler, "notUsed"),new SlimGoat()); 
        out = writeResponse(res);
        assertTrue("Must return a full herd.", StringUtils.contains(out, "{\"name\":\"bigun\",\"quantity\":56}"));
        
        relFiler = ResourceWebScriptHelper.getRelationFilter("herd(name)");
        res = helper.postProcessResponse(v3,"goat",ParamsExtender.valueOf(relFiler, "notUsed"),new SlimGoat()); 
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
