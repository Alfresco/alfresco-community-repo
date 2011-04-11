/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.archive;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This interface defines a filter for ArchivedNodes.
 * 
 * @author Neil Mc Erlean
 * @since 3.5
 */
public interface ArchivedNodesFilter
{
    /**
     * This method checks whether or not the specified {@link NodeRef} should be included,
     * as defined by the concrete filter implementation.
     * @param nodeRef the NodeRef to be checked for filtering.
     * @return <code>true</code> if the {@link NodeRef} is acceptable, else <code>false</code>.
     */
    boolean accept(NodeRef nodeRef);

}