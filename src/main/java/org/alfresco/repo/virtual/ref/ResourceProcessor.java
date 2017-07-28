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

package org.alfresco.repo.virtual.ref;

/**
 * Generic {@link Resource} visitor. It ensures the processing of different
 * types of resources. <br>
 * 
 * @author Bogdan Horje
 * @param <R>
 */
public interface ResourceProcessor<R>
{
    /**
     * Processes a resource of type {@link Resource}.
     * 
     * @param resource a {@link Resource} to be processed.
     * @return generic parameter R that implementors are parameterised with.
     * @throws ResourceProcessingError
     */
    R process(Resource resource) throws ResourceProcessingError;

    /**
     * Processes a resource of type {@link ClasspathResource}.
     * 
     * @param classpath the {@link ClasspathResource} to be processed.
     * @return generic parameter R that implementors are parameterised with.
     * @throws ResourceProcessingError
     */
    R process(ClasspathResource classpath) throws ResourceProcessingError;

    /**
     * Processes a resource of type {@link RepositoryResource}.
     * 
     * @param repository a {@link RepositoryResource} to be processed.
     * @return generic parameter R that implementors are parameterised with.
     * @throws ResourceProcessingError
     */
    R process(RepositoryResource repository) throws ResourceProcessingError;
}
