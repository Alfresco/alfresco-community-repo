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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.store;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Implementors define virtualization rules.<br>
 * Virtualization is the process of converting {@link NodeRef}s into
 * {@link Reference}s in the context of given {@link ActualEnvironment} .
 * 
 * @author Bogdan Horje
 */
public interface VirtualizationMethod
{
    /**
     * Determines if a given {@link NodeRef} can be virtualized by this
     * virtualization method.
     * 
     * @param env the environment in which the virtualization should take place
     * @param nodeRef the {@link NodeRef} that should be virtualized
     * @return <code>true</code> if the given {@link NodeRef} can be virtualized
     *         by this virtualization method<br>
     *         <code>false</code> otherwise
     * @throws VirtualizationException
     */
    boolean canVirtualize(ActualEnvironment env, NodeRef nodeRef) throws VirtualizationException;

    /**
     * Applies this virtualizatio rule on a given {@link NodeRef}.
     * 
     * @param env the environment in which the virtualization takes place
     * @param nodeRef nodeRef the {@link NodeRef} that will be virtualized
     * @return a {@link Reference} correspondent of the given {@link NodeRef}
     * @throws VirtualizationException
     */
    Reference virtualize(ActualEnvironment env, NodeRef nodeRef) throws VirtualizationException;
}
