package org.alfresco.rest.framework.tests.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceLookupDictionary;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.jacksonextensions.ExecutionResult;
import org.alfresco.rest.framework.resource.actions.ActionExecutor;
import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.tests.api.mocks.Goat;
import org.alfresco.rest.framework.tests.api.mocks.Grass;
import org.alfresco.rest.framework.tests.api.mocks.Sheep;
import org.alfresco.rest.framework.webscripts.AbstractResourceWebScript;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests the execution of resources
 */

public class ExecutionTests extends AbstractContextTest
{
    static final Api api3 = Api.valueOf("alfrescomock", "private", "3");

    @Test
    public void testInvokeGet() throws IOException
    {
        ResourceWithMetadata entityResource = locator.locateEntityResource(api,"sheep", HttpMethod.GET);
        AbstractResourceWebScript executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfGets");
        Object result = executor.execute(entityResource, Params.valueOf((String)null, null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), true);
        assertNotNull(result);

        entityResource = locator.locateEntityResource(api,"cow", HttpMethod.GET);
        result = executor.execute(entityResource, Params.valueOf((String)null, null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), true);
        assertNotNull(result);

        result = executor.execute(entityResource, Params.valueOf("543", null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), true);
        assertNotNull(result);

        ResourceWithMetadata baa = locator.locateRelationResource(api,"sheep", "baaahh", HttpMethod.GET);
        result = executor.execute(baa, Params.valueOf("4", null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), true);
        assertNotNull(result);

        executor.execute(baa, Params.valueOf("4", "45", mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), true);
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

        ResourceWithMetadata cowresource = locator.locateEntityResource(api,"cow", HttpMethod.POST);
        result = executor.execute(cowresource, Params.valueOf("654", null, NULL_PARAMS, Arrays.asList(grr), mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertEquals(grr,((ExecutionResult)result).getRoot());

        ResourceWithMetadata entityResource = locator.locateRelationResource(api,"grass", "grow", HttpMethod.POST);
        result = executor.execute(entityResource,  Params.valueOf("654", null, NULL_PARAMS, grr, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
        assertEquals("Growing well",result);

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

        ResourceWithMetadata resource = locator.locateRelationResource(api, "sheep", "blacksheep", HttpMethod.DELETE);
        result = executor.execute(resource,  Params.valueOf("4", null, mock(WebScriptRequest.class)),  mock(WebScriptResponse.class), false);
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
    }
}
