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

package org.alfresco.module.org_alfresco_module_rm.test.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.Group;

/**
 * Utility test case to generate a report of the capabilities in the system.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class GenerateCapabilityReport extends BaseRMTestCase
{
    public void testGetCapability() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                FileWriter writer = new FileWriter("c:\\mywork\\capabilityReport.csv");
                BufferedWriter out = new BufferedWriter(writer);
                try
                {
                    Set<Capability> capabilities = capabilityService.getCapabilities(true);
                    for (Capability capability : capabilities)
                    {
                        Group group = capability.getGroup();
                        String groupId = "none";
                        if (group != null)
                        {
                            groupId = group.getId();
                        }

                        out.write(groupId);
                        out.write(",");
                        out.write(capability.getName());
                        out.write(",");
                        out.write(Boolean.toString(capability.isPrivate()));
                        out.write("\n");
                    }
                }
                finally
                {
                    out.close();
                }

                return null;
            }
        });
    }
}
