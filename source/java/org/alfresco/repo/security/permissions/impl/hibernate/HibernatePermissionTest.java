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
package org.alfresco.repo.security.permissions.impl.hibernate;

import java.io.Serializable;

import org.alfresco.repo.domain.NodeKey;
import org.alfresco.util.BaseSpringTest;

/**
 * Test persistence and retrieval of Hibernate-specific implementations of the
 * {@link org.alfresco.repo.domain.Node} interface
 * 
 * @author Andy Hind
 */
public class HibernatePermissionTest extends BaseSpringTest
{    
  
    public HibernatePermissionTest()
    {
    }
    
    protected void onSetUpInTransaction() throws Exception
    {
        
    }
    
    protected void onTearDownInTransaction()
    {
        // force a flush to ensure that the database updates succeed
        getSession().flush();
        getSession().clear();
    }
   

	public void testSimpleNodePermission() throws Exception
	{
        // create a new Node
        NodePermissionEntry nodePermission = new NodePermissionEntryImpl();
		NodeKey key = new NodeKey("Random Protocol", "Random Identifier", "AAA");
        nodePermission.setNodeKey(key);
        nodePermission.setInherits(true);
        
        Serializable id = getSession().save(nodePermission);
			
        // throw the reference away and get the a new one for the id
        nodePermission = (NodePermissionEntry) getSession().load(NodePermissionEntryImpl.class, id);
        assertNotNull("Node not found", nodePermission);
        assertTrue(nodePermission.getInherits());
        
        // Update inherits 
        
        nodePermission.setInherits(false);
        id = getSession().save(nodePermission);
        
        // throw the reference away and get the a new one for the id
        nodePermission = (NodePermissionEntry) getSession().load(NodePermissionEntryImpl.class, id);
        assertNotNull("Node not found", nodePermission);
        assertFalse(nodePermission.getInherits());
	}
    
    public void testSimplePermissionReference()
    {
        PermissionReference permissionReference = new PermissionReferenceImpl();
        permissionReference.setName("Test");
        permissionReference.setTypeUri("TestUri");
        permissionReference.setTypeName("TestName");
        
        Serializable id = getSession().save(permissionReference);
        
        // throw the reference away and get the a new one for the id
        permissionReference = (PermissionReference) getSession().load(PermissionReferenceImpl.class, id);
        assertNotNull("Node not found", permissionReference);
        assertEquals("Test", permissionReference.getName());
        assertEquals("TestUri", permissionReference.getTypeUri());
        assertEquals("TestName", permissionReference.getTypeName());
        
        // Test key
        
        PermissionReference key = new PermissionReferenceImpl();
        key.setName("Test");
        key.setTypeUri("TestUri");
        key.setTypeName("TestName");
        
        permissionReference = (PermissionReference) getSession().load(PermissionReferenceImpl.class, key);
        assertNotNull("Node not found", permissionReference);
        assertEquals("Test", permissionReference.getName());
        assertEquals("TestUri", permissionReference.getTypeUri());
        assertEquals("TestName", permissionReference.getTypeName());
    }
    
    public void testSimpleRecipient()
    {
        Recipient recipient = new RecipientImpl();
        recipient.setRecipient("Test");
        recipient.getExternalKeys().add("One");
        
        Serializable id = getSession().save(recipient);
        
        // throw the reference away and get the a new one for the id
        recipient = (Recipient) getSession().load(RecipientImpl.class, id);
        assertNotNull("Node not found", recipient);
        assertEquals("Test", recipient.getRecipient());
        assertEquals(1, recipient.getExternalKeys().size());
        
        // Key
        

        Recipient key = new RecipientImpl();
        key.setRecipient("Test");
        
        recipient = (Recipient) getSession().load(RecipientImpl.class, key);
        assertNotNull("Node not found", recipient);
        assertEquals("Test", recipient.getRecipient());
        assertEquals(1, recipient.getExternalKeys().size());
        
        
        // Update
        
        recipient.getExternalKeys().add("Two");
        id  = getSession().save(recipient);
      
        // throw the reference away and get the a new one for the id
        recipient = (Recipient) getSession().load(RecipientImpl.class, id);
        assertNotNull("Node not found", recipient);
        assertEquals("Test", recipient.getRecipient());
        assertEquals(2, recipient.getExternalKeys().size());
        
        
        // complex
        
        recipient.getExternalKeys().add("Three");
        recipient.getExternalKeys().remove("One");
        recipient.getExternalKeys().remove("Two");
        id  = getSession().save(recipient);
        
        // Throw the reference away and get the a new one for the id
        recipient = (Recipient) getSession().load(RecipientImpl.class, id);
        assertNotNull("Node not found", recipient);
        assertEquals("Test", recipient.getRecipient());
        assertEquals(1, recipient.getExternalKeys().size());
        
        
    }
    
    public void testNodePermissionEntry()
    {
        //      create a new Node
        NodePermissionEntry nodePermission = new NodePermissionEntryImpl();
        NodeKey key = new NodeKey("Random Protocol", "Random Identifier", "AAA");
        nodePermission.setNodeKey(key);
        nodePermission.setInherits(true);
        
        Recipient recipient = new RecipientImpl();
        recipient.setRecipient("Test");
        recipient.getExternalKeys().add("One");
        
        PermissionReference permissionReference = new PermissionReferenceImpl();
        permissionReference.setName("Test");
        permissionReference.setTypeUri("TestUri");
        permissionReference.setTypeName("TestName");
        
        PermissionEntry permissionEntry = PermissionEntryImpl.create(nodePermission, permissionReference, recipient, true);
        
        Serializable idNodePermision = getSession().save(nodePermission);
        getSession().save(recipient);
        getSession().save(permissionReference);
        Serializable idPermEnt = getSession().save(permissionEntry);
        
        permissionEntry =  (PermissionEntry) getSession().load(PermissionEntryImpl.class, idPermEnt);
        assertNotNull("Permission entry not found", permissionEntry);
        assertTrue(permissionEntry.isAllowed());
        assertNotNull(permissionEntry.getNodePermissionEntry());
        assertTrue(permissionEntry.getNodePermissionEntry().getInherits());
        assertNotNull(permissionEntry.getPermissionReference());
        assertEquals("Test", permissionEntry.getPermissionReference().getName());
        assertNotNull(permissionEntry.getRecipient());
        assertEquals("Test", permissionEntry.getRecipient().getRecipient());
        assertEquals(1, permissionEntry.getRecipient().getExternalKeys().size());
        
        // Check traversal down
        
        nodePermission = (NodePermissionEntry) getSession().load(NodePermissionEntryImpl.class, idNodePermision);
        assertEquals(1, nodePermission.getPermissionEntries().size());
        
        permissionEntry.delete();
        getSession().delete(permissionEntry);
        
        nodePermission = (NodePermissionEntry) getSession().load(NodePermissionEntryImpl.class, idNodePermision);
        assertEquals(0, nodePermission.getPermissionEntries().size());  
        
        
    }
    
}