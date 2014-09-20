/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.admin.registry.RegistryService;
import org.alfresco.repo.module.tool.ModuleManagementToolException;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.alfresco.util.GUID;
import org.alfresco.util.VersionNumber;
import org.alfresco.repo.module.ModuleVersionNumber;

/**
 * @see org.alfresco.repo.module.ModuleComponentHelper
 * <p/>
 * This test creates a bunch of dummy components and then simulates
 * startups with different module current versions.
 * <p/>
 * There are 3 modules.  There are 3 components.
 * 
 * @author Derek Hulley
 */
public class ModuleComponentHelperTest extends BaseAlfrescoTestCase
{
    private final String SOME_GUID = "" + GUID.generate();
    private final String[] MODULE_IDS =
    {
        "M0 @ " + SOME_GUID,
        "M1 @ " + SOME_GUID,
        "M2 @ " + SOME_GUID
    };
    private final String[] COMPONENT_NAMES =
    {
        "C0 @ " + SOME_GUID,
        "C1 @ " + SOME_GUID,
        "C2 @ " + SOME_GUID
    };
    private final VersionNumber[] VERSIONS =
    {
        new VersionNumber("0"),
        new VersionNumber("1"),
        new VersionNumber("2"),
        new VersionNumber("3")
    };
    private static final Map<ModuleVersionNumber, Integer> EXECUTION_COUNT_BY_VERSION;
    static
    {
        EXECUTION_COUNT_BY_VERSION = new HashMap<ModuleVersionNumber, Integer>(13);
        EXECUTION_COUNT_BY_VERSION.put(new ModuleVersionNumber("0.0"), 3);
        EXECUTION_COUNT_BY_VERSION.put(new ModuleVersionNumber("0.5"), 3);
        EXECUTION_COUNT_BY_VERSION.put(new ModuleVersionNumber("1.0"), 6);
        EXECUTION_COUNT_BY_VERSION.put(new ModuleVersionNumber("1.5"), 3);
        EXECUTION_COUNT_BY_VERSION.put(new ModuleVersionNumber("2.0"), 6);
        EXECUTION_COUNT_BY_VERSION.put(new ModuleVersionNumber("2.5"), 3);
        EXECUTION_COUNT_BY_VERSION.put(new ModuleVersionNumber("3.0"), 3);
        EXECUTION_COUNT_BY_VERSION.put(new ModuleVersionNumber("3.5"), 0);
    };
    
    private RegistryService registryService;
    private TenantAdminService tenantAdminService;
    private DescriptorService descriptorService;
    private DummyModuleService moduleService;
    private ModuleComponentHelper helper;
    
    private DummyModuleComponent[][] components;
    
    public void setUp() throws Exception
    {
        super.setUp();
        
        registryService = (RegistryService) ctx.getBean("RegistryService");
        tenantAdminService = (TenantAdminService) ctx.getBean("tenantAdminService");
        
        descriptorService = serviceRegistry.getDescriptorService();
        
        moduleService = new DummyModuleService();
        helper = new ModuleComponentHelper();
        helper.setModuleService(moduleService);
        helper.setRegistryService(registryService);
        helper.setServiceRegistry(serviceRegistry);
        helper.setDescriptorService(descriptorService);
        helper.setTenantAdminService(tenantAdminService);
        
        // Register the components
        components = new DummyModuleComponent[3][3];    // i,j
        for (int i = 0; i < 3; i++)                     // i = module number
        {
            for (int j = 0; j < 3; j++)                 // j = component number
            {
                DummyModuleComponent component = new DummyModuleComponent(
                        MODULE_IDS[i],
                        COMPONENT_NAMES[j],
                        VERSIONS[j],
                        VERSIONS[j+1]);
                component.setServiceRegistry(serviceRegistry);
                component.setAuthenticationComponent(authenticationComponent);
                component.setModuleService(moduleService);
                component.setTenantAdminService(tenantAdminService);
                // Don't initialize the component as that will do the registration.  We do it manually.
                helper.registerComponent(component);
                // Add to array
                components[i][j] = component;
            }
        }
        // M1-C1 depends on M0-C1
        components[1][1].setDependsOn(Collections.<ModuleComponent>singletonList(components[0][1]));
    }
    
    public void testSetup() throws Exception
    {
        // See that it all starts OK
    }
    
    private void startComponents(ModuleVersionNumber moduleVersion)
    {
        int expectedCount = (Integer) EXECUTION_COUNT_BY_VERSION.get(moduleVersion);
        // Set the current version number for all modules
        moduleService.setCurrentVersion(moduleVersion);
        // Start them
        helper.startModules();

// ALF-19207: MT module startup does not work
//        int tenantCount = 0;
//        if (tenantDeployerService.isEnabled())
//        {
//        	tenantCount = tenantDeployerService.getTenants(true).size();
//        }
//        // Check
//        assertEquals(ModuleVersionNumber
//                "Incorrent number of executions (version " + moduleVersion + ")",
//                expectedCount + (expectedCount * tenantCount),
//                executed);
        // Check
        assertEquals("Incorrent number of executions (version " + moduleVersion + ")", expectedCount, executed);
    }
    
