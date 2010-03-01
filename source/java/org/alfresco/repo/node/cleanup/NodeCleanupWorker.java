/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.node.cleanup;

import java.util.List;

/**
 * Interface for classes that implement a snippet of node cleanup.
 * 
 * @author Derek Hulley
 * @since 2.2 SP2
 */
public interface NodeCleanupWorker
{
    /**
     * Perform some work to clean up data.  All errors must be handled and converted
     * to error messages.
     * 
     * @return              Returns a list of informational messages.
     */
    List<String> doClean();
}