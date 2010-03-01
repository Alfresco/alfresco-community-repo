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
package org.alfresco.service.cmr.repository;

import junit.framework.TestCase;

/**
 * @see org.alfresco.service.cmr.repository.NodeRef
 * 
 * @author Derek Hulley
 */
public class NodeRefTest extends TestCase
{

    public NodeRefTest(String name)
    {
        super(name);
    }

    public void testStoreRef() throws Exception
    {
        StoreRef storeRef = new StoreRef("ABC", "123");
        assertEquals("toString failure", "ABC://123", storeRef.toString());

        StoreRef storeRef2 = new StoreRef(storeRef.getProtocol(), storeRef
                .getIdentifier());
        assertEquals("equals failure", storeRef, storeRef2);
    }

    public void testNodeRef() throws Exception
    {
        StoreRef storeRef = new StoreRef("ABC", "123");
        NodeRef nodeRef = new NodeRef(storeRef, "456");
        assertEquals("toString failure", "ABC://123/456", nodeRef.toString());

        NodeRef nodeRef2 = new NodeRef(storeRef, "456");
        assertEquals("equals failure", nodeRef, nodeRef2);
    }
    
    public void testNodeRefPattern() throws Exception
    {
        StoreRef storeRef = new StoreRef("ABC", "123");
        NodeRef nodeRef = new NodeRef(storeRef, "456");

        assertTrue(NodeRef.isNodeRef(nodeRef.toString()));
        assertFalse(NodeRef.isNodeRef("sdfsdf:sdfsdf"));
    }
}
