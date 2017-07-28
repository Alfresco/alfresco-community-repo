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

import java.io.InputStream;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;

/**
 * A {@link Reference} element that identifies the main or a parameter content
 * location. <br>
 * The semantics of the resource is given by {@link Reference} protocol.
 * 
 * @author Bogdan Horje
 */
public interface Resource
{
    /** 
     * Returns the String representation of the resource.
     * 
     * @param stringifier
     * @return 
     * @throws ReferenceEncodingException
     */
    String stringify(Stringifier stringifier) throws ReferenceEncodingException;

    /**
     * Processes the Resource with a {@link ResourceProcessor}. This method has
     * the role of the accept method in the Visitor pattern, in this case the
     * Visitor being the {@link ResourceProcessor} and the Element - the
     * Resource.
     * 
     * @param processor 
     * @return
     * @throws ResourceProcessingError
     */
    <R> R processWith(ResourceProcessor<R> processor) throws ResourceProcessingError;

    InputStream asStream(ActualEnvironment environment) throws ActualEnvironmentException;
}
