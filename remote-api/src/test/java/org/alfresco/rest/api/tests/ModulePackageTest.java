/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import org.alfresco.repo.module.ModuleDetailsImpl;
import org.alfresco.repo.module.ModuleDetailsImplTest;
import org.alfresco.rest.api.model.ModulePackage;
import org.alfresco.service.cmr.module.ModuleDetails;

/**
 * Basic tests for taking ModuleDetailsImpl and representing it as a ModulePackage class.
 * 
 * @author Gethin James.
 */
public class ModulePackageTest
{
    static Properties props = ModuleDetailsImplTest.DEFAULT_PROPS;

    @Test
    public void testConstructor() throws IOException
    {
        ModuleDetails details = new ModuleDetailsImpl(props);
        ModulePackage modulePackage = ModulePackage.fromModuleDetails(details);
        assertNotNull(modulePackage);
        assertEquals(props.getProperty(ModuleDetails.PROP_ID), modulePackage.getId());
        assertEquals(props.getProperty(ModuleDetails.PROP_VERSION), modulePackage.getVersion());
        assertEquals(props.getProperty(ModuleDetails.PROP_REPO_VERSION_MIN), modulePackage.getVersionMin());
        assertEquals(props.getProperty(ModuleDetails.PROP_REPO_VERSION_MAX), modulePackage.getVersionMax());
        assertEquals(props.getProperty(ModuleDetails.PROP_INSTALL_STATE), modulePackage.getInstallState().toString());
    }

    @Test
    public void testInvalidConstructor() throws IOException
    {
        ModulePackage modulePackage = ModulePackage.fromModuleDetails(null);
        assertNull(modulePackage);
    }
}
