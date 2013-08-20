package org.alfresco.rest.framework.webscripts;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.framework.core.ResourceInspector;
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
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.Params.RecognizedParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.http.HttpMethod;

/**
 * Handles the HTTP Get for a Resource
 * 
 * @author Gethin James
 */
public class ResourceWebScriptGet extends AbstractResourceWebScript implements ParamsExtractor
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
        final RecognizedParams params = ResourceWebScriptHelper.getRecognizedParams(req);
        
        switch (resourceMeta.getType())
        {
            case ENTITY:
                if (StringUtils.isNotBlank(entityId))
                {
                    return Params.valueOf(params, entityId, null);
                } 
                else
                {
                    return Params.valueOf(params, null, null);// collection resource
                }
            case RELATIONSHIP:
                if (StringUtils.isNotBlank(relationshipId))
                {
                    return Params.valueOf(params, entityId, relationshipId);                    
                }
                else
                {
                    return Params.valueOf(params, entityId, null); //relationship collection resource                   
                }
            case PROPERTY:
                final String resourceName = req.getServiceMatch().getTemplateVars().get(ResourceLocator.RELATIONSHIP_RESOURCE);
                if (StringUtils.isNotBlank(entityId) && StringUtils.isNotBlank(resourceName))
                {
                    return Params.valueOf(entityId, null, null, null, resourceName, params, null);
                }
                //Fall through to unsupported.
            default:
                throw new UnsupportedResourceOperationException("GET not supported for Actions");
        }
    }

    /**
     * Executes the action on the resource
     * @param resource
     * @param params parameters to use
     * @return anObject the result of the execute
     */
    private Object executeInternal(ResourceWithMetadata resource, Params params)
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
                    else
                    {
                        throw new UnsupportedResourceOperationException();
                    } 
                } 
                else 
                {   
                    if (resource.getMetaData().isDeleted(RelationshipResourceAction.Read.class))
                    {
                        throw new DeletedResourceException("(GET) "+resource.getMetaData().getUniqueId());
                    }
                    RelationshipResourceAction.Read<?> relationGetter = (RelationshipResourceAction.Read<?>) resource.getResource();
                    CollectionWithPagingInfo<?> relations = relationGetter.readAll(params.getEntityId(),params);
                    return relations;
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
                    else
                    {
                        throw new UnsupportedResourceOperationException();
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

    @Override
    public void execute(final ResourceWithMetadata resource, final Params params, final ExecutionCallback executionCallback)
    {
        final String entityCollectionName = ResourceInspector.findEntityCollectionNameName(resource.getMetaData());
        transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    Object result = executeInternal(resource, params);
                    if (result instanceof BinaryResource)
                    {
                        executionCallback.onSuccess(result, null);
                    }
                    else
                    {
                      executionCallback.onSuccess(helper.postProcessResponse(resource.getMetaData().getApi(), entityCollectionName, params, result), DEFAULT_JSON_CONTENT);
                    }
                    return null;
                }
            }, true, true); //Read only
    }

}
