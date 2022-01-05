/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.framework.webscripts;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.framework.core.OperationResourceMetaData;
import org.alfresco.rest.framework.core.ResourceInspectorUtil;
import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceMetadata;
import org.alfresco.rest.framework.core.ResourceOperation;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.DeletedResourceException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.MultiPartResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.MultiPartRelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.Params.RecognizedParams;
import org.alfresco.rest.framework.tools.RecognizedParamsExtractor;
import org.alfresco.rest.framework.tools.RequestReader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptRequestImpl;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.http.HttpMethod;

/**
 * Handles the HTTP POST for a Resource, equivalent to CRUD Create
 * 
 * @author Gethin James, janv
 */
public class ResourceWebScriptPost extends AbstractResourceWebScript implements ParamsExtractor,
                                                                                RecognizedParamsExtractor, RequestReader
{

    public ResourceWebScriptPost()
    {
       super();
       setHttpMethod(HttpMethod.POST);
       setParamsExtractor(this);
    }
    
    @Override
    public Params extractParams(ResourceMetadata resourceMeta, WebScriptRequest req)
    {
        final Map<String, String> resourceVars = locator.parseTemplateVars(req.getServiceMatch().getTemplateVars());
        final String entityId = resourceVars.get(ResourceLocator.ENTITY_ID);
        final String relationshipId = resourceVars.get(ResourceLocator.RELATIONSHIP_ID);

        final String operationName = resourceVars.get(ResourceLocator.RELATIONSHIP_RESOURCE);
        final String propertyName = resourceVars.get(ResourceLocator.PROPERTY);
        final String relationship2Id = resourceVars.get(ResourceLocator.RELATIONSHIP2_ID);

        final RecognizedParams params = getRecognizedParams(req);
        final ResourceOperation operation = resourceMeta.getOperation(HttpMethod.POST);

        switch (resourceMeta.getType())
        {
            case ENTITY:

                if (StringUtils.isNotBlank(entityId))
                {
                    throw new UnsupportedResourceOperationException("POST is executed against a collection URL");
                }
                else
                {
                    Object postedObj = processRequest(resourceMeta, operation, req);
                    return Params.valueOf(null, params, postedObj, req);
                }
            case RELATIONSHIP:
                if (StringUtils.isNotBlank(propertyName) && (StringUtils.isNotBlank(relationshipId)))
                {
                    // collection resource (second level of relationship)
                    Object postedRel = processRequest(resourceMeta, operation, req);
                    return Params.valueOf(true, entityId, relationshipId, null, postedRel, null, null, params, null, req);
                }
                else if (StringUtils.isNotBlank(relationshipId))
                {
                    throw new UnsupportedResourceOperationException("POST is executed against a collection URL");
                }
                else
                {
                    Object postedRel = processRequest(resourceMeta, operation, req);
                    return Params.valueOf(entityId, params, postedRel, req);
                }
            case OPERATION:
                if (StringUtils.isNotBlank(entityId) && StringUtils.isNotBlank(operationName))
                {
                    Object postedObj = processRequest(resourceMeta, operation, req);

                    if (StringUtils.isNotBlank(propertyName))
                    {
                        return Params.valueOf(false, entityId, relationshipId, relationship2Id, postedObj, null, propertyName, params, null, req);
                    }
                    else
                    {
                        return Params.valueOf(entityId, params, postedObj, req);
                    }
                }
                //Fall through to unsupported.
            default:
                throw new UnsupportedResourceOperationException("POST not supported for Actions");
        }
    }

    /**
     * If the request content-type is <i><b>multipart/form-data</b></i> then it
     * returns the {@link FormData}, otherwise it tries to extract the required
     * object from the JSON payload.
     */
    private Object processRequest(ResourceMetadata resourceMeta, ResourceOperation operation, WebScriptRequest req)
    {
        if (WebScriptRequestImpl.MULTIPART_FORM_DATA.equals(req.getContentType()))
        {
            return (FormData) req.parseContent();
        }

        return extractObjFromJson(resourceMeta, operation, req);
    }

    /**
     * If the @WebApiParam has been used and set allowMultiple to false then this will get a single entry.  It
     * should error if an array is passed in.
     * @param resourceMeta ResourceMetadata
     * @param req WebScriptRequest
     * @return Either an object 
     */
    private Object extractObjFromJson(ResourceMetadata resourceMeta, ResourceOperation operation, WebScriptRequest req)
    {
        if (operation == null)
        {
            return null;
        }
        
        Class<?> objType = resourceMeta.getObjectType(operation);
        boolean isTypeOperation = resourceMeta.getType().equals(ResourceMetadata.RESOURCE_TYPE.OPERATION);
        List<ResourceParameter> params = operation.getParameters();

        if (!params.isEmpty())
        {
            for (ResourceParameter resourceParameter : params)
            {
                // POST to collection may or may not support List as json body, Operations don't support a List as json body
                boolean notMultiple = ((! resourceParameter.isAllowMultiple()) || isTypeOperation);
                
                if (ResourceParameter.KIND.HTTP_BODY_OBJECT.equals(resourceParameter.getParamType()) && notMultiple)
                {
                    // Only allow 1 value.
                    try
                    {
                        Object jsonContent = null;
                        if (objType != null)
                        {
                            // check if the body is optional and is not provided
                            if (!resourceParameter.isRequired() && Integer.valueOf(req.getHeader("content-length")) <= 0)
                            {
                                // in some cases the body is optional and the json doesn't need to be extracted
                                return null;
                            }
                            else
                            {
                                jsonContent = extractJsonContent(req, assistant.getJsonHelper(), objType);
                            }
                        }
                        
                        if (isTypeOperation)
                        {
                            return jsonContent;
                        }
                        else
                        {
                            return Arrays.asList(jsonContent);
                        }
                    }
                    catch (InvalidArgumentException iae)
                    {
                        if (iae.getMessage().contains("START_ARRAY") && iae.getMessage().contains("line: 1, column: 1"))
                        {
                            throw new UnsupportedResourceOperationException("Only 1 entity is supported in the HTTP request body");
                        }
                        else
                        {
                            throw iae;
                        }
                    }
               }
            }
        }
        
        if (objType == null)
        {
            return null;
        }
        
        if (isTypeOperation)
        {
            // Operations don't support a List as json body
            return extractJsonContent(req, assistant.getJsonHelper(), objType);
        }
        else
        {
            return extractJsonContentAsList(req, assistant.getJsonHelper(), objType);
        }
    }


    /**
     * Execute a generic operation method
     * @param resource
     * @param params
     * @return the result of the execution.
     */
    private Object executeOperation(ResourceWithMetadata resource, Params params, WithResponse withResponse)  throws Throwable
    {
        OperationResourceMetaData operationResourceMetaData = (OperationResourceMetaData) resource.getMetaData();

        switch (operationResourceMetaData.getOperationMethod().getParameterTypes().length)
        {
            case 4:
                //EntityResource operation by id
                return ResourceInspectorUtil.invokeMethod(operationResourceMetaData.getOperationMethod(),resource.getResource(), params.getEntityId(), params.getPassedIn(), params, withResponse);
            case 5:
                //RelationshipEntityResource operation by id
                return ResourceInspectorUtil.invokeMethod(operationResourceMetaData.getOperationMethod(),resource.getResource(), params.getEntityId(), params.getRelationshipId(), params.getPassedIn(), params, withResponse);
        }

        throw new UnsupportedResourceOperationException("The operation method has an invalid signature");
    }

    /**
     * Executes the action on the resource
     * @param resource ResourceWithMetadata
     * @param params parameters to use
     * @return anObject the result of the execute
     */
    @Override
    public Object executeAction(ResourceWithMetadata resource, Params params, WithResponse withResponse) throws Throwable
    {
        final Object resObj = resource.getResource();

        switch (resource.getMetaData().getType())
        {
            case ENTITY:

                if (resObj instanceof MultiPartResourceAction.Create<?> && params.getPassedIn() instanceof FormData)
                {
                    MultiPartResourceAction.Create<Object> creator = (MultiPartResourceAction.Create<Object>) resObj;
                    return creator.create((FormData) params.getPassedIn(), params, withResponse);

                }
                else
                {
                    if (EntityResourceAction.Create.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(EntityResourceAction.Create.class))
                        {
                            throw new DeletedResourceException("(DELETE) " + resource.getMetaData().getUniqueId());
                        }
                        EntityResourceAction.Create<Object> creator = (EntityResourceAction.Create<Object>) resObj;
                        List<Object> created = creator.create((List<Object>) params.getPassedIn(), params);
                        if (created != null && created.size() == 1)
                        {
                            // return just one object instead of an array
                            return created.get(0);
                        }
                        else
                        {
                            return wrapWithCollectionWithPaging(created);
                        }
                    }
                    if (EntityResourceAction.CreateWithResponse.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(EntityResourceAction.CreateWithResponse.class))
                        {
                            throw new DeletedResourceException("(DELETE) " + resource.getMetaData().getUniqueId());
                        }
                        EntityResourceAction.CreateWithResponse<Object> creator = (EntityResourceAction.CreateWithResponse<Object>) resObj;
                        List<Object> created = creator.create((List<Object>) params.getPassedIn(), params, withResponse);
                        if (created != null && created.size() == 1)
                        {
                            // return just one object instead of an array
                            return created.get(0);
                        }
                        else
                        {
                            return wrapWithCollectionWithPaging(created);
                        }
                    }
                }

            case RELATIONSHIP:
                if (resObj instanceof MultiPartRelationshipResourceAction.Create<?> && params.getPassedIn() instanceof FormData)
                {
                    MultiPartRelationshipResourceAction.Create<Object> creator = (MultiPartRelationshipResourceAction.Create<Object>) resObj;
                    return creator.create(params.getEntityId(), (FormData) params.getPassedIn(), params, withResponse);
                }
                else
                {
                    if (RelationshipResourceAction.Create.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(RelationshipResourceAction.Create.class))
                        {
                            throw new DeletedResourceException("(DELETE) " + resource.getMetaData().getUniqueId());
                        }

                        RelationshipResourceAction.Create<Object> createRelation = (RelationshipResourceAction.Create<Object>) resource.getResource();
                        List<Object> createdRel = createRelation.create(params.getEntityId(), (List<Object>) params.getPassedIn(), params);
                        if (createdRel != null && createdRel.size() == 1)
                        {
                            // return just one object instead of an array
                            return createdRel.get(0);
                        }
                        else
                        {
                            return wrapWithCollectionWithPaging(createdRel);
                        }
                    }

                    if (RelationshipResourceAction.CreateWithResponse.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(RelationshipResourceAction.CreateWithResponse.class))
                        {
                            throw new DeletedResourceException("(DELETE) " + resource.getMetaData().getUniqueId());
                        }

                        RelationshipResourceAction.CreateWithResponse<Object> createRelation = (RelationshipResourceAction.CreateWithResponse<Object>) resource.getResource();
                        List<Object> createdRel = createRelation.create(params.getEntityId(), (List<Object>) params.getPassedIn(), params, withResponse);
                        if (createdRel != null && createdRel.size() == 1)
                        {
                            // return just one object instead of an array
                            return createdRel.get(0);
                        }
                        else
                        {
                            return wrapWithCollectionWithPaging(createdRel);
                        }
                    }
                }
            case OPERATION:
                return executeOperation(resource, params, withResponse);
            default:
                throw new UnsupportedResourceOperationException("POST not supported for Actions");
        }
    }

    private Object wrapWithCollectionWithPaging(List<Object> created)
    {
        if (created !=null && created.size() > 1)
        {
            return CollectionWithPagingInfo.asPagedCollection(created.toArray());
        }
        else
        {
            return created;
        }
    }

}
