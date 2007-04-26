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
package org.alfresco.repo.module;

import java.util.Properties;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleInstallState;
import org.alfresco.util.VersionNumber;

import junit.framework.TestCase;

/**
 * @see org.alfresco.repo.module.ModuleDetailsImpl
 * 
 * @author Derek Hulley
 */
public class ModuleDetailsImplTest extends TestCase
{
    private Properties defaultProperties;
    
    @Override
    protected void setUp() throws Exception
    {
        defaultProperties = new Properties();
        defaultProperties.setProperty(ModuleDetails.PROP_ID, "org.alfresco.module.Test");
        defaultProperties.setProperty(ModuleDetails.PROP_ALIASES, "test, Test");
        defaultProperties.setProperty(ModuleDetails.PROP_TITLE, "Test");
        defaultProperties.setProperty(ModuleDetails.PROP_DESCRIPTION, "Test description");
        defaultProperties.setProperty(ModuleDetails.PROP_VERSION, "1.0.0");
        defaultProperties.setProperty(ModuleDetails.PROP_REPO_VERSION_MIN, new VersionNumber("1.2").toString());
        defaultProperties.setProperty(ModuleDetails.PROP_REPO_VERSION_MAX, new VersionNumber("1.4.3").toString());
        defaultProperties.setProperty(ModuleDetails.PROP_INSTALL_STATE, ModuleInstallState.INSTALLED.toString());
    }

    @SuppressWarnings("unused")
    public void testDefaults()
    {
         ModuleDetails details = new ModuleDetailsImpl(defaultProperties);
    }
    
    public void testWriteAndReadProperties()
    {
        ModuleDetails details = new ModuleDetailsImpl(defaultProperties);
        // convert back to properties
        Properties processedProperties = details.getProperties();
        assertEquals("The number of properties changed", defaultProperties.size(), processedProperties.size());
        assertEquals("The properties are different", defaultProperties, processedProperties);
    }
    
    public void testTrimming() throws Exception
    {
        defaultProperties.setProperty(ModuleDetails.PROP_INSTALL_STATE, "    ");
        ModuleDetails details = new ModuleDetailsImpl(defaultProperties);
        assertEquals("Expected the install state to be UNKNOWN", ModuleInstallState.UNKNOWN, details.getInstallState());
    }
    
    public void testInvalidIds() throws Exception
    {
        String[] invalidIds = new String[] {"", " ", "$", "module$Test", "module.Test$", "org alfresco module Test"};
        for (String invalidId : invalidIds)
        {
            try
            {
                defaultProperties.setProperty(ModuleDetails.PROP_ID, invalidId);
                new ModuleDetailsImpl(defaultProperties);
                fail("Invalid ID not detected: " + invalidId);
            }
            catch (AlfrescoRuntimeException e)
            {
                // Expected
            }
        }
    }
    
    public void testValidIds() throws Exception
    {
        String[] validIds = new String[] {"abc123", " abc123 ", "a-b-c", "a.b.c", "a_b_c", "A.1.2.3"};
        for (String validId : validIds)
        {
            defaultProperties.setProperty(ModuleDetails.PROP_ID, validId);
            new ModuleDetailsImpl(defaultProperties);
        }
    }
}