    public void testStartComponentsV00()
    {
        ModuleVersionNumber moduleVersion = new ModuleVersionNumber("0.0");
        startComponents(moduleVersion);
    }
    
    public void testStartComponentsV05()
    {
        ModuleVersionNumber moduleVersion = new ModuleVersionNumber("0.5");
        startComponents(moduleVersion);
    }
    
    public void testStartComponentsV10()
    {
        ModuleVersionNumber moduleVersion = new ModuleVersionNumber("1.0");
        startComponents(moduleVersion);
    }
    
    public void testStartComponentsV15()
    {
        ModuleVersionNumber moduleVersion = new ModuleVersionNumber("1.5");
        startComponents(moduleVersion);
    }
    
    public void testStartComponentsV30()
    {
        ModuleVersionNumber moduleVersion = new ModuleVersionNumber("3.0");
        startComponents(moduleVersion);
    }
    
    public void testStartComponentsV35()
    {
        ModuleVersionNumber moduleVersion = new ModuleVersionNumber("3.5");
        startComponents(moduleVersion);
    }
    
	public void testgetModuleVersionNumber() {
		ModuleVersionNumber moduleVersion = new ModuleVersionNumber("3.5");
		VersionNumber versNumber = new VersionNumber("3.5");
		assertEquals(moduleVersion, helper.getModuleVersionNumber(moduleVersion));
		assertEquals(moduleVersion, helper.getModuleVersionNumber(versNumber));

		try {
			helper.getModuleVersionNumber(null);
			assertTrue(false); //should never get here
		} catch (ModuleManagementToolException e) {
			//should get here
		}
//		try {
//			helper.getModuleVersionNumber("any object");
//			assertTrue(false); //should never get here
//		} catch (ModuleManagementToolException e) {
//			//should get here
//		}
//		assertTrue(true);
	}
    /**
     * Helper bean to simulate module presences under controlled conditions.
     */
    private class DummyModuleService implements ModuleService
    {
        private ModuleVersionNumber currentVersion;
        /** Set the current version of all the modules */
        public void setCurrentVersion(ModuleVersionNumber currentVersion)
        {
            this.currentVersion = currentVersion;
        }

        public void registerComponent(ModuleComponent component)
        {
            throw new UnsupportedOperationException();
        }

        public List<ModuleDetails> getAllModules()
        {
            // Reset the execution count
            executed = 0;
            // Create some module details
            List<ModuleDetails> details = new ArrayList<ModuleDetails>(3);
            for (int i = 0; i < 3; i++)
            {
                ModuleDetails moduleDetails = new ModuleDetailsImpl(
                        MODULE_IDS[i],
                        currentVersion,
                        "Module-" + i,
                        "Description-" + i);
                details.add(moduleDetails);
            }
            // Done
            return details; 
        }

        public ModuleDetails getModule(String moduleId)
        {
            for (int i = 0; i < MODULE_IDS.length; i++)
            {
                if (!MODULE_IDS[i].equals(moduleId))
                {
                    continue;
                }
                new ModuleDetailsImpl(
                        MODULE_IDS[i],
                        currentVersion,
                        "Module-" + i,
                        "Description-" + i);
            }
            return null;
        }

        public void startModules()
        {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public List<ModuleDetails> getMissingModules()
        {
            // Create some module details
            List<ModuleDetails> details = new ArrayList<ModuleDetails>(3);
            for (int i = 0; i < 3; i++)
            {
                ModuleDetails moduleDetails = new ModuleDetailsImpl(
                        MODULE_IDS[i],
                        currentVersion,
                        "",
                        "");
                details.add(moduleDetails);
            }
            // Done
            return details; 
        }
        
        public void shutdownModules()
        {
            throw new UnsupportedOperationException();
        }
    }
    
    /** Keep track of the execution count */
    private volatile int executed = 0;
    
    /**
     * A dummy 
     * @author Derek Hulley
     */
    private class DummyModuleComponent extends AbstractModuleComponent
    {
        private DummyModuleComponent(String moduleId, String name, VersionNumber from, VersionNumber to)
        {
            super.setServiceRegistry(serviceRegistry);
            super.setAuthenticationComponent(authenticationComponent);
            super.setModuleService(moduleService);
            super.setTenantAdminService(tenantAdminService);
            
            super.setModuleId(moduleId);
            super.setName(name);
            super.setAppliesFromVersion(from.toString());
            super.setAppliesToVersion(to.toString());
            super.setSinceVersion("10.1.2");
            
            super.setDescription("A dummy module component");
        }
        
        @Override
        protected void executeInternal() throws Throwable
        {
            // Record execution
            executed++;
        }
    }

    /** No-operation tester class */
    public static class NoopModuleComponent extends AbstractModuleComponent
    {
        @Override
        protected void executeInternal() throws Throwable
        {
        }
    }
}
