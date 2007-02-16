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
package org.alfresco.repo.security.permissions.dynamic;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

public class LockOwnerDynamicAuthorityTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private NodeService nodeService;

    private AuthenticationService authenticationService;

    private AuthenticationComponent authenticationComponent;
    
    private MutableAuthenticationDao authenticationDAO;

    private LockService lockService;
    
    private NodeRef rootNodeRef;

    private UserTransaction userTransaction;

    private PermissionService permissionService;

    private LockOwnerDynamicAuthority dynamicAuthority;

    private CheckOutCheckInService checkOutCheckInService;
    
    private OwnableService ownableService;
    
    public LockOwnerDynamicAuthorityTest()
    {
        super();
    }

    public LockOwnerDynamicAuthorityTest(String arg0)
    {
        super(arg0);
    }

    public void setUp() throws Exception
    {
        nodeService = (NodeService) ctx.getBean("nodeService");
        authenticationService = (AuthenticationService) ctx.getBean("authenticationService");
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        lockService = (LockService) ctx.getBean("lockService");
        permissionService = (PermissionService) ctx.getBean("permissionService");
        authenticationDAO = (MutableAuthenticationDao) ctx.getBean("alfDaoImpl");

        checkOutCheckInService = (CheckOutCheckInService) ctx.getBean("checkOutCheckInService");
        ownableService = (OwnableService) ctx.getBean("ownableService");
        
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        TransactionService transactionService = (TransactionService) ctx.getBean(ServiceRegistry.TRANSACTION_SERVICE
                .getLocalName());
        userTransaction = transactionService.getUserTransaction();
        userTransaction.begin();

        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
        permissionService.setPermission(rootNodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ADD_CHILDREN,
                true);

        if (authenticationDAO.userExists("andy"))
        {
            authenticationService.deleteAuthentication("andy");
        }
        authenticationService.createAuthentication("andy", "andy".toCharArray());
        if (authenticationDAO.userExists("lemur"))
        {
            authenticationService.deleteAuthentication("lemur");
        }
        authenticationService.createAuthentication("lemur", "lemur".toCharArray());
        if (authenticationDAO.userExists("frog"))
        {
            authenticationService.deleteAuthentication("frog");
        }
        authenticationService.createAuthentication("frog", "frog".toCharArray());

        dynamicAuthority = new LockOwnerDynamicAuthority();
        dynamicAuthority.setLockService(lockService);

        authenticationComponent.clearCurrentSecurityContext();
    }

    @Override
    protected void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        userTransaction.rollback();
        super.tearDown();
    }

    public void testSetup()
    {
        assertNotNull(nodeService);
        assertNotNull(authenticationService);
        assertNotNull(lockService);
    }

    public void testUnSet()
    {
        permissionService.setPermission(rootNodeRef, "andy", PermissionService.ALL_PERMISSIONS, true);
        authenticationService.authenticate("andy", "andy".toCharArray());
        assertEquals(LockStatus.NO_LOCK, lockService.getLockStatus(rootNodeRef));
        authenticationService.clearCurrentSecurityContext();
    }

    public void testPermissionWithNoLockAspect()
    {
        authenticationService.authenticate("andy", "andy".toCharArray());
        NodeRef testNode = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_PERSON,
                ContentModel.TYPE_CMOBJECT, null).getChildRef();
        assertNotNull(testNode);
        permissionService.setPermission(rootNodeRef, "andy", PermissionService.ALL_PERMISSIONS, true);
     
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rootNodeRef,
                PermissionService.LOCK));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(rootNodeRef,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rootNodeRef, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(rootNodeRef, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(rootNodeRef, PermissionService.CANCEL_CHECK_OUT));
    }
    
    public void testPermissionWithLockAspect()
    {
        permissionService.setPermission(rootNodeRef, "andy", PermissionService.ALL_PERMISSIONS, true);
        permissionService.setPermission(rootNodeRef, "lemur", PermissionService.CHECK_OUT, true);
        permissionService.setPermission(rootNodeRef, "lemur", PermissionService.WRITE, true);
        permissionService.setPermission(rootNodeRef, "lemur", PermissionService.READ, true);
        permissionService.setPermission(rootNodeRef, "frog", PermissionService.CHECK_OUT, true);
        permissionService.setPermission(rootNodeRef, "frog", PermissionService.WRITE, true);
        permissionService.setPermission(rootNodeRef, "frog", PermissionService.READ, true);
        authenticationService.authenticate("andy", "andy".toCharArray());
        NodeRef testNode = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_PERSON,
                ContentModel.TYPE_CMOBJECT, null).getChildRef();
        lockService.lock(testNode, LockType.READ_ONLY_LOCK);
       
     
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        authenticationService.authenticate("lemur", "lemur".toCharArray());
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        authenticationService.authenticate("andy", "andy".toCharArray());
        lockService.unlock(testNode);
        authenticationService.authenticate("lemur", "lemur".toCharArray());
        lockService.lock(testNode, LockType.READ_ONLY_LOCK);
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        
        authenticationService.authenticate("frog", "frog".toCharArray());
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
    }

    public void testCheckOutCheckInAuthorities()
    {
        permissionService.setPermission(rootNodeRef, "andy", PermissionService.ALL_PERMISSIONS, true);
        permissionService.setPermission(rootNodeRef, "lemur", PermissionService.CHECK_OUT, true);
        permissionService.setPermission(rootNodeRef, "lemur", PermissionService.WRITE, true);
        permissionService.setPermission(rootNodeRef, "lemur", PermissionService.READ, true);
        permissionService.setPermission(rootNodeRef, "frog", PermissionService.CHECK_OUT, true);
        permissionService.setPermission(rootNodeRef, "frog", PermissionService.WRITE, true);
        permissionService.setPermission(rootNodeRef, "frog", PermissionService.READ, true);

        authenticationService.authenticate("andy", "andy".toCharArray());
        NodeRef testNode = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_PERSON,
                ContentModel.TYPE_CMOBJECT, null).getChildRef();
        permissionService.setPermission(rootNodeRef, "andy", PermissionService.ALL_PERMISSIONS, false);

     
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        authenticationService.authenticate("lemur", "lemur".toCharArray());
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        authenticationService.authenticate("frog", "frog".toCharArray());
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        // Check out as frog
        NodeRef workingCopy = checkOutCheckInService.checkout(testNode);
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy,
                PermissionService.LOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy, PermissionService.CANCEL_CHECK_OUT));
     
        authenticationService.authenticate("lemur", "lemur".toCharArray());
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy,
                PermissionService.LOCK));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(workingCopy,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(workingCopy, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(workingCopy, PermissionService.CANCEL_CHECK_OUT));
        
     
        // set owner ...frog only has permissions of dynamic lock owner in wc and sourec
        authenticationService.authenticate("frog", "frog".toCharArray());
        ownableService.setOwner(workingCopy, "lemur");
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy,
                PermissionService.LOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy, PermissionService.CANCEL_CHECK_OUT));
        
        // test the new owner..
        authenticationService.authenticate("lemur", "lemur".toCharArray());
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy,
                PermissionService.LOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(workingCopy, PermissionService.CANCEL_CHECK_OUT));
        
        authenticationService.authenticate("frog", "frog".toCharArray());
        checkOutCheckInService.cancelCheckout(workingCopy);
        
        authenticationService.authenticate("andy", "andy".toCharArray());
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        authenticationService.authenticate("lemur", "lemur".toCharArray());
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        authenticationService.authenticate("frog", "frog".toCharArray());
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
       
        
        authenticationService.authenticate("frog", "frog".toCharArray());
        workingCopy = checkOutCheckInService.checkout(testNode);
        ownableService.setOwner(workingCopy, "lemur");
        checkOutCheckInService.checkin(workingCopy, null);
        
    }
   
    public void testCeckInCheckOut()
    {

        permissionService.setPermission(rootNodeRef, "andy", PermissionService.ALL_PERMISSIONS, true);
        permissionService.setPermission(rootNodeRef, "lemur", PermissionService.CHECK_OUT, true);
        permissionService.setPermission(rootNodeRef, "lemur", PermissionService.WRITE, true);
        permissionService.setPermission(rootNodeRef, "lemur", PermissionService.READ, true);
        permissionService.setPermission(rootNodeRef, "frog", PermissionService.CHECK_OUT, true);
        permissionService.setPermission(rootNodeRef, "frog", PermissionService.WRITE, true);
        permissionService.setPermission(rootNodeRef, "frog", PermissionService.READ, true);
        authenticationService.authenticate("andy", "andy".toCharArray());
        NodeRef testNode = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_PERSON,
                ContentModel.TYPE_CMOBJECT, null).getChildRef();
        lockService.lock(testNode, LockType.READ_ONLY_LOCK);
       
     
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        authenticationService.authenticate("lemur", "lemur".toCharArray());
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        authenticationService.authenticate("andy", "andy".toCharArray());
        lockService.unlock(testNode);
        authenticationService.authenticate("lemur", "lemur".toCharArray());
        lockService.lock(testNode, LockType.READ_ONLY_LOCK);
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
        
        
        authenticationService.authenticate("frog", "frog".toCharArray());
        
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode,
                PermissionService.LOCK));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode,
                PermissionService.UNLOCK));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testNode, PermissionService.CHECK_OUT));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CHECK_IN));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.CANCEL_CHECK_OUT));
    }
}
