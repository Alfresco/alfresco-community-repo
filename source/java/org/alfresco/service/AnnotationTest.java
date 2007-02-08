/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.service;

import java.lang.reflect.Method;

import junit.framework.TestCase;

public class AnnotationTest extends TestCase
{

    public AnnotationTest()
    {
        super();
    }

    public AnnotationTest(String arg0)
    {
        super(arg0);
    }

    
    public void testAnnotations() throws Exception, NoSuchMethodException
    {
        Class clazz = AnnotationTestInterface.class;
        
        Method method = clazz.getMethod("noArgs", new Class[]{});
        assertTrue(method.isAnnotationPresent(Auditable.class));
        Auditable auditable = method.getAnnotation(Auditable.class);
        assertEquals(auditable.key(), Auditable.Key.NO_KEY);
        assertEquals(auditable.parameters().length, 0);
        
        
        method = clazz.getMethod("getString", new Class[]{String.class, String.class});
        assertTrue(method.isAnnotationPresent(Auditable.class));
        auditable = method.getAnnotation(Auditable.class);
        assertEquals(auditable.key(), Auditable.Key.ARG_0);
        assertEquals(auditable.parameters().length, 2);
        assertEquals(auditable.parameters()[0], "one");
        assertEquals(auditable.parameters()[1], "two");
       
        
        method = clazz.getMethod("getAnotherString", new Class[]{String.class});
        assertTrue(method.isAnnotationPresent(Auditable.class));
        auditable = method.getAnnotation(Auditable.class);
        assertEquals(auditable.key(), Auditable.Key.ARG_0);
        assertEquals(auditable.parameters().length, 1);
        assertEquals(auditable.parameters()[0], "one");
      
    }
    
}
