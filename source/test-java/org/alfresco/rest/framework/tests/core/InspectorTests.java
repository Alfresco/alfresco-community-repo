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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.api.model.Comment;
import org.alfresco.rest.api.nodes.NodeCommentsRelation;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.OperationResourceMetaData;
import org.alfresco.rest.framework.core.ResourceInspector;
import org.alfresco.rest.framework.core.ResourceInspectorUtil;
import org.alfresco.rest.framework.core.ResourceMetadata;
import org.alfresco.rest.framework.core.ResourceOperation;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.tests.api.mocks.Farmer;
import org.alfresco.rest.framework.tests.api.mocks.GoatEntityResource;
import org.alfresco.rest.framework.tests.api.mocks.Grass;
import org.alfresco.rest.framework.tests.api.mocks.GrassEntityResource;
import org.alfresco.rest.framework.tests.api.mocks.MultiPartTestEntityResource;
import org.alfresco.rest.framework.tests.api.mocks.MultiPartTestRelationshipResource;
import org.alfresco.rest.framework.tests.api.mocks.MultiPartTestResponse;
import org.alfresco.rest.framework.tests.api.mocks.Sheep;
import org.alfresco.rest.framework.tests.api.mocks.SheepBaaaahResource;
import org.alfresco.rest.framework.tests.api.mocks.SheepBlackSheepResource;
import org.alfresco.rest.framework.tests.api.mocks.SheepEntityResource;
import org.alfresco.rest.framework.tests.api.mocks.SheepNoActionEntityResource;
import org.alfresco.rest.framework.tests.api.mocks2.FarmersDaughter;
import org.alfresco.rest.framework.tests.api.mocks2.FarmersGrandson;
import org.alfresco.rest.framework.tests.api.mocks2.FarmersSon;
import org.alfresco.rest.framework.tests.api.mocks3.Flock;
import org.alfresco.rest.framework.tests.api.mocks3.FlockEntityResource;
import org.alfresco.rest.framework.tests.api.mocks3.FlocketEntityResource;
import org.alfresco.rest.framework.tests.api.mocks3.GoatRelationshipResource;
import org.alfresco.rest.framework.tests.api.mocks3.GrassEntityResourceNowDeleted;
import org.alfresco.rest.framework.tests.api.mocks3.SheepBlackSheepResourceIsNoMore;
import org.alfresco.rest.framework.tests.api.mocks3.SheepEntityResourceWithDeletedMethods;
import org.alfresco.rest.framework.tests.api.mocks3.SlimGoat;
import org.alfresco.util.Pair;
import org.junit.Test;
import org.springframework.extensions.webscripts.Status;
import org.springframework.http.HttpMethod;

/**
 * Looks at resources to see what they can do
 * 
 * @author Gethin James
 */
public class InspectorTests
{

