/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.virtual.config;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Dependency inversion of the Provision of Repository Context.<br>
 *
 * @see Repository
 */
public interface NodeRefResolver
{

    /**
     * Path type reference create if absent. Fail substitute for
     * {@link Repository#findNodeRef(String, String[])}.
     * 
     * @param reference path element names array
     * @return reference array of reference segments
     * @throws AlfrescoRuntimeException if an unimplemented or invalid reference
     *             type is provided
     * @see Repository#findNodeRef(String, String[])
     */
    NodeRef createNamePath(String[] reference);

    /**
     * QName type reference create if absent.Fail safe substitute for
     * {@link Repository#findNodeRef(String, String[])}.
     * 
     * @param reference path element qnames array
     * @param names names to be used when creating the given path. If less than
     *            reference elements they will be matched from the end of the
     *            reference path.
     * @return reference array of reference segments
     * @throws AlfrescoRuntimeException if an unimplemented or invalid reference
     *             type is provided
     * @see Repository#findNodeRef(String, String[])
     */
    NodeRef createQNamePath(String[] reference, String[] names);

    /**
     * Node type explicit inversion of
     * {@link Repository#findNodeRef(String, String[])}.
     * 
     * @return reference array of reference segments
     * @throws AlfrescoRuntimeException if an unimplemented or invalid reference
     *             type is provided
     * @see Repository#findNodeRef(String, String[])
     */
    NodeRef resolveNodeReference(String[] reference);

    /**
     * Path type explicit inversion of
     * {@link Repository#findNodeRef(String, String[])}.
     * 
     * @return reference array of reference segments
     * @throws AlfrescoRuntimeException if an unimplemented or invalid reference
     *             type is provided
     * @see Repository#findNodeRef(String, String[])
     */
    NodeRef resolvePathReference(String[] reference);

    /**
     * QName type explicit inversion of
     * {@link Repository#findNodeRef(String, String[])}.<br>
     * Unlike {@link Repository} {@link NodeRefResolver} implementors must
     * provide an adequate implementation.
     * 
     * @return reference array of reference segments
     * @throws AlfrescoRuntimeException if an unimplemented or invalid reference
     *             type is provided
     * @see Repository#findNodeRef(String, String[])
     */
    NodeRef resolveQNameReference(String[] reference);

    /**
     * Gets the Company Home. Note this is tenant-aware if the correct Cache is
     * supplied.
     * 
     * @return company home node ref
     */
    NodeRef getCompanyHome();

    /**
     * Gets the root home of the company home store
     * 
     * @return root node ref
     */
    NodeRef getRootHome();

    /**
     * Gets the Shared Home. Note this is tenant-aware if the correct Cache is
     * supplied.
     * 
     * @return shared home node ref
     */
    NodeRef getSharedHome();

    /**
     * Gets the user home of the currently authenticated person
     * 
     * @param person person
     * @return user home of person
     */
    NodeRef getUserHome(NodeRef person);

}
