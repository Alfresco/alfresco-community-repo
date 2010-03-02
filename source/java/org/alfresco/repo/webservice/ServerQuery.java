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
package org.alfresco.repo.webservice;

import java.io.Serializable;

import org.alfresco.service.ServiceRegistry;

/**
 * Interface definition for a QuerySession.
 * 
 * @author gavinc
 */
public interface ServerQuery<RESULTSET> extends Serializable
{
    /** System column namess */
    public static String SYS_COL_ASSOC_TYPE = "associationType";
    public static String SYS_COL_ASSOC_NAME = "associationName";
    public static String SYS_COL_IS_PRIMARY = "isPrimary";
    public static String SYS_COL_NTH_SIBLING = "nthSibling";

    /**
     * Executes the query and returns the <b>full query results</b>.
     * 
     * @param
     *      The services to help make the query
     * @return
     *      The full set of query results.
     *      The results must be empty if there are no results.
     */
    public RESULTSET execute(ServiceRegistry serviceRegistry);

    /**
     * Executes the query and return all results up to given maximum number.
     * Note that this is not the same as the page size, but rather is a total
     * upper limit to the number of results that can viewed.
     * 
     * @param
     *      The services to help make the query
     * @param maxResults
     *      the total number of results to retrieve
     * @return
     *      The full set of query results up to the maximum given.
     *      The results must be empty if there are no results.
     */
    public RESULTSET execute(ServiceRegistry serviceRegistry, long maxResults);
}