/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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