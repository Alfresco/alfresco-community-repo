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
import org.alfresco.rest.framework.Action;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.core.ActionResourceMetaData;
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
import org.alfresco.rest.framework.tests.api.mocks.SheepBlackSheepResource;
import org.alfresco.rest.framework.tests.api.mocks.SheepEntityResource;
import org.alfresco.rest.framework.tests.api.mocks.SheepNoActionEntityResource;
import org.alfresco.rest.framework.tests.api.mocks2.FarmersDaughter;
import org.alfresco.rest.framework.tests.api.mocks2.FarmersGrandson;
import org.alfresco.rest.framework.tests.api.mocks2.FarmersSon;
import org.alfresco.rest.framework.tests.api.mocks3.Flock;
import org.alfresco.rest.framework.tests.api.mocks3.FlockEntityResource;
import org.alfresco.rest.framework.tests.api.mocks3.FlocketEntityResource;
import org.alfresco.rest.framework.tests.api.mocks3.GrassEntityResourceNowDeleted;
import org.alfresco.rest.framework.tests.api.mocks3.SheepBlackSheepResourceIsNoMore;
import org.alfresco.rest.framework.tests.api.mocks3.SheepEntityResourceWithDeletedMethods;
import org.alfresco.rest.framework.tests.api.mocks3.SlimGoat;
import org.alfresco.util.Pair;
import org.junit.Test;
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
        assertTrue("SheepEntityResource supports GET", metaData.supports(HttpMethod.GET));
        assertTrue("SheepEntityResource supports PUT", metaData.supports(HttpMethod.PUT));
        assertTrue("SheepEntityResource supports DELETE", metaData.supports(HttpMethod.DELETE));
        assertTrue("SheepEntityResource does not support POST", !metaData.supports(HttpMethod.POST));
        assertTrue("SheepEntityResource must support Sheep", Sheep.class.equals(metaData.getObjectType(HttpMethod.PUT)));
        
        metainfo = ResourceInspector.inspect(SheepNoActionEntityResource.class);
        assertTrue("SheepNoActionEntityResource has no actions.",metainfo.isEmpty());

        metainfo = ResourceInspector.inspect(GoatEntityResource.class);
        assertTrue("Must be one ResourceMetadata",metainfo.size()==1);
        metaData = metainfo.get(0);
        assertNotNull(metaData);
        assertTrue("GoatEntityResource supports GET", metaData.supports(HttpMethod.GET));
        List<ResourceParameter> params = metaData.getParameters(HttpMethod.GET);
        assertTrue("readById method should have 1 url param", params.size() == 1);
        
        metainfo = ResourceInspector.inspect(FlockEntityResource.class);
        assertTrue("Must be one ResourceMetadata",metainfo.size()==1);
        metaData = metainfo.get(0);
        assertNotNull(metaData);
        assertTrue("FlockEntityResource supports GET", metaData.supports(HttpMethod.GET));
        assertTrue("FlockEntityResource supports PUT", metaData.supports(HttpMethod.PUT));
        assertTrue("FlockEntityResource supports DELETE", metaData.supports(HttpMethod.DELETE));
        assertTrue("FlockEntityResource does not support POST", !metaData.supports(HttpMethod.POST));

        metainfo = ResourceInspector.inspect(MultiPartTestEntityResource.class);
        assertTrue("Must be one ResourceMetadata",metainfo.size()==1);
        metaData = metainfo.get(0);
        assertNotNull(metaData);
        assertTrue("MultiPartTestEntityResource support POST", metaData.supports(HttpMethod.POST));
        assertFalse("MultiPartTestEntityResource does not supports GET", metaData.supports(HttpMethod.GET));
        assertFalse("MultiPartTestEntityResource does not supports PUT", metaData.supports(HttpMethod.PUT));
        assertFalse("MultiPartTestEntityResource does not supports DELETE", metaData.supports(HttpMethod.DELETE));
        assertTrue("MultiPartTestEntityResource must support MultiPartTestResponse", MultiPartTestResponse.class.equals(metaData.getObjectType(HttpMethod.POST)));

    }

    @Test
    public void testInspectRelationship()
    {
        List<ResourceMetadata> metainfo = ResourceInspector.inspect(SheepBlackSheepResource.class);
        assertTrue("Must be one ResourceMetadata",metainfo.size()==1);
        ResourceMetadata metaData = metainfo.get(0);
        assertNotNull(metaData);
        assertTrue("SheepBlackSheepResource supports GET", metaData.supports(HttpMethod.GET));
        List<ResourceParameter> params = metaData.getParameters(HttpMethod.GET);
        assertTrue("readAll method should have 1 url param and 3 query params", params.size() == 4);
        assertTrue("SheepBlackSheepResource supports PUT", metaData.supports(HttpMethod.PUT));
        params = metaData.getParameters(HttpMethod.PUT);
        assertTrue("update method should have 2 url params and 1 HTTP_OBJECT param ", params.size() == 3);
        assertTrue("SheepBlackSheepResource supports POST", metaData.supports(HttpMethod.POST));
        assertTrue("SheepBlackSheepResource must support Sheep", Sheep.class.equals(metaData.getObjectType(HttpMethod.POST)));
        params = metaData.getParameters(HttpMethod.POST);
        assertTrue("create method should have 1 url param and 1 HTTP_OBJECT param ", params.size() == 2);
        assertNotNull(params);
        for (ResourceParameter resourceParameter : params)
        {
           if (ResourceParameter.KIND.HTTP_BODY_OBJECT.equals(resourceParameter.getParamType()))
           {
               assertFalse(resourceParameter.isAllowMultiple());  //set to not allow multiple
           }
        }
        assertTrue("SheepBlackSheepResource supports DELETE", metaData.supports(HttpMethod.DELETE));
        params = metaData.getParameters(HttpMethod.DELETE);
        assertTrue("DELETE method on a relations should have 2 url params.", params.size() == 2);
        
        metainfo = ResourceInspector.inspect(MultiPartTestRelationshipResource.class);
        assertTrue("Must be one ResourceMetadata",metainfo.size()==1);
        metaData = metainfo.get(0);
        assertNotNull(metaData);
        assertTrue("MultiPartTestRelationshipResource support POST", metaData.supports(HttpMethod.POST));
        assertFalse("MultiPartTestRelationshipResource does not supports GET", metaData.supports(HttpMethod.GET));
        assertFalse("MultiPartTestRelationshipResource does not supports PUT", metaData.supports(HttpMethod.PUT));
        assertFalse("MultiPartTestRelationshipResource does not supports DELETE", metaData.supports(HttpMethod.DELETE));
        assertTrue("MultiPartTestRelationshipResource must support MultiPartTestResponse", MultiPartTestResponse.class.equals(metaData.getObjectType(HttpMethod.POST)));
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
        assertTrue("NodeCommentsRelation must support Comment", Comment.class.equals(metaData.getObjectType(HttpMethod.POST)));
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
        op = ResourceInspector.inspectOperation(FlockEntityResource.class, aMethod, HttpMethod.GET);
        assertNotNull(op);
        assertTrue(op.getTitle().startsWith("Deletes a photo"));

        aMethod = ResourceInspector.findMethod(BinaryResourceAction.Update.class, FlockEntityResource.class);
        op = ResourceInspector.inspectOperation(FlockEntityResource.class, aMethod, HttpMethod.GET);
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
    public void testInspectActions() throws IllegalAccessException, InstantiationException
    {
        Api api = Api.valueOf("alfrescomock", "private", "1");
        List<ResourceMetadata> metainfo = new ArrayList<ResourceMetadata>();

        GrassEntityResource grassEntityResource = new GrassEntityResource();
        ResourceInspector.inspectActions(api, GrassEntityResource.class,"-root-", metainfo);
        assertTrue(metainfo.size()==2);

        for (ResourceMetadata resourceMetadata : metainfo)
        {
            assertEquals(ResourceMetadata.RESOURCE_TYPE.ACTION, resourceMetadata.getType());
            ActionResourceMetaData actionResourceMetaData = (ActionResourceMetaData) resourceMetadata;
            Method actionMethod = actionResourceMetaData.getActionMethod();
            String result = null;
            switch (resourceMetadata.getUniqueId())
            {
                case "/-root-/{entityId}/grow":
                    assertTrue("GrassEntityResource supports POST", resourceMetadata.supports(HttpMethod.POST));
                    assertFalse("GrassEntityResource does not support DELETE", resourceMetadata.supports(HttpMethod.DELETE));
                    Class paramType = resourceMetadata.getObjectType(HttpMethod.POST);
                    Object paramObj = paramType.newInstance();
                    result = (String) ResourceInspectorUtil.invokeMethod(actionMethod,grassEntityResource, "xyz", paramObj, Params.valueOf("notUsed", null));
                    assertEquals("Growing well",result);
                    break;
                case "/-root-/{entityId}/cut":
                    assertTrue("GrassEntityResource supports POST", resourceMetadata.supports(HttpMethod.POST));
                    assertFalse("GrassEntityResource does not support GET", resourceMetadata.supports(HttpMethod.GET));
                    assertNull(resourceMetadata.getObjectType(HttpMethod.POST));
                    result = (String) ResourceInspectorUtil.invokeMethod(actionMethod,grassEntityResource, "xyz", Params.valueOf("notUsed", null));
                    assertEquals("All done",result);
                    break;
                default:
                    fail("Invalid action information.");
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
        assertEquals("/myroot/photo",metaData.getUniqueId()); 
        assertTrue(metaData.getOperations().size()==3);
        assertTrue("FlockEntityResource supports GET", metaData.supports(HttpMethod.GET));
        assertTrue("FlockEntityResource supports PUT", metaData.supports(HttpMethod.PUT));
        assertTrue("FlockEntityResource supports DELETE", metaData.supports(HttpMethod.DELETE));
        
        metainfo.clear();
        ResourceInspector.inspectAddressedProperties(api, FlocketEntityResource.class, "myroot", metainfo);
        assertTrue(metainfo.size()==3);
        for (ResourceMetadata resourceMetadata : metainfo)
        {
            //If this code is running on a Java 7 or above then please use the switch statement instead of an if.
//            switch (resourceMetadata.getUniqueId())
//            {
//                case "/myroot/photo":
//                    assertTrue("FlocketEntityResource supports GET", resourceMetadata.supports(HttpMethod.GET));
//                    assertTrue("FlocketEntityResource supports PUT", resourceMetadata.supports(HttpMethod.PUT));
//                    assertTrue("FlocketEntityResource supports DELETE", resourceMetadata.supports(HttpMethod.DELETE));
//                    break;
//                case "/myroot/album":
//                    assertTrue("FlocketEntityResource supports GET", resourceMetadata.supports(HttpMethod.GET));
//                    assertTrue("FlocketEntityResource supports PUT", resourceMetadata.supports(HttpMethod.PUT));
//                    assertTrue("FlocketEntityResource does not support DELETE", !resourceMetadata.supports(HttpMethod.DELETE));                   
//                    break;
//                case "/myroot/madeUpProp":
//                    assertTrue("FlocketEntityResource supports GET", resourceMetadata.supports(HttpMethod.GET));
//                    assertTrue("FlocketEntityResource does not supports PUT", !resourceMetadata.supports(HttpMethod.PUT));
//                    assertTrue("FlocketEntityResource does not support DELETE", !resourceMetadata.supports(HttpMethod.DELETE));
//                    break;
//                default:
//                    fail("Invalid address property information.");
//            }
          
            if ("/myroot/photo".equals(resourceMetadata.getUniqueId()))
            {
                assertTrue("FlocketEntityResource supports GET", resourceMetadata.supports(HttpMethod.GET));
                assertTrue("FlocketEntityResource supports PUT", resourceMetadata.supports(HttpMethod.PUT));
                assertTrue("FlocketEntityResource supports DELETE", resourceMetadata.supports(HttpMethod.DELETE));
            }
            else
            {
                if ("/myroot/album".equals(resourceMetadata.getUniqueId()))
                {
                    assertTrue("FlocketEntityResource supports GET", resourceMetadata.supports(HttpMethod.GET));
                    assertTrue("FlocketEntityResource supports PUT", resourceMetadata.supports(HttpMethod.PUT));
                    assertTrue("FlocketEntityResource does not support DELETE", !resourceMetadata.supports(HttpMethod.DELETE));
                }
                else
                {
                    if ("/myroot/madeUpProp".equals(resourceMetadata.getUniqueId()))
                    {
                        assertTrue("FlocketEntityResource supports GET", resourceMetadata.supports(HttpMethod.GET));
                        assertTrue("FlocketEntityResource does not supports PUT", !resourceMetadata.supports(HttpMethod.PUT));
                        assertTrue("FlocketEntityResource does not support DELETE", !resourceMetadata.supports(HttpMethod.DELETE));
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
        assertTrue("GrassEntityResourceNowDeleted all methods deleted", !metaData.supports(HttpMethod.GET));
        assertTrue("GrassEntityResourceNowDeleted all methods deleted", !metaData.supports(HttpMethod.PUT));
        assertTrue("GrassEntityResourceNowDeleted all methods deleted", !metaData.supports(HttpMethod.DELETE));
        assertTrue("GrassEntityResourceNowDeleted all methods deleted", !metaData.supports(HttpMethod.POST));
        assertNull("GrassEntityResourceNowDeleted all methods deleted", metaData.getObjectType(HttpMethod.POST));
        
        metainfo = ResourceInspector.inspect(SheepBlackSheepResourceIsNoMore.class);
        assertTrue("Must be at least one ResourceMetadata",metainfo.size()>0);
        metaData = metainfo.get(0);
        assertNotNull(metaData);
        assertTrue("SheepBlackSheepResourceIsNoMore all methods deleted", !metaData.supports(HttpMethod.GET));
        assertTrue("SheepBlackSheepResourceIsNoMore all methods deleted", !metaData.supports(HttpMethod.PUT));
        assertTrue("SheepBlackSheepResourceIsNoMore all methods deleted", !metaData.supports(HttpMethod.DELETE));
        assertTrue("SheepBlackSheepResourceIsNoMore all methods deleted", !metaData.supports(HttpMethod.POST));
        assertNull("SheepBlackSheepResourceIsNoMore all methods deleted", metaData.getObjectType(HttpMethod.POST));
    }
}
