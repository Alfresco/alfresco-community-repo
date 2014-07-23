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
package org.alfresco.repo.management.subsystems;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

/**
 * Tests for the {@link CryptodocSwitchableApplicationContextFactory} class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class CryptodocSwitchableApplicationContextFactoryTest
{
    private static final String UNENCRYPTED_STORE_SUBSYSTEM = "unencrypted";
    private static final String ENCRYPTED_STORE_SUBSYSTEM = "encrypted";
    private static final String SOURCE_BEAN_NAME_PROPERTY = "sourceBeanName";
    
    // The class under test
    private CryptodocSwitchableApplicationContextFactory switchableContext;
    private @Mock PropertyBackedBeanRegistry propertyBackedBeanRegistry;
    private @Mock ApplicationContext parentContext;

    @Before
    public void setUp() throws Exception
    {
        switchableContext = new CryptodocSwitchableApplicationContextFactory();
        
        when(parentContext.containsBean(ENCRYPTED_STORE_SUBSYSTEM)).thenReturn(true);
        when(parentContext.containsBean(UNENCRYPTED_STORE_SUBSYSTEM)).thenReturn(true);
    }
    
    private void initSwitchableContext(String sourceBeanName)
    {
        switchableContext.setSourceBeanName(sourceBeanName);
        switchableContext.setPropertyDefaults(new Properties());
        switchableContext.setUnencryptedContentStoreBeanName(UNENCRYPTED_STORE_SUBSYSTEM);
        switchableContext.setRegistry(propertyBackedBeanRegistry);
        switchableContext.setApplicationContext(parentContext);
        switchableContext.init();
    }
    
    @Test
    public void sourceBeanIsUpdateableWhenCurrentStoreIsUnencrypted()
    {
        initSwitchableContext(UNENCRYPTED_STORE_SUBSYSTEM);
        boolean updateable = switchableContext.isUpdateable(SOURCE_BEAN_NAME_PROPERTY);
        assertTrue("It should be possible to switch subsystems when the current store is unencrypted.", updateable);
    }

    @Test
    public void canSetSourceBeanWhenCurrentStoreIsUnencrypted()
    {
        initSwitchableContext(UNENCRYPTED_STORE_SUBSYSTEM);
        switchableContext.setProperty(SOURCE_BEAN_NAME_PROPERTY, ENCRYPTED_STORE_SUBSYSTEM);
        assertEquals(ENCRYPTED_STORE_SUBSYSTEM, switchableContext.getProperty(SOURCE_BEAN_NAME_PROPERTY));
    }
    
    @Test
    public void sourceBeanIsNotUpdatableWhenCurrentStoreIsEncrypted()
    {
        initSwitchableContext(ENCRYPTED_STORE_SUBSYSTEM);
        boolean updateable = switchableContext.isUpdateable(SOURCE_BEAN_NAME_PROPERTY);
        assertFalse("It should not be possible to switch subsystems when the current store is encrypted.", updateable);
    }
    
    @Test
    public void cannotSetSourceBeanWhenCurrentStoreIsEncrypted()
    {
        initSwitchableContext(ENCRYPTED_STORE_SUBSYSTEM);
        try
        {
            switchableContext.setProperty(SOURCE_BEAN_NAME_PROPERTY, UNENCRYPTED_STORE_SUBSYSTEM);
            fail("It shouldn't be possible to switch to an unencrypted content store from an encrypted one.");
        }
        catch (IllegalStateException e)
        {
            // Good
        }
        assertEquals(ENCRYPTED_STORE_SUBSYSTEM, switchableContext.getProperty(SOURCE_BEAN_NAME_PROPERTY));
    }

}
