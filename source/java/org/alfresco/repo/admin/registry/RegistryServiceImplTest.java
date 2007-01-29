/*
 * Copyright (C) 2007 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.admin.registry;

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
        
        // Run as admin
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
    private static final String KEY_A_B_C_1 = "/a/b/c/1";
    private static final String KEY_A_B_C_2 = "/a/b/c/2";
    private static final String KEY_A_B_C_3 = "/a/b/c/3";
    private static final String KEY_A_B_C_D_1 = "/a/b/c/d/1";
    private static final String KEY_A_B_C_D_2 = "/a/b/c/d/2";
    private static final String KEY_A_B_C_D_3 = "/a/b/c/d/3";
    private static final String KEY_SPECIAL = "/me & you/ whatever";
    /**
     * General writing and reading back.
     */
    public void testProperUsage() throws Exception
    {
        registryService.addValue(KEY_A_B_C_1, VALUE_ONE);
        registryService.addValue(KEY_A_B_C_2, VALUE_TWO);
        registryService.addValue(KEY_A_B_C_3, VALUE_THREE);
        registryService.addValue(KEY_A_B_C_D_1, VALUE_ONE);
        registryService.addValue(KEY_A_B_C_D_2, VALUE_TWO);
        registryService.addValue(KEY_A_B_C_D_3, VALUE_THREE);
        
        assertEquals("Incorrect value from service registry", VALUE_ONE, registryService.getValue(KEY_A_B_C_1));
        assertEquals("Incorrect value from service registry", VALUE_TWO, registryService.getValue(KEY_A_B_C_2));
        assertEquals("Incorrect value from service registry", VALUE_THREE, registryService.getValue(KEY_A_B_C_3));
        assertEquals("Incorrect value from service registry", VALUE_ONE, registryService.getValue(KEY_A_B_C_D_1));
        assertEquals("Incorrect value from service registry", VALUE_TWO, registryService.getValue(KEY_A_B_C_D_2));
        assertEquals("Incorrect value from service registry", VALUE_THREE, registryService.getValue(KEY_A_B_C_D_3));
        
        assertNull("Missing key should return null value", registryService.getValue("/a/b/c/0"));
        assertNull("Missing key should return null value", registryService.getValue("/a/b/c/d/0"));
        assertNull("Missing key should return null value", registryService.getValue("/x/y/z/0"));
    }
    
    public void testSpecialCharacters()
    {
        registryService.addValue(KEY_SPECIAL, VALUE_THREE);
        assertEquals("Incorrect value for special key", VALUE_THREE, registryService.getValue(KEY_SPECIAL));
    }
}