    @Test
    public void testInspectEntity()
    {
        List<ResourceMetadata> metainfo = ResourceInspector.inspect(SheepEntityResource.class);
        assertTrue("Must be one ResourceMetadata",metainfo.size()==1);
        ResourceMetadata metaData = metainfo.get(0);
        assertNotNull(metaData);
        assertNotNull("SheepEntityResource supports GET", metaData.getOperation(HttpMethod.GET));
        assertNotNull("SheepEntityResource supports PUT", metaData.getOperation(HttpMethod.PUT));
        assertNotNull("SheepEntityResource supports DELETE", metaData.getOperation(HttpMethod.DELETE));
        assertNull("SheepEntityResource does not support POST", metaData.getOperation(HttpMethod.POST));
        ResourceOperation op = metaData.getOperation(HttpMethod.GET);
        assertEquals("Sheep ReadALL should return ACCEPTED", Status.STATUS_ACCEPTED, op.getSuccessStatus());
        op = metaData.getOperation(HttpMethod.PUT);
        assertTrue("SheepEntityResource must support Sheep", Sheep.class.equals(metaData.getObjectType(op)));
        
        metainfo = ResourceInspector.inspect(SheepNoActionEntityResource.class);
        assertTrue("SheepNoActionEntityResource has no actions.",metainfo.isEmpty());

        metainfo = ResourceInspector.inspect(GoatEntityResource.class);
        assertTrue("Must be one ResourceMetadata",metainfo.size()==1);
        metaData = metainfo.get(0);
        assertNotNull(metaData);
        assertNotNull("GoatEntityResource supports GET", metaData.getOperation(HttpMethod.GET));
        op = metaData.getOperation(HttpMethod.GET);
        List<ResourceParameter> params = op.getParameters();
        assertTrue("readById method should have 1 url param", params.size() == 1);
        
        metainfo = ResourceInspector.inspect(FlockEntityResource.class);
        assertTrue("Must be one ResourceMetadata",metainfo.size()==1);
        metaData = metainfo.get(0);
        assertNotNull(metaData);
        assertNotNull("FlockEntityResource supports GET", metaData.getOperation(HttpMethod.GET));
        assertNotNull("FlockEntityResource supports PUT", metaData.getOperation(HttpMethod.PUT));
        assertNotNull("FlockEntityResource supports DELETE", metaData.getOperation(HttpMethod.DELETE));
        assertNull("FlockEntityResource does not support POST", metaData.getOperation(HttpMethod.POST));
        metainfo = ResourceInspector.inspect(MultiPartTestEntityResource.class);
        assertTrue("Must be one ResourceMetadata",metainfo.size()==1);
        metaData = metainfo.get(0);
        assertNotNull(metaData);
        assertNotNull("MultiPartTestEntityResource support POST", metaData.getOperation(HttpMethod.POST));
        assertNull("MultiPartTestEntityResource does not supports GET", metaData.getOperation(HttpMethod.GET));
        assertNull("MultiPartTestEntityResource does not supports PUT", metaData.getOperation(HttpMethod.PUT));
        assertNull("MultiPartTestEntityResource does not supports DELETE", metaData.getOperation(HttpMethod.DELETE));
        op = metaData.getOperation(HttpMethod.POST);
        assertTrue("MultiPartTestEntityResource must support MultiPartTestResponse", MultiPartTestResponse.class.equals(metaData.getObjectType(op)));
        assertEquals("MultiPartTestEntityResource should return ACCEPTED", Status.STATUS_ACCEPTED, op.getSuccessStatus());

    }

    @Test
    public void testInspectRelationship()
    {
        List<ResourceMetadata> metainfo = ResourceInspector.inspect(SheepBlackSheepResource.class);
        assertTrue("Must be one ResourceMetadata",metainfo.size()==1);
        ResourceMetadata metaData = metainfo.get(0);
        assertNotNull(metaData);
        assertNotNull("SheepBlackSheepResource supports GET", metaData.getOperation(HttpMethod.GET));
        ResourceOperation op = metaData.getOperation(HttpMethod.GET);
        List<ResourceParameter> params = op.getParameters();
        assertTrue("readAll method should have 1 url param and 3 query params", params.size() == 4);
        assertNotNull("SheepBlackSheepResource supports PUT", metaData.getOperation(HttpMethod.PUT));
        op = metaData.getOperation(HttpMethod.PUT);
        params = op.getParameters();
        assertTrue("update method should have 2 url params and 1 HTTP_OBJECT param ", params.size() == 3);
        assertNotNull("SheepBlackSheepResource supports POST", metaData.getOperation(HttpMethod.POST));
        op = metaData.getOperation(HttpMethod.POST);
        params = op.getParameters();
        assertTrue("SheepBlackSheepResource must support Sheep", Sheep.class.equals(metaData.getObjectType(op)));
        assertTrue("create method should have 1 url param and 1 HTTP_OBJECT param ", params.size() == 2);
        assertNotNull(params);
        for (ResourceParameter resourceParameter : params)
        {
           if (ResourceParameter.KIND.HTTP_BODY_OBJECT.equals(resourceParameter.getParamType()))
           {
               assertFalse(resourceParameter.isAllowMultiple());  //set to not allow multiple
           }
        }
        assertNotNull("SheepBlackSheepResource supports DELETE", metaData.getOperation(HttpMethod.DELETE));
        op = metaData.getOperation(HttpMethod.DELETE);
        assertEquals("SheepBlackSheepResource should return STATUS_CONFLICT", Status.STATUS_CONFLICT, op.getSuccessStatus());
        params = op.getParameters();
        assertTrue("DELETE method on a relations should have 2 url params.", params.size() == 2);
        
        metainfo = ResourceInspector.inspect(MultiPartTestRelationshipResource.class);
        assertTrue("Must be one ResourceMetadata",metainfo.size()==1);
        metaData = metainfo.get(0);
        assertNotNull(metaData);
        assertNotNull("MultiPartTestRelationshipResource support POST", metaData.getOperation(HttpMethod.POST));
        assertNull("MultiPartTestRelationshipResource does not supports GET", metaData.getOperation(HttpMethod.GET));
        assertNull("MultiPartTestRelationshipResource does not supports PUT", metaData.getOperation(HttpMethod.PUT));
        assertNull("MultiPartTestRelationshipResource does not supports DELETE", metaData.getOperation(HttpMethod.DELETE));
        op = metaData.getOperation(HttpMethod.POST);
        assertTrue("MultiPartTestRelationshipResource must support MultiPartTestResponse", MultiPartTestResponse.class.equals(metaData.getObjectType(op)));
    }

