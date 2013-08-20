package org.alfresco.repo.webdav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.alfresco.repo.lock.mem.Lifetime;
import org.alfresco.repo.lock.mem.LockState;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
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
    private LockState lockState1;
    private LockInfoImpl lockInfo2;
    private LockState lockState2;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception
    {
        davLockService = new WebDAVLockServiceImpl();
        davLockService.setNodeService(nodeService);
        davLockService.setCheckOutCheckInService(cociService);
        davLockService.setCurrentSession(session);
        davLockService.setLockService(lockService);
        
        // Train the mock LockStore to respond to get() requests for certain noderefs.
        nodeRef1 = new NodeRef("workspace://SpacesStore/f6e3f82a-cfef-445b-9fca-7986a14181cc");
        lockInfo1 = new LockInfoImplTest.LockInfoImplEx();
        lockState1 = LockState.createLock(nodeRef1, LockType.WRITE_LOCK, "user1", null, Lifetime.EPHEMERAL, null);
        Mockito.when(lockService.getLockState(nodeRef1)).thenReturn(lockState1);
        nodeRef2 = new NodeRef("workspace://SpacesStore/a6a4371c-99b9-4618-8cd2-e71d7d96aa87");
        lockInfo2 = new LockInfoImplTest.LockInfoImplEx();
        lockInfo2.setExclusiveLockToken("a-random-token");
        lockInfo2.setDepth("infinity");
        lockInfo2.setScope(WebDAV.XML_EXCLUSIVE);
        lockState2 = LockState.createLock(nodeRef2, LockType.WRITE_LOCK, "user2", new Date(999L), Lifetime.EPHEMERAL, lockInfo2.toJSON());
        Mockito.when(lockService.getLockState(nodeRef2)).thenReturn(lockState2);
        
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
        
        // nodeRef2 is not unlocked
        Mockito.verify(lockService, Mockito.never()).unlock(nodeRef2);        
    }

    @Test
    public void lockLessThan24Hours()
    {
        lockInfo1.setTimeoutSeconds(100);
        
        davLockService.lock(nodeRef1, lockInfo1);
        
        Mockito.verify(lockService).lock(nodeRef1, LockType.WRITE_LOCK, 100, Lifetime.EPHEMERAL, lockInfo1.toJSON());
        // 100 seconds (in millis) should have been added to the date/time stamp.
        assertEquals(86500000, lockInfo1.getExpires().getTime());
    }
    
    @Test
    public void lockGreaterThan24Hours()
    {
        int timeout25hours = WebDAV.TIMEOUT_24_HOURS + 3600;
        lockInfo1.setTimeoutSeconds(timeout25hours);
        
        davLockService.lock(nodeRef1, lockInfo1);
        
        Mockito.verify(lockService).lock(nodeRef1, LockType.WRITE_LOCK, WebDAV.TIMEOUT_24_HOURS, Lifetime.EPHEMERAL, lockInfo1.toJSON());
        Mockito.verify(sessionList).add(new Pair<String, NodeRef>("some_user_name", nodeRef1));
        // Timeout should be capped at 24 hours.
        assertEquals(WebDAV.TIMEOUT_24_HOURS, lockInfo1.getRemainingTimeoutSeconds());
    }
    
    @Test
    public void lockForInfinityTime()
    {
        lockInfo1.setTimeoutSeconds(WebDAV.TIMEOUT_INFINITY);
        
        davLockService.lock(nodeRef1, lockInfo1);
        
        Mockito.verify(lockService).lock(nodeRef1, LockType.WRITE_LOCK, WebDAV.TIMEOUT_24_HOURS, Lifetime.EPHEMERAL, lockInfo1.toJSON());
        Mockito.verify(sessionList).add(new Pair<String, NodeRef>("some_user_name", nodeRef1));
        // Timeout should be capped at 24 hours.
        assertEquals(WebDAV.TIMEOUT_24_HOURS, lockInfo1.getRemainingTimeoutSeconds());
    }

    @Test
    public void canUnlock()
    {
        davLockService.unlock(nodeRef1);
        
        // NodeRef should have been unlocked.
        Mockito.verify(lockService).unlock(nodeRef1);
        // Node should have been removed from the list in the user's session.
        Mockito.verify(sessionList).remove(new Pair<String, NodeRef>("some_user_name", nodeRef1));
    }

    @Test
    public void canGetLockInfo()
    {
        NodeRef nodeRef3 = new NodeRef("workspace://SpacesStore/a6a4371c-99b9-4618-8cd2-e71d28374859");
        Mockito.when(lockService.getLockState(nodeRef1)).thenReturn(lockState1);
        Mockito.when(lockService.getLockState(nodeRef2)).thenReturn(lockState2);
        Mockito.when(lockService.getLockState(nodeRef3)).thenReturn(null);

        // nodeRef1
        LockInfo lockInfo = davLockService.getLockInfo(nodeRef1);
        assertEquals(null, lockInfo.getExpires());
        assertEquals("user1", lockInfo.getOwner());
        
        // nodeRef2
        lockInfo = davLockService.getLockInfo(nodeRef2);
        assertEquals(new Date(999L), lockInfo.getExpires());
        assertEquals("user2", lockInfo.getOwner());
        assertEquals("a-random-token", lockInfo.getExclusiveLockToken());
        assertEquals("infinity", lockInfo.getDepth());
        assertEquals(WebDAV.XML_EXCLUSIVE, lockInfo.getScope());
        
        // nodeRef3
        lockInfo = davLockService.getLockInfo(nodeRef3);
        assertEquals(null, lockInfo);
    }
}
