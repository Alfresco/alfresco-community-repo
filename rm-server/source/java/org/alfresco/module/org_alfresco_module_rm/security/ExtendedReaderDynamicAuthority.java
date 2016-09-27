/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Extended readers dynamic authority implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedReaderDynamicAuthority extends ExtendedSecurityBaseDynamicAuthority
{
    /** Extended reader role */
    public static final String EXTENDED_READER = "ROLE_EXTENDED_READER";
    
    /**
     * @see org.alfresco.repo.security.permissions.DynamicAuthority#getAuthority()
     */
    @Override
    public String getAuthority()
    {
        return EXTENDED_READER;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityBaseDynamicAuthority#getAuthorites(org.alfresco.service.cmr.repository.NodeRef)
     */
    protected Set<String> getAuthorites(NodeRef nodeRef) 
    {
        return getExtendedSecurityService().getExtendedReaders(nodeRef);
    }
}