    @Test
    public void testInspectApi()
    {
        Api api = ResourceInspector.inspectApi(SheepBlackSheepResource.class);
        assertNotNull(api);
        assertEquals(Api.SCOPE.PRIVATE,api.getScope());
        assertEquals(1,api.getVersion());
        
        api = ResourceInspector.inspectApi(String.class);
        assertNull(api);
        
        api = ResourceInspector.inspectApi(NodeCommentsRelation.class);
        assertNotNull(api);
        assertEquals(Api.SCOPE.PUBLIC,api.getScope());
        assertEquals(1,api.getVersion());
    }
    
    @Test
    public void testNodesCommentsRelation()
    {
        List<ResourceMetadata> metainfo = ResourceInspector.inspect(NodeCommentsRelation.class);
        assertTrue("Must be one ResourceMetadata",metainfo.size()==1);
        ResourceMetadata metaData = metainfo.get(0);
        assertNotNull(metaData);
        ResourceOperation op = metaData.getOperation(HttpMethod.POST);
        assertTrue("NodeCommentsRelation must support Comment", Comment.class.equals(metaData.getObjectType(op)));
    }
    
    @Test
    public void testUniqueIdAnnotation()
    {
        String uniqueId = ResourceInspector.findUniqueId(new Grass("34"));
        assertNull(uniqueId);

        uniqueId = ResourceInspector.findUniqueId(new Farmer("345"));
        assertNotNull(uniqueId);
        assertTrue("345".equals(uniqueId));
        
        //inherited classes are ok (with overidden unique id method)
        uniqueId = ResourceInspector.findUniqueId(new FarmersSon("567"));
        assertNotNull(uniqueId);
        assertTrue("567".equals(uniqueId));
        
        //inherited classes are ok (with overidden unique id method but @UniqueId annotation not specified)
        uniqueId = ResourceInspector.findUniqueId(new FarmersGrandson("12"));
        assertNotNull(uniqueId);
        assertTrue("12".equals(uniqueId));
        
        //More than 1 annotation should throw IllegalArgumentException
        try
        {
            uniqueId = ResourceInspector.findUniqueId(new FarmersDaughter("21"));
            fail("Should throw an InvalidArgumentException");
        }
        catch (IllegalArgumentException error)
        {
            //this is correct
        }

    }
    
