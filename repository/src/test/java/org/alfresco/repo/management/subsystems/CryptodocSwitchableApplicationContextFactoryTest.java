/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.management.subsystems;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Properties;

import org.alfresco.repo.descriptor.DescriptorServiceAvailableEvent;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

/**
 * Tests for the {@link CryptodocSwitchableApplicationContextFactory} class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class CryptodocSwitchableApplicationContextFactoryTest
{
    private static final String UNENCRYPTED_STORE_SUBSYSTEM = "unencryptedContentStore";
    private static final String NEW_UNENCRYPTED_STORE_SUBSYSTEM = "newUnencryptedContentStore";
    private static final String ENCRYPTED_STORE_SUBSYSTEM = "encryptedContentStore";
    private static final String NEW_STORE_SUBSYSTEM = "newContentStore";
    private static final String UNKNOWN_STORE_SUBSYSTEM = "unknownBean";
    
    private static final String SOURCE_BEAN_NAME_PROPERTY = "sourceBeanName";
    
    // The class under test
    private CryptodocSwitchableApplicationContextFactory switchableContext;
    private @Mock ChildApplicationContextFactory unencrytedContentStore;
    private @Mock ChildApplicationContextFactory newUnencrytedContentStore;
    private @Mock ChildApplicationContextFactory cryptodocContentStore;
    private @Mock EncryptedContentStoreChildApplicationContextFactory newContentStore;
    
    private @Mock PropertyBackedBeanRegistry propertyBackedBeanRegistry;
    private @Mock ApplicationContext parentContext;
    private @Mock DescriptorService descriptorService;
    private @Mock LicenseDescriptor licenseDescriptor;

    @Before
    public void setUp() throws Exception
    {
        switchableContext = new CryptodocSwitchableApplicationContextFactory();

        when(parentContext.getBean(UNENCRYPTED_STORE_SUBSYSTEM)).thenReturn(unencrytedContentStore);
        when(parentContext.getBean(UNENCRYPTED_STORE_SUBSYSTEM, ChildApplicationContextFactory.class)).thenReturn(unencrytedContentStore);

        when(parentContext.containsBean(NEW_UNENCRYPTED_STORE_SUBSYSTEM)).thenReturn(true);
        when(parentContext.getBean(NEW_UNENCRYPTED_STORE_SUBSYSTEM, ChildApplicationContextFactory.class)).thenReturn(newUnencrytedContentStore);
        
        when(parentContext.containsBean(ENCRYPTED_STORE_SUBSYSTEM)).thenReturn(true);
        when(parentContext.getBean(ENCRYPTED_STORE_SUBSYSTEM)).thenReturn(cryptodocContentStore);
        when(parentContext.getBean(ENCRYPTED_STORE_SUBSYSTEM, ChildApplicationContextFactory.class)).thenReturn(cryptodocContentStore);
        
        when(parentContext.containsBean(NEW_STORE_SUBSYSTEM)).thenReturn(true);
        when(parentContext.getBean(NEW_STORE_SUBSYSTEM)).thenReturn(newContentStore);
        when(parentContext.getBean(NEW_STORE_SUBSYSTEM, ChildApplicationContextFactory.class)).thenReturn(newContentStore);

        when(parentContext.containsBean(UNKNOWN_STORE_SUBSYSTEM)).thenReturn(false);
    }
    
    private void initSwitchableContext(String sourceBeanName)
    {
        switchableContext.setSourceBeanName(sourceBeanName);
        switchableContext.setPropertyDefaults(new Properties());
        switchableContext.setRegistry(propertyBackedBeanRegistry);
        switchableContext.setApplicationContext(parentContext);
        switchableContext.init();
    }

    @Test
    public void canSwitchFromUnencryptedToUnencrypted()
    {
        initSwitchableContext(UNENCRYPTED_STORE_SUBSYSTEM);
        
        switchableContext.setProperty(SOURCE_BEAN_NAME_PROPERTY, NEW_UNENCRYPTED_STORE_SUBSYSTEM);
        assertEquals(NEW_UNENCRYPTED_STORE_SUBSYSTEM, switchableContext.getProperty(SOURCE_BEAN_NAME_PROPERTY));
    }

    @Test
    public void canSwitchFromUnencryptedToEncrypted_NoLicenseInfo()
    {
        initSwitchableContext(UNENCRYPTED_STORE_SUBSYSTEM);
        
        switchableContext.setProperty(SOURCE_BEAN_NAME_PROPERTY, ENCRYPTED_STORE_SUBSYSTEM);
        assertEquals(ENCRYPTED_STORE_SUBSYSTEM, switchableContext.getProperty(SOURCE_BEAN_NAME_PROPERTY));
    }
    
    @Test
    public void canSwitchFromUnencryptedToEncrypted_Supported()
    {
        initSwitchableContext(UNENCRYPTED_STORE_SUBSYSTEM);
        
        DescriptorServiceAvailableEvent event = new DescriptorServiceAvailableEvent(descriptorService);
        when(descriptorService.getLicenseDescriptor()).thenReturn(licenseDescriptor);
        when(licenseDescriptor.isCryptodocEnabled()).thenReturn(true);
        switchableContext.onApplicationEvent(event);
                
        switchableContext.setProperty(SOURCE_BEAN_NAME_PROPERTY, ENCRYPTED_STORE_SUBSYSTEM);
        assertEquals(ENCRYPTED_STORE_SUBSYSTEM, switchableContext.getProperty(SOURCE_BEAN_NAME_PROPERTY));
    }

    @Test
    public void canSwitchFromEncryptedToEncrypted_NoLicenseInfo()
    {
        initSwitchableContext(ENCRYPTED_STORE_SUBSYSTEM);
        
        when(newContentStore.isEncryptedContent()).thenReturn(true);
        switchableContext.setProperty(SOURCE_BEAN_NAME_PROPERTY, NEW_STORE_SUBSYSTEM);
        assertEquals(NEW_STORE_SUBSYSTEM, switchableContext.getProperty(SOURCE_BEAN_NAME_PROPERTY));
    }

    @Test
    public void canSwitchFromNewEncryptedToEncrypted_NoLicenseInfo()
    {
        initSwitchableContext(NEW_STORE_SUBSYSTEM);

        when(newContentStore.isEncryptedContent()).thenReturn(true);
        switchableContext.setProperty(SOURCE_BEAN_NAME_PROPERTY, ENCRYPTED_STORE_SUBSYSTEM);
        assertEquals(ENCRYPTED_STORE_SUBSYSTEM, switchableContext.getProperty(SOURCE_BEAN_NAME_PROPERTY));
    }

    @Test
    public void cannotSwitchFromUnencryptedToEncrypted_NotSupported()
    {
        initSwitchableContext(UNENCRYPTED_STORE_SUBSYSTEM);
        
        DescriptorServiceAvailableEvent event = new DescriptorServiceAvailableEvent(descriptorService);
        when(descriptorService.getLicenseDescriptor()).thenReturn(licenseDescriptor);
        when(licenseDescriptor.isCryptodocEnabled()).thenReturn(false);
        switchableContext.onApplicationEvent(event);
        try
        {
            switchableContext.setProperty(SOURCE_BEAN_NAME_PROPERTY, ENCRYPTED_STORE_SUBSYSTEM);
            fail("It shouldn't be possible to switch to an encrypted content store when the license doesn't support it.");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
        // The content store didn't change
        assertEquals(UNENCRYPTED_STORE_SUBSYSTEM, switchableContext.getProperty(SOURCE_BEAN_NAME_PROPERTY));
    }
    
    @Test
    public void cannotSwitchFromEncryptedToUnencrypted()
    {
        initSwitchableContext(ENCRYPTED_STORE_SUBSYSTEM);
        try
        {
            switchableContext.setProperty(SOURCE_BEAN_NAME_PROPERTY, UNENCRYPTED_STORE_SUBSYSTEM);
            fail("It shouldn't be possible to switch to an unencrypted content store from an encrypted one.");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
        // The content store didn't change
        assertEquals(ENCRYPTED_STORE_SUBSYSTEM, switchableContext.getProperty(SOURCE_BEAN_NAME_PROPERTY));
    }

    @Test
    public void cannotSwitchFromEncryptedToNewUnencrypted()
    {
        initSwitchableContext(ENCRYPTED_STORE_SUBSYSTEM);
        
        when(newContentStore.isEncryptedContent()).thenReturn(false);
        try
        {
            switchableContext.setProperty(SOURCE_BEAN_NAME_PROPERTY, NEW_STORE_SUBSYSTEM);
            fail("It shouldn't be possible to switch to an unencrypted content store from an encrypted one.");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
        // The content store didn't change
        assertEquals(ENCRYPTED_STORE_SUBSYSTEM, switchableContext.getProperty(SOURCE_BEAN_NAME_PROPERTY));
    }
    
    @Test
    public void sourceBeanIsNotUpdatableToUnknownBean()
    {
        initSwitchableContext(UNENCRYPTED_STORE_SUBSYSTEM);
        try
        {
            switchableContext.setProperty(SOURCE_BEAN_NAME_PROPERTY, UNKNOWN_STORE_SUBSYSTEM);
            fail("It shouldn't be possible to set the sourceBean to an unknown one.");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
        // The content store didn't change
        assertEquals(UNENCRYPTED_STORE_SUBSYSTEM, switchableContext.getProperty(SOURCE_BEAN_NAME_PROPERTY));
    }
}
