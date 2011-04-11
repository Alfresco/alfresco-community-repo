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
package org.alfresco.repo.security.authority;

import org.alfresco.repo.security.permissions.impl.AbstractPermissionTest;
import org.alfresco.repo.security.permissions.impl.SimplePermissionEntry;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;

public class ExtendedPermissionServiceTest extends AbstractPermissionTest
{
    public void testGroupPermission()
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        personService.getPerson("andy");
        authenticationComponent.clearCurrentSecurityContext();
        
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "GROUP_test", AccessStatus.ALLOWED));
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        authorityService.createAuthority(AuthorityType.GROUP, "test");
        authorityService.addAuthority("GROUP_test", "andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        authorityService.removeAuthority("GROUP_test", "andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        permissionService.clearPermission(rootNodeRef, "andy");
    }
    
    public void testDeletePermissionByRecipient()
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        personService.getPerson("andy");
        authenticationComponent.clearCurrentSecurityContext();
        
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "GROUP_test", AccessStatus.ALLOWED));
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        authorityService.createAuthority(AuthorityType.GROUP, "test");
        authorityService.addAuthority("GROUP_test", "andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        permissionService.deletePermissions("GROUP_test");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
    }
}
