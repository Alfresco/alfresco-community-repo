/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
        personService.getPerson("andy");
        
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "GROUP_test", AccessStatus.ALLOWED));
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        authorityService.createAuthority(AuthorityType.GROUP, null, "test");
        authorityService.addAuthority("GROUP_test", "andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        authorityService.removeAuthority("GROUP_test", "andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        permissionService.clearPermission(rootNodeRef, "andy");
    }
    
    public void testDeletePermissionByRecipient()
    {
        personService.getPerson("andy");
        
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "GROUP_test", AccessStatus.ALLOWED));
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        authorityService.createAuthority(AuthorityType.GROUP, null, "test");
        authorityService.addAuthority("GROUP_test", "andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        permissionService.deletePermissions("GROUP_test");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
    }
}