    @Test
    public void testEmbeddedAnnotation()
    {
        Map<String,Pair<String,Method>> embeds = ResourceInspector.findEmbeddedResources(Farmer.class);
        assertNotNull(embeds);
        assertTrue(embeds.size() == 2);
        assertTrue("sheep".equals(embeds.get("sheep").getFirst()));
        assertTrue("goat".equals(embeds.get("goat").getFirst()));
        
        //inherited annotations on classes are ok.
        embeds = ResourceInspector.findEmbeddedResources(FarmersSon.class);
        assertNotNull(embeds);
        assertTrue(embeds.size() == 2);
        assertTrue("sheep".equals(embeds.get("sheep").getFirst()));
        assertTrue("goat".equals(embeds.get("goat").getFirst()));
        
        //inherited annotations on classes are ok, with another added embedded
        embeds = ResourceInspector.findEmbeddedResources(FarmersDaughter.class);
        assertNotNull(embeds);
        assertTrue(embeds.size() == 3);
        assertTrue("sheep".equals(embeds.get("sheep").getFirst()));
        assertTrue("goat".equals(embeds.get("goat").getFirst()));
        assertTrue("grass".equals(embeds.get("specialgrass").getFirst()));
        
        //inherited annotations on classes are ok, 
        //Sheep is overridden but no annotation specified
        //Goat is overridden and new annotation key is specified (THIS IS NOT RECOMMENDED)
        embeds = ResourceInspector.findEmbeddedResources(FarmersGrandson.class);
        assertNotNull(embeds);
        assertTrue(embeds.size() == 2);
        assertTrue("sheep".equals(embeds.get("sheep").getFirst()));
        assertTrue("goat".equals(embeds.get("grandgoat").getFirst()));
        
        embeds = ResourceInspector.findEmbeddedResources(Sheep.class);
        assertNotNull(embeds);
        assertTrue("Sheep has no embedded entities", embeds.isEmpty());
        
        embeds = ResourceInspector.findEmbeddedResources(Grass.class);
        assertNotNull(embeds);
        assertTrue("Grass has no embedded entities", embeds.isEmpty());
        
        embeds = ResourceInspector.findEmbeddedResources(SlimGoat.class);
        assertNotNull(embeds);
        assertTrue("SlimGoat has no embedded entities", embeds.isEmpty());
    }
    
    @Test
    public void testWebDeletedAnnotationOnMethods()
    {
       Method aMethod = ResourceInspector.findMethod(EntityResourceAction.Read.class, SheepEntityResourceWithDeletedMethods.class);
       assertNotNull("This method is untouched so should be found.",aMethod);
       assertTrue("readAll".equals(aMethod.getName()));
       
       aMethod = ResourceInspector.findMethod(EntityResourceAction.ReadById.class, SheepEntityResourceWithDeletedMethods.class);
       assertNotNull("This method is overriden but not deleted so should be found.",aMethod);
       assertTrue("readById".equals(aMethod.getName()));
       
       aMethod = ResourceInspector.findMethod(EntityResourceAction.Update.class, SheepEntityResourceWithDeletedMethods.class);
       assertTrue("Update method has been marketed as deleted.",ResourceInspector.isDeleted(aMethod));
       
       aMethod = ResourceInspector.findMethod(EntityResourceAction.Delete.class, SheepEntityResourceWithDeletedMethods.class);
       assertTrue("Delete method has been marketed as deleted.",ResourceInspector.isDeleted(aMethod));

    }
    
