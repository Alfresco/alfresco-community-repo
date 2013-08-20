
package org.alfresco.rest.framework.tests.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceDictionaryBuilder;
import org.alfresco.rest.framework.core.ResourceInspector;
import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceLookupDictionary;
import org.alfresco.rest.framework.core.ResourceMetadata;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.tests.api.mocks.Farmer;
import org.alfresco.rest.framework.tests.api.mocks.GoatEntityResource;
import org.alfresco.rest.framework.tests.api.mocks.SheepBaaaahResource;
import org.alfresco.rest.framework.tests.api.mocks.SheepBlackSheepResource;
import org.alfresco.rest.framework.tests.api.mocks.SheepEntityResource;
import org.alfresco.rest.framework.tests.api.mocks3.FlockEntityResource;
import org.alfresco.rest.framework.tests.api.mocks3.FlocketEntityResource;
import org.alfresco.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test rest resource setup / locating
 * 
 * @author Gethin James
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-rest-context.xml" })
public class ResourceLocatorTests
{
    @Autowired
    ResourceLookupDictionary locator;

    @Autowired
    private ApplicationContext applicationContext;

    private static Api api = Api.valueOf("alfrescomock", "private", "1");
    
    @Before
    public void setUp() throws Exception
    {
        Map<String, Object> entityResourceBeans = applicationContext.getBeansWithAnnotation(EntityResource.class);
        Map<String, Object> relationResourceBeans = applicationContext.getBeansWithAnnotation(RelationshipResource.class);
        locator.setDictionary(ResourceDictionaryBuilder.build(entityResourceBeans.values(), relationResourceBeans.values()));
    }

    @Test(expected=org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException.class)
    public void testLocateResourcesByType()
    {

        Api api1 = Api.valueOf("alfrescomock", "private", "1");
        ResourceWithMetadata entityResource = locator.locateEntityResource(api1,"sheep", HttpMethod.GET);
        assertNotNull(entityResource);
        assertEquals(SheepEntityResource.class, entityResource.getResource().getClass());
        assertTrue(ResourceMetadata.RESOURCE_TYPE.ENTITY.equals(entityResource.getMetaData().getType()));

        Api api2 = Api.valueOf("alfrescomock", "private", "2");
        locateByVersion(api1);
        locateByVersion(api2);
        @SuppressWarnings("unused")
        ResourceWithMetadata relationResource = locator.locateRelationResource(api1, "sheep", "baah", HttpMethod.GET);
    }

    protected void locateByVersion(Api api)
    {
        ResourceWithMetadata relationResource = locator.locateRelationResource(api, "sheep", "baaahh", HttpMethod.GET);
        assertNotNull(relationResource);
        assertEquals(SheepBaaaahResource.class, relationResource.getResource().getClass());
        assertTrue(ResourceMetadata.RESOURCE_TYPE.RELATIONSHIP.equals(relationResource.getMetaData().getType()));
        
        relationResource = locator.locateRelationResource(api, "sheep", "blacksheep", HttpMethod.GET);
        assertNotNull(relationResource);
        assertEquals(SheepBlackSheepResource.class, relationResource.getResource().getClass());
        assertTrue(ResourceMetadata.RESOURCE_TYPE.RELATIONSHIP.equals(relationResource.getMetaData().getType()));
    }
    
    @Test(expected=org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException.class)
    public void testLocateResource()
    {
        Map<String, String> templateVars = new HashMap<String, String>();
        templateVars.put(ResourceLocator.COLLECTION_RESOURCE, "sheep");
        ResourceWithMetadata collResource = locator.locateResource(api, templateVars, HttpMethod.GET);
        assertNotNull(collResource);
        assertTrue(collResource.getMetaData().supports(HttpMethod.GET));

        collResource = locator.locateResource(api, templateVars, HttpMethod.POST);
        assertNotNull(collResource);
        assertTrue(collResource.getMetaData().supports(HttpMethod.POST));
        
        templateVars.put(ResourceLocator.ENTITY_ID, "farmersUniqueId");
        ResourceWithMetadata entityResource = locator.locateResource(api,templateVars, HttpMethod.GET);
        assertNotNull(entityResource);
        assertTrue(entityResource.getMetaData().supports(HttpMethod.GET));
        
        entityResource = locator.locateResource(api, templateVars, HttpMethod.PUT);
        assertNotNull(entityResource);
        assertTrue(entityResource.getMetaData().supports(HttpMethod.PUT));    
        
        templateVars.clear();
        templateVars.put(ResourceLocator.COLLECTION_RESOURCE, "sheepnoaction");
        collResource = locator.locateResource(api,templateVars, HttpMethod.GET);
        
    }
    
