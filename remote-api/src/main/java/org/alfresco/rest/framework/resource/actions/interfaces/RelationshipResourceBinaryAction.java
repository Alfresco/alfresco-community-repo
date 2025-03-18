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

import java.io.InputStream;

import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.FileBinaryResource;
import org.alfresco.rest.framework.resource.content.NodeBinaryResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;

/**
 * Permissible actions for binary resources of an @RelationshipResource
 * 
 * Supports full CRUD (Read, Update, Delete)
 *
 * @author Gethin James
 */

public interface RelationshipResourceBinaryAction
{

    /**
     * HTTP GET - Retrieve a binary resource
     */
    public static interface Read extends ResourceAction
    {
        /**
         * Retrieves a binary property by returning a BinaryResource object. The specific property is specified in the {@link Parameters} object. See {@link Parameters#hasBinaryProperty(String)} or {@link Parameters#getBinaryProperty()}
         * 
         * @param entityId
         *            unique id
         * @param entityResourceId
         *            Entity resource context for this relationship
         * @param parameters
         *            {@link Parameters}
         * @return BinaryResource - Either {@link FileBinaryResource} or {@link NodeBinaryResource}
         * @throws EntityNotFoundException
         */
        public BinaryResource readProperty(String entityId, String entityResourceId, Parameters parameters) throws EntityNotFoundException;
    }

    /**
     * HTTP GET - Retrieve a binary resource
     */
    public static interface ReadWithResponse extends ResourceAction
    {
        /**
         * Retrieves a binary property by returning a BinaryResource object. The specific property is specified in the {@link Parameters} object. See {@link Parameters#hasBinaryProperty(String)} or {@link Parameters#getBinaryProperty()}
         * 
         * @param entityId
         *            unique id
         * @param entityResourceId
         *            Entity resource context for this relationship
         * @param parameters
         *            {@link Parameters}
         * @return BinaryResource - Either {@link FileBinaryResource} or {@link NodeBinaryResource}
         * @throws EntityNotFoundException
         */
        public BinaryResource readProperty(String entityId, String entityResourceId, Parameters parameters, WithResponse withResponse) throws EntityNotFoundException;
    }

    /**
     * HTTP DELETE - Deletes a binary resource
     */
    public static interface Delete extends ResourceAction
    {

        /**
         * Deletes a binary property. The specific property is specified in the {@link Parameters} object. See {@link Parameters#hasBinaryProperty(String)} or {@link Parameters#getBinaryProperty()}
         * 
         * @param entityId
         *            unique id
         * @param entityResourceId
         *            Entity resource context for this relationship
         * @param parameters
         *            {@link Parameters}
         */
        public void deleteProperty(String entityId, String entityResourceId, Parameters parameters);
    }

    /**
     * HTTP DELETE - Deletes a binary resource
     */
    public static interface DeleteWithResponse extends ResourceAction
    {

        /**
         * Deletes a binary property. The specific property is specified in the {@link Parameters} object. See {@link Parameters#hasBinaryProperty(String)} or {@link Parameters#getBinaryProperty()}
         * 
         * @param entityId
         *            unique id
         * @param entityResourceId
         *            Entity resource context for this relationship
         * @param parameters
         *            {@link Parameters}
         */
        public void deleteProperty(String entityId, String entityResourceId, Parameters parameters, WithResponse withResponse);
    }

    /**
     * HTTP PUT - Updates a binary resource if it exists, error if not
     */
    public static interface Update<E> extends ResourceAction
    {

        /**
         * Updates a binary property. The specific property is specified in the {@link Parameters} object. See {@link Parameters#hasBinaryProperty(String)} or {@link Parameters#getBinaryProperty()}
         * 
         * @param entityId
         *            unique id
         * @param entityResourceId
         *            Entity resource context for this relationship
         * @param stream
         *            An inputstream
         * @param contentInfo
         *            Basic information about the content stream
         * @param params
         *            {@link Parameters}
         */
        public E updateProperty(String entityId, String entityResourceId, BasicContentInfo contentInfo, InputStream stream, Parameters params);
    }

    /**
     * HTTP PUT - Updates a binary resource if it exists, error if not
     */
    public static interface UpdateWithResponse<E> extends ResourceAction
    {

        /**
         * Updates a binary property. The specific property is specified in the {@link Parameters} object. See {@link Parameters#hasBinaryProperty(String)} or {@link Parameters#getBinaryProperty()}
         * 
         * @param entityId
         *            unique id
         * @param entityResourceId
         *            Entity resource context for this relationship
         * @param stream
         *            An inputstream
         * @param contentInfo
         *            Basic information about the content stream
         * @param params
         *            {@link Parameters}
         */
        public E updateProperty(String entityId, String entityResourceId, BasicContentInfo contentInfo,
                InputStream stream, Parameters params, WithResponse withResponse);
    }
}
