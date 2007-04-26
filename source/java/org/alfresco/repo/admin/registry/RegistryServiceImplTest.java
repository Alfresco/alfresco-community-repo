/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin.registry;

import java.util.Collection;

import junit.framework.TestCase;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.admin.registry.RegistryService
 * 
 * @author Derek Hulley
 */
public class RegistryServiceImplTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private AuthenticationComponent authenticationComponent;
    private RegistryService registryService;
    
    @Override
    protected void setUp() throws Exception
    {
        authenticationComponent = (AuthenticationComponent) ctx.getBean("AuthenticationComponent");
        registryService = (RegistryService) ctx.getBean("RegistryService");
        
        // Run as system user
        authenticationComponent.setSystemUserAsCurrentUser();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        // Clear authentication
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    public void testSetup() throws Exception
    {
    }

    private static final Long VALUE_ONE = 1L;
    private static final Long VALUE_TWO = 2L;
    private static final Long VALUE_THREE = 3L;
    private static final RegistryKey KEY_A = new RegistryKey(null, "a", null);
    private static final RegistryKey KEY_A_B = new RegistryKey(null, "a", "b", null);
    private static final RegistryKey KEY_A_B_0 = new RegistryKey(null, "a", "b", "0");
    private static final RegistryKey KEY_A_B_1 = new RegistryKey(null, "a", "b", "1");
    private static final RegistryKey KEY_A_B_C = new RegistryKey(null, "a", "b", "c", null);
    private static final RegistryKey KEY_A_B_C_0 = new RegistryKey(null, "a", "b", "c", "0");
    private static final RegistryKey KEY_A_B_C_1 = new RegistryKey(null, "a", "b", "c", "1");
    private static final RegistryKey KEY_A_B_C_2 = new RegistryKey(null, "a", "b", "c", "2");
    private static final RegistryKey KEY_A_B_C_3 = new RegistryKey(null, "a", "b", "c", "3");
    private static final RegistryKey KEY_A_B_C_D_0 = new RegistryKey(null, "a", "b", "c", "d", "0");
    private static final RegistryKey KEY_A_B_C_D_1 = new RegistryKey(null, "a", "b", "c", "d", "1");
    private static final RegistryKey KEY_A_B_C_D_2 = new RegistryKey(null, "a", "b", "c", "d", "2");
    private static final RegistryKey KEY_A_B_C_D_3 = new RegistryKey(null, "a", "b", "c", "d", "3");
    private static final RegistryKey KEY_X_Y_Z = new RegistryKey(null, "x", "y", "z", null);
    private static final RegistryKey KEY_X_Y_Z_0 = new RegistryKey(null, "x", "y", "z", "0");
    private static final RegistryKey KEY_X_Y_Z_1 = new RegistryKey(null, "x", "y", "z", "1");
    private static final RegistryKey KEY_X_Y_Z_2 = new RegistryKey(null, "x", "y", "z", "2");
    private static final RegistryKey KEY_X_Y_Z_D_1 = new RegistryKey(null, "x", "y", "z", "d", "1");
    private static final RegistryKey KEY_X_Y_Z_D_2 = new RegistryKey(null, "x", "y", "z", "d", "2");
    private static final RegistryKey KEY_SPECIAL = new RegistryKey(null, "me & you", "whatever");
    private static final RegistryKey KEY_DOES_NOT_EXIST = new RegistryKey(null, "does", "not", "exist");
    /**
     * General writing and reading back.
     */
    public void testProperUsage() throws Exception
    {
        registryService.addProperty(KEY_A_B_C_1, VALUE_ONE);
        registryService.addProperty(KEY_A_B_C_2, VALUE_TWO);
        registryService.addProperty(KEY_A_B_C_3, VALUE_THREE);
        registryService.addProperty(KEY_A_B_C_D_1, VALUE_ONE);
        registryService.addProperty(KEY_A_B_C_D_2, VALUE_TWO);
        registryService.addProperty(KEY_A_B_C_D_3, VALUE_THREE);
        
        assertEquals("Incorrect value from service registry", VALUE_ONE, registryService.getProperty(KEY_A_B_C_1));
        assertEquals("Incorrect value from service registry", VALUE_TWO, registryService.getProperty(KEY_A_B_C_2));
        assertEquals("Incorrect value from service registry", VALUE_THREE, registryService.getProperty(KEY_A_B_C_3));
        assertEquals("Incorrect value from service registry", VALUE_ONE, registryService.getProperty(KEY_A_B_C_D_1));
        assertEquals("Incorrect value from service registry", VALUE_TWO, registryService.getProperty(KEY_A_B_C_D_2));
        assertEquals("Incorrect value from service registry", VALUE_THREE, registryService.getProperty(KEY_A_B_C_D_3));
        
        assertNull("Missing key should return null value", registryService.getProperty(KEY_A_B_C_0));
        assertNull("Missing key should return null value", registryService.getProperty(KEY_A_B_C_D_0));
        assertNull("Missing key should return null value", registryService.getProperty(KEY_X_Y_Z_0));
    }
    
    public void testGetElements() throws Exception
    {
        registryService.addProperty(KEY_A_B_C_1, VALUE_ONE);
        registryService.addProperty(KEY_A_B_C_2, VALUE_TWO);
        
        // Check that we get an empty list for a random query
        assertEquals("Excpected empty collection for random query", 0, registryService.getChildElements(KEY_DOES_NOT_EXIST).size());
        
        // Check that the property part of the key is ignored
        assertEquals("Incorrect number ofchild elements", 1, registryService.getChildElements(KEY_A_B_0).size());
        assertEquals("Incorrect number ofchild elements", 1, registryService.getChildElements(KEY_A_B_1).size());
        
        Collection<String> childElements = registryService.getChildElements(KEY_A_B_0);
        assertTrue("Incorrect child elements retrieved", childElements.contains("c"));
    }
    
    public void testSpecialCharacters()
    {
        registryService.addProperty(KEY_SPECIAL, VALUE_THREE);
        assertEquals("Incorrect value for special key", VALUE_THREE, registryService.getProperty(KEY_SPECIAL));
    }
    
    public void testDelete()
    {
        registryService.addProperty(KEY_A_B_C_1, VALUE_ONE);
        registryService.addProperty(KEY_A_B_C_2, VALUE_TWO);
        // Safety check
        assertEquals("Incorrect value from service registry", VALUE_ONE, registryService.getProperty(KEY_A_B_C_1));
        assertEquals("Incorrect value from service registry", VALUE_TWO, registryService.getProperty(KEY_A_B_C_2));

        // Property-based delete
        registryService.delete(KEY_A_B_C_1);
        assertNull("Expected deleted value to be null", registryService.getProperty(KEY_A_B_C_1));
        
        // Path-based delete
        registryService.delete(KEY_A_B);
        assertNull("Expected deleted value to be null", registryService.getProperty(KEY_A_B_C_1));
        assertNull("Expected deleted value to be null", registryService.getProperty(KEY_A_B_C_2));

        Collection<String> childElements = registryService.getChildElements(KEY_A);
        assertEquals("There should be no more elements within A", 0, childElements.size());
    }
    
    public void testCopy()
    {
        registryService.addProperty(KEY_A_B_C_1, VALUE_ONE);
        registryService.addProperty(KEY_A_B_C_2, VALUE_TWO);
        registryService.addProperty(KEY_A_B_C_D_1, VALUE_ONE);
        registryService.addProperty(KEY_A_B_C_D_2, VALUE_TWO);
        
        // Check illegal copy
        try
        {
            registryService.copy(KEY_A_B_C_1, KEY_X_Y_Z);
            fail("Failed to detect copy from property to path");
        }
        catch (Throwable e)
        {
            // Expected
        }
        try
        {
            registryService.copy(KEY_A_B_C, KEY_X_Y_Z_0);
            fail("Failed to detect copy from path to property");
        }
        catch (Throwable e)
        {
            // Expected
        }
        
        // Property-based copy
        registryService.copy(KEY_A_B_C_1, KEY_X_Y_Z_0);
        assertEquals("Incorrect value after property copy", VALUE_ONE, registryService.getProperty(KEY_X_Y_Z_0));
        
        // Path-based copy
        registryService.copy(KEY_A_B_C, KEY_X_Y_Z);
        assertEquals("Value not recursively copied during path copy", VALUE_ONE, registryService.getProperty(KEY_X_Y_Z_1));
        assertEquals("Value not recursively copied during path copy", VALUE_TWO, registryService.getProperty(KEY_X_Y_Z_2));
        assertEquals("Path not recursively copied during path copy", VALUE_ONE, registryService.getProperty(KEY_X_Y_Z_D_1));
        assertEquals("Path not recursively copied during path copy", VALUE_TWO, registryService.getProperty(KEY_X_Y_Z_D_2));
    }
}
