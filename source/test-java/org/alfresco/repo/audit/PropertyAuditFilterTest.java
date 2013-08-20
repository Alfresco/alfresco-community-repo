/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.audit;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A low level unit test of the filter on audit maps.
 * 
 * @author Alan Davis
 */
public class PropertyAuditFilterTest
{
    private PropertyAuditFilter filter;
    private Properties properties;

    private String rootPath;
    private Map<String, Serializable> auditMap;
    
    @Before
    public void setUp() throws Exception
    {
        filter = new PropertyAuditFilter();
        properties = new Properties();
        filter.setProperties(properties);

        rootPath = "root/action";
        auditMap = new HashMap<String, Serializable>();
        auditMap.put("name", "value");
    }

    @Test
    public void testNoFilterIfNoProperties()
    {
        boolean actual = filter.accept(rootPath, auditMap);        
        assertTrue("Filter should only run if properties are set.", actual);
    }

    @Test
    public void testNoRegexOnValue()
    {
        properties.put("audit.filter.root.action.enabled", "true");

        boolean actual = filter.accept(rootPath, auditMap);        
        assertTrue("Value should have been accepted.", actual);
    }

    @Test
    public void testRegexOnValue()
    {
        properties.put("audit.filter.root.action.enabled", "true");
        properties.put("audit.filter.root.action.name", "value");

        boolean actual = filter.accept(rootPath, auditMap);        
        assertTrue("Value should have been accepted.", actual);
    }

    @Test
    public void testRegexOnBadValue()
    {
        properties.put("audit.filter.root.action.enabled", "true");
        properties.put("audit.filter.root.action.name", "~value");

        boolean actual = filter.accept(rootPath, auditMap);        
        assertFalse("Value should have been rejected.", actual);
    }

    @Test
    public void testNullValue()
    {
        auditMap.put("name", null);

        properties.put("audit.filter.root.action.enabled", "true");
        properties.put("audit.filter.root.action.name", "null");

        boolean actual = filter.accept(rootPath, auditMap);        
        assertTrue("A null value should match null", actual);
    }

    @Test
    public void testNullStringValue()
    {
        auditMap.put("name", "null");

        properties.put("audit.filter.root.action.enabled", "true");
        properties.put("audit.filter.root.action.name", "null");

        boolean actual = filter.accept(rootPath, auditMap);        
        assertTrue("A null value should match null", actual);
    }

    @Test
    public void testNonStringValue()
    {
        LinkedHashSet<Integer> value = new LinkedHashSet<Integer>();
        value.add(Integer.valueOf(1));
        value.add(Integer.valueOf(2));
        value.add(Integer.valueOf(3));
        auditMap.put("name", value);

        properties.put("audit.filter.root.action.enabled", "true");
        properties.put("audit.filter.root.action.name", "\\[1, 2, 3\\]");

        boolean actual = filter.accept(rootPath, auditMap);        
        assertTrue("The check should have worked on the value.toString().", actual);
    }

    @Test
    public void testZeroLengthRegex()
    {
        properties.put("audit.filter.root.action.enabled", "true");
        properties.put("audit.filter.root.action.name", "");
        
        boolean actual = filter.accept(rootPath, auditMap);
        assertTrue("Should match any values just like having no property", actual);
    }

    @Test
    public void testDefaultActionUsedAsFallback()
    {
        properties.put("audit.filter.root.default.enabled", "true");
        properties.put("audit.filter.root.default.name", "~value");

        boolean actual = filter.accept(rootPath, auditMap);
        assertFalse("The 'default' fallback action should have been used to " +
        	    "enable the filter and reject the value.", actual);
    }

    @Test
    public void testRedirect()
    {
        properties.put("audit.filter.root.action.enabled", "true");
        properties.put("audit.filter.root.action.name", "$anotherProperty");
        properties.put("anotherProperty", "$theFinalProperty");
        properties.put("theFinalProperty", "~value");

        boolean actual = filter.accept(rootPath, auditMap);        
        assertFalse("Redirected properties should have rejected the value.", actual);
    }

    @Test
    public void testMultipleRegExp()
    {
        properties.put("audit.filter.root.action.enabled", "true");
        properties.put("audit.filter.root.action.name", "beGood;~b.*;.*");
        
        auditMap.put("name", "beGood");
        assertTrue("Should match 1st regex", filter.accept(rootPath, auditMap));
        
        auditMap.put("name", "bad");
        assertFalse("Should match 2nd regex", filter.accept(rootPath, auditMap));
        
        auditMap.put("name", "value");
        assertTrue("Should match 3rd regex", filter.accept(rootPath, auditMap));
    }

    @Test
    public void testMultipleRegExpWithNoCatchAll()
    {
        properties.put("audit.filter.root.action.enabled", "true");
        properties.put("audit.filter.root.action.name", "beGood;~b.*");
        
        auditMap.put("name", "value");
        assertFalse("Should match nothing", filter.accept(rootPath, auditMap));
    }

    @Test
    public void testEscapedSemicolon()
    {
        properties.put("audit.filter.root.action.enabled", "true");
        properties.put("audit.filter.root.action.name", "value\\\\;value");
        
        auditMap.put("name", "value\\;value");
        assertTrue("Should match 1st regex", filter.accept(rootPath, auditMap));
    }

    @Test
    public void testEscapedRedirect()
    {
        properties.put("audit.filter.root.action.enabled", "true");
        properties.put("audit.filter.root.action.name", "\\$");
        
        auditMap.put("name", "");
        assertTrue("Should match only zero length values", filter.accept(rootPath, auditMap));
    }

    @Test
    public void testEscapedNot()
    {
        properties.put("audit.filter.root.action.enabled", "true");
        properties.put("audit.filter.root.action.name", "\\~.*");
        
        auditMap.put("name", "~good");
        assertTrue("Should match any value starting with '~'.", filter.accept(rootPath, auditMap));
    }
}
