package org.alfresco.repo.security.authority;

import org.alfresco.repo.security.permissions.impl.AbstractPermissionTest;
import org.alfresco.repo.security.permissions.impl.SimplePermissionEntry;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.experimental.categories.Category;

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
        authorityService.createAuthority(AuthorityType.GROUP, "test");
        authorityService.addAuthority("GROUP_test", "andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        authorityService.removeAuthority("GROUP_test", "andy");
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
        authorityService.createAuthority(AuthorityType.GROUP, "test");
        authorityService.addAuthority("GROUP_test", "andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        permissionService.deletePermissions("GROUP_test");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);

        //At the store level
        permissionService.clearPermission(testStoreRef, "GROUP_test");
        permissionService.deletePermission(testStoreRef, "GROUP_test", PermissionService.READ);
    }

    public void testExplainPermission()
    {
        assertNull("This method has not been implemented", permissionService.explainPermission(null,null));
    }
}
