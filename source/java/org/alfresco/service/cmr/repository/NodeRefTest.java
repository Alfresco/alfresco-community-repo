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
