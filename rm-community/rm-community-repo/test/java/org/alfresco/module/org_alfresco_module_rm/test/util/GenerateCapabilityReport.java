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
