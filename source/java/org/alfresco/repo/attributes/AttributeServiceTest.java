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

import org.alfresco.service.cmr.attributes.AttributeService;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import junit.framework.TestCase;

/**
 * Basic tests for AttributeService.
 * @author britt
 */
public class AttributeServiceTest extends TestCase
{
    private FileSystemXmlApplicationContext fContext = null;
    
    private AttributeService fService;
    
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
        fContext.close();
        fContext = null;
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
            Attribute found = fService.getAttribute("map");
            System.out.println(found);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }
}
