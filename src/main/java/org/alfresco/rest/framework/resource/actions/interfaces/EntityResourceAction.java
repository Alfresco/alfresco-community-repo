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

import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * Permissible actions for an Entity Resource
 * 
 * Supports full CRUD (Create, Read, Update, Delete) and ReadAll
 *
 * @author Gethin James
 */

public interface EntityResourceAction
{
    /**
     * HTTP POST - Create a new entity
     */
    public static interface Create<E> extends ResourceAction
    {
        public List<E> create (List<E> entity,  Parameters parameters);
    }

    /**
     * HTTP POST - Create a new entity
     */
    public static interface CreateWithResponse<E> extends ResourceAction
    {
        public List<E> create (List<E> entity,  Parameters parameters, WithResponse withResponse);
    }

    /**
     * HTTP GET - Retrieve list of entities
     */
    public static interface Read<E> extends ResourceAction
    {
        
        /**
         * Reads all the entries from the collection.
         * 
         * Paging information is provided.
         * @param params - will never be null and will have the PAGING default values
         */
        public CollectionWithPagingInfo<E> readAll (Parameters params);
    }

    /**
     * HTTP GET - Retrieve list of entities
     */
    public static interface ReadWithResponse<E> extends ResourceAction
    {

        /**
         * Reads all the entries from the collection.
         *
         * Paging information is provided.
         * @param params - will never be null and will have the PAGING default values
         */
        public CollectionWithPagingInfo<E> readAll (Parameters params, WithResponse withResponse);
    }

    /**
     * HTTP GET - Retrieve an entity by its unique id
     */
    public static interface ReadById<E> extends ResourceAction
    {
        public E readById (String id, Parameters parameters) throws EntityNotFoundException;
    }

    /**
     * HTTP GET - Retrieve an entity by its unique id
     */
    public static interface ReadByIdWithResponse<E> extends ResourceAction
    {
        public E readById (String id, Parameters parameters, WithResponse withResponse) throws EntityNotFoundException;
    }

    /**
     * HTTP PUT - Update entity if it exists, error if not
     */
    public static interface Update<E> extends ResourceAction
    {
        public E update (String id, E entity,  Parameters parameters);
    }

    /**
     * HTTP PUT - Update entity if it exists, error if not
     */
    public static interface UpdateWithResponse<E> extends ResourceAction
    {
        public E update (String id, E entity,  Parameters parameters, WithResponse withResponse);
    }

    /**
     * HTTP DELETE - Deletes an entity
     */
    public static interface Delete extends ResourceAction
    {
        public void delete (String id,  Parameters parameters);
    }

    /**
     * HTTP DELETE - Deletes an entity
     */
    public static interface DeleteWithResponse extends ResourceAction
    {
        public void delete (String id,  Parameters parameters, WithResponse withResponse);
    }
}
