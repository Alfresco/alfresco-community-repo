/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.module;

import java.util.List;
import java.util.Properties;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.module.ModuleDependency;
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
        defaultProperties.setProperty(ModuleDetails.PROP_EDITIONS, "Community, Enterprise");
        defaultProperties.setProperty(ModuleDetails.PROP_REPO_VERSION_MIN, new VersionNumber("1.2").toString());
        defaultProperties.setProperty(ModuleDetails.PROP_REPO_VERSION_MAX, new VersionNumber("1.4.3").toString());
        defaultProperties.setProperty(ModuleDetails.PROP_DEPENDS_PREFIX + "a", "1.2.3");
        defaultProperties.setProperty(ModuleDetails.PROP_DEPENDS_PREFIX + "b", "*");
        defaultProperties.setProperty(ModuleDetails.PROP_DEPENDS_PREFIX + "c", "- 1.2");
        defaultProperties.setProperty(ModuleDetails.PROP_DEPENDS_PREFIX + "d", "1.2 -");
        defaultProperties.setProperty(ModuleDetails.PROP_DEPENDS_PREFIX + "e", "* - 1.2");
        defaultProperties.setProperty(ModuleDetails.PROP_DEPENDS_PREFIX + "f", "1.2 - *");
        defaultProperties.setProperty(ModuleDetails.PROP_DEPENDS_PREFIX + "g", "0.5, 0.6");
        defaultProperties.setProperty(ModuleDetails.PROP_DEPENDS_PREFIX + "h", "0.5 - 0.6, 0.9 - *");
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
    
    public void testDependencyChecks()
    {
        ModuleDetails details = new ModuleDetailsImpl(defaultProperties);
        
        Properties tempProperties = new Properties();
        tempProperties.setProperty(ModuleDetails.PROP_ID, "a");
        tempProperties.setProperty(ModuleDetails.PROP_TITLE, "A");
        tempProperties.setProperty(ModuleDetails.PROP_DESCRIPTION, "A description");
        tempProperties.setProperty(ModuleDetails.PROP_VERSION, "1.0.0");
        ModuleDetails tempDetails = new ModuleDetailsImpl(tempProperties);
        
        List<ModuleDependency> dependencies = details.getDependencies();
        assertEquals("Incorrect number of dependencies", 8, dependencies.size());
        for (ModuleDependency dependency : dependencies)
        {
            if (dependency.getDependencyId().equals(tempDetails.getId()))
            {
                // It should not match
                assertFalse("No match expected", dependency.isValidDependency(tempDetails));
            }
        }
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
