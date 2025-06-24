/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.authority;

import org.junit.experimental.categories.Category;

import org.alfresco.repo.security.permissions.impl.AbstractPermissionTest;
import org.alfresco.repo.security.permissions.impl.SimplePermissionEntry;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.test_category.OwnJVMTestsCategory;

@Category(OwnJVMTestsCategory.class)
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
        runAs("admin");
        authorityService.createAuthority(AuthorityType.GROUP, "test");
        authorityService.addAuthority("GROUP_test", "andy");
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        runAs("admin");
        authorityService.removeAuthority("GROUP_test", "andy");
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        permissionService.clearPermission(rootNodeRef, "andy");
    }

    public void testDeletePermissions()
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        personService.getPerson("andy");
        authenticationComponent.clearCurrentSecurityContext();

        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "GROUP_test", AccessStatus.ALLOWED));
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        runAs("admin");
        authorityService.createAuthority(AuthorityType.GROUP, "test");
        authorityService.addAuthority("GROUP_test", "andy");
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        permissionService.deletePermissions("GROUP_test");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);

        // At the store level
        permissionService.clearPermission(testStoreRef, "GROUP_test");
        permissionService.deletePermission(testStoreRef, "GROUP_test", PermissionService.READ);
    }

    public void testExplainPermission()
    {
        assertNull("This method has not been implemented", permissionService.explainPermission(null, null));
    }
}