    @Test
    public void testLocateProperties()
    {
        Api api3 = Api.valueOf("alfrescomock", "private", "3");
        Map<String, String> templateVars = new HashMap<String, String>();
        templateVars.put(ResourceLocator.COLLECTION_RESOURCE, "flock");
        templateVars.put(ResourceLocator.ENTITY_ID, "flockId");
        templateVars.put(ResourceLocator.RELATIONSHIP_RESOURCE, "photo");
        ResourceWithMetadata collResource = locator.locateResource(api3, templateVars, HttpMethod.GET);
        assertNotNull(collResource);
        assertTrue(collResource.getMetaData().supports(HttpMethod.GET));
        assertEquals(FlockEntityResource.class, collResource.getResource().getClass());
        
        templateVars.put(ResourceLocator.COLLECTION_RESOURCE, "flocket");
        templateVars.put(ResourceLocator.RELATIONSHIP_RESOURCE, "madeUpProp");
        try
        {
            collResource = locator.locateResource(api3, templateVars, HttpMethod.PUT);
            fail("Should throw an UnsupportedResourceOperationException");
        }
        catch (UnsupportedResourceOperationException error)
        {
            //this is correct
        }
        try
        {
            collResource = locator.locateResource(api3, templateVars, HttpMethod.DELETE);
            fail("Should throw an UnsupportedResourceOperationException");
        }
        catch (UnsupportedResourceOperationException error)
        {
            //this is correct
        }
        collResource = locator.locateResource(api3, templateVars, HttpMethod.GET);
        assertNotNull(collResource);
        assertTrue(collResource.getMetaData().supports(HttpMethod.GET));
        assertEquals(FlocketEntityResource.class, collResource.getResource().getClass());
        
        templateVars.put(ResourceLocator.RELATIONSHIP_RESOURCE, "album");
        try
        {
            collResource = locator.locateResource(api3, templateVars, HttpMethod.DELETE);
            fail("Should throw an UnsupportedResourceOperationException");
        }
        catch (UnsupportedResourceOperationException error)
        {
            //this is correct
        }
        collResource = locator.locateResource(api3, templateVars, HttpMethod.GET);
        assertNotNull(collResource);
        assertTrue(collResource.getMetaData().supports(HttpMethod.GET));
        assertEquals(FlocketEntityResource.class, collResource.getResource().getClass());
        collResource = locator.locateResource(api3, templateVars, HttpMethod.PUT);
        assertNotNull(collResource);
        assertTrue(collResource.getMetaData().supports(HttpMethod.PUT));
        assertEquals(FlocketEntityResource.class, collResource.getResource().getClass());
    }
    
    @Test
    public void testApiValues()
    {

        Api testApi;
        try
        {
            //Tests by passing invalid values
            testApi = Api.valueOf(null,null,null);
            fail("Should throw an InvalidArgumentException");
        }
        catch (InvalidArgumentException error)
        {
            //this is correct
        }
        
        try
        {
            //version as 0
            testApi = Api.valueOf("alfrescomock", "public","0"); 
            fail("Should throw an InvalidArgumentException");
        }
        catch (InvalidArgumentException error)
        {
            //this is correct
        }
        
        try
        {
            //name as upper case
            testApi = Api.valueOf("AlfrescoMock", "public","1");
            fail("Should throw an InvalidArgumentException");
        }
        catch (InvalidArgumentException error)
        {
            //this is correct
        }

        try
        {
            //scope as anything
            testApi = Api.valueOf("alfrescomock", "nonsense","1");
            fail("Should throw an InvalidArgumentException");
        }
        catch (InvalidArgumentException error)
        {
            //this is correct
        }        
        
        //scope as mixed case
        testApi = Api.valueOf("alfrescomock", "Public","1");
        assertNotNull(testApi);
    }

    @Test
    public void testInvalidApis()
    {
        ResourceWithMetadata entityResource = null;
        
        try
        {
            entityResource = locator.locateEntityResource(Api.valueOf("alfrescomock", "public", "1"),"sheep", HttpMethod.GET);
            fail("Should throw an InvalidArgumentException");
        }
        catch (InvalidArgumentException error)
        {
            //this is correct
        }
        
        try
        {
            entityResource = locator.locateEntityResource(Api.valueOf("alfrescomock", "public", "999"),"sheep", HttpMethod.GET);
            fail("Should throw an InvalidArgumentException");
        }
        catch (InvalidArgumentException error)
        {
            //this is correct
        }
        
        entityResource = locator.locateEntityResource(Api.valueOf("alfrescomock", "private", "1"),"sheep", HttpMethod.GET);
        assertNotNull(entityResource);

    }
    