    @Test
    public void testInspectOperation()
    {
        Method aMethod = ResourceInspector.findMethod(EntityResourceAction.Read.class, SheepEntityResource.class);
        ResourceOperation op = ResourceInspector.inspectOperation(SheepEntityResource.class, aMethod, HttpMethod.GET);
        assertNotNull(op);
        assertTrue(HttpMethod.GET.equals(op.getHttpMethod()));
        assertTrue("Gets all the Sheep".equals(op.getTitle()));
        assertEquals("Sheep ReadALL should return ACCEPTED", Status.STATUS_ACCEPTED, op.getSuccessStatus());
        assertTrue("".equals(op.getDescription()));
        assertNotNull(op.getParameters());
        assertTrue(op.getParameters().size() == 7);
        
        for (ResourceParameter aParam : op.getParameters())
        { 
            assertNotNull(aParam.getName());
            assertNotNull(aParam.getDescription());
            assertNotNull(aParam.getTitle());
            
            switch (aParam.getParamType()) {
                case QUERY_STRING:
                    if (("requiredParam".equals(aParam.getName())))
                    {
                        assertTrue(aParam.isRequired());
                    }
                    break;
                case URL_PATH:
                    assertTrue(("siteId".equals(aParam.getName())));
                    assertFalse(aParam.isRequired());
                    break;
                case HTTP_BODY_OBJECT:
                    assertTrue(("body".equals(aParam.getName())));
                    assertFalse(aParam.isRequired());
                    break;
                case HTTP_HEADER:
                    assertTrue(("who".equals(aParam.getName())));
                    assertFalse(aParam.isRequired());
                    
            }

        }
        
        //No @WebApiDescription or param (in future WebApiDescription will be mandatory)
        aMethod = ResourceInspector.findMethod(EntityResourceAction.ReadById.class, SheepEntityResource.class);
        op = ResourceInspector.inspectOperation(SheepEntityResource.class, aMethod, HttpMethod.GET);
        assertNotNull(op);
        assertTrue(op.getTitle().startsWith("Missing @WebApiDescription annotation"));

        aMethod = ResourceInspector.findMethod(EntityResourceAction.ReadById.class, GrassEntityResource.class);
        op = ResourceInspector.inspectOperation(GrassEntityResource.class, aMethod, HttpMethod.GET);
        assertNotNull(op);
        assertTrue(op.getTitle().startsWith("Gets grass by id"));
        assertTrue("readById method should have 1 url param", op.getParameters().size() == 1);
        ResourceParameter singleParam = op.getParameters().get(0);
        assertTrue(ResourceParameter.KIND.URL_PATH.equals(singleParam.getParamType()));
        assertFalse("URL paths can never suport multiple params, its always FALSE", singleParam.isAllowMultiple());
        assertNotNull(singleParam.getDescription());
        assertNotNull(singleParam.getTitle());
        assertNotNull(singleParam.getName());
        assertTrue(singleParam.isRequired());
        
        aMethod = ResourceInspector.findMethod(BinaryResourceAction.Read.class, FlockEntityResource.class);
        op = ResourceInspector.inspectOperation(FlockEntityResource.class, aMethod, HttpMethod.GET);
        assertNotNull(op);
        assertTrue(op.getTitle().startsWith("Reads a photo as a Stream"));
          
        aMethod = ResourceInspector.findMethod(BinaryResourceAction.Delete.class, FlockEntityResource.class);
        op = ResourceInspector.inspectOperation(FlockEntityResource.class, aMethod, HttpMethod.DELETE);
        assertNotNull(op);
        assertTrue(op.getTitle().startsWith("Deletes a photo"));

        aMethod = ResourceInspector.findMethod(BinaryResourceAction.Update.class, FlockEntityResource.class);
        op = ResourceInspector.inspectOperation(FlockEntityResource.class, aMethod, HttpMethod.PUT);
        assertNotNull(op);
        assertTrue(op.getTitle().startsWith("Updates a photo"));

        aMethod = ResourceInspector.findMethod(BinaryResourceAction.Read.class, SheepBaaaahResource.class);
        op = ResourceInspector.inspectOperation(SheepBaaaahResource.class, aMethod, HttpMethod.GET);
        assertNotNull(op);
        assertTrue(op.getTitle().startsWith("Reads a photo"));

        aMethod = ResourceInspector.findMethod(BinaryResourceAction.Delete.class, SheepBaaaahResource.class);
        op = ResourceInspector.inspectOperation(SheepBaaaahResource.class, aMethod, HttpMethod.DELETE);
        assertNotNull(op);
        assertTrue(op.getTitle().startsWith("Deletes a photo"));

        aMethod = ResourceInspector.findMethod(BinaryResourceAction.Update.class, SheepBaaaahResource.class);
        op = ResourceInspector.inspectOperation(SheepBaaaahResource.class, aMethod, HttpMethod.PUT);
        assertNotNull(op);
        assertTrue(op.getTitle().startsWith("Updates a photo"));
    }

