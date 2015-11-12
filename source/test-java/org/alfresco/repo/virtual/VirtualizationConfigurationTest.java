/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual;

import junit.framework.TestCase;

import org.alfresco.util.ApplicationContextHelper;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Checks test configurations.
 * 
 * @author Bogdan Horje
 */
public class VirtualizationConfigurationTest extends TestCase implements VirtualizationTest
{

    protected static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext(CONFIG_LOCATIONS);

    private VirtualizationConfigTestBootstrap virtualizationConfigTestBootstrap;

    @Override
    protected void setUp() throws Exception
    {
        virtualizationConfigTestBootstrap = (VirtualizationConfigTestBootstrap) ctx
                    .getBean(VIRTUALIZATION_CONFIG_TEST_BOOTSTRAP_BEAN_ID,
                             VirtualizationConfigTestBootstrap.class);
    }

    @Test
    public void testVirtualizationEnabled() throws Exception
    {
        assertTrue("Virtual folders are disabled!",
                   virtualizationConfigTestBootstrap.areVirtualFoldersEnabled());
    }
}
