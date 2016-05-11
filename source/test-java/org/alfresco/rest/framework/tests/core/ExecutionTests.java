package org.alfresco.rest.framework.tests.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceLookupDictionary;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.jacksonextensions.ExecutionResult;
import org.alfresco.rest.framework.resource.actions.ActionExecutor;
import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.tests.api.mocks.Grass;
import org.alfresco.rest.framework.tests.api.mocks.Sheep;
import org.alfresco.rest.framework.webscripts.AbstractResourceWebScript;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Arrays;

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
        executor.execute(entityResource, Params.valueOf((String)null, null), new ActionExecutor.ExecutionCallback<CollectionWithPagingInfo>(){
            @Override
            public void onSuccess(CollectionWithPagingInfo result, ContentInfo contentInfo)
            {
                assertNotNull(result);
            }});

        ResourceWithMetadata baa = locator.locateRelationResource(api,"sheep", "baaahh", HttpMethod.GET);
        executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfGets");
        executor.execute(baa, Params.valueOf("4", null), new ActionExecutor.ExecutionCallback<CollectionWithPagingInfo>(){
            @Override
            public void onSuccess(CollectionWithPagingInfo result, ContentInfo contentInfo)
            {
                assertNotNull(result);
            }});

        executor.execute(baa, Params.valueOf("4", "45"), new ActionExecutor.ExecutionCallback<ExecutionResult>(){
            @Override
            public void onSuccess(ExecutionResult result, ContentInfo contentInfo)
            {
                assertNotNull(result);
            }});

        ResourceWithMetadata baaPhoto = locator.locateRelationResource(api,"sheep/{entityId}/baaahh", "photo", HttpMethod.GET);
        executor.execute(baaPhoto, Params.valueOf("4", "45"), new ActionExecutor.ExecutionCallback<CollectionWithPagingInfo>(){
            @Override
            public void onSuccess(CollectionWithPagingInfo result, ContentInfo contentInfo)
            {
                assertNull(result);
            }});

    }

    @Test
    public void testInvokePost() throws IOException
    {
        AbstractResourceWebScript executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfPost");

        ResourceWithMetadata resource = locator.locateRelationResource(api, "sheep", "blacksheep", HttpMethod.POST);
        final Sheep aSheep = new Sheep("xyz");
        executor.execute(resource, Params.valueOf("654", null, NULL_PARAMS, Arrays.asList(aSheep)), new ActionExecutor.ExecutionCallback<ExecutionResult>(){
            @Override
            public void onSuccess(ExecutionResult result, ContentInfo contentInfo)
            {
                assertNotNull(result);
                assertEquals(aSheep,result.getRoot());
            }});

        ResourceWithMetadata grassResource = locator.locateEntityResource(api,"grass", HttpMethod.POST);
        final Grass grr = new Grass("grr");
        executor.execute(grassResource, Params.valueOf("654", null, NULL_PARAMS, Arrays.asList(grr)), new ActionExecutor.ExecutionCallback<ExecutionResult>(){
            @Override
            public void onSuccess(ExecutionResult result, ContentInfo contentInfo)
            {
                assertEquals(grr,result.getRoot());
            }});

        ResourceWithMetadata entityResource = locator.locateRelationResource(api,"grass", "grow", HttpMethod.POST);
        executor.execute(entityResource, Params.valueOf("654", null, NULL_PARAMS, grr), new ActionExecutor.ExecutionCallback<String>(){
            @Override
            public void onSuccess(String result, ContentInfo contentInfo)
            {
                assertEquals("Growing well",result);
            }});

    }

    @Test
    public void testInvokeDelete() throws IOException
    {
        ResourceWithMetadata grassResource = locator.locateEntityResource(api,"grass", HttpMethod.DELETE);
        AbstractResourceWebScript executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfDelete");
        executor.execute(grassResource, Params.valueOf("4", null), new ActionExecutor.ExecutionCallback<Object>(){
            @Override
            public void onSuccess(Object result, ContentInfo contentInfo)
            {
                assertNull(result);
            }});
        ResourceWithMetadata resource = locator.locateRelationResource(api, "sheep", "blacksheep", HttpMethod.DELETE);
        executor.execute(resource, Params.valueOf("4", null), new ActionExecutor.ExecutionCallback<Object>(){
            @Override
            public void onSuccess(Object result, ContentInfo contentInfo)
            {
                assertNull(result);
            }});

        ResourceWithMetadata goatDelete = locator.locateRelationResource(api3,"goat/{entityId}/herd", "content", HttpMethod.DELETE);
        executor.execute(goatDelete, Params.valueOf("4", "56"), new ActionExecutor.ExecutionCallback<Object>(){
            @Override
            public void onSuccess(Object result, ContentInfo contentInfo)
            {
                assertNull(result);
            }});
    }


    @Test
    public void testInvokePut() throws IOException
    {
        ResourceWithMetadata entityResource = locator.locateEntityResource(api,"sheep", HttpMethod.PUT);
        AbstractResourceWebScript executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfPut");
        final Sheep aSheep = new Sheep("xyz");
        executor.execute(entityResource, Params.valueOf("654", null, NULL_PARAMS, aSheep), new ActionExecutor.ExecutionCallback<ExecutionResult>(){
            @Override
            public void onSuccess(ExecutionResult result, ContentInfo contentInfo)
            {
                assertNotNull(result);
                assertEquals(aSheep,result.getRoot());
            }});

        ResourceWithMetadata resource = locator.locateRelationResource(api, "sheep", "blacksheep", HttpMethod.PUT);
        executor.execute(resource, Params.valueOf("654", null, NULL_PARAMS, aSheep), new ActionExecutor.ExecutionCallback<ExecutionResult>(){
            @Override
            public void onSuccess(ExecutionResult result, ContentInfo contentInfo)
            {
                assertNotNull(result);
                assertEquals(aSheep,result.getRoot());
            }});
        ResourceWithMetadata baaPhoto = locator.locateRelationResource(api,"sheep/{entityId}/baaahh", "photo", HttpMethod.PUT);
        executor.execute(baaPhoto, Params.valueOf("4", "56"), new ActionExecutor.ExecutionCallback<Object>(){
            @Override
            public void onSuccess(Object result, ContentInfo contentInfo)
            {
                assertNull(result);
            }});
    }
}
