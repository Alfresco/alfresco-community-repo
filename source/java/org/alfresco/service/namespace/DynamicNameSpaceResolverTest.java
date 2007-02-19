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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.namespace;

import junit.framework.TestCase;

public class DynamicNameSpaceResolverTest extends TestCase
{

    public DynamicNameSpaceResolverTest()
    {
        super();
    }
    
    public void testOne()
    {
        DynamicNamespacePrefixResolver dnpr = new DynamicNamespacePrefixResolver(null);
        dnpr.registerNamespace("one", "http:/namespace/one");
        dnpr.registerNamespace("two", "http:/namespace/two");
        dnpr.registerNamespace("three", "http:/namespace/three");
        dnpr.registerNamespace("oneagain", "http:/namespace/one");
        dnpr.registerNamespace("four", "http:/namespace/one");
        dnpr.registerNamespace("four", "http:/namespace/four");
        
        assertEquals("http:/namespace/one", dnpr.getNamespaceURI("one"));
        assertEquals("http:/namespace/two", dnpr.getNamespaceURI("two"));
        assertEquals("http:/namespace/three", dnpr.getNamespaceURI("three"));
        assertEquals("http:/namespace/one", dnpr.getNamespaceURI("oneagain"));
        assertEquals("http:/namespace/four", dnpr.getNamespaceURI("four"));
        assertEquals(null, dnpr.getNamespaceURI("five"));
        
        dnpr.unregisterNamespace("four");
        assertEquals(null, dnpr.getNamespaceURI("four"));
        
        assertEquals(0, dnpr.getPrefixes("http:/namespace/four").size());
        assertEquals(1, dnpr.getPrefixes("http:/namespace/two").size());
        assertEquals(2, dnpr.getPrefixes("http:/namespace/one").size());
        
        
    }
    
    
    public void testTwo()
    {
        DynamicNamespacePrefixResolver dnpr1 = new DynamicNamespacePrefixResolver(null);
        dnpr1.registerNamespace("one", "http:/namespace/one");
        dnpr1.registerNamespace("two", "http:/namespace/two");
        dnpr1.registerNamespace("three", "http:/namespace/three");
        dnpr1.registerNamespace("oneagain", "http:/namespace/one");
        dnpr1.registerNamespace("four", "http:/namespace/one");
        dnpr1.registerNamespace("four", "http:/namespace/four");
        dnpr1.registerNamespace("five", "http:/namespace/five");
        dnpr1.registerNamespace("six", "http:/namespace/six");
        
        DynamicNamespacePrefixResolver dnpr2 = new DynamicNamespacePrefixResolver(dnpr1);
        dnpr2.registerNamespace("a", "http:/namespace/one");
        dnpr2.registerNamespace("b", "http:/namespace/two");
        dnpr2.registerNamespace("c", "http:/namespace/three");
        dnpr2.registerNamespace("d", "http:/namespace/one");
        dnpr2.registerNamespace("e", "http:/namespace/one");
        dnpr2.registerNamespace("f", "http:/namespace/four");
        dnpr2.registerNamespace("five", "http:/namespace/one");
        dnpr2.registerNamespace("six", "http:/namespace/seven");
        
        assertEquals("http:/namespace/one", dnpr2.getNamespaceURI("one"));
        assertEquals("http:/namespace/two", dnpr2.getNamespaceURI("two"));
        assertEquals("http:/namespace/three", dnpr2.getNamespaceURI("three"));
        assertEquals("http:/namespace/one", dnpr2.getNamespaceURI("oneagain"));
        assertEquals("http:/namespace/four", dnpr2.getNamespaceURI("four"));
        assertEquals("http:/namespace/one", dnpr2.getNamespaceURI("five"));
        dnpr2.unregisterNamespace("five");
        
        assertEquals("http:/namespace/five", dnpr2.getNamespaceURI("five"));
        assertEquals("http:/namespace/one", dnpr2.getNamespaceURI("a"));
        assertEquals("http:/namespace/two", dnpr2.getNamespaceURI("b"));
        assertEquals("http:/namespace/three", dnpr2.getNamespaceURI("c"));
        assertEquals("http:/namespace/one", dnpr2.getNamespaceURI("d"));
        assertEquals("http:/namespace/one", dnpr2.getNamespaceURI("e"));
        assertEquals("http:/namespace/four", dnpr2.getNamespaceURI("f"));
        
        assertEquals(5, dnpr2.getPrefixes("http:/namespace/one").size());
        assertEquals(2, dnpr2.getPrefixes("http:/namespace/two").size());
        assertEquals(2, dnpr2.getPrefixes("http:/namespace/three").size());
        assertEquals(2, dnpr2.getPrefixes("http:/namespace/four").size());
        assertEquals(1, dnpr2.getPrefixes("http:/namespace/five").size());
        assertEquals(0, dnpr2.getPrefixes("http:/namespace/six").size());
        assertEquals(1, dnpr2.getPrefixes("http:/namespace/seven").size());
    }

}