    @Test
    public void testResourceVersions()
    {
        ResourceWithMetadata aResource = null;
        
        try
        {
            aResource = locator.locateEntityResource(api, "sheepnoaction", HttpMethod.GET);
            fail("Should throw an InvalidArgumentException");
        }
        catch (InvalidArgumentException error)
        {
            //this is correct
        }
        
        //Previously no actions for this entity, with v2 now have a GET action
        Api v2 = Api.valueOf(api.getName(), api.getScope().toString(), "2");
        aResource = locator.locateEntityResource(v2, "sheepnoaction", HttpMethod.GET);
        assertNotNull(aResource);
        
        //Not defined in v2 but now available because all v1 are available in v2.
        aResource = locator.locateEntityResource(v2, "sheep", HttpMethod.GET);
        assertNotNull(aResource);
        
        try
        {
            //Not defined in v1
            aResource = locator.locateRelationResource(api, "sheepnoaction","v3isaresource", HttpMethod.GET);
            fail("Only available in v3");
        }
        catch (InvalidArgumentException error)
        {
            //this is correct
        }
        
        try
        {
            //Not defined in v2
            aResource = locator.locateRelationResource(v2, "sheepnoaction","v3isaresource", HttpMethod.GET);
            fail("Only available in v3");
        }
        catch (InvalidArgumentException error)
        {
            //this is correct
        }
        
        //Only defined in V3
        Api v3 = Api.valueOf(api.getName(), api.getScope().toString(), "3");
        aResource = locator.locateRelationResource(v3, "sheepnoaction","v3isaresource", HttpMethod.GET);
        assertNotNull("This resource is only available in v3",aResource);
        
        //Defined in v1 but available in v3
        aResource = locator.locateEntityResource(v3, "sheep", HttpMethod.GET);
        assertNotNull("This resource should be available in v3",aResource);
        
    }
    
    @Test
    public void testGetEmbeddedResources()
    {
        Map<String,Pair<String,Method>> embeddded = ResourceInspector.findEmbeddedResources(Farmer.class);
        assertNotNull(embeddded);
        final Map<String,ResourceWithMetadata> results = new HashMap<String,ResourceWithMetadata>(embeddded.size());
        for (Entry<String, Pair<String,Method>> embeddedEntry : embeddded.entrySet())
        {
            ResourceWithMetadata res = locator.locateEntityResource(api,embeddedEntry.getValue().getFirst(), HttpMethod.GET);
            results.put(embeddedEntry.getKey(),res);
        }
        assertNotNull(results);
        assertTrue(results.size() == 2);
        assertTrue(SheepEntityResource.class.equals(results.get("sheep").getResource().getClass()));
        Object goatResource = results.get("goat").getResource();
        assertTrue(GoatEntityResource.class.equals(goatResource.getClass()));
    }
   
    
    @Test
    public void testFindEntityCollectionName()
    {
        ResourceWithMetadata collResource =  locator.locateEntityResource(api,"sheep", HttpMethod.GET);
        String name = ResourceInspector.findEntityCollectionNameName(collResource.getMetaData());
        assertEquals ("sheep", name);
        
        ResourceWithMetadata relationResource = locator.locateRelationResource(api, "sheep", "baaahh", HttpMethod.GET);
        name = ResourceInspector.findEntityCollectionNameName(relationResource.getMetaData());
        assertEquals ("sheep", name);
        
        relationResource = locator.locateRelationResource(api, "sheep", "blacksheep", HttpMethod.GET);
        name = ResourceInspector.findEntityCollectionNameName(relationResource.getMetaData());
        assertEquals ("sheep", name);

        Api v2 = Api.valueOf(api.getName(), api.getScope().toString(), "2");
        Map<String, String> templateVars = new HashMap<String, String>();
        templateVars.put(ResourceLocator.COLLECTION_RESOURCE, "sheepnoaction");
        collResource = locator.locateResource(v2, templateVars, HttpMethod.GET);
        name = ResourceInspector.findEntityCollectionNameName(collResource.getMetaData());
        assertEquals ("sheepnoaction", name);
    }
    
    @Test(expected=org.alfresco.rest.framework.core.exceptions.InvalidArgumentException.class)
    public void testLocateRelationResource()
    {
        Collection<String> relKeys = Arrays.asList("blacksheep","baaahh");
        Map<String,ResourceWithMetadata> embeds = locator.locateRelationResource(api,"sheep", relKeys, HttpMethod.GET);
        assertNotNull(embeds);
        
        relKeys = Arrays.asList("nonsense");
        embeds = locator.locateRelationResource(api,"sheep", relKeys, HttpMethod.GET);
        assertNotNull(embeds);
        
    }
}
