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
package org.alfresco.rest.framework.webscripts;

import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceMetadata;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.DeletedResourceException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction.Read;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction.ReadById;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.Params.RecognizedParams;
import org.alfresco.rest.framework.tools.RecognizedParamsExtractor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.http.HttpMethod;

/**
 * Handles the HTTP Get for a Resource
 * 
 * @author Gethin James
 */
public class ResourceWebScriptGet extends AbstractResourceWebScript implements ParamsExtractor, RecognizedParamsExtractor
{
    private static Log logger = LogFactory.getLog(ResourceWebScriptGet.class);
    
    public ResourceWebScriptGet()
    {
        super();
        setHttpMethod(HttpMethod.GET);
        setParamsExtractor(this);
    }

    @Override
    public Params extractParams(ResourceMetadata resourceMeta, WebScriptRequest req)
    {
        final String entityId = req.getServiceMatch().getTemplateVars().get(ResourceLocator.ENTITY_ID);
        final String relationshipId = req.getServiceMatch().getTemplateVars().get(ResourceLocator.RELATIONSHIP_ID);
        final RecognizedParams params = getRecognizedParams(req);
        
        switch (resourceMeta.getType())
        {
            case ENTITY:
                if (StringUtils.isNotBlank(entityId))
                {
                    return Params.valueOf(params, entityId, null, req);
                } 
                else
                {
                    return Params.valueOf(params, null, null, req);// collection resource
                }
            case RELATIONSHIP:
                if (StringUtils.isNotBlank(relationshipId))
                {
                    return Params.valueOf(params, entityId, relationshipId, req);
                }
                else
                {
                    return Params.valueOf(params, entityId, null, req); //relationship collection resource
                }
            case PROPERTY:
                final String resourceName = req.getServiceMatch().getTemplateVars().get(ResourceLocator.RELATIONSHIP_RESOURCE);
                final String propertyName = req.getServiceMatch().getTemplateVars().get(ResourceLocator.PROPERTY);

                if (StringUtils.isNotBlank(entityId) && StringUtils.isNotBlank(resourceName))
                {
                    if (StringUtils.isNotBlank(propertyName))
                    {
                        return Params.valueOf(entityId, relationshipId, null, null, propertyName, params, null, req);
                    }
                    else
                    {
                        return Params.valueOf(entityId, null, null, null, resourceName, params, null, req);
                    }
                }
                //Fall through to unsupported.
            default:
                throw new UnsupportedResourceOperationException("GET not supported for Actions");
        }
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
        
        switch (resource.getMetaData().getType())
        {
            case ENTITY:
                if (StringUtils.isBlank(params.getEntityId()))
                {
                    //Get the collection
                    if (EntityResourceAction.Read.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(EntityResourceAction.Read.class))
                        {
                            throw new DeletedResourceException("(GET) "+resource.getMetaData().getUniqueId());
                        }
                        EntityResourceAction.Read<?> getter = (Read<?>) resource.getResource();
                        CollectionWithPagingInfo<?> resources = getter.readAll(params);
                        return resources;              
                    }
                    else if (EntityResourceAction.ReadWithResponse.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(EntityResourceAction.ReadWithResponse.class))
                        {
                            throw new DeletedResourceException("(GET) "+resource.getMetaData().getUniqueId());
                        }
                        EntityResourceAction.ReadWithResponse<?> getter = (EntityResourceAction.ReadWithResponse<?>) resource.getResource();
                        CollectionWithPagingInfo<?> resources = getter.readAll(params, withResponse);
                        return resources;
                    }
                    else if (EntityResourceAction.DeleteSetWithResponse.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(EntityResourceAction.DeleteSetWithResponse.class))
                        {
                            throw new DeletedResourceException("(DELETE) " + resource.getMetaData().getUniqueId());
                        }
                        EntityResourceAction.DeleteSetWithResponse relationDeleter = (EntityResourceAction.DeleteSetWithResponse) resource.getResource();
                        relationDeleter.deleteSet(params, withResponse);
                        //Don't pass anything to the callback - its just successful
                        return null;
                    }
                    else if (EntityResourceAction.DeleteSet.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(EntityResourceAction.Delete.class))
                        {
                            throw new DeletedResourceException("(DELETE) " + resource.getMetaData().getUniqueId());
                        }
                        EntityResourceAction.DeleteSet relationDeleter = (EntityResourceAction.DeleteSet) resource.getResource();
                        relationDeleter.deleteSet(params);
                        //Don't pass anything to the callback - its just successful
                        return null;
                    }
                    else
                    {
                        throw new UnsupportedResourceOperationException();
                    }

                }
                else
                {
                    if (EntityResourceAction.ReadById.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(EntityResourceAction.ReadById.class))
                        {
                            throw new DeletedResourceException("(GET by id) "+resource.getMetaData().getUniqueId());
                        }
                        EntityResourceAction.ReadById<?> entityGetter = (ReadById<?>) resource.getResource();
                        Object result = entityGetter.readById(params.getEntityId(), params);
                        return result;   
                    }
                    else if (EntityResourceAction.ReadByIdWithResponse.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(EntityResourceAction.ReadByIdWithResponse.class))
                        {
                            throw new DeletedResourceException("(GET by id) "+resource.getMetaData().getUniqueId());
                        }
                        EntityResourceAction.ReadByIdWithResponse<?> entityGetter = (EntityResourceAction.ReadByIdWithResponse<?>) resource.getResource();
                        Object result = entityGetter.readById(params.getEntityId(), params, withResponse);
                        return result;
                    }
                    else
                    {
                        throw new UnsupportedResourceOperationException();
                    }
                }
            case RELATIONSHIP:
                if(StringUtils.isNotBlank(params.getRelationshipId()))
                {
                    if (RelationshipResourceAction.ReadById.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(RelationshipResourceAction.ReadById.class))
                        {
                            throw new DeletedResourceException("(GET by id) "+resource.getMetaData().getUniqueId());
                        }
                        RelationshipResourceAction.ReadById<?> relationGetter = (RelationshipResourceAction.ReadById<?>) resource.getResource();
                        Object result = relationGetter.readById(params.getEntityId(), params.getRelationshipId(), params);
                        return result;
                    }
                    else if (RelationshipResourceAction.ReadByIdWithResponse.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(RelationshipResourceAction.ReadByIdWithResponse.class))
                        {
                            throw new DeletedResourceException("(GET by id) "+resource.getMetaData().getUniqueId());
                        }
                        RelationshipResourceAction.ReadByIdWithResponse<?> relationGetter = (RelationshipResourceAction.ReadByIdWithResponse<?>) resource.getResource();
                        Object result = relationGetter.readById(params.getEntityId(), params.getRelationshipId(), params, withResponse);
                        return result;
                    }
                    else
                    {
                        throw new UnsupportedResourceOperationException();
                    } 
                } 
                else 
                {
                    if (RelationshipResourceAction.Read.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(RelationshipResourceAction.Read.class))
                        {
                            throw new DeletedResourceException("(GET) "+resource.getMetaData().getUniqueId());
                        }
                        RelationshipResourceAction.Read<?> relationGetter = (RelationshipResourceAction.Read<?>) resource.getResource();
                        CollectionWithPagingInfo<?> relations = relationGetter.readAll(params.getEntityId(),params);
                        return relations;
                    }
                    else
                    {
                        if (resource.getMetaData().isDeleted(RelationshipResourceAction.ReadWithResponse.class))
                        {
                            throw new DeletedResourceException("(GET) "+resource.getMetaData().getUniqueId());
                        }
                        RelationshipResourceAction.ReadWithResponse<?> relationGetter = (RelationshipResourceAction.ReadWithResponse<?>) resource.getResource();
                        CollectionWithPagingInfo<?> relations = relationGetter.readAll(params.getEntityId(),params,withResponse);
                        return relations;
                    }
                }
            case PROPERTY:
                if (StringUtils.isNotBlank(params.getEntityId()))
                {
                    if (BinaryResourceAction.Read.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(BinaryResourceAction.Read.class))
                        {
                            throw new DeletedResourceException("(GET) "+resource.getMetaData().getUniqueId());
                        }
                        BinaryResourceAction.Read getter = (BinaryResourceAction.Read) resource.getResource();
                        BinaryResource prop = getter.readProperty(params.getEntityId(), params);
                        return prop;
                    }
                    if (BinaryResourceAction.ReadWithResponse.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(BinaryResourceAction.ReadWithResponse.class))
                        {
                            throw new DeletedResourceException("(GET) "+resource.getMetaData().getUniqueId());
                        }
                        BinaryResourceAction.ReadWithResponse getter = (BinaryResourceAction.ReadWithResponse) resource.getResource();
                        BinaryResource prop = getter.readProperty(params.getEntityId(), params, withResponse);
                        return prop;
                    }
                    if (RelationshipResourceBinaryAction.Read.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(RelationshipResourceBinaryAction.Read.class))
                        {
                            throw new DeletedResourceException("(GET) "+resource.getMetaData().getUniqueId());
                        }
                        RelationshipResourceBinaryAction.Read getter = (RelationshipResourceBinaryAction.Read) resource.getResource();
                        BinaryResource prop = getter.readProperty(params.getEntityId(), params.getRelationshipId(), params);
                        return prop;
                    }
                    if (RelationshipResourceBinaryAction.ReadWithResponse.class.isAssignableFrom(resource.getResource().getClass()))
                    {
                        if (resource.getMetaData().isDeleted(RelationshipResourceBinaryAction.ReadWithResponse.class))
                        {
                            throw new DeletedResourceException("(GET) "+resource.getMetaData().getUniqueId());
                        }
                        RelationshipResourceBinaryAction.ReadWithResponse getter = (RelationshipResourceBinaryAction.ReadWithResponse) resource.getResource();
                        BinaryResource prop = getter.readProperty(params.getEntityId(), params.getRelationshipId(), params, withResponse);
                        return prop;
                    }
                }
                else
                {
                    throw new UnsupportedResourceOperationException();
                }
            default:
                throw new UnsupportedResourceOperationException("GET not supported for Actions");
        }
    }


}