    @Test
    public void testInspectBodyParam()
    {
        Method aMethod = ResourceInspector.findMethod(BinaryResourceAction.Update.class, FlockEntityResource.class);
        ResourceOperation op = ResourceInspector.inspectOperation(FlockEntityResource.class, aMethod, HttpMethod.PUT);
        assertNotNull(op);
        List<ResourceParameter> params  = op.getParameters();
        assertTrue(params.size()==2);
        for (ResourceParameter param:params)
        {
            if (ResourceParameter.KIND.HTTP_BODY_OBJECT.equals(param.getParamType()))
            {
                assertEquals(Flock.class, param.getDataType());
            }
        }

        aMethod = ResourceInspector.findMethod(RelationshipResourceAction.Create.class, SheepBlackSheepResource.class);
        op = ResourceInspector.inspectOperation(SheepBlackSheepResource.class, aMethod, HttpMethod.POST);
        assertNotNull(op);
        params  = op.getParameters();
        assertTrue(params.size()==2);
        for (ResourceParameter param:params)
        {
            if (ResourceParameter.KIND.HTTP_BODY_OBJECT.equals(param.getParamType()))
            {
                assertEquals(Sheep.class, param.getDataType());
            }
        }

        aMethod = ResourceInspector.findMethod(EntityResourceAction.Update.class, SheepEntityResourceWithDeletedMethods.class);
        op = ResourceInspector.inspectOperation(SheepEntityResourceWithDeletedMethods.class, aMethod, HttpMethod.POST);
        assertNotNull(op);
        params  = op.getParameters();
        assertNotNull(params);
        for (ResourceParameter param:params)
        {
            if (ResourceParameter.KIND.HTTP_BODY_OBJECT.equals(param.getParamType()))
            {
                assertEquals(Sheep.class, param.getDataType());
            }
        }
    }

    @Test
    public void testInspectOperations() throws IllegalAccessException, InstantiationException, Throwable
    {
        Api api = Api.valueOf("alfrescomock", "private", "1");
        List<ResourceMetadata> metainfo = new ArrayList<ResourceMetadata>();

        GrassEntityResource grassEntityResource = new GrassEntityResource();
        ResourceInspector.inspectOperations(api, GrassEntityResource.class,"-root-", metainfo);
        assertTrue(metainfo.size()==2);

        for (ResourceMetadata resourceMetadata : metainfo)
        {
            assertEquals(ResourceMetadata.RESOURCE_TYPE.OPERATION, resourceMetadata.getType());
            OperationResourceMetaData operationResourceMetaData = (OperationResourceMetaData) resourceMetadata;
            Method actionMethod = operationResourceMetaData.getOperationMethod();
            String result = null;
            switch (resourceMetadata.getUniqueId())
            {
                case "/-root-/{id}/grow":
                    assertNotNull("GrassEntityResource supports POST", resourceMetadata.getOperation(HttpMethod.POST));
                    assertNull("GrassEntityResource does not support DELETE", resourceMetadata.getOperation(HttpMethod.DELETE));
                    ResourceOperation op = resourceMetadata.getOperation(HttpMethod.POST);
                    assertEquals("grow should return ACCEPTED", Status.STATUS_ACCEPTED, op.getSuccessStatus());
                    Class paramType = resourceMetadata.getObjectType(op);
                    Object paramObj = paramType.newInstance();
                    result = (String) ResourceInspectorUtil.invokeMethod(actionMethod,grassEntityResource, "xyz", paramObj, Params.valueOf("notUsed", null));
                    assertEquals("Growing well",result);
                    break;
                case "/-root-/{id}/cut":
                    assertNotNull("GrassEntityResource supports POST", resourceMetadata.getOperation(HttpMethod.POST));
                    assertNull("GrassEntityResource does not support GET", resourceMetadata.getOperation(HttpMethod.GET));
                    op = resourceMetadata.getOperation(HttpMethod.POST);
                    assertNull(resourceMetadata.getObjectType(op));
                    assertEquals("cut should return ACCEPTED", Status.STATUS_NOT_IMPLEMENTED, op.getSuccessStatus());
                    result = (String) ResourceInspectorUtil.invokeMethod(actionMethod,grassEntityResource, "xyz", null, Params.valueOf("notUsed", null));
                    assertEquals("All done",result);
                    break;
                default:
                    fail("Invalid action information.");
            }

        }
    }



