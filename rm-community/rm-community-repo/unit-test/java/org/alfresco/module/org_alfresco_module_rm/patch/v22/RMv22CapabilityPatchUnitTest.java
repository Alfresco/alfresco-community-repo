 
package org.alfresco.module.org_alfresco_module_rm.patch.v22;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.security.AuthorityType;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Unit test for RMv22CapabilityPatch
 * 
 * @author Roy Wetherall
 */
public class RMv22CapabilityPatchUnitTest extends BaseUnitTest 
{
	/** patch */
	private @InjectMocks RMv22CapabilityPatch patch;
	
	/**
	 * Given that I am upgrading an existing repository to v2.2
	 * When I execute the patch
	 * Then the capabilities are updated
	 */
	@Test
	public void executePatch()
	{
		when(mockedFilePlanService.getFilePlans())
			.thenReturn(Collections.singleton(filePlan));
		when(mockedAuthorityService.getName(eq(AuthorityType.GROUP), anyString()))
			.thenReturn(
					FilePlanRoleService.ROLE_ADMIN,
                    FilePlanRoleService.ROLE_RECORDS_MANAGER,
					FilePlanRoleService.ROLE_ADMIN,
                    FilePlanRoleService.ROLE_RECORDS_MANAGER,
					FilePlanRoleService.ROLE_ADMIN,
                    FilePlanRoleService.ROLE_RECORDS_MANAGER,
					FilePlanRoleService.ROLE_ADMIN,
                    FilePlanRoleService.ROLE_RECORDS_MANAGER, 
      		      	FilePlanRoleService.ROLE_SECURITY_OFFICER,
      		      	FilePlanRoleService.ROLE_RECORDS_MANAGER);
		
		// execute patch
		patch.applyInternal();
		
		// verify that the correct capabilities have been added
		verify(mockedPermissionService, times(1)).setPermission(
				filePlan, 
				FilePlanRoleService.ROLE_ADMIN, 
				"FileDestructionReport", 
				true);
		verify(mockedPermissionService, times(1)).setPermission(
				filePlan, 
				FilePlanRoleService.ROLE_RECORDS_MANAGER, 
				"FileDestructionReport", 
				true);
		verify(mockedPermissionService, times(1)).setPermission(
				filePlan, 
				FilePlanRoleService.ROLE_ADMIN, 
				"CreateHold", 
				true);
		verify(mockedPermissionService, times(1)).setPermission(
				filePlan, 
				FilePlanRoleService.ROLE_RECORDS_MANAGER, 
				"CreateHold", 
				true);
		verify(mockedPermissionService, times(1)).setPermission(
				filePlan, 
				FilePlanRoleService.ROLE_ADMIN, 
				"AddToHold", 
				true);
		verify(mockedPermissionService, times(1)).setPermission(
				filePlan, 
				FilePlanRoleService.ROLE_RECORDS_MANAGER, 
				"AddToHold", 
				true);
		verify(mockedPermissionService, times(1)).setPermission(
				filePlan, 
				FilePlanRoleService.ROLE_ADMIN, 
				"RemoveFromHold", 
				true);
		verify(mockedPermissionService, times(1)).setPermission(
				filePlan, 
				FilePlanRoleService.ROLE_RECORDS_MANAGER, 
				"RemoveFromHold", 
				true);
		verify(mockedPermissionService, times(1)).setPermission(
				filePlan, 
				FilePlanRoleService.ROLE_RECORDS_MANAGER, 
				"ManageAccessControls", 
				true);
		verify(mockedPermissionService, times(1)).setPermission(
				filePlan, 
				FilePlanRoleService.ROLE_SECURITY_OFFICER, 
				"ManageAccessControls", 
				true);
	}
	
}
