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

package org.alfresco.module.org_alfresco_module_rm.bootstrap;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Module compatibility component unit test
 * 
 * @author Roy Wetherall
 * @since 2.4
 */
public class ModuleCompatibilityComponentUnitTest 
{
	/** mocks */
	@Mock private DescriptorService mockedDescriptorService;
	@Mock private ModuleService mockedModuleService;
	@Mock private ContextRefreshedEvent mockedContextRefreshedEvent;
	@Mock private ConfigurableApplicationContext mockedApplicationContext;
	@Mock private ModuleDetails mockedModuleDetails;
	@Mock private LicenseDescriptor mockedDescriptor;
	
	/** object under test */
	@InjectMocks private ModuleCompatibilityComponent moduleCompatibilityComponent;
	
	/**
	 * Before test execution
	 */
	@Before
	public void before()
	{
		MockitoAnnotations.initMocks(this);
		
		when(mockedContextRefreshedEvent.getApplicationContext())
			.thenReturn(mockedApplicationContext);
		when(mockedDescriptorService.getLicenseDescriptor())
			.thenReturn(mockedDescriptor);		
	}
	
	/**
	 * Given that core community is installed
	 * And that RM community is installed
	 * When the application context is loaded
	 * Then it is successful
	 */
	@Test
	public void communityOnCommunity()
	{
		// community core installed
		when(mockedDescriptor.getLicenseMode())
			.thenReturn(LicenseMode.UNKNOWN);
		
		// community RM installed
		when(mockedModuleService.getModule(anyString()))
			.thenReturn(null);
		
		// on app context refresh
		moduleCompatibilityComponent.onApplicationEvent(mockedContextRefreshedEvent);
		
		// verify close never called
		verify(mockedApplicationContext, never()).close();
		
	}
	
	/**
	 * Given that core community is installed
	 * And that RM enterprise is installed
	 * When the application context is loaded
	 * Then it fails
	 */
	@Test
	public void enterpriseOnCommunity()
	{
		// community core installed
		when(mockedDescriptor.getLicenseMode())
			.thenReturn(LicenseMode.UNKNOWN);
		
		// enterprise RM installed
		when(mockedModuleService.getModule(anyString()))
			.thenReturn(mockedModuleDetails);
		
		// on app context refresh
		moduleCompatibilityComponent.onApplicationEvent(mockedContextRefreshedEvent);
		
		// verify close is called
		verify(mockedApplicationContext).close();
		
	}
	
	/**
	 * Given that core enterprise is installed
	 * And that RM community is installed
	 * When the application context is loaded
	 * Then it fails
	 */
	@Test
	public void communityOnEnterprise()
	{
		// enterprise core installed
		when(mockedDescriptor.getLicenseMode())
			.thenReturn(LicenseMode.ENTERPRISE);
		
		// community RM installed
		when(mockedModuleService.getModule(anyString()))
			.thenReturn(null);
		
		// on app context refresh
		moduleCompatibilityComponent.onApplicationEvent(mockedContextRefreshedEvent);
		
		// verify close is called
		verify(mockedApplicationContext).close();		
	}
	
	/**
	 * Given that core enterprise is installed
	 * And that RM enterprise is installed
	 * When the application context is loaded
	 * Then it is successful
	 */
	@Test
	public void enterpriseOnEnterprise()
	{
		// enterprise core installed
		when(mockedDescriptor.getLicenseMode())
			.thenReturn(LicenseMode.ENTERPRISE);
		
		// enterprise RM installed
		when(mockedModuleService.getModule(anyString()))
			.thenReturn(mockedModuleDetails);
		
		// on app context refresh
		moduleCompatibilityComponent.onApplicationEvent(mockedContextRefreshedEvent);
		
		// verify close never called
		verify(mockedApplicationContext, never()).close();
		
	}
}
