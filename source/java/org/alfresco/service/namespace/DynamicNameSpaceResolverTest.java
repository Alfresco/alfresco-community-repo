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
