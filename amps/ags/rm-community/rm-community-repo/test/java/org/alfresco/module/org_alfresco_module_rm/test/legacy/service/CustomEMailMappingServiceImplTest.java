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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.email.CustomEmailMappingService;
import org.alfresco.module.org_alfresco_module_rm.email.CustomMapping;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;

/**
 * Custom EMail Mapping Service
 *
 * @author Roy Wetherall
 * @since 2.0
 */
public class CustomEMailMappingServiceImplTest extends BaseRMTestCase
{
    private CustomEmailMappingService eMailMappingService;

    @Override
    protected void initServices()
    {
        super.initServices();

        eMailMappingService = (CustomEmailMappingService)applicationContext.getBean("customEmailMappingService");
    }

    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        eMailMappingService.registerEMailMappingKey("EmailMappingKeyTest1");
        eMailMappingService.registerEMailMappingKey("EmailMappingKeyTest2");
    }

    public void testCRUD() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                // Check the initial custom mapping size
                assertTrue(checkCustomMappingsSize(20));

                String firstKey = eMailMappingService.getEmailMappingKeys().get(0);

                // Add a custom mapping
                eMailMappingService.addCustomMapping(firstKey, "cm:monkeyFace");

                // Check the new size
                assertTrue(checkCustomMappingsSize(21));

                // Check the new added custom mapping
                CustomMapping monkeyMapping = getCustomMapping(firstKey, "cm:monkeyFace");
                assertNotNull(monkeyMapping);
                assertEquals(firstKey, monkeyMapping.getFrom());
                assertEquals("cm:monkeyFace", monkeyMapping.getTo());

                // Delete the new added custom mapping
                eMailMappingService.deleteCustomMapping(firstKey, "cm:monkeyFace");

                // Check the size after deletion
                assertTrue(checkCustomMappingsSize(20));

                // Check the custom mapping after deletion if it exists
                assertNull(getCustomMapping(firstKey, "cm:monkeyFace"));

                // Check the email mapping keys size
                // There are 6 "standard" EmailMappingKeys + 2 CustomEmailMappingKeys are added on setUp
                assertTrue(checkEmailMappingKeysSize(8));

                try
                {
                    eMailMappingService.addCustomMapping(" ", "cm:monkeyFace");
                    fail("Should not get here. Invalid data.");
                }
                catch (AlfrescoRuntimeException are)
                {
                    assertNotNull(are);  //Must throw this exception
                    assertTrue(are.getMessage().contains("Invalid values for"));
                }

                try
                {
                    eMailMappingService.addCustomMapping("monkey", " ");
                    fail("Should not get here. Invalid data.");
                }
                catch (AlfrescoRuntimeException are)
                {
                    assertNotNull(are);  //Must throw this exception
                    assertTrue(are.getMessage().contains("Invalid values for"));
                }

                eMailMappingService.addCustomMapping(firstKey, "cm:monkeyFace"); //valid key

                return null;
            }
        }, ADMIN_USER);
    }

    private CustomMapping getCustomMapping(String from, String to)
    {
        CustomMapping result = null;
        for (CustomMapping customMapping : eMailMappingService.getCustomMappings())
        {
            if (customMapping.getFrom().equalsIgnoreCase(from) && customMapping.getTo().equalsIgnoreCase(to))
            {
                result = customMapping;
                break;
            }
        }
        return result;
    }

    private boolean checkCustomMappingsSize(int expected)
    {
        return expected == eMailMappingService.getCustomMappings().size();
    }

    private boolean checkEmailMappingKeysSize(int expected)
    {
        return expected == eMailMappingService.getEmailMappingKeys().size();
    }
}
