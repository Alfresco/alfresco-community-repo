/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.integration.issue.rm3314;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Test for https://issues.alfresco.com/jira/browse/RM-3114
 *
 * @author Roy Wetherall
 * @since 2.2.1.5
 */
public class RM3314Test extends BaseRMTestCase
{
    /** Application context */
    protected String[] getConfigLocations()
    {
        return ArrayUtils.add(super.getConfigLocations(), "classpath:test-rm3314-context.xml");
    }
    
	/** registry to record callback from test beans "test.rm3114.1" and "test.rm3114.2" */
    public static Map<String, Boolean> callback = new HashMap<>(2);
    
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
    	/**
    	 * The related test beans will call back into the callback map showing
    	 * whether at the end of their execution whether the custom model has been
    	 * initialised or not.  Given the order in which these test beans are executed
    	 * on spring context load, we would expect that .1 executes with the custom
    	 * map unloaded, and the .2 with it loaded.
    	 */
    	
        assertFalse(callback.isEmpty());
        assertFalse(callback.get("test.rm3314.1"));
        assertTrue(callback.get("test.rm3314.2"));
    }
}
