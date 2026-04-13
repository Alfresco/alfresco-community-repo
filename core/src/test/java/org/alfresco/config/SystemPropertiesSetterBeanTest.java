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
package org.alfresco.config;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @see SystemPropertiesSetterBean
 * 
 * @author Derek Hulley
 */
public class SystemPropertiesSetterBeanTest extends TestCase
{
    private static final String KEY_A = "SystemPropertiesSetterBeanTest.A";
    private static final String KEY_B = "SystemPropertiesSetterBeanTest.B";
    private static final String KEY_C = "SystemPropertiesSetterBeanTest.C";
    private static final String KEY_EXISTING = "SystemPropertiesSetterBeanTest.EXISTING ";
    private static final String KEY_PLACEHOLDER = "SystemPropertiesSetterBeanTest.PLACEHOLDER";
    private static final String KEY_EMPTY_STRING = "SystemPropertiesSetterBeanTest.EMPTY_STRING";
    private static final String KEY_NULL = "SystemPropertiesSetterBeanTest.NULL";
    private static final String VALUE_A = "A";
    private static final String VALUE_B = "B";
    private static final String VALUE_C = "C";
    private static final String VALUE_EXISTING = "EXISTING";
    private static final String VALUE_PLACEHOLDER = "${OOPS}";
    private static final String VALUE_EMPTY_STRING = "";
    private static final String VALUE_NULL = null;

    SystemPropertiesSetterBean setter;
    private Map<String, String> propertyMap;
    
    public void setUp() throws Exception
    {
        System.setProperty(KEY_EXISTING, VALUE_EXISTING);
        
        propertyMap = new HashMap<String, String>(7);
        propertyMap.put(KEY_A, VALUE_A);
        propertyMap.put(KEY_B, VALUE_B);
        propertyMap.put(KEY_C, VALUE_C);
        propertyMap.put(KEY_EXISTING, "SHOULD NOT HAVE OVERRIDDEN EXISTING PROPERTY");
        propertyMap.put(KEY_PLACEHOLDER, VALUE_PLACEHOLDER);
        propertyMap.put(KEY_EMPTY_STRING, VALUE_EMPTY_STRING);
        propertyMap.put(KEY_NULL, VALUE_NULL);
        
        setter = new SystemPropertiesSetterBean();
        setter.setPropertyMap(propertyMap);
    }
    
    public void testSetUp()
    {
        assertEquals(VALUE_EXISTING, System.getProperty(KEY_EXISTING));
        assertNull(System.getProperty(KEY_A));
        assertNull(System.getProperty(KEY_B));
        assertNull(System.getProperty(KEY_C));
    }
    
    public void testSettingOfSystemProperties()
    {
        setter.init();
        // Check
        assertEquals(VALUE_A, System.getProperty(KEY_A));
        assertEquals(VALUE_B, System.getProperty(KEY_B));
        assertEquals(VALUE_C, System.getProperty(KEY_C));
        assertEquals(VALUE_EXISTING, System.getProperty(KEY_EXISTING));
        assertNull("Property placeholder not detected", System.getProperty(KEY_PLACEHOLDER));
        assertNull("Empty string not detected", System.getProperty(KEY_EMPTY_STRING));
        assertNull("Null string not detected", System.getProperty(KEY_NULL));
    }
}
