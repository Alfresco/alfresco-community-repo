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
package org.alfresco.web.sharepoint.auth;

import javax.servlet.http.HttpServletRequest;

/**
 * An object capable of answering whether a particular user is a member of the site indicated by the request URL.
 * 
 * @author dward
 */
public interface SiteMemberMapper
{

    /**
     * Determines whether a particular user is a member of the site indicated by the request URI.
     * 
     * @param request
     *            the request
     * @param alfrescoContext
     *            the context path to strip from the request URI
     * @param userName
     *            the user name
     * @return <code>true</code> if the user is a member
     * @throws SiteMemberMappingException
     *             on error
     */
    boolean isSiteMember(HttpServletRequest request, String alfrescoContext, String userName)
            throws SiteMemberMappingException;
}
