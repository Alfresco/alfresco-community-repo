package org.alfresco.repo.lock.mem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class LockableAspectInterceptorTest
{
    private static ApplicationContext appCtx;
    private NodeService nodeService;
    private NodeService rawNodeService;
    private LockStore lockStore;
    private NodeRef rootNode;
    private String userName;
    private String lockOwner;
    private LockableAspectInterceptor interceptor;
    
    @BeforeClass
    public static void setUpClass() throws Exception
    {
        appCtx = ApplicationContextHelper.getApplicationContext();
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
        ApplicationContextHelper.closeApplicationContext();
    }

    @Before
    public void setUp()
    {
        // The user that will create locks, this should be different from the user that queries them (ALF-19465)
        lockOwner = "jbloggs";
        // The 'current' user.
        userName = AuthenticationUtil.getAdminUserName();
        AuthenticationUtil.setFullyAuthenticatedUser(userName);
        nodeService = (NodeService) appCtx.getBean("NodeService");
        rawNodeService = (NodeService) appCtx.getBean("dbNodeService");
        lockStore = (LockStore) appCtx.getBean("lockStore");
        rootNode = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        interceptor = (LockableAspectInterceptor) appCtx.getBean("lockableAspectInterceptor");
    }
    
    @Test
    public void testHasAspectEphemeralLock()
    {
        QName nodeName = QName.createQName("http://www.alfresco.org/test/" + getClass().getSimpleName(), "testNode");
        NodeRef nodeRef = nodeService.createNode(
                    rootNode,
                    ContentModel.ASSOC_CHILDREN,
                    nodeName,
                    ContentModel.TYPE_BASE).getChildRef();
        
        assertFalse("Node should not be reported as lockable",
                    nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE));
        
        lockStore.set(nodeRef, LockState.createLock(nodeRef, LockType.WRITE_LOCK, lockOwner, null, Lifetime.EPHEMERAL, null));
        
        assertTrue("Node should be reported as lockable",
                    nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE));
    }
    
    @Test
    public void testHasAspectPersistentLock()
    {
        QName nodeName = QName.createQName("http://www.alfresco.org/test/" + getClass().getSimpleName(), "testNode");
        NodeRef nodeRef = nodeService.createNode(
                    rootNode,
                    ContentModel.ASSOC_CHILDREN,
                    nodeName,
                    ContentModel.TYPE_BASE).getChildRef();
        
        assertFalse("Node should not be reported as lockable",
                    nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE));
        
        lockStore.set(nodeRef,
                    LockState.createLock(nodeRef, LockType.WRITE_LOCK, lockOwner, null, Lifetime.PERSISTENT, null));

        // Persistent lock should not result in the aspects being augmented
        assertFalse("Node should not be reported as lockable",
                    nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE));
        
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_LOCKABLE, new HashMap<QName, Serializable>(0));
        
        // The existence of the real aspect should be reported.
        assertTrue("Node should be reported as lockable",
                    nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE));
    }
    
    @Test
    public void testGetAspectsEphemeralLock()
    {
        QName nodeName = QName.createQName("http://www.alfresco.org/test/" + getClass().getSimpleName(), "testNode");
        NodeRef nodeRef = nodeService.createNode(
                    rootNode,
                    ContentModel.ASSOC_CHILDREN,
                    nodeName,
                    ContentModel.TYPE_BASE).getChildRef();
        
        // We want to check that the spoofed cm:lockable aspect is joined with the other aspects for the node.
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_AUDITABLE, new HashMap<QName, Serializable>());
        
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        
        assertFalse("Node should not have lockable aspect",
                    aspects.contains(ContentModel.ASPECT_LOCKABLE));
        assertTrue("Node should have auditable aspect",
                    aspects.contains(ContentModel.ASPECT_AUDITABLE));
        
        lockStore.set(nodeRef,
                    LockState.createLock(nodeRef, LockType.WRITE_LOCK, lockOwner, null, Lifetime.EPHEMERAL, null));
        aspects = nodeService.getAspects(nodeRef);
        
        assertTrue("Node should have lockable aspect",
                    aspects.contains(ContentModel.ASPECT_LOCKABLE));
        assertTrue("Node should have auditable aspect",
                    aspects.contains(ContentModel.ASPECT_AUDITABLE));
    }
    
    @Test
    public void testGetAspectsPersistentLock()
    {
        QName nodeName = QName.createQName("http://www.alfresco.org/test/" + getClass().getSimpleName(), "testNode");
        NodeRef nodeRef = nodeService.createNode(
                    rootNode,
                    ContentModel.ASSOC_CHILDREN,
                    nodeName,
                    ContentModel.TYPE_BASE).getChildRef();
        
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_AUDITABLE, new HashMap<QName, Serializable>());
        
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        
        assertFalse("Node should not have lockable aspect",
                    aspects.contains(ContentModel.ASPECT_LOCKABLE));
        assertTrue("Node should have auditable aspect",
                    aspects.contains(ContentModel.ASPECT_AUDITABLE));
        
        lockStore.set(nodeRef,
                    LockState.createLock(nodeRef, LockType.WRITE_LOCK, lockOwner, null, Lifetime.PERSISTENT, null));
        aspects = nodeService.getAspects(nodeRef);
        
        // Nothing should have changed since the persistent lock was added.
        assertFalse("Node should not have lockable aspect",
                    aspects.contains(ContentModel.ASPECT_LOCKABLE));
        assertTrue("Node should have auditable aspect",
                    aspects.contains(ContentModel.ASPECT_AUDITABLE));
    }
    
    @Test
    public void testGetPropertiesPersistentLock()
    {
        QName nodeName = QName.createQName("http://www.alfresco.org/test/" + getClass().getSimpleName(), "testNode");
        NodeRef nodeRef = nodeService.createNode(
                    rootNode,
                    ContentModel.ASSOC_CHILDREN,
                    nodeName,
                    ContentModel.TYPE_BASE).getChildRef();

        // adding cm:auditable will result in cm:created property being added
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_AUDITABLE, new HashMap<QName, Serializable>());
        
        Date now = new Date();
        Map<QName, Serializable> lockProps = new HashMap<QName, Serializable>();
        lockProps.put(ContentModel.PROP_LOCK_OWNER, "jbloggs");
        lockProps.put(ContentModel.PROP_LOCK_TYPE, LockType.READ_ONLY_LOCK.toString());
        lockProps.put(ContentModel.PROP_EXPIRY_DATE, now);
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_LOCKABLE, lockProps);
        
        Map<QName, Serializable> readProps = nodeService.getProperties(nodeRef);
        
        assertEquals("jbloggs", readProps.get(ContentModel.PROP_LOCK_OWNER));
        assertEquals(LockType.READ_ONLY_LOCK.toString(), readProps.get(ContentModel.PROP_LOCK_TYPE));
        assertEquals(now, readProps.get(ContentModel.PROP_EXPIRY_DATE));
        // Spoofed - wasn't explicitly added.
        assertEquals(Lifetime.PERSISTENT, readProps.get(ContentModel.PROP_LOCK_LIFETIME));
        // Double check - not really present
        assertFalse(rawNodeService.getProperties(nodeRef).containsKey(ContentModel.PROP_LOCK_LIFETIME));
    }
    
    @Test
    public void testGetPropertiesEphemeralLock()
    {
        QName nodeName = QName.createQName("http://www.alfresco.org/test/" + getClass().getSimpleName(), "testNode");
        NodeRef nodeRef = nodeService.createNode(
                    rootNode,
                    ContentModel.ASSOC_CHILDREN,
                    nodeName,
                    ContentModel.TYPE_BASE).getChildRef();
        
        // adding cm:auditable will result in cm:created property being added
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_AUDITABLE, new HashMap<QName, Serializable>());
        
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        
        assertFalse("Node should not have lockOwner property",
                    properties.containsKey(ContentModel.PROP_LOCK_OWNER));
        assertTrue("Node should have created property",
                    properties.containsKey(ContentModel.PROP_CREATED));
        
        Date now = new Date();
        // Set a lock on the node and reload the properties
        lockStore.set(nodeRef,
                    LockState.createLock(nodeRef, LockType.WRITE_LOCK, lockOwner, now, Lifetime.EPHEMERAL, null));
        properties = nodeService.getProperties(nodeRef);
        
        // cm:lockable properties should be spoofed
        assertEquals(lockOwner, properties.get(ContentModel.PROP_LOCK_OWNER));
        assertEquals(LockType.WRITE_LOCK.toString(), properties.get(ContentModel.PROP_LOCK_TYPE));
        assertEquals(now, properties.get(ContentModel.PROP_EXPIRY_DATE));
        
        // In addition to spoofed cm:lockable properties, others properties should still be present.
        assertTrue("Node should have created property",
                    properties.containsKey(ContentModel.PROP_CREATED));
    }
    
    @Test
    public void testGetPropertyEphemeralLock()
    {
      QName nodeName = QName.createQName("http://www.alfresco.org/test/" + getClass().getSimpleName(), "testNode");
      NodeRef nodeRef = nodeService.createNode(
                  rootNode,
                  ContentModel.ASSOC_CHILDREN,
                  nodeName,
                  ContentModel.TYPE_BASE).getChildRef();
      
      assertEquals(null, nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_OWNER));
      assertEquals(null, nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_TYPE));
      assertEquals(null, nodeService.getProperty(nodeRef, ContentModel.PROP_EXPIRY_DATE));
      assertEquals(null, nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_LIFETIME));
      Date now = new Date();
      // Set a lock on the node
      lockStore.set(nodeRef,
                  LockState.createLock(nodeRef, LockType.WRITE_LOCK, lockOwner, now, Lifetime.EPHEMERAL, null));
      
      // cm:lockable properties should be spoofed
      assertEquals(lockOwner, nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_OWNER));
      assertEquals(LockType.WRITE_LOCK.toString(), nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_TYPE));
      assertEquals(now, nodeService.getProperty(nodeRef, ContentModel.PROP_EXPIRY_DATE));
      assertEquals(Lifetime.EPHEMERAL.toString(), nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_LIFETIME));
    }
    
    @Test
    public void testGetPropertyPersistentLock()
    {
        QName nodeName = QName.createQName("http://www.alfresco.org/test/" + getClass().getSimpleName(), "testNode");
        NodeRef nodeRef = nodeService.createNode(
                    rootNode,
                    ContentModel.ASSOC_CHILDREN,
                    nodeName,
                    ContentModel.TYPE_BASE).getChildRef();
        
        assertEquals(null, nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_OWNER));
        assertEquals(null, nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_TYPE));
        assertEquals(null, nodeService.getProperty(nodeRef, ContentModel.PROP_EXPIRY_DATE));
        
        Date now = new Date();
        
        // Set a lock on the node
        Map<QName, Serializable> lockProps = new HashMap<QName, Serializable>();
        lockProps.put(ContentModel.PROP_LOCK_OWNER, "another");
        lockProps.put(ContentModel.PROP_LOCK_TYPE, LockType.WRITE_LOCK.toString());
        lockProps.put(ContentModel.PROP_EXPIRY_DATE, now);
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_LOCKABLE, lockProps);
        
        // cm:lockable properties should be unaffected
        assertEquals("another", nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_OWNER));
        assertEquals(LockType.WRITE_LOCK.toString(), nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_TYPE));
        assertEquals(now, nodeService.getProperty(nodeRef, ContentModel.PROP_EXPIRY_DATE));
        // Spoofed property
        assertEquals(Lifetime.PERSISTENT.toString(), nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_LIFETIME));
        // Double check, not really present
        assertNull(rawNodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_LIFETIME));
    }
    
    @Test
    public void testEnableDisableForThread() throws InterruptedException, ExecutionException
    {
        QName nodeName = QName.createQName("http://www.alfresco.org/test/" + getClass().getSimpleName(), "testNode");
        final NodeRef nodeRef = nodeService.createNode(
                    rootNode,
                    ContentModel.ASSOC_CHILDREN,
                    nodeName,
                    ContentModel.TYPE_BASE).getChildRef();
        
        assertFalse("Node should not be reported as lockable",
                    nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE));
        
        lockStore.set(nodeRef,
                    LockState.createLock(nodeRef, LockType.WRITE_LOCK, lockOwner, null, Lifetime.EPHEMERAL, null));
        
        // Interceptor enabled by default for current thread.
        assertTrue("Node should be reported as lockable",
                    nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE));

        // Interceptor can be disabled for current thread.
        interceptor.disableForThread();
        assertFalse("Node should NOT be reported as lockable",
                    nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE));
        
        // Interceptor can be re-enabled for current thread.
        interceptor.enableForThread();
        assertTrue("Node should be reported as lockable",
                    nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE));
        
        Callable<Boolean> callable = new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
                // Disable for the 'other' thread
                interceptor.disableForThread();
                return nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE);
            }   
        };
        
        ExecutorService executor = Executors.newSingleThreadExecutor();  
        Future<Boolean> future = executor.submit(callable);
        
        // The 'other' thread should not spoof the aspect as the interceptor is disabled. 
        assertFalse("Node should be reported as lockable (new thread)", future.get());
        
        // The interceptor is still enabled in the primary thread though.
        assertTrue("Node should be reported as lockable (main thread)",
                    nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE));
        
        executor.shutdown();
    }
    
    @Test
    public void testSetPropertiesPersistentLock()
    {
        QName nodeName = QName.createQName("http://www.alfresco.org/test/" + getClass().getSimpleName(), "testNode");
        NodeRef nodeRef = nodeService.createNode(
                    rootNode,
                    ContentModel.ASSOC_CHILDREN,
                    nodeName,
                    ContentModel.TYPE_BASE).getChildRef();

        Map<QName, Serializable> properties = rawNodeService.getProperties(nodeRef);
        
        // With the exception of cm:lockLifetime, lock properties should be unaffected after setProperties()
        properties.put(ContentModel.PROP_AUTHOR, "Joe Bloggs");
        properties.put(ContentModel.PROP_NAME, "A Name");
        properties.put(ContentModel.PROP_LOCK_TYPE, LockType.NODE_LOCK);
        properties.put(ContentModel.PROP_LOCK_LIFETIME, Lifetime.PERSISTENT);
        properties.put(ContentModel.PROP_LOCK_OWNER, "Alison Bloggs");
        Date expiryDate = new Date();
        properties.put(ContentModel.PROP_EXPIRY_DATE, expiryDate);
        
        // Set the properties (intercepted)
        nodeService.setProperties(nodeRef, properties);
        
        // Check the persisted properties
        properties = rawNodeService.getProperties(nodeRef);
        assertEquals("Joe Bloggs", properties.get(ContentModel.PROP_AUTHOR));
        assertEquals("A Name", properties.get(ContentModel.PROP_NAME));
        assertEquals(LockType.NODE_LOCK.toString(), properties.get(ContentModel.PROP_LOCK_TYPE));
        assertEquals("Alison Bloggs", properties.get(ContentModel.PROP_LOCK_OWNER));
        assertEquals(expiryDate, properties.get(ContentModel.PROP_EXPIRY_DATE));
        
        // cm:lockLifetime is not persisted.
        assertFalse(properties.containsKey(ContentModel.PROP_LOCK_LIFETIME));
    }
    
    @Test
    public void testSetPropertiesEphemeralLock()
    {
        QName nodeName = QName.createQName("http://www.alfresco.org/test/" + getClass().getSimpleName(), "testNode");
        NodeRef nodeRef = nodeService.createNode(
                    rootNode,
                    ContentModel.ASSOC_CHILDREN,
                    nodeName,
                    ContentModel.TYPE_BASE).getChildRef();

        Map<QName, Serializable> properties = rawNodeService.getProperties(nodeRef);
        // Non-lock properties should be unaffected after setProperties()
        properties.put(ContentModel.PROP_AUTHOR, "Joe Bloggs");
        properties.put(ContentModel.PROP_NAME, "A Name");
        // Lock properties should not be persisted (filtered out by interceptor)
        properties.put(ContentModel.PROP_LOCK_TYPE, LockType.NODE_LOCK);
        properties.put(ContentModel.PROP_LOCK_LIFETIME, Lifetime.EPHEMERAL);
        properties.put(ContentModel.PROP_LOCK_OWNER, "Alison Bloggs");
        Date expiryDate = new Date();
        properties.put(ContentModel.PROP_EXPIRY_DATE, expiryDate);
        
        // Set the properties
        nodeService.setProperties(nodeRef, properties);
        
        // Check the persisted properties
        properties = rawNodeService.getProperties(nodeRef);
        assertEquals("Joe Bloggs", properties.get(ContentModel.PROP_AUTHOR));
        assertEquals("A Name", properties.get(ContentModel.PROP_NAME));
        // Check the filtered properties
        assertNull(properties.get(ContentModel.PROP_LOCK_TYPE));
        assertNull(properties.get(ContentModel.PROP_LOCK_LIFETIME));
        assertNull(properties.get(ContentModel.PROP_LOCK_OWNER));
        assertNull(properties.get(ContentModel.PROP_EXPIRY_DATE));
    }
}
