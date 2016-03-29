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

package org.alfresco.module.org_alfresco_module_rm.test.integration.issue.rm3314;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;

/**
 * Test for https://issues.alfresco.com/jira/browse/RM-3114
 *
 * @author Roy Wetherall
 * @since 2.2.1.5
 */
public class RM3314Test extends BaseRMTestCase
{
    public static Map<String, Boolean> callback = new HashMap<String, Boolean>(2);
    
    /**
     * Given that the custom model hasn't been initialised 
     * When an aspect is added 
     * Then nothing happens
     *
     * Given that the custom model has been initialised 
     * When an aspect is added 
     * Then something happens
     */
    public void testListenersExecutedInTheCorrectOrder()
    {
        assertFalse(callback.isEmpty());
        assertFalse(callback.get("test.rm3314.1"));
        assertTrue(callback.get("test.rm3314.2"));
    }
}
