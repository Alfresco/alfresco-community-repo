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
package org.alfresco.repo.management.subsystems.test;

import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseSpringTest;
import org.apache.cxf.endpoint.ServerRegistryImpl;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Ensures the following features of subsystems are working:
 * <ul>
 * <li>Subsystem default properties
 * <li>Global property overrides (via alfresco-global.properties)
 * <li>Subsystem instance specific property overrides (via extension classpath)
 * <li>Subsystem instance specific Spring overrides (via extension classpath)
 * <li>Composite property defaults
 * <li>Composite property instance overrides
 * </ul>
 * 
 * @see ChildApplicationContextFactory
 * @author dward
 */
public class SubsystemsTest extends BaseSpringTest
{

    /*
     * (non-Javadoc)
     * @see org.alfresco.util.BaseSpringTest#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations()
    {
        return new String[]
        {
            ApplicationContextHelper.CONFIG_LOCATIONS[0], "classpath:subsystem-test-context.xml"
        };
    }

    /**
     * Test subsystems.
     * 
     * @throws Exception
     *             the exception
     */
    public void testSubsystems() throws Exception
    {
        ApplicationContextFactory subsystem = (ApplicationContextFactory) getApplicationContext().getBean(
                "testsubsystem");
        ConfigurableApplicationContext childContext = (ConfigurableApplicationContext) subsystem
                .getApplicationContext();
        assertTrue("Subsystem not started", childContext.isActive());
        TestService testService = (TestService) childContext.getBean("testService");
        // Make sure subsystem defaults work
        assertEquals("Subsystem Default1", testService.getSimpleProp1());
        // Make sure global property overrides work
        assertEquals(true, testService.getSimpleProp2().booleanValue());
        // Make sure extension classpath property overrides work
        assertEquals("Instance Override3", testService.getSimpleProp3());
        // Make sure extension classpath Spring overrides work
        assertEquals("An extra bean I changed", childContext.getBean("anotherBean"));
        // Make sure composite properties and their defaults work
        TestBean[] testBeans = testService.getTestBeans();
        assertNotNull("Composite property not set", testBeans);
        assertEquals(3, testBeans.length);
        assertEquals("inst1", testBeans[0].getId());
        assertEquals(false, testBeans[0].isBoolProperty());
        assertEquals(123456789123456789L, testBeans[0].getLongProperty());
        assertEquals("Global Default", testBeans[0].getAnotherStringProperty());
        assertEquals("inst2", testBeans[1].getId());
        assertEquals(true, testBeans[1].isBoolProperty());
        assertEquals(123456789123456789L, testBeans[1].getLongProperty());
        assertEquals("Global Default", testBeans[1].getAnotherStringProperty());
        assertEquals("inst3", testBeans[2].getId());
        assertEquals(false, testBeans[2].isBoolProperty());
        assertEquals(123456789123456789L, testBeans[2].getLongProperty());
        assertEquals("Global Instance Default", testBeans[2].getAnotherStringProperty());
    }

    public void testALF6058() throws Exception
    {
    	ServerRegistryImpl serverRegistry = getApplicationContext().getBean(ServerRegistryImpl.class);
        ApplicationContextFactory subsystem = (ApplicationContextFactory) getApplicationContext().getBean("testsubsystem");
        int beforeStop = serverRegistry.getServers().size();
        subsystem.stop();
        //Make sure CXF doesn't remove its endpoints after subsystem stops
        assertEquals(beforeStop, serverRegistry.getServers().size());
        subsystem.start();

    }

}
