
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

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.WebApi;
import org.alfresco.rest.framework.WebApiDeleted;
import org.alfresco.rest.framework.WebApiDescription;
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
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.ResourceAction;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.ReflectionUtils;

/**
 * Looks at resources to see what they can do
 * 
 * @author Gethin James
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
        
        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.Create.class);
        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.Read.class);
        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.ReadById.class);
        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.Update.class);
        ALL_RELATIONSHIP_RESOURCE_INTERFACES.add(RelationshipResourceAction.Delete.class);
        
        ALL_PROPERTY_RESOURCE_INTERFACES.add(BinaryResourceAction.Read.class);
        ALL_PROPERTY_RESOURCE_INTERFACES.add(BinaryResourceAction.Delete.class);
        ALL_PROPERTY_RESOURCE_INTERFACES.add(BinaryResourceAction.Update.class);
    }
    
    /**
     * Inspects the entity resource and returns meta data about it
     * 
     * @param annot
     * @param resource
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

        if (resource.isAnnotationPresent(WebApiDeleted.class))
        {
            metainfo.add(new ResourceMetadata(ResourceDictionary.resourceKey(urlPath,null), RESOURCE_TYPE.ENTITY,
                        null, api, ALL_ENTITY_RESOURCE_INTERFACES, null));
        }
        else 
        {
            if (!helper.apiDeleted.isEmpty() || !helper.operations.isEmpty())
            {
                metainfo.add(new ResourceMetadata(ResourceDictionary.resourceKey(urlPath,null), RESOURCE_TYPE.ENTITY,
                        helper.operations, api, helper.apiDeleted, null));
            }
        }
           
        inspectAddressedProperties(api, resource, urlPath, metainfo);
        return metainfo;
    }


    /**
     * Inspects the entity resource and returns meta data about any addresssed/binary properties
     * @param api 
     */
    public static void inspectAddressedProperties(Api api, Class<?> resource, final String entityPath, List<ResourceMetadata> metainfo)
    {
        final Map<String,List<ResourceOperation>> operationGroupedByProperty = new HashMap<String,List<ResourceOperation>>();
        MetaHelperCallback helperForAddressProps = new MetaHelperCallback(resource) {

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
        };
        findOperation(BinaryResourceAction.Read.class,   HttpMethod.GET, helperForAddressProps);
        findOperation(BinaryResourceAction.Delete.class, HttpMethod.DELETE, helperForAddressProps);
        findOperation(BinaryResourceAction.Update.class, HttpMethod.PUT, helperForAddressProps);
         
        if (resource.isAnnotationPresent(WebApiDeleted.class))
        {
            metainfo.add(new ResourceMetadata(ResourceDictionary.propertyResourceKey(entityPath,"FIX_ME"), RESOURCE_TYPE.PROPERTY,
                        null, inspectApi(resource), ALL_PROPERTY_RESOURCE_INTERFACES, null));
        }
        else 
        {
            for (Entry<String, List<ResourceOperation>> groupedOps : operationGroupedByProperty.entrySet())
            {           
                metainfo.add(new ResourceMetadata(groupedOps.getKey(), RESOURCE_TYPE.PROPERTY, groupedOps.getValue(), api, null, null));
            }   
        }
    }

    /**
     * Inspects the relationship resource and returns meta data about it
     * 
     * @param annot
     * @param resource
     */
    private static List<ResourceMetadata> inspectRelationship(RelationshipResource annot, Class<?> resource)
    {
        Map<String, Object> annotAttribs = AnnotationUtils.getAnnotationAttributes(annot);
        String urlPath = String.valueOf(annotAttribs.get("name"));
        String entityPath = findEntityNameByAnnotationAttributes(annotAttribs);

        MetaHelper helper = new MetaHelper(resource);
        findOperation(RelationshipResourceAction.Create.class,   HttpMethod.POST, helper);
        findOperation(RelationshipResourceAction.Read.class,     HttpMethod.GET, helper);
        findOperation(RelationshipResourceAction.ReadById.class, HttpMethod.GET, helper);
        findOperation(RelationshipResourceAction.Update.class,   HttpMethod.PUT, helper);  
        findOperation(RelationshipResourceAction.Delete.class,   HttpMethod.DELETE, helper);   
        
        if (resource.isAnnotationPresent(WebApiDeleted.class))
        {
            return Arrays.asList(new ResourceMetadata(ResourceDictionary.resourceKey(entityPath,urlPath), RESOURCE_TYPE.RELATIONSHIP, null, inspectApi(resource), ALL_RELATIONSHIP_RESOURCE_INTERFACES, entityPath));
        }
        else 
        {
            return Arrays.asList(new ResourceMetadata(ResourceDictionary.resourceKey(entityPath,urlPath), RESOURCE_TYPE.RELATIONSHIP, helper.operations, inspectApi(resource), helper.apiDeleted, entityPath));
        }
        
   }

    /**
     * Determines if the resources supports the resource action specified by resourceInterfaceWithOneMethod 
     * @param resourceInterfaceWithOneMethod The resource action
     * @param method http method the action supports.
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
        }
    }


    /**
     * Inspects the method and returns meta data about its operations
     * @param resource
     * @param aMethod
     * @param httpMethod
     * @param defaultParams
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
            return new ResourceOperation(httpMethod, title, desc, parameters);
        }
        else {
            return new ResourceOperation(httpMethod, 
                        "Missing @WebApiDescription annotation", "This method should be annotated with @WebApiDescription", parameters); 
        }
    }
    
    /**
     * Inspects the Method to find any @WebApiParameters and @WebApiParam
     * @param resource the class
     * @param aMethod the method
     * @param httpMethod
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
                    Class<?> dType = ResourceInspectorUtil.determineType(resource,aMethod);
                    params.add(ResourceParameter.valueOf(dType.getSimpleName().toUpperCase(), "The entity", "Unique entity properties", true, ResourceParameter.KIND.HTTP_BODY_OBJECT, true, dType));
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
                    Class<?> dType = ResourceInspectorUtil.determineType(resource,aMethod);
                    params.add(ResourceParameter.valueOf(dType.getSimpleName().toUpperCase(), "The entity", "Unique entity properties", true, ResourceParameter.KIND.HTTP_BODY_OBJECT, true, dType));
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


    /**
     * Indicates the number of params of the Kind specified
     * @param params
     * @param KIND kind of parameter eg. URL_PATH
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
     * @param paramAnot
     * @return
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
     * Returns the method for the interface
     * @param resourceInterfaceWithOneMethod
     * @param resource
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
     * @param annotAttribs
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
     * @param entityAnnot
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
     * @param entityAnnot
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
     * @param anyClass
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
     * Inspects the resource to determine what api it belongs to.
     * It does this by looking for the WebApi package annotation.
     * 
     * @param resource
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
     * @param resource
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
     * @param obj any object
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
     * @param Method uniqueIdMethod
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
    }

}
