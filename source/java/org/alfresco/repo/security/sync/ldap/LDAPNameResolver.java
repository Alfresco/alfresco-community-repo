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
package org.alfresco.repo.security.sync.ldap;

import org.alfresco.repo.security.authentication.AuthenticationDiagnostic;
import org.alfresco.repo.security.authentication.AuthenticationException;

/**
 * An interface for objects capable of resolving user IDs to full LDAP Distinguished Names (DNs).
 * 
 * @author dward
 */
public interface LDAPNameResolver
{
    
    /**
     * Resolves a user ID to a distinguished name.
     * 
     * @param userId
     *            the user id
     * @return the DN
     * @throws AuthenticationException
     *             if the user ID cannot be resolved
     */
    public String resolveDistinguishedName(String userId, AuthenticationDiagnostic diagnostic) throws AuthenticationException;
}
