/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.rest.framework.core;

import java.util.Collection;
import java.util.Map;

import org.springframework.http.HttpMethod;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;

/**
 * Locates rest resources in the system. It can locate Entity,Relationship and Action resources. It can also find embedded resources on a value object. Additionally, it supports locating multiple relationship resources at the same time.
 *
 * @author Gethin James
 */
public interface ResourceLocator
{
    public static final String COLLECTION_RESOURCE = "collectionResource";
    public static final String ENTITY_ID = "entityId";
    public static final String RELATIONSHIP_RESOURCE = "relationResource";
    public static final String RELATIONSHIP_ID = "relationshipId";
    public static final String PROPERTY = "property";
    public static final String LEFTOVER = "leftover";

    public static final String RELATIONSHIP2_ID = "relationship2Id";
    public static final String PROPERTY2 = "property2";

    /**
     *
     * @param templateVars
     * @return
     */
    Map<String, String> parseTemplateVars(Map<String, String> templateVars);

    /**
     * Finds an Entity Resource and returns it in ResourceWithMetadata wrapper.
     * 
     * @param api
     *            - The API being used.
     * @param resourceName
     *            - The entity resource name - this is the "name" property on the @EntityResource annotation.
     * @param httpMethod
     *            - A permitted HttpMethod
     * @return ResourceWithMetadata - The resource and its metadata.
     * @throws InvalidArgumentException
     *             - thrown if either the api or resourceName's are invalid. ie. A resource doesn't exist.
     * @throws UnsupportedResourceOperationException
     *             - throw if the resource does not support the specified HttpMethod.
     */
    ResourceWithMetadata locateEntityResource(Api api, String resourceName, HttpMethod httpMethod) throws InvalidArgumentException, UnsupportedResourceOperationException;

    /**
     * Finds a property or action on a Relationship Resource and returns it in ResourceWithMetadata wrapper.
     * 
     * @param api
     *            - The API being used.
     * @param resourceName
     *            - The entity resource name - this is the "entityResourceName" property on the @RelationshipResource annotation.
     * @param relationName
     *            - The relationship resource name - this is the "name" property on the @RelationshipResource annotation.
     * @param property
     *            - The property resource name - can be either an action or a @BinaryProperty
     * @param httpMethod
     *            - A permitted HttpMethod
     * @return ResourceWithMetadata - The resource and its metadata.
     * @throws InvalidArgumentException
     *             - thrown if either the api or resourceName's are invalid. ie. A resource doesn't exist.
     * @throws UnsupportedResourceOperationException
     *             - throw if the resource does not support the specified HttpMethod.
     */
    ResourceWithMetadata locateRelationPropertyResource(Api api, String entityResource, String relationResource, String property, HttpMethod httpMethod) throws InvalidArgumentException, UnsupportedResourceOperationException;

    /**
     * Finds an Relationship Resource and returns it in ResourceWithMetadata wrapper.
     * 
     * @param api
     *            - The API being used.
     * @param resourceName
     *            - The entity resource name - this is the "entityResourceName" property on the @RelationshipResource annotation.
     * @param relationName
     *            - The relationship resource name - this is the "name" property on the @RelationshipResource annotation.
     * @param httpMethod
     *            - A permitted HttpMethod
     * @return ResourceWithMetadata - The resource and its metadata.
     * @throws InvalidArgumentException
     *             - thrown if either the api or resourceName's are invalid. ie. A resource doesn't exist.
     * @throws UnsupportedResourceOperationException
     *             - throw if the resource does not support the specified HttpMethod.
     */
    ResourceWithMetadata locateRelationResource(Api api, String resourceName, String relationName, HttpMethod httpMethod) throws InvalidArgumentException, UnsupportedResourceOperationException;

    /**
     * Used by webscripts to locate a resource based on the URL template variables.
     * 
     * @param api
     *            - The API being used.
     * @param templateVars
     *            A map of variables representing the request
     * @param httpMethod
     *            - A permitted HttpMethod
     * @return ResourceWithMetadata - The resource and its metadata.
     */
    ResourceWithMetadata locateResource(Api api, Map<String, String> templateVars, HttpMethod httpMethod);

    /**
     * For a given Map finds any resources that should be embedded inside a class.
     * 
     * @param api
     *            - The API being used.
     * @param embeddedKeys
     *            - Likely to be the result of a call to ResourceInspector.findEmbeddedResources()
     * @return ResourceWithMetadata - The resources with metadata.
     */
    Map<String, ResourceWithMetadata> locateEmbeddedResources(Api api, Map<String, String> embeddedKeys);

    /**
     * Finds multiple relationship Resources and returns them as a Map of ResourceWithMetadata.
     * 
     * @param api
     *            - The API being used.
     * @param entityKey
     *            - this is the "entityResourceName" property on the @RelationshipResource annotation.
     * @param relationshipKeys
     *            - The relationship resource names - this is the "name" property on the @RelationshipResource annotation.
     * @param httpMethod
     *            - A permitted HttpMethod
     * @return ResourceWithMetadata - The resource and its metadata.
     * @throws InvalidArgumentException
     *             - thrown if either the api or resourceName's are invalid. ie. A resource doesn't exist.
     * @throws UnsupportedResourceOperationException
     *             - throw if the resource does not support the specified HttpMethod.
     */
    Map<String, ResourceWithMetadata> locateRelationResource(Api api, String entityKey, Collection<String> relationshipKeys, HttpMethod httpMethod) throws InvalidArgumentException, UnsupportedResourceOperationException;
}
