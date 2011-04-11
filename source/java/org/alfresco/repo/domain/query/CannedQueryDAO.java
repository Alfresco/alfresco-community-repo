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
package org.alfresco.repo.domain.query;

/**
 * DAO services for general-use canned queries
 * 
 * @author Derek Hulley
 * @since 3.5
 */
public interface CannedQueryDAO
{
    /**
     * Execute a <b>count(*)</b>-style query returning a count value.  The implementation
     * will ensure that <tt>null</tt> is substituted with <tt>0</tt>, if required.
     * <p>
     * All exceptions can be safely caught and handled as required.
     * 
     * @param sqlNamespace          the query namespace (defined by config file) e.g. <b>alfresco.query.usage</b>
     * @param queryName             the name of the query e.g. <b>select_userCount</b>
     * @param parameterObj          the values to drive the selection
     * @return                      a non-null count
     * 
     * @throws QueryException       if the query returned multiple results
     */
    Long executeCountQuery(String sqlNamespace, String queryName, Object parameterObj);
}
