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
package org.alfresco.repo.service;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class StoreRedirectorProxyFactoryTest extends TestCase
{

    private ApplicationContext factory = null;

    public void setUp()
    {
        factory = new ClassPathXmlApplicationContext("org/alfresco/repo/service/testredirector.xml");
    }

    public void testRedirect()
    {
        StoreRef storeRef1 = new StoreRef("Type1", "id");
        StoreRef storeRef2 = new StoreRef("Type2", "id");
        StoreRef storeRef3 = new StoreRef("Type3", "id");
        StoreRef storeRef4 = new StoreRef("Type3", "woof");
        NodeRef nodeRef1 = new NodeRef(storeRef1, "id");
        NodeRef nodeRef2 = new NodeRef(storeRef2, "id");

        TestServiceInterface service = (TestServiceInterface) factory.getBean("redirector_service1");

        String result1 = service.defaultBinding("redirector_service1");
        assertEquals("Type1:redirector_service1", result1);
        String result1a = service.noArgs();
        assertEquals("Type1", result1a);
        String result2 = service.storeRef(storeRef1);
        assertEquals("Type1:" + storeRef1, result2);
        String result3 = service.storeRef(storeRef2);
        assertEquals("Type2:" + storeRef2, result3);
        String result4 = service.nodeRef(nodeRef1);
        assertEquals("Type1:" + nodeRef1, result4);
        String result5 = service.nodeRef(nodeRef2);
        assertEquals("Type2:" + nodeRef2, result5);
        String result6 = service.multiStoreRef(storeRef1, storeRef1);
        assertEquals("Type1:" + storeRef1 + "," + storeRef1, result6);
        String result7 = service.multiStoreRef(storeRef2, storeRef2);
        assertEquals("Type2:" + storeRef2 + "," + storeRef2, result7);
        String result8 = service.multiNodeRef(nodeRef1, nodeRef1);
        assertEquals("Type1:" + nodeRef1 + "," + nodeRef1, result8);
        String result9 = service.multiNodeRef(nodeRef2, nodeRef2);
        assertEquals("Type2:" + nodeRef2 + "," + nodeRef2, result9);
        String result10 = service.mixedStoreNodeRef(storeRef1, nodeRef1);
        assertEquals("Type1:" + storeRef1 + "," + nodeRef1, result10);
        String result11 = service.mixedStoreNodeRef(storeRef2, nodeRef2);
        assertEquals("Type2:" + storeRef2 + "," + nodeRef2, result11);
        String result12 = service.mixedStoreNodeRef(null, null);
        assertEquals("Type1:null,null", result12);
        String result13 = service.mixedStoreNodeRef(storeRef1, null);
        assertEquals("Type1:" + storeRef1 + ",null", result13);

        // Direct store refs
        String result14 = service.storeRef(storeRef3);
        assertEquals("Type3:" + storeRef3, result14);
        String result15 = service.storeRef(storeRef4);
        assertEquals("Type1:" + storeRef4, result15);
    }

    public void testInvalidArgs()
    {
        StoreRef defaultRef = new StoreRef("Type1", "id");
        StoreRef storeRef1 = new StoreRef("InvalidType1", "id");
        NodeRef nodeRef1 = new NodeRef(storeRef1, "id");

        TestServiceInterface service = (TestServiceInterface) factory.getBean("redirector_service1");
        String result1 = service.storeRef(storeRef1);
        assertEquals("Type1:" + storeRef1, result1);
        String result2 = service.nodeRef(nodeRef1);
        assertEquals("Type1:" + nodeRef1, result2);
    }
    
    public void testException()
    {
        StoreRef storeRef1 = new StoreRef("Type1", "id");
        NodeRef nodeRef1 = new NodeRef(storeRef1, "id");
        TestServiceInterface service = (TestServiceInterface) factory.getBean("redirector_service1");
        
        try
        {
            service.throwException(nodeRef1);
            fail("Service method did not throw exception");
        }
        catch(Exception e)
        {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals(nodeRef1.toString(), e.getMessage());
        }
    }
    

    public interface TestServiceInterface
    {
        public String noArgs();
        
        public String defaultBinding(String arg);

        public String storeRef(StoreRef ref1);

        public String nodeRef(NodeRef ref1);

        public String multiStoreRef(StoreRef ref1, StoreRef ref2);

        public String multiNodeRef(NodeRef ref1, NodeRef ref2);

        public String mixedStoreNodeRef(StoreRef ref2, NodeRef ref1);
        
        public void throwException(NodeRef ref1);
    }

    
    public static abstract class Component implements TestServiceInterface
    {
        private String type;

        private Component(String type)
        {
            this.type = type;
        }
        
        public String noArgs()
        {
            return type;
        }

        public String defaultBinding(String arg)
        {
            return type + ":" + arg;
        }

        public String nodeRef(NodeRef ref1)
        {
            return type + ":" + ref1;
        }

        public String storeRef(StoreRef ref1)
        {
            return type + ":" + ref1;
        }

        public String multiNodeRef(NodeRef ref1, NodeRef ref2)
        {
            return type + ":" + ref1 + "," + ref2;
        }

        public String multiStoreRef(StoreRef ref1, StoreRef ref2)
        {
            return type + ":" + ref1 + "," + ref2;
        }

        public String mixedStoreNodeRef(StoreRef ref1, NodeRef ref2)
        {
            return type + ":" + ref1 + "," + ref2;
        }
        
        public void throwException(NodeRef ref1)
        {
            throw new IllegalArgumentException(ref1.toString());
        }
        
    }

    public static class Type1Component extends Component
    {
        private Type1Component()
        {
            super("Type1");
        }
    }

    public static class Type2Component extends Component
    {
        private Type2Component()
        {
            super("Type2");
        }
    }

    public static class Type3Component extends Component
    {
        private Type3Component()
        {
            super("Type3");
        }
    }

}
