/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
        assertEquals(auditable.key(), -1);
        assertEquals(auditable.parameters().length, 0);
        
        
        method = clazz.getMethod("getString", new Class[]{String.class, String.class});
        assertTrue(method.isAnnotationPresent(Auditable.class));
        auditable = method.getAnnotation(Auditable.class);
        assertEquals(auditable.key(), 0);
        assertEquals(auditable.parameters().length, 2);
        assertEquals(auditable.parameters()[0], "one");
        assertEquals(auditable.parameters()[1], "two");
       
        
        method = clazz.getMethod("getAnotherString", new Class[]{String.class});
        assertTrue(method.isAnnotationPresent(Auditable.class));
        auditable = method.getAnnotation(Auditable.class);
        assertEquals(auditable.key(), 0);
        assertEquals(auditable.parameters().length, 1);
        assertEquals(auditable.parameters()[0], "one");
      
    }
    
}
