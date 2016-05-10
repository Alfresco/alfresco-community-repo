/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.rest.framework.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.WebApi;
import org.alfresco.rest.framework.WebApiDeleted;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiNoAuth;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.WebApiParameters;
import org.alfresco.rest.framework.core.ResourceMetadata.RESOURCE_TYPE;
import org.alfresco.rest.framework.core.ResourceParameter.KIND;
import org.alfresco.rest.framework.resource.EmbeddedEntityResource;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.MultiPartResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.MultiPartRelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.actions.interfaces.ResourceAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.extensions.webscripts.Status;
import org.springframework.http.HttpMethod;
import org.springframework.util.ReflectionUtils;

/**
 * Looks at resources to see what they can do
 * 
 * @author Gethin James
 * @author janv
 */
public class ResourceInspector
{
    private static Log logger = LogFactory.getLog(ResourceInspector.class);
    private static final Set<Class<? extends ResourceAction>> ALL_ENTITY_RESOURCE_INTERFACES = new HashSet<Class<? extends ResourceAction>>();
    private static final Set<Class<? extends ResourceAction>> ALL_RELATIONSHIP_RESOURCE_INTERFACES = new HashSet<Class<? extends ResourceAction>>();
    private static final Set<Class<? extends ResourceAction>> ALL_PROPERTY_RESOURCE_INTERFACES = new HashSet<Class<? extends ResourceAction>>();
    
    private static String READ_BY_ID_METHODNAME = "readById";

