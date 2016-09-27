/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.Group;
import org.alfresco.module.org_alfresco_module_rm.capability.GroupImpl;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;

/**
 * Test class for testing the methods in {@link CapabilityService}
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class CapabilityServiceImplTest extends BaseRMTestCase
{
    public void testGetCapability() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                // FIXME
                return null;
            }
        });
    }

    public void testGetCapabilities() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                // FIXME
                return null;
            }
        });
    }

    public void testGetCapabilityAccessState() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                // FIXME
                return null;
            }
        });
    }

    public void testGetCapabilitiesAccessState() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                // FIXME
                return null;
            }
        });
    }

    public void testGetAddRemoveGroups() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                Group auditGroup = capabilityService.getGroup("audit");
                assertNotNull(auditGroup);
                assertEquals(10, auditGroup.getIndex());
                assertEquals("Audit", auditGroup.getTitle());
                assertEquals("audit", auditGroup.getId());

                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                int initialSize = capabilityService.getGroups().size();
                
                GroupImpl testGroup = new GroupImpl();
                testGroup.setId("testGroup");
                testGroup.setIndex(140);
                testGroup.setTitle("Test group");
                capabilityService.addGroup(testGroup);

                assertEquals(initialSize+1, capabilityService.getGroups().size());

                Group group = capabilityService.getGroup("testGroup");
                assertNotNull(group);
                assertTrue(group.getId().equalsIgnoreCase("testGroup"));
                assertTrue(group.getTitle().equalsIgnoreCase("Test group"));
                assertTrue(group.getIndex() == 140);

                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                Group testGroup = capabilityService.getGroup("testGroup");
                assertNotNull(testGroup);
                int initialSize = capabilityService.getGroups().size();

                capabilityService.removeGroup(testGroup);
                assertEquals(initialSize-1, capabilityService.getGroups().size());

                return null;
            }
        });
    }

    public void testGetCapabilitiesByGroup() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                List<Group> groups = capabilityService.getGroups();
                assertNotNull(groups);

                Group auditGroup = groups.get(0);
                assertNotNull(auditGroup);

                List<Capability> auditCapabilities = capabilityService.getCapabilitiesByGroup(auditGroup);
                assertNotNull(auditCapabilities);

                int vitalRecordCapabilitiesSize = auditCapabilities.size();
                assertTrue(vitalRecordCapabilitiesSize > 1);

                for (int i = 1; i == vitalRecordCapabilitiesSize; i++)
                {
                    Capability capability = auditCapabilities.get(i);
                    assertNotNull(capability);
                    assertEquals(i * 10, capability.getIndex());
                }

                Group vitalRecordsGroup = groups.get(groups.size() - 1);
                assertNotNull(vitalRecordsGroup);

                List<Capability> vitalRecordCapabilities = capabilityService.getCapabilitiesByGroupId(vitalRecordsGroup.getId());
                assertNotNull(vitalRecordCapabilities);

                vitalRecordCapabilitiesSize = vitalRecordCapabilities.size();
                assertTrue(vitalRecordCapabilitiesSize > 0);

                return null;
            }
        });
    }
}
