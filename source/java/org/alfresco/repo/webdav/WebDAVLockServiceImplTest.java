package org.alfresco.repo.webdav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class WebDAVLockServiceImplTest
{
    private WebDAVLockServiceImpl davLockService;
    private @Mock LockStore lockStore;
    private @Mock HttpSession session;
    private @Mock List<Pair<String, NodeRef>> sessionList;
    private @Mock AuthenticationUtil authenticationUtil;
    private @Mock TransactionService transactionService;
    private @Mock RetryingTransactionHelper txHelper;
    private @Mock NodeService nodeService;
    private @Mock LockService lockService;
    private @Mock CheckOutCheckInService cociService;
    private NodeRef nodeRef1;
    private NodeRef nodeRef2;
    private LockInfoImpl lockInfo1;
    private LockInfoImpl lockInfo2;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception
    {
        davLockService = new WebDAVLockServiceImpl();
        LockStoreFactory lockStoreFactory = Mockito.mock(LockStoreFactory.class);
        Mockito.when(lockStoreFactory.createLockStore()).thenReturn(lockStore);
        davLockService.setLockStoreFactory(lockStoreFactory);
        davLockService.setNodeService(nodeService);
        davLockService.setCheckOutCheckInService(cociService);
        davLockService.setCurrentSession(session);
        davLockService.setLockService(lockService);
        
        // Train the mock LockStore to respond to get() requests for certain noderefs.
        nodeRef1 = new NodeRef("workspace://SpacesStore/f6e3f82a-cfef-445b-9fca-7986a14181cc");
        lockInfo1 = new LockInfoImplTest.LockInfoImplEx();
        Mockito.when(lockStore.get(nodeRef1)).thenReturn(lockInfo1);
        nodeRef2 = new NodeRef("workspace://SpacesStore/a6a4371c-99b9-4618-8cd2-e71d7d96aa87");
        lockInfo2 = new LockInfoImplTest.LockInfoImplEx();
        Mockito.when(lockStore.get(nodeRef2)).thenReturn(lockInfo2);
        
        // The mock HttpSession should return the mock session list.
        Mockito.when(session.getAttribute("_webdavLockedResources")).thenReturn(sessionList);
        
        // Provide a user name for our fictional user.
        authenticationUtil = new AuthenticationUtil();
        authenticationUtil.afterPropertiesSet();
        AuthenticationUtil.setFullyAuthenticatedUser("some_user_name");
        
        Mockito.when(txHelper.doInTransaction(any(RetryingTransactionCallback.class), anyBoolean())).thenAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                Object[] args = invocation.getArguments();
                RetryingTransactionCallback<Void> callback = (RetryingTransactionCallback<Void>) args[0];
                callback.execute();
                return null;
            }
            
        });
        Mockito.when(transactionService.getRetryingTransactionHelper()).thenReturn(txHelper);
        davLockService.setTransactionService(transactionService);
    }


    @Test
    public void testSessionDestroyed()
    {
        List<Pair<String, NodeRef>> lockedNodes = new ArrayList<Pair<String, NodeRef>>(2);
        lockedNodes.add(new Pair<String, NodeRef>("some_user_name", nodeRef1));
        lockedNodes.add(new Pair<String, NodeRef>("another_user_name", nodeRef2));
        Mockito.when(sessionList.size()).thenReturn(2);
        Mockito.when(sessionList.iterator()).thenReturn(lockedNodes.iterator());
        
        Mockito.when(nodeService.exists(nodeRef1)).thenReturn(true);
        Mockito.when(nodeService.exists(nodeRef2)).thenReturn(true);
        
        Mockito.when(lockService.getLockStatus(nodeRef1)).thenReturn(LockStatus.LOCKED);
        Mockito.when(lockService.getLockStatus(nodeRef2)).thenReturn(LockStatus.LOCKED);

        // We're not going to do anything with nodeRef2
        NodeRef wcNodeRef2 = new NodeRef("workspace://SpacesStore/a6e3f82a-cfef-363d-9fca-3986a14180a0");
        Mockito.when(cociService.getWorkingCopy(nodeRef2)).thenReturn(wcNodeRef2);
        
        davLockService.sessionDestroyed();
        
        // nodeRef1 is unlocked
        Mockito.verify(lockService).unlock(nodeRef1);
        Mockito.verify(lockStore).remove(nodeRef1);
        
        // nodeRef2 is not unlocked
        Mockito.verify(lockService, Mockito.never()).unlock(nodeRef2);        
        Mockito.verify(lockStore, Mockito.never()).remove(nodeRef2);
    }

    @Test
    public void lockLessThan24Hours()
    {
        lockInfo1.setTimeoutSeconds(100);
        
        davLockService.lock(nodeRef1, lockInfo1);
        
        Mockito.verify(lockStore).put(nodeRef1, lockInfo1);
        // 100 seconds (in millis) should have been added to the date/time stamp.
        assertEquals(86500000, lockInfo1.getExpires().getTime());
    }
    
    @Test
    public void lockGreaterThan24Hours()
    {
        int timeout25hours = WebDAV.TIMEOUT_24_HOURS + 3600;
        lockInfo1.setTimeoutSeconds(timeout25hours);
        
        davLockService.lock(nodeRef1, lockInfo1);
        
        Mockito.verify(lockStore).put(nodeRef1, lockInfo1);
        Mockito.verify(sessionList).add(new Pair<String, NodeRef>("some_user_name", nodeRef1));
        // Timeout should be capped at 24 hours.
        assertEquals(WebDAV.TIMEOUT_24_HOURS, lockInfo1.getRemainingTimeoutSeconds());
    }
    
    @Test
    public void lockForInfinityTime()
    {
        lockInfo1.setTimeoutSeconds(WebDAV.TIMEOUT_INFINITY);
        
        davLockService.lock(nodeRef1, lockInfo1);
        
        Mockito.verify(lockStore).put(nodeRef1, lockInfo1);
        Mockito.verify(sessionList).add(new Pair<String, NodeRef>("some_user_name", nodeRef1));
        // Timeout should be capped at 24 hours.
        assertEquals(WebDAV.TIMEOUT_24_HOURS, lockInfo1.getRemainingTimeoutSeconds());
    }

    @Test
    public void canUnlock()
    {
        davLockService.unlock(nodeRef1);
        
        // NodeRef should have been removed from the LockStore
        Mockito.verify(lockStore).remove(nodeRef1);
        // Node should have been removed from the list in the user's session.
        Mockito.verify(sessionList).remove(new Pair<String, NodeRef>("some_user_name", nodeRef1));
    }

    @Test
    public void canGetLockInfo()
    {        
        // Sanity check that what we're putting in, is what we're getting out.
        assertNull("LockInfo should be null", davLockService.getLockInfo(null));
        assertEquals(lockInfo1, davLockService.getLockInfo(nodeRef1));
        assertEquals(lockInfo2, davLockService.getLockInfo(nodeRef2));
    }
}
