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
package org.alfresco.rest.framework.resource.actions.interfaces;

import java.util.List;

import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;

/**
 * Permissible actions for an Relationship Resources
 * Based around CRUD - Create, ReadAll, ReadById, Update, Delete
 * 
 * @author Gethin James
 */
public interface RelationshipResourceAction
{

    /**
     * HTTP GET - Retrieve list of related entities by its related entityResource Id
     */
    public static interface Read<E> extends ResourceAction
    {
        /**
         * Reads all the relationship entities from the collection using the related entityResourceId.
         * 
         * Paging information is provided.  
         * @param entityResourceId Entity resource context for this relationship
         * @param params - will never be null and will have the PAGING default values
         */
        public CollectionWithPagingInfo<E> readAll (String entityResourceId, Parameters params);
    }

    /**
     * HTTP GET - Retrieve list of related entities by its related entityResource Id
     */
    public static interface ReadWithResponse<E> extends ResourceAction
    {
        /**
         * Reads all the relationship entities from the collection using the related entityResourceId.
         *
         * Paging information is provided.
         * @param entityResourceId Entity resource context for this relationship
         * @param params - will never be null and will have the PAGING default values
         */
        public CollectionWithPagingInfo<E> readAll (String entityResourceId, Parameters params, WithResponse withResponse);
    }

    /**
     * HTTP GET - Retrieve a relation by its unique id & entity context
     */
    public static interface ReadById<E> extends ResourceAction
    {
        public E readById (String entityResourceId, String id,  Parameters parameters) throws RelationshipResourceNotFoundException;
    }

    /**
     * HTTP GET - Retrieve a relation by its unique id & entity context
     */
    public static interface ReadByIdWithResponse<E> extends ResourceAction
    {
        public E readById (String entityResourceId, String id,  Parameters parameters, WithResponse withResponse) throws RelationshipResourceNotFoundException;
    }

    /**
     * HTTP PUT - Update entity (by its related entityResource Id) if it exists, error if not
     */
    public static interface Update<E> extends ResourceAction
    {
        public E update (String entityResourceId, E entity,  Parameters parameters);
    }

    /**
     * HTTP PUT - Update entity (by its related entityResource Id) if it exists, error if not
     */
    public static interface UpdateWithResponse<E> extends ResourceAction
    {
        public E update (String entityResourceId, E entity,  Parameters parameters, WithResponse withResponse);
    }

    /**
     * HTTP POST - Create one or more new entity
     */
    public static interface Create<E> extends ResourceAction
    {
        public List<E> create (String entityResourceId, List<E> entity,  Parameters parameters);
    }

    /**
     * HTTP POST - Create one or more new entity
     */
    public static interface CreateWithResponse<E> extends ResourceAction
    {
        public List<E> create (String entityResourceId, List<E> entity,  Parameters parameters, WithResponse withResponse);
    }

    /**
     * HTTP DELETE - Deletes a relation by its unique id & entity context
     */
    public static interface Delete extends ResourceAction
    {
        public void delete (String entityResourceId, String id,  Parameters parameters);
    }

    /**
     * HTTP DELETE - Deletes a relation by its unique id & entity context
     */
    public static interface DeleteWithResponse extends ResourceAction
    {
        public void delete (String entityResourceId, String id,  Parameters parameters, WithResponse withResponse);
    }
}
