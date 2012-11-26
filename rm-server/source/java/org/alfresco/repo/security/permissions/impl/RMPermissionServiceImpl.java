/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.security.permissions.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Extends the core permission service implementation allowing the consideration of the read records
 * permission.
 * <p>
 * This is required for SOLR support.
 * 
 * @author Roy Wetherall
 */
public class RMPermissionServiceImpl extends PermissionServiceImpl
{
    
    /**
     * Builds the set of authorities who can read the given ACL.  No caching is done here.
     * 
     * @return              an <b>unmodifiable</b> set of authorities
     */
    protected Set<String> buildReaders(Long aclId)
    {
        AccessControlList acl = aclDaoComponent.getAccessControlList(aclId);
        if (acl == null)
        {
            return Collections.emptySet();
        }

        HashSet<String> assigned = new HashSet<String>();
        HashSet<String> readers = new HashSet<String>();

        for (AccessControlEntry ace : acl.getEntries())
        {
            assigned.add(ace.getAuthority());
        }

        for (String authority : assigned)
        {
            UnconditionalAclTest test = new UnconditionalAclTest(getPermissionReference(PermissionService.READ));
            UnconditionalAclTest rmTest = new UnconditionalAclTest(getPermissionReference(RMPermissionModel.READ_RECORDS));
            if (test.evaluate(authority, aclId) || rmTest.evaluate(authority, aclId))
            {
                readers.add(authority);
            }
        }
                        
        return Collections.unmodifiableSet(readers);
    }
    
    /**
     * @param aclId
     * @return set of authorities with read permission on the ACL
     */
    protected Set<String> buildReadersDenied(Long aclId)
    {
        HashSet<String> assigned = new HashSet<String>();
        HashSet<String> denied = new HashSet<String>();
        AccessControlList acl = aclDaoComponent.getAccessControlList(aclId);

        if (acl == null)
        {
            return denied;
        }

        for (AccessControlEntry ace : acl.getEntries())
        {
            assigned.add(ace.getAuthority());
        }

        for(String authority : assigned)
        {
            UnconditionalDeniedAclTest test = new UnconditionalDeniedAclTest(getPermissionReference(PermissionService.READ));
            UnconditionalDeniedAclTest rmTest = new UnconditionalDeniedAclTest(getPermissionReference(RMPermissionModel.READ_RECORDS));
            if(test.evaluate(authority, aclId) || rmTest.evaluate(authority, aclId))
            {
                denied.add(authority);
            }
        }

        return denied;
    }
}
