/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

package org.alfresco.util.test.junitrules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.alfresco.service.cmr.repository.ContentService;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Test class for {@link ApplicationContextInit}.
 * 
 * @author Neil Mc Erlean
 * @since Odin
 */
public class ApplicationContextInitTest
{
    // Some dummy contexts with test beans in them.
    public static final String[] dummySpringContexts = new String[] {"classpath:org/alfresco/util/test/junitrules/dummy1-context.xml",
                                                                     "classpath:org/alfresco/util/test/junitrules/dummy2-context.xml"};
    
    // Rule to initialise the default Alfresco spring configuration
    @ClassRule public static ApplicationContextInit APP_CONTEXT_INIT = ApplicationContextInit.createStandardContextWithOverrides(dummySpringContexts);
    
    @Test public void ensureSpringContextWasInitedWithOverrides() throws Exception
    {
        // Bean from the standard Alfresco context
        assertNotNull("Spring context did not contain expected bean.",
                      APP_CONTEXT_INIT.getApplicationContext().getBean("contentService", ContentService.class));
        
        // Bean from the first override context
        assertEquals("Value from dummy1-context.xml",
                      APP_CONTEXT_INIT.getApplicationContext().getBean("testBean1", String.class));
        
        // Bean from the second override context
        assertEquals("Value from dummy2-context.xml",
                APP_CONTEXT_INIT.getApplicationContext().getBean("testBean2", String.class));
        
        // Bean overridden in second context
        assertEquals("Value from dummy2-context.xml",
                APP_CONTEXT_INIT.getApplicationContext().getBean("testBean1and2", String.class));
    }
}