    static {
        
        ALL_ENTITY_RESOURCE_INTERFACES.add(EntityResourceAction.Create.class);
        ALL_ENTITY_RESOURCE_INTERFACES.add(EntityResourceAction.Read.class);
        ALL_ENTITY_RESOURCE_INTERFACES.add(EntityResourceAction.ReadById.class);
        ALL_ENTITY_RESOURCE_INTERFACES.add(EntityResourceAction.Update.class);
        ALL_ENTITY_RESOURCE_INTERFACES.add(EntityResourceAction.Delete.class);
        ALL_ENTITY_RESOURCE_INTERFACES.add(BinaryResourceAction.Read.class);

        ALL_ENTITY_RESOURCE_INTERFACES.add(EntityResourceAction.CreateWithResponse.class);
        ALL_ENTITY_RESOURCE_INTERFACES.add(EntityResourceAction.ReadWithResponse.class);
        ALL_ENTITY_RESOURCE_INTERFACES.add(EntityResourceAction.ReadByIdWithResponse.class);
        ALL_ENTITY_RESOURCE_INTERFACES.add(EntityResourceAction.UpdateWithResponse.class);
        ALL_ENTITY_RESOURCE_INTERFACES.add(EntityResourceAction.DeleteWithResponse.class);
        ALL_ENTITY_RESOURCE_INTERFACES.add(BinaryResourceAction.ReadWithResponse.class);

        ALL_ENTITY_RESOURCE_INTERFACES.add(MultiPartResourceAction.Create.class);

        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.Create.class);
        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.Read.class);
        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.ReadById.class);
        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.Update.class);
        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.Delete.class);

        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.CreateWithResponse.class);
        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.ReadWithResponse.class);
        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.ReadByIdWithResponse.class);
        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.UpdateWithResponse.class);
        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.DeleteWithResponse.class);

        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(MultiPartRelationshipResourceAction.Create.class);

        ALL_PROPERTY_RESOURCE_INTERFACES.add(BinaryResourceAction.Read.class);
        ALL_PROPERTY_RESOURCE_INTERFACES.add(BinaryResourceAction.Delete.class);
        ALL_PROPERTY_RESOURCE_INTERFACES.add(BinaryResourceAction.Update.class);

        ALL_PROPERTY_RESOURCE_INTERFACES.add(BinaryResourceAction.ReadWithResponse.class);
        ALL_PROPERTY_RESOURCE_INTERFACES.add(BinaryResourceAction.DeleteWithResponse.class);
        ALL_PROPERTY_RESOURCE_INTERFACES.add(BinaryResourceAction.UpdateWithResponse.class);
    }
    
    /**
     * Inspects the entity resource and returns meta data about it
     * 
     * @param annot EntityResource
     * @param resource Class<?>
     */
    private static List<ResourceMetadata> inspectEntity(EntityResource annot, Class<?> resource)
    {
        
        String urlPath = findEntityName(annot);
        logger.debug("Found EntityResource: "+urlPath);
        List<ResourceMetadata> metainfo = new ArrayList<ResourceMetadata>();
        Api api = inspectApi(resource);
        
        MetaHelper helper = new MetaHelper(resource);
        findOperation(EntityResourceAction.Create.class,   HttpMethod.POST, helper);
        findOperation(EntityResourceAction.Read.class,     HttpMethod.GET, helper);
        findOperation(EntityResourceAction.ReadById.class, HttpMethod.GET, helper);
        findOperation(EntityResourceAction.Update.class,   HttpMethod.PUT, helper);  
        findOperation(EntityResourceAction.Delete.class,   HttpMethod.DELETE, helper);

        findOperation(EntityResourceAction.CreateWithResponse.class,   HttpMethod.POST, helper);
        findOperation(EntityResourceAction.ReadWithResponse.class,     HttpMethod.GET, helper);
        findOperation(EntityResourceAction.ReadByIdWithResponse.class, HttpMethod.GET, helper);
        findOperation(EntityResourceAction.UpdateWithResponse.class,   HttpMethod.PUT, helper);
        findOperation(EntityResourceAction.DeleteWithResponse.class,   HttpMethod.DELETE, helper);

        findOperation(MultiPartResourceAction.Create.class,   HttpMethod.POST, helper);

        boolean noAuth = resource.isAnnotationPresent(WebApiNoAuth.class);
        if (noAuth)
        {
            throw new IllegalArgumentException("@WebApiNoAuth should not be on all (entity resource class) - only on individual methods: "+urlPath);
        }

        Set<Class<? extends ResourceAction>> apiNoAuth = helper.apiNoAuth;

        if (resource.isAnnotationPresent(WebApiDeleted.class))
        {
            metainfo.add(new ResourceMetadata(ResourceDictionary.resourceKey(urlPath,null), RESOURCE_TYPE.ENTITY,
                                              null, api, ALL_ENTITY_RESOURCE_INTERFACES, apiNoAuth, null));
        }
        else 
        {
            if (!helper.apiDeleted.isEmpty() || !helper.operations.isEmpty())
            {
                metainfo.add(new ResourceMetadata(ResourceDictionary.resourceKey(urlPath,null), RESOURCE_TYPE.ENTITY,
                                                  helper.operations, api, helper.apiDeleted, apiNoAuth, null));
            }
        }

        inspectAddressedProperties(api, resource, urlPath, metainfo);
        inspectOperations(api, resource, urlPath, metainfo);
        return metainfo;
    }

    /**
     * Inspects the entity resource and returns meta data about any addresssed/binary properties
     * @param api Api
     * @param resource Class<?>
     * @param entityPath String
     * @param metainfo List<ResourceMetadata>
     */
    public static void inspectAddressedProperties(Api api, Class<?> resource, final String entityPath, List<ResourceMetadata> metainfo)
    {
        final Map<String,List<ResourceOperation>> operationGroupedByProperty = new HashMap<String,List<ResourceOperation>>();

        MetaHelperAddressable helperForAddressProps = new MetaHelperAddressable(resource, entityPath, operationGroupedByProperty);

        findOperation(BinaryResourceAction.Read.class,   HttpMethod.GET, helperForAddressProps);
        findOperation(BinaryResourceAction.Delete.class, HttpMethod.DELETE, helperForAddressProps);
        findOperation(BinaryResourceAction.Update.class, HttpMethod.PUT, helperForAddressProps);

        findOperation(BinaryResourceAction.ReadWithResponse.class,   HttpMethod.GET, helperForAddressProps);
        findOperation(BinaryResourceAction.DeleteWithResponse.class, HttpMethod.DELETE, helperForAddressProps);
        findOperation(BinaryResourceAction.UpdateWithResponse.class, HttpMethod.PUT, helperForAddressProps);

        findOperation(RelationshipResourceBinaryAction.Read.class,   HttpMethod.GET, helperForAddressProps);
        findOperation(RelationshipResourceBinaryAction.Delete.class, HttpMethod.DELETE, helperForAddressProps);
        findOperation(RelationshipResourceBinaryAction.Update.class, HttpMethod.PUT, helperForAddressProps);

        findOperation(RelationshipResourceBinaryAction.ReadWithResponse.class,   HttpMethod.GET, helperForAddressProps);
        findOperation(RelationshipResourceBinaryAction.DeleteWithResponse.class, HttpMethod.DELETE, helperForAddressProps);
        findOperation(RelationshipResourceBinaryAction.UpdateWithResponse.class, HttpMethod.PUT, helperForAddressProps);

        boolean noAuth = resource.isAnnotationPresent(WebApiNoAuth.class);
        if (noAuth)
        {
            throw new IllegalArgumentException("@WebApiNoAuth should not be on all (address properties) - only on individual methods: "+entityPath);
        }

        Set<Class<? extends ResourceAction>> apiNoAuth = helperForAddressProps.apiNoAuth;

        if (resource.isAnnotationPresent(WebApiDeleted.class))
        {
            metainfo.add(new ResourceMetadata(ResourceDictionary.propertyResourceKey(entityPath,"FIX_ME"), RESOURCE_TYPE.PROPERTY,
                                              null, inspectApi(resource), ALL_PROPERTY_RESOURCE_INTERFACES, apiNoAuth, null));
        }
        else 
        {
            for (Entry<String, List<ResourceOperation>> groupedOps : operationGroupedByProperty.entrySet())
            {           
                metainfo.add(new ResourceMetadata(groupedOps.getKey(), RESOURCE_TYPE.PROPERTY, groupedOps.getValue(), api, null, apiNoAuth, null));
            }
        }

    }

    /**
     * Inspects the relationship resource and returns meta data about it
     * 
     * @param annot RelationshipResource
     * @param resource Class<?>
     */
    private static List<ResourceMetadata> inspectRelationship(RelationshipResource annot, Class<?> resource)
    {
        Map<String, Object> annotAttribs = AnnotationUtils.getAnnotationAttributes(annot);
        String urlPath = String.valueOf(annotAttribs.get("name"));
        String entityPath = findEntityNameByAnnotationAttributes(annotAttribs);
        String relationshipKey = ResourceDictionary.resourceKey(entityPath,urlPath);
        Api api = inspectApi(resource);
        List<ResourceMetadata> metainfo = new ArrayList<ResourceMetadata>();

        MetaHelper helper = new MetaHelper(resource);
        findOperation(RelationshipResourceAction.Create.class,   HttpMethod.POST, helper);
        findOperation(RelationshipResourceAction.Read.class,     HttpMethod.GET, helper);
        findOperation(RelationshipResourceAction.ReadById.class, HttpMethod.GET, helper);
        findOperation(RelationshipResourceAction.Update.class,   HttpMethod.PUT, helper);  
        findOperation(RelationshipResourceAction.Delete.class,   HttpMethod.DELETE, helper);

        findOperation(RelationshipResourceAction.CreateWithResponse.class,   HttpMethod.POST, helper);
        findOperation(RelationshipResourceAction.ReadWithResponse.class,     HttpMethod.GET, helper);
        findOperation(RelationshipResourceAction.ReadByIdWithResponse.class, HttpMethod.GET, helper);
        findOperation(RelationshipResourceAction.UpdateWithResponse.class,   HttpMethod.PUT, helper);
        findOperation(RelationshipResourceAction.DeleteWithResponse.class,   HttpMethod.DELETE, helper);

        findOperation(MultiPartRelationshipResourceAction.Create.class, HttpMethod.POST, helper);

        boolean noAuth = resource.isAnnotationPresent(WebApiNoAuth.class);
        if (noAuth)
        {
            throw new IllegalArgumentException("@WebApiNoAuth should not be on all (relationship resource class) - only on methods: "+urlPath);
        }

        Set<Class<? extends ResourceAction>> apiNoAuth = helper.apiNoAuth;

        if (resource.isAnnotationPresent(WebApiDeleted.class))
        {
            metainfo.add(new ResourceMetadata(relationshipKey, RESOURCE_TYPE.RELATIONSHIP, null, inspectApi(resource), ALL_RELATIONSHIP_RESOURCE_INTERFACES, apiNoAuth, entityPath));
        }
        else 
        {
            metainfo.add(new ResourceMetadata(relationshipKey, RESOURCE_TYPE.RELATIONSHIP, helper.operations, inspectApi(resource), helper.apiDeleted, apiNoAuth, entityPath));
        }

        inspectAddressedProperties(api, resource, relationshipKey, metainfo);
        inspectOperations(api, resource, relationshipKey, metainfo);
        return metainfo;
   }

    /**
     * Determines if the resources supports the resource action specified by resourceInterfaceWithOneMethod 
     * @param resourceInterfaceWithOneMethod The resource action
     * @param httpMethod http method the action supports.
     * @param helper Holder of simple meta data
     */
    private static void findOperation(Class<? extends ResourceAction> resourceInterfaceWithOneMethod, HttpMethod httpMethod, MetaHelperCallback helper)
    {
        if (resourceInterfaceWithOneMethod.isAssignableFrom(helper.resource))
        {
            Method aMethod = findMethod(resourceInterfaceWithOneMethod, helper.resource);
            ResourceOperation operation = inspectOperation(helper.resource, aMethod, httpMethod);

            if (isDeleted(aMethod))
            {
                helper.whenOperationDeleted(resourceInterfaceWithOneMethod, aMethod);    
            } 
            else 
            {
                helper.whenNewOperation(operation, aMethod);
            }

            if (isNoAuth(aMethod))
            {
                if (! httpMethod.equals(HttpMethod.GET))
                {
                    throw new IllegalArgumentException("@WebApiNoAuth should only be on GET methods: "+operation.getTitle());
                }
                helper.whenOperationNoAuth(resourceInterfaceWithOneMethod, aMethod);
            }
        }
    }


    /**
     * Inspects the method and returns meta data about its operations
     * @param resource Class<?>
     * @param aMethod Method
     * @param httpMethod HttpMethod
     * @return ResourceOperation
     */
    public static ResourceOperation inspectOperation(Class<?> resource, Method aMethod, HttpMethod httpMethod)
    {
        Annotation annot = AnnotationUtils.findAnnotation(aMethod, WebApiDescription.class);
        List<ResourceParameter> parameters = new ArrayList<ResourceParameter>();
        parameters.addAll(inspectParameters(resource, aMethod, httpMethod));
 
        if (annot != null)
        {
            Map<String, Object> annotAttribs = AnnotationUtils.getAnnotationAttributes(annot);
            String title = String.valueOf(annotAttribs.get("title"));
            String desc = String.valueOf(annotAttribs.get("description"));
            Integer success = (Integer) annotAttribs.get("successStatus");
            return new ResourceOperation(httpMethod, title, desc, parameters, validSuccessCode(httpMethod,success));
        }
        else {
            return new ResourceOperation(httpMethod, 
                        "Missing @WebApiDescription annotation",
                        "This method should be annotated with @WebApiDescription", parameters, validSuccessCode(httpMethod, ResourceOperation.UNSET_STATUS));
        }
    }

    public static int validSuccessCode(HttpMethod httpMethod, int success)
    {
        if (!(ResourceOperation.UNSET_STATUS == success))
        {
            //The status has been set by the api implementor so use it.
            return success;
        }

        switch (httpMethod)
        {
            case GET:
                return Status.STATUS_OK;
            case POST:
                return Status.STATUS_CREATED;
            case PUT:
                return Status.STATUS_OK;
            case DELETE:
                return Status.STATUS_NO_CONTENT;
            default:
                return Status.STATUS_OK;
        }

    }

    /**
     * Inspects the Method to find any @WebApiParameters and @WebApiParam
     * @param resource the class
     * @param aMethod the method
     * @param httpMethod HttpMethod
     * @return a List of parameters
     */
    private static List<ResourceParameter> inspectParameters(Class<?> resource, Method aMethod, HttpMethod httpMethod)
    {
        List<ResourceParameter> params = new ArrayList<ResourceParameter>();
        Annotation annot = AnnotationUtils.findAnnotation(aMethod, WebApiParameters.class);
        if (annot != null)         
        {
            Map<String, Object> annotAttribs = AnnotationUtils.getAnnotationAttributes(annot);
            WebApiParam[] apiParams = (WebApiParam[]) annotAttribs.get("value");
            for (int i = 0; i < apiParams.length; i++)
            {
                params.add(findResourceParameter(apiParams[i], resource, aMethod));
            }
        }
        else
        {
            Annotation paramAnot = AnnotationUtils.findAnnotation(aMethod, WebApiParam.class);
            if (paramAnot!=null)
            {
                params.add(findResourceParameter(paramAnot, resource, aMethod));
            }
        }
        
        
        //Setup default parameters
        switch(httpMethod)
        {
            case POST:
                if (paramsCount(params,ResourceParameter.KIND.URL_PATH) == 0)
                {
                    params.add(ResourceParameter.ENTITY_PARAM);  
                }
                if (paramsCount(params,ResourceParameter.KIND.HTTP_BODY_OBJECT) == 0)
                {
                    inspectBodyParamAndReturnType(resource, aMethod, params);
                }
                break;
            case PUT:
                int urlPathForPut = paramsCount(params,ResourceParameter.KIND.URL_PATH);
                if (urlPathForPut == 0)
                {
                    params.add(ResourceParameter.ENTITY_PARAM);  
                }
                if (RelationshipResourceAction.Update.class.isAssignableFrom(resource) && urlPathForPut <2)
                {
                    params.add(ResourceParameter.RELATIONSHIP_PARAM);
                }
                if (paramsCount(params,ResourceParameter.KIND.HTTP_BODY_OBJECT)== 0)
                {
                    inspectBodyParamAndReturnType(resource, aMethod, params);
                }
                break;
            case GET:
                int urlPathForGet = paramsCount(params,ResourceParameter.KIND.URL_PATH);
                if (urlPathForGet == 0 && (EntityResourceAction.ReadById.class.isAssignableFrom(resource) && READ_BY_ID_METHODNAME.equals(aMethod.getName())))
                {
                    params.add(ResourceParameter.ENTITY_PARAM);
                }
                else if (RelationshipResourceAction.ReadById.class.isAssignableFrom(resource)||RelationshipResourceAction.Read.class.isAssignableFrom(resource))
                {
                    //Its a RelationshipResourceAction
                    if (urlPathForGet == 0)
                    {
                        params.add(ResourceParameter.ENTITY_PARAM);
                    }
                    //This method is what we are inspecting not what the class implements.
                    if (READ_BY_ID_METHODNAME.equals(aMethod.getName()) && urlPathForGet< 2)
                    {
                        params.add(ResourceParameter.RELATIONSHIP_PARAM);
                    }
                }
                if (!READ_BY_ID_METHODNAME.equals(aMethod.getName()))
                {
                    params.add(ResourceParameter.SKIP_PARAM);
                    params.add(ResourceParameter.MAX_ITEMS_PARAM);
                    params.add(ResourceParameter.PROPS_PARAM);
                }
                break;
            case DELETE:
                int urlPathForDelete = paramsCount(params,ResourceParameter.KIND.URL_PATH);
                if (urlPathForDelete == 0)
                {
                    params.add(ResourceParameter.ENTITY_PARAM);  
                }
                //Add relationship param ?
                if (RelationshipResourceAction.Delete.class.isAssignableFrom(resource) && urlPathForDelete <2)
                {
                    params.add(ResourceParameter.RELATIONSHIP_PARAM);
                }
        }
        
        return params;
    }

    private static void inspectBodyParamAndReturnType(Class<?> resource, Method aMethod, List<ResourceParameter> params)
    {
        Class<?> dType = ResourceInspectorUtil.determineType(resource,aMethod);
        if (dType!= null)
        {
            params.add(ResourceParameter.valueOf(dType.getSimpleName().toUpperCase(), "The entity", "Unique entity properties", true, KIND.HTTP_BODY_OBJECT, true, dType));
        }
    }


    /**
     * Indicates the number of params of the Kind specified
     * @param params List<ResourceParameter>
     * @param kind kind of parameter eg. URL_PATH
     * @return int count
     */
    private static int paramsCount(List<ResourceParameter> params, KIND kind)
    {
        int numParams = 0;
    
        for (ResourceParameter resourceParameter : params)
        {
           if (kind.equals(resourceParameter.getParamType())) numParams++;
        }
        return numParams; //the default
    }


    /**
     * @param paramAnot Annotation
     * @param resource Class<?>
     * @param aMethod Method
     * @return ResourceParameter
     */
    private static ResourceParameter findResourceParameter(Annotation paramAnot, Class<?> resource, Method aMethod)
    {
        Map<String, Object> annotAttribs = AnnotationUtils.getAnnotationAttributes(paramAnot);
        ResourceParameter.KIND paramKind = (ResourceParameter.KIND) annotAttribs.get("kind");
        Class<?> dType = String.class;
        if (ResourceParameter.KIND.HTTP_BODY_OBJECT.equals(paramKind))
        {
            dType = ResourceInspectorUtil.determineType(resource, aMethod);
        }
        return ResourceParameter.valueOf(
                    String.valueOf(annotAttribs.get("name")), 
                    String.valueOf(annotAttribs.get("title")), 
                    String.valueOf(annotAttribs.get("description")), 
                    (Boolean)annotAttribs.get("required"),
                    paramKind,
                    (Boolean)annotAttribs.get("allowMultiple"),               
                    dType);
    }


    /**
     * Returns true if the method has been marked as deleted.
     * @param method the method
     * @return true - if is is marked as deleted.
     */
    public static boolean isDeleted(Method method)
    {
        WebApiDeleted deleted = AnnotationUtils.getAnnotation(method, WebApiDeleted.class);
        return (deleted!=null);
    }

    /**
     * Returns true if the method has been marked as no auth required.
     * @param method the method
     * @return true - if is is marked as no auth required.
     */
    public static boolean isNoAuth(Method method)
    {
        WebApiNoAuth noAuth = AnnotationUtils.getAnnotation(method, WebApiNoAuth.class);
        return (noAuth!=null);
    }
    
    /**
     * Returns the method for the interface
     * @param resourceInterfaceWithOneMethod Class<? extends ResourceAction>
     * @param resource Class<?>
     * @return null or a Method
     */
    public static Method findMethod(Class<? extends ResourceAction> resourceInterfaceWithOneMethod, Class<?> resource)
    {
        Method[] resourceMethods = resourceInterfaceWithOneMethod.getMethods();
        if (resourceMethods == null || resourceMethods.length != 1)
        {
            //All the interfaces should have just one method.
            throw new IllegalArgumentException(resourceInterfaceWithOneMethod + " should be an interface with just one method.");
        }
        Method method = ReflectionUtils.findMethod(resource, resourceMethods[0].getName(), null);
        return method;
    }
    
    /**
     * Finds the name of the entity using its annotation.
     * @param annotAttribs Map<String, Object>
     * @return the entity name/path
     */
    protected static String findEntityNameByAnnotationAttributes(Map<String, Object> annotAttribs)
    {
        Class<?> entityResourceRef = (Class<?>) annotAttribs.get("entityResource");
        EntityResource entityAnnot = AnnotationUtils.findAnnotation(entityResourceRef, EntityResource.class);
        return findEntityName(entityAnnot);
    }
    
    /**
     * Finds the name of the entity using its annotation.
     * @param entityAnnot EntityResource
     * @return the entity name/path
     */
    protected static String findEntityName(EntityResource entityAnnot)
    {
        Map<String, Object> annotAttribs =  AnnotationUtils.getAnnotationAttributes(entityAnnot);
        String urlPath = String.valueOf(annotAttribs.get("name"));
        return urlPath;
    }

    /**
     * Finds the name of the entity collection using the meta information.
     * @param meta ResourceMetadata
     * @return the entity name/path
     */
    public static String findEntityCollectionNameName(ResourceMetadata meta)
    {   
        String name;
        switch (meta.getType())
        {
            case RELATIONSHIP:
                name = meta.getParentResource();
                break;
            default:
                //an entity so just get its id.
                name = meta.getUniqueId();
        }
        
        if (name.startsWith("/"))
        {
            name = name.substring(1);
        }
        return name;
    }
   
    /**
     * For a given class, looks for @EmbeddedEntityResource annotations, using the annotation produce
     * a Map of the property name key and the entity key
     * @param anyClass Class<?>
     * @return A map of property key name and a value of the entity path name
     */
    public static Map<String,Pair<String,Method>> findEmbeddedResources(Class<?> anyClass)
    {
        Map<String, Pair<String,Method>> embeds = new HashMap<String, Pair<String,Method>>();
        List<Method> annotatedMethods = ResourceInspectorUtil.findMethodsByAnnotation(anyClass, EmbeddedEntityResource.class);
        if (annotatedMethods != null && !annotatedMethods.isEmpty())
        {
            for (Method annotatedMethod : annotatedMethods)
            {
                Annotation annot = AnnotationUtils.findAnnotation(annotatedMethod, EmbeddedEntityResource.class);
                if (annot != null)
                {
                    Map<String, Object> annotAttribs = AnnotationUtils.getAnnotationAttributes(annot);
                    String entityPath = findEntityNameByAnnotationAttributes(annotAttribs);
                    String key = String.valueOf(annotAttribs.get("propertyName"));
                    embeds.put(key, new Pair<String,Method>(entityPath,annotatedMethod));
                }                
            }

        }
        return embeds;
    }
    
    /**
     * Inspect a resource to find operations on it.
     * @param api Api
     * @param resource Class<?>
     * @param entityPath String
     * @param metainfo List<ResourceMetadata>
     */
    public static void inspectOperations(Api api, Class<?> resource, final String entityPath, List<ResourceMetadata> metainfo)
    {
        Map<String,Pair<ResourceOperation,Method>> operations = findOperations(entityPath, resource);
        if (operations != null && !operations.isEmpty())
        {
            for (Entry<String, Pair<ResourceOperation, Method>> opera : operations.entrySet())
            {
                if (isDeleted(opera.getValue().getSecond()))
                {
                    metainfo.add(new OperationResourceMetaData(opera.getKey(), api, new HashSet(Arrays.asList(opera.getValue().getFirst()))));
                }
                else
                {
                    metainfo.add(new OperationResourceMetaData(opera.getKey(), Arrays.asList(opera.getValue().getFirst()), api, opera.getValue().getSecond()));
                }
            }
        }
    }

    /**
     * Finds operations on an entity
     * @param entityPath path to the entity
     * @param anyClass resource clause
     * @return The operations
     */
    private static Map<String,Pair<ResourceOperation,Method>> findOperations(String entityPath, Class<?> anyClass)
    {
        Map<String, Pair<ResourceOperation,Method>> embeds = new HashMap<String, Pair<ResourceOperation,Method>>();
        List<Method> annotatedMethods = ResourceInspectorUtil.findMethodsByAnnotation(anyClass, Operation.class);
        if (annotatedMethods != null && !annotatedMethods.isEmpty())
            for (Method annotatedMethod : annotatedMethods)
            {
                //validateOperationMethod(annotatedMethod, anyClass);
                Annotation annot = AnnotationUtils.findAnnotation(annotatedMethod, Operation.class);
                if (annot != null)
                {
                    Map<String, Object> annotAttribs = AnnotationUtils.getAnnotationAttributes(annot);
                    String actionName = String.valueOf(annotAttribs.get("value"));
                    String actionPath = ResourceDictionary.propertyResourceKey(entityPath, actionName);
                    ResourceOperation ro = inspectOperation(anyClass, annotatedMethod, HttpMethod.POST);
                    embeds.put(actionPath, new Pair<ResourceOperation, Method>(ro, annotatedMethod));
                }
            }
        return embeds;
    }
    
    /**
     * Inspects the resource to determine what api it belongs to.
     * It does this by looking for the WebApi package annotation.
     * 
     * @param resource Class<?>
     * @return Api
     */
    public static Api inspectApi(Class<?> resource)
    {
        Package myPackage = resource.getPackage();
        Annotation annot = myPackage.getAnnotation(WebApi.class);
  
        if (annot != null)
        {
            Map<String, Object> annotAttribs =  AnnotationUtils.getAnnotationAttributes(annot);
            String apiName = String.valueOf(annotAttribs.get("name"));
            String apiScope = String.valueOf(annotAttribs.get("scope"));
            String apiVersion = String.valueOf(annotAttribs.get("version"));
            return Api.valueOf(apiName, apiScope, apiVersion);
        }
        return null;
    }

    /**
     * Inspects the annotated resource to understand its capabilities
     * 
     * @param resource Class
     */
    @SuppressWarnings("rawtypes")
    public static List<ResourceMetadata> inspect(Class resource)
    {
        EntityResource annot = AnnotationUtils.findAnnotation(resource, EntityResource.class);
        if (annot != null) { return inspectEntity(annot, resource); }

        RelationshipResource relAnnot = AnnotationUtils.findAnnotation(resource, RelationshipResource.class);
        if (relAnnot != null) { return inspectRelationship(relAnnot, resource); }

        throw new UnsupportedOperationException("Unable to inspect " + resource.getName());

    }

    /**
     * Finds the unique id of an object using the @UniqueId annotation.
     * 
     * @param obj any object
     * @return a String object with the entity id set
     */
    public static String findUniqueId(Object obj)
    {
        @SuppressWarnings("rawtypes")
        Class objClass = obj.getClass();

        Method annotatedMethod = findUniqueIdMethod(objClass);
        Object id = ResourceInspectorUtil.invokeMethod(annotatedMethod, obj);
        if (id != null)
        {
            if (id instanceof NodeRef)
            {
                return ((NodeRef)id).getId();
            }
            return String.valueOf(id);
        }
        else
        {
            return null;
        }

    }


    /**
     * Finds a single method with the @UniqueId annotation.
     * 
     * @param objClass any object class
     * @return the Method
     * @throws IllegalArgumentException if there is is more than 1 method annotated with @UniqueId
     */
    public static Method findUniqueIdMethod(Class<?> objClass) throws IllegalArgumentException
    {
        List<Method> annotatedMethods = ResourceInspectorUtil.findMethodsByAnnotation(objClass, UniqueId.class);
        if (annotatedMethods != null && !annotatedMethods.isEmpty())
        {
            if (annotatedMethods.size() != 1)
            {
                //There should only ever be 1 annotated method for unique id
                throw new IllegalArgumentException("There should only ever be one UniqueId annotation on a class but "+objClass+" has "+annotatedMethods.size());
            }
            return annotatedMethods.get(0);
        }

        return null;
    }

    /**
     * Finds the property name that is used as the unique id.
     * @param uniqueIdMethod Method
     * @return String the property name that is used as the unique id.
     */
    public static String findUniqueIdName(Method uniqueIdMethod)
    {
        Annotation annot = AnnotationUtils.findAnnotation(uniqueIdMethod, UniqueId.class);
        if (annot != null)
        {
            Map<String, Object> annotAttribs =  AnnotationUtils.getAnnotationAttributes(annot);
            String uniqueIdName = String.valueOf(annotAttribs.get("name"));
            return uniqueIdName;
        }
        return UniqueId.UNIQUE_NAME;
    }

    private static class MetaHelperAddressable extends MetaHelperCallback {

        private Set<Class<? extends ResourceAction>> apiNoAuth = new HashSet<Class<? extends ResourceAction>>();

        private String entityPath;
        private Map<String,List<ResourceOperation>> operationGroupedByProperty;

        public MetaHelperAddressable(Class<?> resource, String entityPath, Map<String,List<ResourceOperation>> operationGroupedByProperty)
        {
            super(resource);

            this.entityPath = entityPath;
            this.operationGroupedByProperty = operationGroupedByProperty;
        }

        public MetaHelperAddressable(Class<?> resource)
        {
            super(resource);
        }

        @Override
        public void whenNewOperation(ResourceOperation operation, Method aMethod)
        {
            Annotation addressableProps = AnnotationUtils.findAnnotation(aMethod, BinaryProperties.class);
            if (addressableProps != null)
            {
                Map<String, Object> annotAttribs = AnnotationUtils.getAnnotationAttributes(addressableProps);
                String[] props = (String[]) annotAttribs.get("value");
                for (String property : props)
                {
                    String propKey = ResourceDictionary.propertyResourceKey(entityPath,property);
                    if (!operationGroupedByProperty.containsKey(propKey))
                    {
                        List<ResourceOperation> ops = new ArrayList<ResourceOperation>();
                        operationGroupedByProperty.put(propKey, ops);
                    }
                    List<ResourceOperation> operations = operationGroupedByProperty.get(propKey);
                    operations.add(operation);
                }

            }
            else
            {
                logger.warn("Resource "+resource.getCanonicalName()+" should declare a @BinaryProperties annotation.");
            }
        }

        @Override
        public void whenOperationDeleted(Class<? extends ResourceAction> deleted, Method aMethod)
        {
        }

        @Override
        public void whenOperationNoAuth(Class<? extends ResourceAction> noAuth, Method aMethod)
        {
            // TODO review - is this right ?
            apiNoAuth.add(noAuth);
        }
    }
    
    /**
     * Little container of a subset of metadata
     *
     * @author Gethin James
     */
    private static class MetaHelper extends MetaHelperCallback{
        
        public MetaHelper(Class<?> resource)
        {
            super(resource);
        }
        
        private List<ResourceOperation> operations = new ArrayList<ResourceOperation>();

        private Set<Class<? extends ResourceAction>> apiDeleted = new HashSet<Class<? extends ResourceAction>>();
        private Set<Class<? extends ResourceAction>> apiNoAuth = new HashSet<Class<? extends ResourceAction>>();
        
        @Override
        public void whenNewOperation(ResourceOperation operation, Method aMethod)
        {
            operations.add(operation);
        }
        
        @Override
        public void whenOperationDeleted(Class<? extends ResourceAction> deleted, Method aMethod)
        {
            apiDeleted.add(deleted);
        }

        @Override
        public void whenOperationNoAuth(Class<? extends ResourceAction> noAuth, Method aMethod)
        {
            apiNoAuth.add(noAuth);
        }
    }
    
    /**
     * Little container of a subset of metadata with a callback
     *
     * @author Gethin James
     */
    private abstract static class MetaHelperCallback {
        
        public MetaHelperCallback(Class<?> resource)
        {
            super();
            this.resource = resource;
        }
        
        final Class<?> resource;
        
        public abstract void whenNewOperation(ResourceOperation operation, Method aMethod);
        public abstract void whenOperationDeleted(Class<? extends ResourceAction> deleted, Method aMethod);
        public abstract void whenOperationNoAuth(Class<? extends ResourceAction> noAuth, Method aMethod);
    }

}
