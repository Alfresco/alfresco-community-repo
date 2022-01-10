/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
