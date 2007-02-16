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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
