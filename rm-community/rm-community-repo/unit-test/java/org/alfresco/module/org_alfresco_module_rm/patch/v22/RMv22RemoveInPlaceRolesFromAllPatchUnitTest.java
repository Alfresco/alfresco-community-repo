package org.alfresco.module.org_alfresco_module_rm.patch.v22;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Unit test for remove in-place roles from 'all roles' group patch unit test.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class RMv22RemoveInPlaceRolesFromAllPatchUnitTest extends BaseUnitTest
{
    /** test data */
    private static final String ALL_ROLES = "allroles";
    
    /** patch */
    @InjectMocks private RMv22RemoveInPlaceRolesFromAllPatch patch;
    
    /**
     * Given there are no file plans to update then the 'all roles' group should not
     * be changed.
     */
    @Test
    public void noFilePlans()
    {
        // given        
        doReturn(Collections.EMPTY_SET).when(mockedFilePlanService).getFilePlans();
        
        // when
        patch.applyInternal();
        
        // then
        verifyZeroInteractions(mockedAuthorityService);
    }

    /**
     * Given that there is one file plan whose 'all roles' group does not contain the 
     * in-place roles the 'all roles' groups should not be changed.
     */
    @Test
    public void rolesDontNeedRemovingFromGroup()
    {
        // given 
        doReturn(Collections.singleton(filePlan)).when(mockedFilePlanService).getFilePlans();
        doReturn(getMockedRole(FilePlanRoleService.ROLE_EXTENDED_READERS)).when(mockedFilePlanRoleService).getRole(filePlan, FilePlanRoleService.ROLE_EXTENDED_READERS);
        doReturn(getMockedRole(FilePlanRoleService.ROLE_EXTENDED_WRITERS)).when(mockedFilePlanRoleService).getRole(filePlan, FilePlanRoleService.ROLE_EXTENDED_WRITERS);
        doReturn(ALL_ROLES).when(mockedFilePlanRoleService).getAllRolesContainerGroup(filePlan);
        doReturn(Collections.EMPTY_SET).when(mockedAuthorityService).getContainedAuthorities(null, ALL_ROLES, true);
        
        // when
        patch.applyInternal();
        
        // then
        verify(mockedAuthorityService, times(1)).getContainedAuthorities(null, ALL_ROLES, true);
        verifyNoMoreInteractions(mockedAuthorityService);
    }
    
    /**
     * Given that there is one file plan whose 'all roles' group contains the in-place
     * roles then they should be revoved.
     */
    @Test
    public void removeRolesFromGroup()
    {
        // given 
        doReturn(Collections.singleton(filePlan)).when(mockedFilePlanService).getFilePlans();        
        doReturn(getMockedRole(FilePlanRoleService.ROLE_EXTENDED_READERS)).when(mockedFilePlanRoleService).getRole(filePlan, FilePlanRoleService.ROLE_EXTENDED_READERS);
        doReturn(getMockedRole(FilePlanRoleService.ROLE_EXTENDED_WRITERS)).when(mockedFilePlanRoleService).getRole(filePlan, FilePlanRoleService.ROLE_EXTENDED_WRITERS);        
        doReturn(ALL_ROLES).when(mockedFilePlanRoleService).getAllRolesContainerGroup(filePlan);        
        Set<String> contains = new HashSet<String>(2);
        contains.add(FilePlanRoleService.ROLE_EXTENDED_READERS);
        contains.add(FilePlanRoleService.ROLE_EXTENDED_WRITERS);
        doReturn(contains).when(mockedAuthorityService).getContainedAuthorities(null, ALL_ROLES, true);
        
        // when
        patch.applyInternal();
        
        // then
        verify(mockedAuthorityService, times(1)).getContainedAuthorities(null, ALL_ROLES, true);
        verify(mockedAuthorityService, times(1)).removeAuthority(ALL_ROLES, FilePlanRoleService.ROLE_EXTENDED_READERS);
        verify(mockedAuthorityService, times(1)).removeAuthority(ALL_ROLES, FilePlanRoleService.ROLE_EXTENDED_WRITERS);
        verifyNoMoreInteractions(mockedAuthorityService);        
    }
    
    /**
     * Helper method to create a mocked role.
     */
    private Role getMockedRole(String name)
    {
        Role mockedRole = mock(Role.class);
        doReturn(name).when(mockedRole).getRoleGroupName();
        return mockedRole;
    }
}
