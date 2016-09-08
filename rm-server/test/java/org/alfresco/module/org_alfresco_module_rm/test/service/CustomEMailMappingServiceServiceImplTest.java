/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.email.CustomEmailMappingService;
import org.alfresco.module.org_alfresco_module_rm.email.CustomMapping;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;

/**
 * Custom EMail Mapping Service
 * 
 * @author Roy Wetherall
 * @since 2.0
 */
public class CustomEMailMappingServiceServiceImplTest extends BaseRMTestCase
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
    
    public void testCRUD() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                checkCustomMappingSize(20);
                
                eMailMappingService.addCustomMapping("monkey", "cm:monkeyFace");
                
                CustomMapping monkeyMapping = getCustomMapping("monkey", "cm:monkeyFace", checkCustomMappingSize(21), true);
                assertNotNull(monkeyMapping);
                assertEquals("monkey", monkeyMapping.getFrom());
                assertEquals("cm:monkeyFace", monkeyMapping.getTo());
                
                eMailMappingService.deleteCustomMapping("monkey", "cm:monkeyFace");
                getCustomMapping("monkey", "cm:monkeyFace", checkCustomMappingSize(20), false);                
                
                return null;
            }
        }, rmAdminName);
    }
    
    private CustomMapping getCustomMapping(String from, String to, Set<CustomMapping> maps, boolean contains)
    {
        CustomMapping result = null;
        CustomMapping key = new CustomMapping(from, to);
        assertEquals(contains, maps.contains(key));
        
        if (contains == true)
        {            
            for (CustomMapping map : maps)
            {
                if (map.equals(key) == true)
                {
                    result = key;
                    break;
                }
            }            
        }
        return result;
    }
    
    private Set<CustomMapping> checkCustomMappingSize(int expected)
    {
        Set<CustomMapping> maps = eMailMappingService.getCustomMappings();
        assertEquals(expected, maps.size());
        return maps;
    }
    
    @SuppressWarnings("unused")
    private void print(Set<CustomMapping> maps)
    {
        for (CustomMapping map : maps)
        {
            System.out.println(map.getFrom() + " -> " + map.getTo());
        }
    }
}
