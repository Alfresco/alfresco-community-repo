/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.lock.mem;

import java.util.Date;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.ConcurrencyFailureException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Integration tests that check transaction related functionality of {@link LockStore} implementations.
 * @author Matt Ward
 */
public abstract class AbstractLockStoreTxTest<T extends LockStore>
{
    /**
     * Instance of the Class Under Test.
     */
    protected T lockStore;
    
    protected static ApplicationContext ctx;
    protected static TransactionService transactionService;
    
    /**
     * Concrete subclasses must implement this method to provide the tests with a LockStore instance.
     * 
     * @return LockStore to test
     */
    protected abstract T createLockStore();
    
    @BeforeClass
    public static void setUpSpringContext()
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        transactionService = (TransactionService) ctx.getBean("TransactionService");
    }
    
    @Before
    public void setUpLockStore()
    {
        lockStore = createLockStore();
    }
    
    /**
     * <ul>
     *   <li>Start outer txn</li>
     *   <li>Modify lock in outer txn</li>
     *   <li>Start inner txn</li>
     *   <li>Modify lock in inner txn</li>
     * </ul>
     * Inner transaction should fail while outer succeeds
     */
    @Test
    public void testRepeatableRead_01() throws Exception
    {
        
    }
    
    @Test
    public void testRepeatableReadsInTransaction() throws NotSupportedException, SystemException
    {
        final TransactionService txService = (TransactionService) ctx.getBean("TransactionService");
        UserTransaction txA = txService.getUserTransaction();
        
        final NodeRef nodeRef = new NodeRef("workspace://SpacesStore/UUID-1");
        final NodeRef nodeRef2 = new NodeRef("workspace://SpacesStore/UUID-2");
        Date now = new Date();
        Date expires = new Date(now.getTime() + 180000);
        final LockState lockState1 = LockState.createLock(nodeRef, LockType.WRITE_LOCK,
                    "jbloggs", expires, Lifetime.EPHEMERAL, null);
        

        Thread txB = new Thread("TxB")
        {
            @Override
            public void run()
            {
                Object main = AbstractLockStoreTxTest.this;
                UserTransaction tx = txService.getUserTransaction();
                try
                {
                    tx.begin();
                    try
                    {                        
                        // txB read lock state
                        LockState lockState = lockStore.get(nodeRef);
                        assertEquals("jbloggs", lockState.getOwner());
                        assertEquals(Lifetime.EPHEMERAL, lockState.getLifetime());
                        
                        // Wait, while txA changes the lock state
                        passControl(this, main);
                        
                        // assert txB still sees state A
                        lockState = lockStore.get(nodeRef);
                        assertEquals("jbloggs", lockState.getOwner());
                        
                        // Wait, while txA checks whether it can see lock for nodeRef2 (though it doesn't exist yet)
                        passControl(this, main);
                        
                        // txB sets a value, already seen as non-existent lock by txA
                        lockStore.set(nodeRef2, LockState.createLock(nodeRef2, LockType.WRITE_LOCK,
                                    "csmith", null, Lifetime.EPHEMERAL, null));
                    }
                    finally
                    {
                        tx.rollback();
                    }
                }
                catch (Throwable e)
                {
                    throw new RuntimeException("Error in transaction B", e);
                }
                finally
                {
                    // Stop 'main' from waiting
                    synchronized(main)
                    {
                        main.notifyAll();
                    }
                }
            }  
        };
        
        txA.begin();
        try
        {
            // txA set lock state 1
            lockStore.set(nodeRef, lockState1);
            
            // Wait while txB reads and checks the LockState
            txB.setDaemon(true);
            txB.start();
            passControl(this, txB);
            
            // txA set different lock state
            AuthenticationUtil.setFullyAuthenticatedUser("jbloggs"); // Current lock owner needed to change lock.
            final LockState lockState2 = LockState.createWithOwner(lockState1, "another");
            lockStore.set(nodeRef, lockState2);
            
            // Wait while txB reads/checks the LockState again for nodeRef
            passControl(this, txB);
            
            // Another update
            AuthenticationUtil.setFullyAuthenticatedUser("another"); // Current lock owner needed to change lock.
            final LockState lockState3 = LockState.createWithOwner(lockState2, "bsmith");
            lockStore.set(nodeRef, lockState3);
            // Check we can see the update.
            assertEquals("bsmith", lockStore.get(nodeRef).getOwner());
            
            // Perform a read, that we know will retrieve a null value
            assertNull("nodeRef2 LockState", lockStore.get(nodeRef2));
            
            // Wait while txB populates the store with a value for nodeRef2
            passControl(this, txB);
            
            // Perform the read again - update should not be visible in this transaction
            assertNull("nodeRef2 LockState", lockStore.get(nodeRef2));            
        }
        finally
        {
            txA.rollback();
        }
    }
    
    protected void passControl(Object from, Object to)
    {
        synchronized(to)
        {
            to.notifyAll();
        }
        synchronized(from)
        {
            try
            {
                // TODO: wait should be called in a loop with repeated wait condition check,
                //       but what's the condition we're waiting on?
                from.wait(10000);
            }
            catch (InterruptedException error)
            {
                throw new RuntimeException(error);
            }
        }
    }
    
    @Test
    public void testCannotSetLockWhenChangedByAnotherTx() throws NotSupportedException, SystemException
    {
        final TransactionService txService = (TransactionService) ctx.getBean("TransactionService");
        UserTransaction txA = txService.getUserTransaction();
        final NodeRef nodeRef = new NodeRef("workspace://SpacesStore/UUID-1");
        Date now = new Date();
        Date expires = new Date(now.getTime() + 180000);
        final LockState lockState1 = LockState.createLock(nodeRef, LockType.WRITE_LOCK,
                    "jbloggs", expires, Lifetime.EPHEMERAL, null);
        

        Thread txB = new Thread("TxB")
        {
            @Override
            public void run()
            {
                Object main = AbstractLockStoreTxTest.this;
                UserTransaction tx = txService.getUserTransaction();
                try
                {
                    tx.begin();
                    try
                    {                        
                        // txB read lock state
                        LockState lockState = lockStore.get(nodeRef);
                        assertEquals("jbloggs", lockState.getOwner());
                        assertEquals(Lifetime.EPHEMERAL, lockState.getLifetime());
                        
                        // Wait, while txA changes the lock state
                        passControl(this, main);
                        
                        try
                        {
                            // Attempt to change the lock state for a NodeRef should fail
                            // when it has been modified by another tx since this tx last inspected it.
                            AuthenticationUtil.setFullyAuthenticatedUser("jbloggs"); // Current lock owner
                            lockStore.set(nodeRef, LockState.createLock(nodeRef, LockType.WRITE_LOCK,
                                        "csmith", null, Lifetime.EPHEMERAL, null));
                            fail("Exception should have been thrown but was not.");
                        }
                        catch (ConcurrencyFailureException e)
                        {
                            // Good!
                        }
                    }
                    finally
                    {
                        tx.rollback();
                    }
                }
                catch (Throwable e)
                {
                    throw new RuntimeException("Error in transaction B", e);
                }
                finally
                {
                    // Stop 'main' from waiting
                    synchronized(main)
                    {
                        main.notifyAll();
                    }
                }
            }  
        };
        
        txA.begin();
        try
        {
            // txA set lock state 1
            lockStore.set(nodeRef, lockState1);
            
            // Wait while txB reads and checks the LockState
            txB.setDaemon(true);
            txB.start();
            passControl(this, txB);
            
            // txA set different lock state
            AuthenticationUtil.setFullyAuthenticatedUser("jbloggs"); // Current lock owner needed to change lock.
            final LockState lockState2 = LockState.createWithOwner(lockState1, "another");
            lockStore.set(nodeRef, lockState2);
            
            // Wait while txB attempts to modify the lock info
            passControl(this, txB);
            
            // Lock shouldn't have changed since this tx updated it.
            assertEquals(lockState2, lockStore.get(nodeRef));
        }
        finally
        {
            txA.rollback();
        }
    }
    
    @Test
    public void testCanChangeLockIfLatestValueIsHeldEvenIfAlreadyChangedByAnotherTx() throws NotSupportedException, SystemException
    {
        final TransactionService txService = (TransactionService) ctx.getBean("TransactionService");
        UserTransaction txA = txService.getUserTransaction();
        final NodeRef nodeRef = new NodeRef("workspace://SpacesStore/UUID-1");
        final Date now = new Date();
        Date expired = new Date(now.getTime() - 180000);
        final LockState lockState1 = LockState.createLock(nodeRef, LockType.WRITE_LOCK,
                    "jbloggs", expired, Lifetime.EPHEMERAL, null);
        
        final LockState lockState2 = LockState.createWithOwner(lockState1, "another");
        
        Thread txB = new Thread("TxB")
        {
            @Override
            public void run()
            {
                Object main = AbstractLockStoreTxTest.this;
                UserTransaction tx = txService.getUserTransaction();
                try
                {
                    tx.begin();
                    try
                    {
                        AuthenticationUtil.setFullyAuthenticatedUser("new-user");
                        
                        // txB read lock state
                        LockState readLockState = lockStore.get(nodeRef);
                        assertEquals(lockState2, readLockState);
                        
                        // Set new value, even though txA has already set new values
                        // (but not since this tx's initial read)
                        Date expiresFuture = new Date(now.getTime() + 180000);
                        final LockState newUserLockState = LockState.createLock(nodeRef, LockType.WRITE_LOCK,
                                    "new-user", expiresFuture, Lifetime.EPHEMERAL, null);
                        lockStore.set(nodeRef, newUserLockState);
                        
                        // Read
                        assertEquals(newUserLockState, lockStore.get(nodeRef));
                    }
                    finally
                    {
                        tx.rollback();
                    }
                }
                catch (Throwable e)
                {
                    throw new RuntimeException("Error in transaction B", e);
                }
                finally
                {
                    // Stop 'main' from waiting
                    synchronized(main)
                    {
                        main.notifyAll();
                    }
                }
            }  
        };
        
        txA.begin();
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser("jbloggs"); // Current lock owner needed to change lock.
            
            // txA set lock state 1
            lockStore.set(nodeRef, lockState1);
            assertEquals(lockState1, lockStore.get(nodeRef));
            
            // txA set different lock state
            lockStore.set(nodeRef, lockState2);
            assertEquals(lockState2, lockStore.get(nodeRef));
            
            // Wait while txB modifies the lock info
            txB.setDaemon(true);
            txB.start();
            passControl(this, txB);
            
            // This tx should still see the same state, though it has been changed by txB.
            assertEquals(lockState2, lockStore.get(nodeRef));
        }
        finally
        {
            txA.rollback();
        }
    }
    
    
    @Test
    public void testNotOnlyCurrentLockOwnerCanChangeInfo() throws NotSupportedException, SystemException
    {
        final TransactionService txService = (TransactionService) ctx.getBean("TransactionService");
        UserTransaction txA = txService.getUserTransaction();
        final NodeRef nodeRef = new NodeRef("workspace://SpacesStore/UUID-1");
        Date now = new Date();
        Date expires = new Date(now.getTime() + 180000);
        final LockState lockState1 = LockState.createLock(nodeRef, LockType.WRITE_LOCK,
                    "jbloggs", expires, Lifetime.EPHEMERAL, null);
        
        txA.begin();
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser("jbloggs");
            
            // Set initial lock state
            lockStore.set(nodeRef, lockState1);
            
            // Set different lock state
            // Current lock owner is still authenticated (jbloggs)
            final LockState lockState2 = LockState.createWithOwner(lockState1, "csmith");
            lockStore.set(nodeRef, lockState2);
            
            // Check update
            assertEquals(lockState2, lockStore.get(nodeRef));

            // Incorrect lock owner - this shouldn't fail. See ACE-2181
            final LockState lockState3 = LockState.createWithOwner(lockState1, "dsmithers");

            lockStore.set(nodeRef, lockState3);
            
            // Check update.
            assertEquals(lockState3, lockStore.get(nodeRef));
        }
        finally
        {
            txA.rollback();
        }
    }
    
    @Test
    public void testOtherUserCanChangeLockInfoOnceExpired() throws NotSupportedException, SystemException
    {
        final TransactionService txService = (TransactionService) ctx.getBean("TransactionService");
        UserTransaction txA = txService.getUserTransaction();
        final NodeRef nodeRef = new NodeRef("workspace://SpacesStore/UUID-1");
        Date now = new Date();
        Date expired = new Date(now.getTime() - 900);
        final LockState lockState1 = LockState.createLock(nodeRef, LockType.WRITE_LOCK,
                    "jbloggs", expired, Lifetime.EPHEMERAL, null);
        
        txA.begin();
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser("jbloggs");
            
            // Set initial lock state
            lockStore.set(nodeRef, lockState1);
            
            // Set different lock state
            AuthenticationUtil.setFullyAuthenticatedUser("csmith");
            Date expiresFuture = new Date(now.getTime() + 180000);
            final LockState lockState2 = LockState.createLock(nodeRef, LockType.WRITE_LOCK,
                        "csmith", expiresFuture, Lifetime.EPHEMERAL, null);
            lockStore.set(nodeRef, lockState2);
            
            // Updated, since lock had expired.
            assertEquals(lockState2, lockStore.get(nodeRef));
            
            // Incorrect lock owner - this shouldn't fail
            // LockStore does not check for lock owning
            // and is owned by csmith.
            AuthenticationUtil.setFullyAuthenticatedUser("dsmithers");
            final LockState lockState3 = LockState.createWithOwner(lockState2, "dsmithers");
            
            lockStore.set(nodeRef, lockState3);
            
            // Check update.
            assertEquals(lockState3, lockStore.get(nodeRef));
        }
        finally
        {
            txA.rollback();
        }
    }
}
