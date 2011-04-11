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
package org.alfresco.repo.ownable.impl;

import java.io.Serializable;
import java.util.HashMap;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.permissions.dynamic.OwnerDynamicAuthority;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

public class OwnableServiceTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private NodeService nodeService;

    private MutableAuthenticationService authenticationService;
    
    private AuthenticationComponent authenticationComponent;
    
    private MutableAuthenticationDao authenticationDAO;

    private OwnableService ownableService;

    private NodeRef rootNodeRef;

    private UserTransaction txn;
    
    private PermissionService permissionService;
    
    private OwnerDynamicAuthority dynamicAuthority;
    
    public OwnableServiceTest()
    {
        super();
    }

    public OwnableServiceTest(String arg0)
    {
        super(arg0);
    }
    
    public void setUp() throws Exception
    {
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            throw new AlfrescoRuntimeException(
                    "A previous tests did not clean up transaction: " +
                    AlfrescoTransactionSupport.getTransactionId());
        }
        
        nodeService = (NodeService) ctx.getBean("nodeService");
        authenticationService = (MutableAuthenticationService) ctx.getBean("authenticationService");
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        ownableService = (OwnableService) ctx.getBean("ownableService");
        permissionService = (PermissionService) ctx.getBean("permissionService");
    
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        authenticationDAO = (MutableAuthenticationDao) ctx.getBean("authenticationDao");
        
        
        TransactionService transactionService = (TransactionService) ctx.getBean(ServiceRegistry.TRANSACTION_SERVICE.getLocalName());
        txn = transactionService.getUserTransaction();
        txn.begin();
        
        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
        permissionService.setPermission(rootNodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ADD_CHILDREN, true);
        
        if(authenticationDAO.userExists("andy"))
        {
            authenticationService.deleteAuthentication("andy");
        }
        authenticationService.createAuthentication("andy", "andy".toCharArray());
        
        dynamicAuthority = new OwnerDynamicAuthority();
        dynamicAuthority.setOwnableService(ownableService);
       
        authenticationComponent.clearCurrentSecurityContext();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
            txn.rollback();
        }
        catch (Throwable e)
        {
            // don't absorb any exceptions going past
        }
        super.tearDown();
    }
    
    public void testSetup()
    {
        assertNotNull(nodeService);
        assertNotNull(authenticationService);
        assertNotNull(ownableService);
    }
    
    public void testUnSet()
    {
        assertNull(ownableService.getOwner(rootNodeRef));
        assertFalse(ownableService.hasOwner(rootNodeRef));
    }
    
    public void testCMObject()
    {
        authenticationService.authenticate("andy", "andy".toCharArray());
        NodeRef testNode = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_PERSON, ContentModel.TYPE_CMOBJECT, null).getChildRef();
        permissionService.setPermission(rootNodeRef, "andy", PermissionService.TAKE_OWNERSHIP, true);
        assertEquals("andy", ownableService.getOwner(testNode));
        assertTrue(ownableService.hasOwner(testNode));
        assertTrue(nodeService.hasAspect(testNode, ContentModel.ASPECT_AUDITABLE));
        assertFalse(nodeService.hasAspect(testNode, ContentModel.ASPECT_OWNABLE));
        assertTrue(dynamicAuthority.hasAuthority(testNode, "andy"));
        
        assertEquals("andy", ownableService.getOwner(testNode));
        
//        nodeService.setProperty(testNode, ContentModel.PROP_CREATOR, "woof");
//        assertEquals("woof", ownableService.getOwner(testNode));
//        
//        nodeService.setProperty(testNode, ContentModel.PROP_CREATOR, "andy");
//        assertEquals("andy", ownableService.getOwner(testNode));
//        
        permissionService.setInheritParentPermissions(testNode, false);
        
        
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(rootNodeRef, PermissionService.TAKE_OWNERSHIP));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(rootNodeRef, PermissionService.SET_OWNER));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.TAKE_OWNERSHIP));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.SET_OWNER));
        
        permissionService.setPermission(rootNodeRef, "andy", PermissionService.WRITE_PROPERTIES, true);
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rootNodeRef, PermissionService.TAKE_OWNERSHIP));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rootNodeRef, PermissionService.SET_OWNER));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.TAKE_OWNERSHIP));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.SET_OWNER));
        
       
        
        ownableService.setOwner(testNode, "woof");
        assertEquals("woof", ownableService.getOwner(testNode));
        assertTrue(dynamicAuthority.hasAuthority(testNode, "woof"));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.TAKE_OWNERSHIP));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.SET_OWNER));
        
        
        ownableService.setOwner(testNode, "muppet");
        assertEquals("muppet", ownableService.getOwner(testNode));
        assertTrue(dynamicAuthority.hasAuthority(testNode, "muppet"));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.TAKE_OWNERSHIP));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.SET_OWNER));
        
        
        ownableService.takeOwnership(testNode);
        assertEquals("andy", ownableService.getOwner(testNode));
        assertTrue(dynamicAuthority.hasAuthority(testNode, "andy"));
        assertTrue(nodeService.hasAspect(testNode, ContentModel.ASPECT_AUDITABLE));
        assertTrue(nodeService.hasAspect(testNode, ContentModel.ASPECT_OWNABLE));
      
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rootNodeRef, PermissionService.TAKE_OWNERSHIP));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rootNodeRef, PermissionService.SET_OWNER));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.TAKE_OWNERSHIP));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.SET_OWNER));
        
        nodeService.setProperty(testNode, ContentModel.PROP_OWNER, "muppet");
        assertEquals("muppet", ownableService.getOwner(testNode));
        nodeService.removeAspect(testNode, ContentModel.ASPECT_OWNABLE);
        assertEquals("andy", ownableService.getOwner(testNode));
        
        HashMap<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
        aspectProperties.put(ContentModel.PROP_OWNER, "muppet");
        nodeService.addAspect(testNode, ContentModel.ASPECT_OWNABLE, aspectProperties);
        assertEquals("muppet", ownableService.getOwner(testNode));
        
       
    }
    
    public void testContainer()
    {  
        authenticationService.authenticate("andy", "andy".toCharArray());
        NodeRef testNode = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_PERSON, ContentModel.TYPE_CONTAINER, null).getChildRef();
        assertNull(ownableService.getOwner(testNode));
        assertFalse(ownableService.hasOwner(testNode));
        assertFalse(nodeService.hasAspect(testNode, ContentModel.ASPECT_AUDITABLE));
        assertFalse(nodeService.hasAspect(testNode, ContentModel.ASPECT_OWNABLE));
        assertFalse(dynamicAuthority.hasAuthority(testNode, "andy"));
        
        assertFalse(permissionService.hasPermission(testNode, PermissionService.READ) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(testNode, permissionService.getAllPermission()) == AccessStatus.ALLOWED);
        
        permissionService.setPermission(rootNodeRef, permissionService.getOwnerAuthority(), permissionService.getAllPermission(), true);
        
        ownableService.setOwner(testNode, "muppet");
        assertEquals("muppet", ownableService.getOwner(testNode));
        ownableService.takeOwnership(testNode);
        assertEquals("andy", ownableService.getOwner(testNode));
        assertFalse(nodeService.hasAspect(testNode, ContentModel.ASPECT_AUDITABLE));
        assertTrue(nodeService.hasAspect(testNode, ContentModel.ASPECT_OWNABLE));
        assertTrue(dynamicAuthority.hasAuthority(testNode, "andy"));
        
        assertTrue(permissionService.hasPermission(testNode, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(testNode, permissionService.getAllPermission())== AccessStatus.ALLOWED);
        
        
    }
    
}
