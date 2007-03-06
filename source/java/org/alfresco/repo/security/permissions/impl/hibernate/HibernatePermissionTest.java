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
package org.alfresco.repo.security.permissions.impl.hibernate;

import java.io.Serializable;

import org.alfresco.repo.domain.DbAccessControlEntry;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.DbAuthority;
import org.alfresco.repo.domain.DbPermission;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.domain.hibernate.DbAccessControlEntryImpl;
import org.alfresco.repo.domain.hibernate.DbAccessControlListImpl;
import org.alfresco.repo.domain.hibernate.DbAuthorityImpl;
import org.alfresco.repo.domain.hibernate.DbPermissionImpl;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * @see org.alfresco.repo.domain.hibernate.PermissionsDaoComponentImpl
 * @see org.alfresco.repo.domain.DbAccessControlList
 * @see org.alfresco.repo.domain.DbAccessControlEntry
 * 
 * @author Andy Hind
 */
public class HibernatePermissionTest extends BaseSpringTest
{
    private NodeDaoService nodeDaoService;
    private Node node;
    private QName qname;
  
    public HibernatePermissionTest()
    {
    }
    
    protected void onSetUpInTransaction() throws Exception
    {
        nodeDaoService = (NodeDaoService) applicationContext.getBean("nodeDaoService");
        
        // create the node to play with
        Store store = nodeDaoService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                getName() + "_" + System.currentTimeMillis());
        qname = QName.createQName(NamespaceService.ALFRESCO_URI, getName());
        node = nodeDaoService.newNode(
                store,
                GUID.generate(),
                qname);
    }
    
    protected void onTearDownInTransaction()
    {
        try
        {
            // force a flush to ensure that the database updates succeed
            getSession().flush();
            getSession().clear();
        }
        catch (Throwable e)
        {
            // don't mask any other exception coming through
            e.printStackTrace();
        }
    }

	public void testSimpleAccessControlList() throws Exception
	{
        // create a new Node
        DbAccessControlList accessControlList = new DbAccessControlListImpl();
        accessControlList.setInherits(true);
        Serializable id = getSession().save(accessControlList);
        node.setAccessControlList(accessControlList);
			
        // throw the reference away and get the a new one for the id
        accessControlList = (DbAccessControlList) getSession().load(DbAccessControlListImpl.class, id);
        assertNotNull("Access control list not found", accessControlList);
        assertTrue(accessControlList.getInherits());
        
        // Update inherits 
        
        accessControlList.setInherits(false);
        id = getSession().save(accessControlList);
        
        // throw the reference away and get the a new one for the id
        accessControlList = (DbAccessControlList) getSession().load(DbAccessControlListImpl.class, id);
        assertNotNull("Node not found", accessControlList);
        assertFalse(accessControlList.getInherits());
	}
    
    public void testSimplePermission()
    {
        DbPermission permission = new DbPermissionImpl();
        permission.setTypeQname(qname);
        permission.setName("Test");
        
        Serializable id = getSession().save(permission);
        
        // throw the reference away and get the a new one for the id
        permission = (DbPermission) getSession().load(DbPermissionImpl.class, id);
        assertNotNull("Permission not found", permission);
        assertEquals(qname, permission.getTypeQname());
    }
    
    public void testSimpleAuthority()
    {
        DbAuthority authority = new DbAuthorityImpl();
        authority.setRecipient("Test");
        authority.getExternalKeys().add("One");
        
        Serializable id = getSession().save(authority);
        
        // throw the reference away and get the a new one for the id
        authority = (DbAuthority) getSession().load(DbAuthorityImpl.class, id);
        assertNotNull("Node not found", authority);
        assertEquals("Test", authority.getRecipient());
        assertEquals(1, authority.getExternalKeys().size());
        
        // Update
        
        authority.getExternalKeys().add("Two");
        id  = getSession().save(authority);
      
        // throw the reference away and get the a new one for the id
        authority = (DbAuthority) getSession().load(DbAuthorityImpl.class, id);
        assertNotNull("Node not found", authority);
        assertEquals("Test", authority.getRecipient());
        assertEquals(2, authority.getExternalKeys().size());
        
        
        // complex
        
        authority.getExternalKeys().add("Three");
        authority.getExternalKeys().remove("One");
        authority.getExternalKeys().remove("Two");
        id  = getSession().save(authority);
        
        // Throw the reference away and get the a new one for the id
        authority = (DbAuthority) getSession().load(DbAuthorityImpl.class, id);
        assertNotNull("Node not found", authority);
        assertEquals("Test", authority.getRecipient());
        assertEquals(1, authority.getExternalKeys().size());
    }
    
    public void testAccessControlList()
    {
        // create a new access control list for the node
        DbAccessControlList accessControlList = new DbAccessControlListImpl();
        accessControlList.setInherits(true);
        Serializable nodeAclId = getSession().save(accessControlList);
        node.setAccessControlList(accessControlList);
        
        DbAuthority recipient = new DbAuthorityImpl();
        recipient.setRecipient("Test");
        recipient.getExternalKeys().add("One");
        getSession().save(recipient);
        
        DbPermission permission = new DbPermissionImpl();
        permission.setTypeQname(qname);
        permission.setName("Test");
        getSession().save(permission);
        
        DbAccessControlEntry accessControlEntry = accessControlList.newEntry(permission, recipient, true);
        Long aceEntryId = accessControlEntry.getId();
        assertNotNull("Entry is still transient", aceEntryId);
        
        accessControlEntry = (DbAccessControlEntry) getSession().load(DbAccessControlEntryImpl.class, aceEntryId);
        assertNotNull("Permission entry not found", accessControlEntry);
        assertTrue(accessControlEntry.isAllowed());
        assertNotNull(accessControlEntry.getAccessControlList());
        assertTrue(accessControlEntry.getAccessControlList().getInherits());
        assertNotNull(accessControlEntry.getPermission());
        assertEquals("Test", accessControlEntry.getPermission().getKey().getName());
        assertNotNull(accessControlEntry.getAuthority());
        assertEquals("Test", accessControlEntry.getAuthority().getRecipient());
        assertEquals(1, accessControlEntry.getAuthority().getExternalKeys().size());
        
        // Check that deletion of the list cascades
        node.setAccessControlList(null);
        getSession().delete(accessControlList);
        DbAccessControlEntry deletedAcl = (DbAccessControlEntry) getSession().get(DbAccessControlListImpl.class, nodeAclId);
        assertNull("Access control list was not deleted", deletedAcl);
        DbAccessControlEntry deletedAclEntry = (DbAccessControlEntry) getSession().get(DbAccessControlEntryImpl.class, aceEntryId);
        assertNull("Access control entries were not cascade deleted", deletedAclEntry);
    }
}