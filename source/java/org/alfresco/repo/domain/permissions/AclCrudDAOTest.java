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
package org.alfresco.repo.domain.permissions;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see AclCrudDAO
 * 
 * @author janv
 * @since 3.4
 */
public class AclCrudDAOTest extends TestCase
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private AclCrudDAO aclCrudDAO;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        aclCrudDAO = (AclCrudDAO)ctx.getBean("aclCrudDAO");
    }
    
    // TODO - alf_access_control_list, alf_acl_member, alf_access_control_entry
    
    //
    // alf_acl_change_set
    //
    
    private long createAclChangeSet() throws Exception
    {
        RetryingTransactionCallback<Long> callback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                return aclCrudDAO.createAclChangeSet();
            }
        };
        return txnHelper.doInTransaction(callback);
    }
    
    private void deleteAclChangeSet(final long aclChangeSetId) throws Exception
    {
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                aclCrudDAO.deleteAclChangeSet(aclChangeSetId);
                return null;
            }
        };
        txnHelper.doInTransaction(callback);
    }
    
    private AclChangeSet getAclChangeSet(final long aclChangeSetId) throws Exception
    {
        RetryingTransactionCallback<AclChangeSet> callback = new RetryingTransactionCallback<AclChangeSet>()
        {
            public AclChangeSet execute() throws Throwable
            {
                return aclCrudDAO.getAclChangeSet(aclChangeSetId);
            }
        };
        return txnHelper.doInTransaction(callback);
    }
    
    public void testCreateAndDeleteAclChangeSet() throws Exception
    {
        long aclChangeSetId = createAclChangeSet();
        
        AclChangeSet acsEntity= getAclChangeSet(aclChangeSetId);
        assertNotNull(acsEntity);
        assertEquals(new Long(aclChangeSetId), acsEntity.getId());
        
        deleteAclChangeSet(aclChangeSetId);
        
        assertNull(getAclChangeSet(aclChangeSetId));
    }
    
    public void testCreateAclChangeSetWithRollback() throws Exception
    {
        final List<Long> tmp = new ArrayList<Long>(1);
        
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                long acsEntityId = createAclChangeSet();
                tmp.add(acsEntityId);
                
                // Now force a rollback
                throw new RuntimeException("Forced");
            }
        };
        
        try
        {
            txnHelper.doInTransaction(callback);
            fail("Transaction didn't roll back");
        }
        catch (RuntimeException e)
        {
            // Expected
        }
        
        assertEquals(1, tmp.size());
        
        // Check that it doesn't exist
        assertNull(getAclChangeSet(tmp.get(0)));
    }
    
    //
    // alf_authority
    //
    
    private Authority createAuth(final String authName) throws Exception
    {
        RetryingTransactionCallback<Authority> callback = new RetryingTransactionCallback<Authority>()
        {
            public Authority execute() throws Throwable
            {
                return aclCrudDAO.getOrCreateAuthority(authName);
            }
        };
        return txnHelper.doInTransaction(callback);
    }
    
    private void deleteAuth(final long authId) throws Exception
    {
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                aclCrudDAO.deleteAuthority(authId);
                return null;
            }
        };
        txnHelper.doInTransaction(callback);
    }
    
    private void updateAuth(final String before, final String after) throws Exception
    {
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                aclCrudDAO.renameAuthority(before, after);
                return null;
            }
        };
        txnHelper.doInTransaction(callback);
    }
    
    private Authority getAuth(final String authName) throws Exception
    {
        RetryingTransactionCallback<Authority> callback = new RetryingTransactionCallback<Authority>()
        {
            public Authority execute() throws Throwable
            {
                return aclCrudDAO.getAuthority(authName);
            }
        };
        return txnHelper.doInTransaction(callback);
    }
    
    public void testCreateUpdateAndDeleteAuth() throws Exception
    {
        final String authName = getName() + "-" + System.currentTimeMillis();
        
        Authority authEntity= getAuth(authName);
        assertNull(authEntity);
        
        Authority createAuthEntity = createAuth(authName);
        assertNotNull(createAuthEntity);
        
        authEntity= getAuth(authName);
        assertEquals(createAuthEntity, authEntity);
        
        String newAuthName = authName+"-new";
        updateAuth(authName, newAuthName);
        
        assertNull(getAuth(authName));
        
        authEntity = getAuth(newAuthName);
        assertNotNull(authEntity);
        assertEquals(createAuthEntity.getId(), authEntity.getId());
        assertEquals(newAuthName, authEntity.getAuthority());
        
        deleteAuth(authEntity.getId());
        
        assertNull(getAuth(newAuthName));
    }
    
    public void testCreateAuthWithRollback() throws Exception
    {
        final String authName = getName() + "-" + System.currentTimeMillis();
        
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                createAuth(authName);
                // Now force a rollback
                throw new RuntimeException("Forced");
            }
        };
        
        try
        {
            txnHelper.doInTransaction(callback);
            fail("Transaction didn't roll back");
        }
        catch (RuntimeException e)
        {
            // Expected
        }
        
        // Check that it doesn't exist
        assertNull(getAuth(authName));
    }
    
    //
    // alf_permission
    //
    
    private Permission createPermission(final PermissionReference permissionReference) throws Exception
    {
        RetryingTransactionCallback<Permission> callback = new RetryingTransactionCallback<Permission>()
        {
            public Permission execute() throws Throwable
            {
                return aclCrudDAO.getOrCreatePermission(permissionReference);
            }
        };
        return txnHelper.doInTransaction(callback);
    }
    
    private void deletePermission(final long permissionId) throws Exception
    {
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                aclCrudDAO.deletePermission(permissionId);
                return null;
            }
        };
        txnHelper.doInTransaction(callback);
    }
    
    private Permission getPermission(final PermissionReference permissionReference) throws Exception
    {
        RetryingTransactionCallback<Permission> callback = new RetryingTransactionCallback<Permission>()
        {
            public Permission execute() throws Throwable
            {
                return aclCrudDAO.getPermission(permissionReference);
            }
        };
        return txnHelper.doInTransaction(callback);
    }
    
    public void testCreateAndDeletePermission() throws Exception
    {
        String name = getName() + "-" + System.currentTimeMillis();
        final SimplePermissionReference permRef = SimplePermissionReference.getPermissionReference(QName.createQName("cm:cmobject"), name);
        
        Permission createdPermEntity = createPermission(permRef);
        assertNotNull(createdPermEntity);
        
        Permission permEntity = getPermission(permRef);
        assertEquals(createdPermEntity, permEntity);
        
        deletePermission(permEntity.getId());
        
        assertNull(getPermission(permRef));
    }
    
    public void testCreatePermissionWithRollback() throws Exception
    {
        String name = getName() + "-" + System.currentTimeMillis();
        final SimplePermissionReference permRef = SimplePermissionReference.getPermissionReference(QName.createQName("cm:cmobject"), name);
        
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                createPermission(permRef);
                // Now force a rollback
                throw new RuntimeException("Forced");
            }
        };
        
        try
        {
            txnHelper.doInTransaction(callback);
            fail("Transaction didn't roll back");
        }
        catch (RuntimeException e)
        {
            // Expected
        }
        
        // Check that it doesn't exist
        assertNull(getPermission(permRef));
    }
}
