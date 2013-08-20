package org.alfresco.rest.framework.core;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.resource.actions.interfaces.ResourceAction;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.http.HttpMethod;

/**
 * Describes a resource and its properties.  Metadata about what functions
 * the resource can perform and what properties it has.
 *
 * @author Gethin James
 */
public class ResourceMetadata
{
    public enum RESOURCE_TYPE {ENTITY,RELATIONSHIP, PROPERTY};
    private final String uniqueId;
    private final RESOURCE_TYPE type;
    private final List<ResourceOperation> operations;
    private final String parentResource;
    
    @JsonIgnore
    private final Api api;
    private final Set<Class<? extends ResourceAction>> apiDeleted;

    @SuppressWarnings("unchecked")
    public ResourceMetadata(String uniqueId, RESOURCE_TYPE type, List<ResourceOperation> operations, Api api, Set<Class<? extends ResourceAction>> apiDeleted, String parentResource)
    {
        super();
        this.uniqueId = uniqueId;
        this.type = type;
        this.operations = (List<ResourceOperation>) (operations==null?Collections.emptyList():operations);
        this.api = api;
        this.apiDeleted  = (Set<Class<? extends ResourceAction>>) (apiDeleted==null?Collections.emptySet():apiDeleted);
        this.parentResource = parentResource!=null?(parentResource.startsWith("/")?parentResource:"/"+parentResource):null;
    }

    /**
     * Indicates if this resource can support the specified HTTPMethod
     * @param supportedMethod
     * @return true if can support it
     */
    public boolean supports(HttpMethod supportedMethod)
    {
        for (ResourceOperation ops : operations)
        {
            if (ops.getHttpMethod().equals(supportedMethod)) return true;
        }
        return false;
    }

    /**
     * Indicates if this resource can support the specified HTTPMethod
     * @param supportedMethod
     * @return true if can support it
     */
    @SuppressWarnings("rawtypes")
    public Class getObjectType(HttpMethod supportedMethod)
    {
        for (ResourceOperation ops : operations)
        {
            if (ops.getHttpMethod().equals(supportedMethod)) 
            {
                for (ResourceParameter param : ops.getParameters())
                {
                    if (ResourceParameter.KIND.HTTP_BODY_OBJECT.equals(param.getParamType())) {
                        return param.getDataType(); 
                    }
                }   
            }
        }
        return null;
    }
    
    /**
     * Indicates if this resource action is no longer supported.
     * @param resourceAction
     * @return true if it is no longer supported
     */
    public boolean isDeleted(Class<? extends ResourceAction> resourceAction)
    {
       return apiDeleted.contains(resourceAction);
    }
    
    /**
     * URL uniqueId to the resource
     * 
     * @return String uniqueId
     */
    public String getUniqueId()
    {
        return this.uniqueId;
    }

    /**
     * The type of this resource
     * 
     * @return RESOURCE_TYPE type
     */
    public RESOURCE_TYPE getType()
    {
        return this.type;
    }

    /**
     * @return the api
     */
    public Api getApi()
    {
        return this.api;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ResourceMetadata [api=");
        builder.append(this.api);
        builder.append(", uniqueId=");
        builder.append(this.uniqueId);
        builder.append(", type=");
        builder.append(this.type);
        builder.append(", parent=");
        builder.append(this.parentResource);
        builder.append(", operations=");
        builder.append(this.operations);
        builder.append(", apiDeleted=");
        builder.append(this.apiDeleted);
        builder.append("]");
        return builder.toString();
    }

    public List<ResourceOperation> getOperations()
    {
        return this.operations;
    }

    protected Set<Class<? extends ResourceAction>> getApiDeleted()
    {
        return this.apiDeleted;
    }

    public String getParentResource()
    {
        return this.parentResource;
    }
//
//    /**
//     * Gets the properties for the specified http method. That are available to be changed by a url path.
//     * Matches the first operation.
//     * @param httpMethod
//     * @return If not found returns an empty list
//     */
//    public List<String> getAddressableProperties(HttpMethod httpMethod)
//    {
//        for (ResourceOperation ops : operations)
//        {
//            if (ops.getHttpMethod().equals(httpMethod))return ops.getAddressableProperties();
//        }
//        return Collections.emptyList();
//    }
//    
    /**
     * Gets the parameters for the specified http method.
     * Matches the first operation.
     * @param httpMethod
     * @return If not found returns an empty list
     */
    public List<ResourceParameter> getParameters(HttpMethod httpMethod)
    {
        for (ResourceOperation ops : operations)
        {
            if (ops.getHttpMethod().equals(httpMethod))return ops.getParameters();
        }
        return Collections.emptyList();
    }
  
}