    @Test
    public void testInspectRelationshipProperties()
    {
        List<ResourceMetadata> metainfo = ResourceInspector.inspect(GoatRelationshipResource.class);
        assertTrue(metainfo.size()==2);
        for (ResourceMetadata resourceMetadata : metainfo)
        {
            switch (resourceMetadata.getUniqueId())
            {
                case "/goat/{entityId}/herd":
                    assertNotNull("GoatRelationshipResource supports GET", resourceMetadata.getOperation(HttpMethod.GET));
                    break;
                case "/goat/{entityId}/herd/{id}/content":
                    assertNotNull("GoatRelationshipResource supports GET", resourceMetadata.getOperation(HttpMethod.GET));
                    break;
                default:
                    fail("Invalid information.");
            }
        }
    }

    @Test
    public void testInspectAddressedProperties()
    {
        
        Api api = Api.valueOf("alfrescomock", "private", "1");
        List<ResourceMetadata> metainfo = new ArrayList<ResourceMetadata>();
        ResourceInspector.inspectAddressedProperties(api, FlockEntityResource.class, "myroot", metainfo);
        assertTrue(metainfo.size()==1);
        ResourceMetadata metaData = metainfo.get(0);
        assertEquals("/myroot/{id}/photo",metaData.getUniqueId());
        assertTrue(metaData.getOperations().size()==3);
        assertNotNull("FlockEntityResource supports GET", metaData.getOperation(HttpMethod.GET));
        assertNotNull("FlockEntityResource supports PUT", metaData.getOperation(HttpMethod.PUT));
        assertNotNull("FlockEntityResource supports DELETE", metaData.getOperation(HttpMethod.DELETE));
        
        metainfo.clear();
        ResourceInspector.inspectAddressedProperties(api, FlocketEntityResource.class, "myroot", metainfo);
        assertTrue(metainfo.size()==3);
        for (ResourceMetadata resourceMetadata : metainfo)
        {
            //If this code is running on a Java 7 or above then please use the switch statement instead of an if.
//            switch (resourceMetadata.getUniqueId())
//            {
//                case "/myroot/photo":
//                    assertTrue("FlocketEntityResource supports GET", resourcemetaData.getOperation(HttpMethod.GET));
//                    assertTrue("FlocketEntityResource supports PUT", resourcemetaData.getOperation(HttpMethod.PUT));
//                    assertTrue("FlocketEntityResource supports DELETE", resourcemetaData.getOperation(HttpMethod.DELETE));
//                    break;
//                case "/myroot/album":
//                    assertTrue("FlocketEntityResource supports GET", resourcemetaData.getOperation(HttpMethod.GET));
//                    assertTrue("FlocketEntityResource supports PUT", resourcemetaData.getOperation(HttpMethod.PUT));
//                    assertTrue("FlocketEntityResource does not support DELETE", !resourcemetaData.getOperation(HttpMethod.DELETE));
//                    break;
//                case "/myroot/madeUpProp":
//                    assertTrue("FlocketEntityResource supports GET", resourcemetaData.getOperation(HttpMethod.GET));
//                    assertTrue("FlocketEntityResource does not supports PUT", !resourcemetaData.getOperation(HttpMethod.PUT));
//                    assertTrue("FlocketEntityResource does not support DELETE", !resourcemetaData.getOperation(HttpMethod.DELETE));
//                    break;
//                default:
//                    fail("Invalid address property information.");
//            }
          
            if ("/myroot/{id}/photo".equals(resourceMetadata.getUniqueId()))
            {
                assertNotNull("FlocketEntityResource supports GET", resourceMetadata.getOperation(HttpMethod.GET));
                assertNotNull("FlocketEntityResource supports PUT", resourceMetadata.getOperation(HttpMethod.PUT));
                assertNotNull("FlocketEntityResource supports DELETE", resourceMetadata.getOperation(HttpMethod.DELETE));
            }
            else
            {
                if ("/myroot/{id}/album".equals(resourceMetadata.getUniqueId()))
                {
                    assertNotNull("FlocketEntityResource supports GET", resourceMetadata.getOperation(HttpMethod.GET));
                    assertNotNull("FlocketEntityResource supports PUT", resourceMetadata.getOperation(HttpMethod.PUT));
                    assertNull("FlocketEntityResource does not support DELETE", resourceMetadata.getOperation(HttpMethod.DELETE));
                }
                else
                {
                    if ("/myroot/{id}/madeUpProp".equals(resourceMetadata.getUniqueId()))
                    {
                        assertNotNull("FlocketEntityResource supports GET", resourceMetadata.getOperation(HttpMethod.GET));
                        assertNull("FlocketEntityResource does not supports PUT", resourceMetadata.getOperation(HttpMethod.PUT));
                        assertNull("FlocketEntityResource does not support DELETE", resourceMetadata.getOperation(HttpMethod.DELETE));
                    }
                    else
                    {
                        fail("Invalid address property information.");
                    }
                }
            }
   
        }
    }
    
