/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.rest.framework.webscripts;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.framework.core.ResourceInspector;
import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceMetadata;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.DeletedResourceException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.ContentInfoImpl;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.Params.RecognizedParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WrappingWebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
/**
 * Handles the HTTP PUT for a Resource, equivalent to CRUD Update
 * 
 * @author Gethin James
 */
public class ResourceWebScriptPut extends AbstractResourceWebScript implements ParamsExtractor
{

    private static Log logger = LogFactory.getLog(ResourceWebScriptPut.class);
    
    public ResourceWebScriptPut()
    {
       super();
       setHttpMethod(HttpMethod.PUT);
       setParamsExtractor(this);
    }
    
    @Override
    public Params extractParams(ResourceMetadata resourceMeta, WebScriptRequest req)
    {
        
        final String relationshipId = req.getServiceMatch().getTemplateVars().get(ResourceLocator.RELATIONSHIP_ID);
        final String entityId = req.getServiceMatch().getTemplateVars().get(ResourceLocator.ENTITY_ID);
        final RecognizedParams params = ResourceWebScriptHelper.getRecognizedParams(req);
        
        switch (resourceMeta.getType())
        {
            case ENTITY:
                if (StringUtils.isBlank(entityId))
                {
                    throw new UnsupportedResourceOperationException("PUT is executed against the instance URL");                  
                } else
                {

                    Object putEnt = ResourceWebScriptHelper.extractJsonContent(req, jsonHelper, resourceMeta.getObjectType(HttpMethod.PUT));
                    return Params.valueOf(entityId,params,putEnt);
                }
            case RELATIONSHIP:
                if (StringUtils.isBlank(relationshipId))
                {
                    throw new UnsupportedResourceOperationException("PUT is executed against the instance URL");                  
                } else
                {
                    Object putRel = ResourceWebScriptHelper.extractJsonContent(req, jsonHelper, resourceMeta.getObjectType(HttpMethod.PUT));
                    ResourceWebScriptHelper.setUniqueId(putRel,relationshipId);
                    return Params.valueOf(entityId, params, putRel);
                }
            case PROPERTY:
                final String resourceName = req.getServiceMatch().getTemplateVars().get(ResourceLocator.RELATIONSHIP_RESOURCE);
                final String propertyName = req.getServiceMatch().getTemplateVars().get(ResourceLocator.PROPERTY);

                if (StringUtils.isNotBlank(entityId) && StringUtils.isNotBlank(resourceName))
                {
                    if (StringUtils.isNotBlank(propertyName))
                    {
                        return Params.valueOf(entityId, relationshipId, null, getStream(req), propertyName, params, getContentInfo(req));
                    }
                    else
                    {
                        return Params.valueOf(entityId, null, null, getStream(req), resourceName, params, getContentInfo(req));
                    }

                }
                //Fall through to unsupported.
            default:
                throw new UnsupportedResourceOperationException("PUT not supported for this request.");
        }
    }

	/**
     * Returns the basic content info from the request.
     * @param req WebScriptRequest
     * @return BasicContentInfo
     */
    private BasicContentInfo getContentInfo(WebScriptRequest req) {
    	
		String encoding = "UTF-8";
		String contentType = MimetypeMap.MIMETYPE_BINARY;
		
		if (StringUtils.isNotEmpty(req.getContentType()))
		{
			MediaType media = MediaType.parseMediaType(req.getContentType());
			contentType = media.getType()+'/'+media.getSubtype();
			if (media.getCharSet() != null)
			{
				encoding = media.getCharSet().toString();
			}			
		}

        return new ContentInfoImpl(contentType, encoding, -1, Locale.getDefault());
	}

	/**
     * Returns the input stream for the request
     * @param req WebScriptRequest
     * @return InputStream
     */
    private InputStream getStream(WebScriptRequest req)
    {
        try
        {
            if (req instanceof WebScriptServletRequest)
            {
                WebScriptServletRequest servletRequest = (WebScriptServletRequest) req;
                return servletRequest.getHttpServletRequest().getInputStream();
            }
            else if (req instanceof WrappingWebScriptRequest)
            {
                // eg. BufferredRequest
                WrappingWebScriptRequest wrappedRequest = (WrappingWebScriptRequest) req;
                return wrappedRequest.getContent().getInputStream();
            }
        }
        catch (IOException error)
        {
            logger.warn("Failed to get the input stream.", error);
        }

        return null;
    }
    
    /**
     * Executes the action on the resource
     * @param resource ResourceWithMetadata
     * @param params parameters to use
     * @return anObject the result of the execute
     */
    @SuppressWarnings("unchecked")
    private Object executeInternal(ResourceWithMetadata resource, Params params)
    {
        switch (resource.getMetaData().getType())
        {
            case ENTITY:
                if (resource.getMetaData().isDeleted(EntityResourceAction.Update.class))
                {
                    throw new DeletedResourceException("(UPDATE) "+resource.getMetaData().getUniqueId());
                }
                EntityResourceAction.Update<Object> updateEnt = (EntityResourceAction.Update<Object>) resource.getResource();
                Object result = updateEnt.update(params.getEntityId(), params.getPassedIn(), params);
                return result;
            case RELATIONSHIP:
                if (resource.getMetaData().isDeleted(RelationshipResourceAction.Update.class))
                {
                    throw new DeletedResourceException("(UPDATE) "+resource.getMetaData().getUniqueId());
                }
                RelationshipResourceAction.Update<Object> relationUpdater = (RelationshipResourceAction.Update<Object>) resource.getResource();
                Object relResult = relationUpdater.update(params.getEntityId(), params.getPassedIn(), params);
                return relResult;
            case PROPERTY:
                if (BinaryResourceAction.Update.class.isAssignableFrom(resource.getResource().getClass()))
                {
                    if (resource.getMetaData().isDeleted(BinaryResourceAction.Update.class))
                    {
                        throw new DeletedResourceException("(UPDATE) "+resource.getMetaData().getUniqueId());
                    }
                    BinaryResourceAction.Update<Object> binUpdater = (BinaryResourceAction.Update<Object>) resource.getResource();
                    return binUpdater.updateProperty(params.getEntityId(), params.getContentInfo(), params.getStream(), params);
                }
                if (RelationshipResourceBinaryAction.Update.class.isAssignableFrom(resource.getResource().getClass()))
                {
                    if (resource.getMetaData().isDeleted(RelationshipResourceBinaryAction.Update.class))
                    {
                        throw new DeletedResourceException("(UPDATE) "+resource.getMetaData().getUniqueId());
                    }
                    RelationshipResourceBinaryAction.Update<Object> binUpdater = (RelationshipResourceBinaryAction.Update<Object>) resource.getResource();
                    return binUpdater.updateProperty(params.getEntityId(), params.getRelationshipId(), params.getContentInfo(), params.getStream(), params);
                }
            default:
                throw new UnsupportedResourceOperationException("PUT not supported for Actions");
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
                    executionCallback.onSuccess(helper.processAdditionsToTheResponse(resource.getMetaData().getApi(), entityCollectionName, params, result), DEFAULT_JSON_CONTENT);
                    return null;
                }
            }, false, true);
    }
}
