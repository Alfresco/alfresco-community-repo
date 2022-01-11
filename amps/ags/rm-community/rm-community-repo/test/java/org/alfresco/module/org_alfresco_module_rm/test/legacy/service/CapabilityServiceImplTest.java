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

                Group recordsGroup = groups.get(0);
                assertNotNull(recordsGroup);

                List<Capability> recordCapabilities = capabilityService.getCapabilitiesByGroup(recordsGroup);
                assertNotNull(recordCapabilities);

                int recordCapabilitiesSize = recordCapabilities.size();
                assertTrue(recordCapabilitiesSize > 1);

                for (int i = 1; i == recordCapabilitiesSize; i++)
                {
                    Capability capability = recordCapabilities.get(i);
                    assertNotNull(capability);
                    assertEquals(i * 10, capability.getIndex());
                }

                Group rulesGroup = groups.get(groups.size() - 2);
                assertNotNull(rulesGroup);

                List<Capability> ruleCapabilities = capabilityService.getCapabilitiesByGroupId(rulesGroup.getId());
                assertNotNull(ruleCapabilities);

                int ruleCapabilitiesSize = ruleCapabilities.size();
                assertTrue(ruleCapabilitiesSize > 0);

                return null;
            }
        });
    }
}
