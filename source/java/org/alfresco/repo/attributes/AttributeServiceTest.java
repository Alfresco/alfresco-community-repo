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
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.attributes;

import java.util.List;

import org.alfresco.service.cmr.attributes.AttrAndQuery;
import org.alfresco.service.cmr.attributes.AttrNotQuery;
import org.alfresco.service.cmr.attributes.AttrOrQuery;
import org.alfresco.service.cmr.attributes.AttrQueryEquals;
import org.alfresco.service.cmr.attributes.AttrQueryGT;
import org.alfresco.service.cmr.attributes.AttrQueryGTE;
import org.alfresco.service.cmr.attributes.AttrQueryLT;
import org.alfresco.service.cmr.attributes.AttrQueryLTE;
import org.alfresco.service.cmr.attributes.AttrQueryLike;
import org.alfresco.service.cmr.attributes.AttrQueryNE;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.util.Pair;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import junit.framework.TestCase;

/**
 * Basic tests for AttributeService.
 * @author britt
 */
public class AttributeServiceTest extends TestCase
{
    private static FileSystemXmlApplicationContext fContext = null;
    
    private static AttributeService fService;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        if (fContext == null)
        {
            fContext = new FileSystemXmlApplicationContext("config/alfresco/application-context.xml");
            fService = (AttributeService)fContext.getBean("AttributeService");
        }
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        List<String> globalKeys = fService.getKeys("");
        for (String key : globalKeys)
        {
            fService.removeAttribute("", key);
        }
    }
    
    public void testBasic()
    {
        try
        {
            fService.setAttribute("", "boolean", new BooleanAttributeValue(true));
            fService.setAttribute("", "byte", new ByteAttributeValue((byte)0x20));
            fService.setAttribute("", "short", new ShortAttributeValue((short)42));
            fService.setAttribute("", "int", new IntAttributeValue(43));
            fService.setAttribute("", "long", new LongAttributeValue(1000000000000L));
            fService.setAttribute("", "float", new FloatAttributeValue(1.414f));
            fService.setAttribute("", "double", new DoubleAttributeValue(3.1415926));
            fService.setAttribute("", "string", new StringAttributeValue("This is a string."));
            fService.setAttribute("", "serializable", new SerializableAttributeValue(new Long(1010101L)));
            MapAttribute map = new MapAttributeValue();
            map.put("foo", new StringAttributeValue("I walk."));
            map.put("bar", new StringAttributeValue("I talk."));
            map.put("baz", new StringAttributeValue("I shop."));
            map.put("funky", new StringAttributeValue("I sneeze."));
            map.put("monkey", 
                    new StringAttributeValue("I'm going to be a fireman when the floods roll back."));
            fService.setAttribute("", "map", map);
            assertNotNull(fService.getAttribute("boolean"));
            assertEquals(42, (int)fService.getAttribute("short").getShortValue());
            assertEquals("I sneeze.", fService.getAttribute("map/funky").getStringValue());
            assertEquals(10, fService.getKeys("").size());
            assertEquals(5, fService.getKeys("map").size());
            List<String> keys = fService.getKeys("");
            for (String key : keys)
            {
                System.out.println(key + " => " + fService.getAttribute(key));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * Test the query capability.
     */
    public void testQuery()
    {
        try
        {
            // Put some attributes in place.
            MapAttribute map = new MapAttributeValue();
            map.put("a", new StringAttributeValue("a"));
            map.put("b", new StringAttributeValue("a"));
            map.put("c", new StringAttributeValue("a"));
            map.put("d", new StringAttributeValue("a"));
            map.put("e", new StringAttributeValue("a"));
            map.put("f", new StringAttributeValue("a"));
            map.put("g", new StringAttributeValue("a"));
            map.put("h", new StringAttributeValue("a"));
            map.put("i", new StringAttributeValue("a"));
            map.put("j", new StringAttributeValue("a"));
            map.put("k", new StringAttributeValue("a"));
            map.put("l", new StringAttributeValue("a"));
            map.put("m", new StringAttributeValue("a"));
            map.put("n", new StringAttributeValue("a"));
            map.put("o", new StringAttributeValue("a"));
            map.put("p", new StringAttributeValue("a"));
            map.put("q", new StringAttributeValue("a"));
            map.put("r", new StringAttributeValue("a"));
            map.put("s", new StringAttributeValue("a"));
            map.put("t", new StringAttributeValue("a"));
            map.put("u", new StringAttributeValue("a"));
            map.put("v", new StringAttributeValue("a"));
            map.put("w", new StringAttributeValue("a"));
            map.put("x", new StringAttributeValue("a"));
            map.put("y", new StringAttributeValue("a"));
            map.put("z", new StringAttributeValue("a"));
            fService.setAttribute("", "map1", map);
            fService.setAttribute("", "map2", map);
            List<Pair<String, Attribute>> result =
                fService.query("map1", new AttrQueryEquals("w"));
            assertEquals(1, result.size());
            result =
                fService.query("map1", new AttrQueryLT("d"));
            assertEquals(3, result.size());
            result =
                fService.query("map1", new AttrQueryLTE("d"));
            assertEquals(4, result.size());
            result = 
                fService.query("map1", new AttrQueryGT("v"));
            assertEquals(4, result.size());
            result =
                fService.query("map1", new AttrQueryGTE("v"));
            assertEquals(5, result.size());
            result =
                fService.query("map1", new AttrQueryNE("g"));
            assertEquals(25, result.size());
            result =
                fService.query("map1", new AttrNotQuery(new AttrQueryGT("d")));
            assertEquals(4, result.size());
            result =
                fService.query("map1", new AttrAndQuery(new AttrQueryGT("g"),
                                                        new AttrQueryLT("l")));
            assertEquals(4, result.size());
            result =
                fService.query("map1", new AttrOrQuery(new AttrQueryLT("d"),
                                                       new AttrQueryGT("w")));
            assertEquals(6, result.size());
            result =
                fService.query("map1", new AttrQueryLike("%"));
            assertEquals(26, result.size());
            fService.setAttribute("map2", "submap", map);
            result =
                fService.query("map2/submap", new AttrQueryEquals("w"));
            assertEquals(1, result.size());
            result =
                fService.query("map2/submap", new AttrQueryLT("d"));
            assertEquals(3, result.size());
            result =
                fService.query("map2/submap", new AttrQueryLTE("d"));
            assertEquals(4, result.size());
            result = 
                fService.query("map2/submap", new AttrQueryGT("v"));
            assertEquals(4, result.size());
            result =
                fService.query("map2/submap", new AttrQueryGTE("v"));
            assertEquals(5, result.size());
            result =
                fService.query("map2/submap", new AttrQueryNE("g"));
            assertEquals(25, result.size());
            result =
                fService.query("map2/submap", new AttrNotQuery(new AttrQueryGT("d")));
            assertEquals(4, result.size());
            result =
                fService.query("map2/submap", new AttrAndQuery(new AttrQueryGT("g"),
                                                        new AttrQueryLT("l")));
            assertEquals(4, result.size());
            result =
                fService.query("map2/submap", new AttrOrQuery(new AttrQueryLT("d"),
                                                       new AttrQueryGT("w")));
            assertEquals(6, result.size());
            result =
                fService.query("map2/submap", new AttrQueryLike("%"));
            assertEquals(26, result.size());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }
}
