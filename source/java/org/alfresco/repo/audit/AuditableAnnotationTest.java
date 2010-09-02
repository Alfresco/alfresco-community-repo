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
package org.alfresco.repo.audit;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.alfresco.service.Auditable;

/**
 * @author Andy Hind
 */
public class AuditableAnnotationTest extends TestCase
{

    public AuditableAnnotationTest()
    {
        super();
    }

    @SuppressWarnings("unchecked")
    public void testAnnotations() throws Exception, NoSuchMethodException
    {
        Class clazz = AnnotationTestInterface.class;
        
        Method method = clazz.getMethod("noArgs", new Class[]{});
        assertTrue(method.isAnnotationPresent(Auditable.class));
        Auditable auditable = method.getAnnotation(Auditable.class);
        assertEquals(auditable.parameters().length, 0);
        
        
        method = clazz.getMethod("getString", new Class[]{String.class, String.class});
        assertTrue(method.isAnnotationPresent(Auditable.class));
        auditable = method.getAnnotation(Auditable.class);
        assertEquals(auditable.parameters().length, 2);
        assertEquals(auditable.parameters()[0], "one");
        assertEquals(auditable.parameters()[1], "two");
       
        
        method = clazz.getMethod("getAnotherString", new Class[]{String.class});
        assertTrue(method.isAnnotationPresent(Auditable.class));
        auditable = method.getAnnotation(Auditable.class);
        assertEquals(auditable.parameters().length, 1);
        assertEquals(auditable.parameters()[0], "one");
    }
}
