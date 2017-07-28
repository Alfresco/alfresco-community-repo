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

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceInspector;
import org.alfresco.rest.framework.core.ResourceInspectorUtil;
import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.jacksonextensions.ExecutionResult;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.resource.actions.ActionExecutor;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.InvalidSelectException;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.Params.RecognizedParams;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.resource.parameters.where.InvalidQueryException;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryImpl;
import org.alfresco.rest.framework.resource.parameters.where.WhereCompiler;
import org.alfresco.rest.framework.tools.ApiAssistant;
import org.alfresco.rest.framework.tools.ResponseWriter;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonErrorNode;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.RewriteCardinalityException;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.BeanUtils;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.HttpMethod;

/**
 * Helps a Webscript with various tasks
 * 
 * @author Gethin James
 * @author janv
 */
public class ResourceWebScriptHelper
{
    private static Log logger = LogFactory.getLog(ResourceWebScriptHelper.class);
    private ResourceLocator locator;

    private ActionExecutor executor;

    /**
     * Set the id of theObj to the uniqueId. Attempts to find a set method and
     * invoke it. If it fails it just swallows the exceptions and doesn't throw
     * them further.
     * 
     * @param theObj Object
     * @param uniqueId String
     */
    public static void setUniqueId(Object theObj, String uniqueId)
    {
        Method annotatedMethod = ResourceInspector.findUniqueIdMethod(theObj.getClass());
        if (annotatedMethod != null)
        {
            PropertyDescriptor pDesc = BeanUtils.findPropertyForMethod(annotatedMethod);
            if (pDesc != null)
            {
                Method writeMethod = pDesc.getWriteMethod();
                if (writeMethod != null)
                {
                    try
                    {
                        writeMethod.invoke(theObj, uniqueId);
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Unique id set for property: " + pDesc.getName());
                        }
                    }
                    catch (IllegalArgumentException error)
                    {
                        logger.warn("Invocation error", error);
                    }
                    catch (IllegalAccessException error)
                    {
                        logger.warn("IllegalAccessException", error);
                    }
                    catch (InvocationTargetException error)
                    {
                        logger.warn("InvocationTargetException", error);
                    }
                }
                else
                {
                    logger.warn("No setter method for property: " + pDesc.getName());
                }
            }

        }
    }

    /**
     * Looks at the object passed in and recursively expands any @EmbeddedEntityResource annotations or related relationship.
     * {@link org.alfresco.rest.framework.resource.EmbeddedEntityResource EmbeddedEntityResource} is expanded by calling the ReadById method for this entity.
     * 
     * Either returns a ExecutionResult object or a CollectionWithPagingInfo containing a collection of ExecutionResult objects.
     * 
     * @param api Api
     * @param entityCollectionName String
     * @param params  Params
     * @param objectToWrap Object
     * @return Object - Either ExecutionResult or CollectionWithPagingInfo<ExecutionResult>
     */
    public Object processAdditionsToTheResponse(WebScriptResponse res, Api api, String entityCollectionName, Params params, Object objectToWrap)
    {
        PropertyCheck.mandatory(this, null, params);
        if (objectToWrap == null ) return null;
        if (objectToWrap instanceof CollectionWithPagingInfo<?>)
        {
            CollectionWithPagingInfo<?> collectionToWrap = (CollectionWithPagingInfo<?>) objectToWrap;
            Object sourceEntity = executeIncludedSource(api, params, entityCollectionName, collectionToWrap);
            Collection<Object> resultCollection = new ArrayList(collectionToWrap.getCollection().size());
            if (!collectionToWrap.getCollection().isEmpty())
            {
                for (Object obj : collectionToWrap.getCollection())
                {
                    resultCollection.add(processAdditionsToTheResponse(res, api,entityCollectionName,params,obj));
                }
            }
            return CollectionWithPagingInfo.asPaged(collectionToWrap.getPaging(), resultCollection, collectionToWrap.hasMoreItems(),
                                                    collectionToWrap.getTotalItems(), sourceEntity, collectionToWrap.getContext());
        }
        else
        {           
            if (BeanUtils.isSimpleProperty(objectToWrap.getClass())  || objectToWrap instanceof Collection)
            {
                //Simple property or Collection that can't be embedded so just return it.
                return objectToWrap;
            }

            final ExecutionResult execRes = new ExecutionResult(objectToWrap, params.getFilter());
            
            Map<String,Pair<String,Method>> embeddded = ResourceInspector.findEmbeddedResources(objectToWrap.getClass());
            if (embeddded != null && !embeddded.isEmpty())
            {
                Map<String, Object> results = executeEmbeddedResources(api, params,objectToWrap, embeddded);
                execRes.addEmbedded(results);
            }
            
            if (params.getRelationsFilter() != null && !params.getRelationsFilter().isEmpty())
            {
                Map<String, ResourceWithMetadata> relationshipResources = locator.locateRelationResource(api,entityCollectionName, params.getRelationsFilter().keySet(), HttpMethod.GET);
                String uniqueEntityId = ResourceInspector.findUniqueId(objectToWrap);
                Map<String,Object> relatedResources = executeRelatedResources(api, params, relationshipResources, uniqueEntityId);
                execRes.addRelated(relatedResources);
            }

            return execRes; 

        }
    }

    private Object executeIncludedSource(Api api, Params params, String entityCollectionName, CollectionWithPagingInfo<?> collectionToWrap)
    {
        if (params.includeSource())
        {
            if (collectionToWrap.getSourceEntity() != null)
            {
                //The implementation has already set it so return it;
                return collectionToWrap.getSourceEntity();
            }

            ResourceWithMetadata res = locator.locateEntityResource(api, entityCollectionName, HttpMethod.GET);
            if (res != null)
            {
                Object result = executeResource(api, params, params.getEntityId(), null, res);
                if (result!=null && result instanceof ExecutionResult) return ((ExecutionResult) result).getRoot();
            }
        }
        return null;
    }

    /**
     * Loops through the embedded Resources and executes them.  The results are added to list of embedded results used by
     * the ExecutionResult object.
     *
     * @param api Api
     * @param params Params
     * @param objectToWrap Object
     * @param embeddded Map<String, Pair<String, Method>>
     * @return Map
     */
    private Map<String, Object> executeEmbeddedResources(Api api, Params params, Object objectToWrap, Map<String, Pair<String, Method>> embeddded)
    {
        final Map<String,Object> results = new HashMap<String,Object>(embeddded.size());
        for (Entry<String, Pair<String,Method>> embeddedEntry : embeddded.entrySet())
        {
            ResourceWithMetadata res = locator.locateEntityResource(api, embeddedEntry.getValue().getFirst(), HttpMethod.GET);
            if (res != null)
            {
                Object id = ResourceInspectorUtil.invokeMethod(embeddedEntry.getValue().getSecond(), objectToWrap);
                if (id != null)
                {
                    Object execEmbeddedResult = executeResource(api, params, String.valueOf(id), embeddedEntry.getKey(), res);
                    if (execEmbeddedResult != null)
                    {
                        if (execEmbeddedResult instanceof ExecutionResult)
                        {
                           ((ExecutionResult) execEmbeddedResult).setAnEmbeddedEntity(true);
                        }
                        results.put(embeddedEntry.getKey(), execEmbeddedResult);
                    }
                }
                else
                {
                    //Call to embedded id for null value, 
                    logger.warn("Cannot embed resource with path "+embeddedEntry.getKey()+". No unique id because the method annotated with @EmbeddedEntityResource returned null.");
                }
            }
        }
        return results;
    }

    /**
     * Loops through the related Resources and executed them.  The results are added to list of embedded results used by
     * the ExecutionResult object.
     *
     * @param api Api
     * @param params Params
     * @param relatedResources Map<String, ResourceWithMetadata>
     * @param uniqueEntityId String
     * @return Map
     */
    private Map<String,Object> executeRelatedResources(final Api api, Params params,
                                                       Map<String, ResourceWithMetadata> relatedResources,
                                                       String uniqueEntityId)
    {
        final Map<String,Object> results = new HashMap<String,Object>(relatedResources.size());
        for (final Entry<String, ResourceWithMetadata> relation : relatedResources.entrySet())
        {
            Object execResult = executeResource(api, params, uniqueEntityId, relation.getKey(), relation.getValue());
            if (execResult != null)
            {
              results.put(relation.getKey(), execResult);
            }
        }
        return results;
    }

    /**
     * Executes a single related Resource.  The results are added to list of embedded results used by
     * the ExecutionResult object.
     *
     * @param api Api
     * @param params Params
     * @param uniqueEntityId String
     * @param resourceKey String
     * @param resource ResourceWithMetadata
     * @return Object
     */
    private Object executeResource(final Api api, Params params,
                                   final String uniqueEntityId, final String resourceKey, final ResourceWithMetadata resource)
    {
        try
        {
            BeanPropertiesFilter paramFilter = null;
            final Object[] resultOfExecution = new Object[1];
            Map<String, BeanPropertiesFilter> filters = params.getRelationsFilter();
            if (filters!=null)
            {
                paramFilter = filters.get(resourceKey);
            }
            final Params executionParams = Params.valueOf(paramFilter, uniqueEntityId, params.getRequest());
            final WithResponse callBack = new WithResponse(Status.STATUS_OK, ResponseWriter.DEFAULT_JSON_CONTENT,ResponseWriter.CACHE_NEVER);
            //Read only because this only occurs for GET requests
            Object result = executor.executeAction(resource, executionParams, callBack);
            return processAdditionsToTheResponse(null, api, null, executionParams, result);
        }
        catch(NotFoundException e)
        {
        	// ignore, cannot access the object so don't embed it
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignored error, cannot access the object so can't embed it ", e);
            }
        }
        catch(PermissionDeniedException e)
        {
        	// ignore, cannot access the object so don't embed it
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignored error, cannot access the object so can't embed it ", e);
            }
        } catch (Throwable throwable)
        {
            logger.warn("Failed to execute a RelatedResource for "+resourceKey+" "+throwable.getMessage());
        }

        return null; //default
    }

    public void setLocator(ResourceLocator locator)
    {
        this.locator = locator;
    }

    public void setExecutor(ActionExecutor executor)
    {
        this.executor = executor;
    }
        
}