    @Test
    public void testWebDeletedAnnotationOnClass()
    {
        List<ResourceMetadata> metainfo = ResourceInspector.inspect(GrassEntityResourceNowDeleted.class);
        assertTrue("Must be at least one ResourceMetadata",metainfo.size()>0);
        ResourceMetadata metaData = metainfo.get(0);
        assertNotNull(metaData);
        assertNull("GrassEntityResourceNowDeleted all methods deleted", metaData.getOperation(HttpMethod.GET));
        assertNull("GrassEntityResourceNowDeleted all methods deleted", metaData.getOperation(HttpMethod.PUT));
        assertNull("GrassEntityResourceNowDeleted all methods deleted", metaData.getOperation(HttpMethod.DELETE));
        assertNull("GrassEntityResourceNowDeleted all methods deleted", metaData.getOperation(HttpMethod.POST));
        
        metainfo = ResourceInspector.inspect(SheepBlackSheepResourceIsNoMore.class);
        assertTrue("Must be at least one ResourceMetadata",metainfo.size()>0);
        metaData = metainfo.get(0);
        assertNotNull(metaData);
        assertNull("SheepBlackSheepResourceIsNoMore all methods deleted", metaData.getOperation(HttpMethod.GET));
        assertNull("SheepBlackSheepResourceIsNoMore all methods deleted", metaData.getOperation(HttpMethod.PUT));
        assertNull("SheepBlackSheepResourceIsNoMore all methods deleted", metaData.getOperation(HttpMethod.DELETE));
        assertNull("SheepBlackSheepResourceIsNoMore all methods deleted", metaData.getOperation(HttpMethod.POST));

    }

    @Test
    public void testValidSuccessCode()
    {
        //Test defaults
        assertEquals(Status.STATUS_OK,ResourceInspector.validSuccessCode(HttpMethod.GET, ResourceOperation.UNSET_STATUS));
        assertEquals(Status.STATUS_CREATED,ResourceInspector.validSuccessCode(HttpMethod.POST, ResourceOperation.UNSET_STATUS));
        assertEquals(Status.STATUS_OK,ResourceInspector.validSuccessCode(HttpMethod.PUT, ResourceOperation.UNSET_STATUS));
        assertEquals(Status.STATUS_NO_CONTENT,ResourceInspector.validSuccessCode(HttpMethod.DELETE, ResourceOperation.UNSET_STATUS));

        //Test custom values
        assertEquals(Status.STATUS_ACCEPTED,ResourceInspector.validSuccessCode(HttpMethod.GET, Status.STATUS_ACCEPTED));
        assertEquals(Status.STATUS_FOUND,ResourceInspector.validSuccessCode(HttpMethod.POST, Status.STATUS_FOUND));
        assertEquals(Status.STATUS_ACCEPTED,ResourceInspector.validSuccessCode(HttpMethod.PUT, Status.STATUS_ACCEPTED));
        assertEquals(Status.STATUS_NOT_MODIFIED,ResourceInspector.validSuccessCode(HttpMethod.DELETE, Status.STATUS_NOT_MODIFIED));
    }
}
