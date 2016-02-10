/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.bootstrap;

import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.descriptor.DescriptorService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextStartedEvent;

/**
 * @author Roy Wetherall
 */
public class ModuleCompatibilityComponent implements ApplicationListener<ContextStartedEvent>
{
	private static final String RM_ENT_MODULE_ID = "alfresco-rm-enterprise-repo";
	
	private DescriptorService descriptorService;
	
	private ModuleService moduleService;
	
	@Override
	public void onApplicationEvent(ContextStartedEvent contextStartedEvent) 
	{
		// get the license mode
		LicenseMode licenseMode = descriptorService.getLicenseDescriptor().getLicenseMode();
		if (LicenseMode.ENTERPRISE.equals(licenseMode))
		{
			// ensure RM enterprise module is installed
			if (moduleService.getModule(RM_ENT_MODULE_ID) == null)
			{
				// log something
				
				// report an error				
				
				// close the application context!
				((ConfigurableApplicationContext)contextStartedEvent.getApplicationContext()).close();
			}
		}
	}
}
