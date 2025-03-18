/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ContentMetadataExtracter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;

public class LockableAspectInterceptorTest
{
    private ApplicationContext appCtx;
    private TransactionService transactionService;
    private NodeService nodeService;
    private NodeService rawNodeService;
    private LockStore lockStore;
    private NodeRef rootNode;
    private String userName;
    private String lockOwner;
    private LockableAspectInterceptor interceptor;
    private LockService lockService;
    private FileFolderService fileFolderService;
    private ActionService actionService;

    @Before
    public void setUp()
    {
        appCtx = ApplicationContextHelper.getApplicationContext();
        // The user that will create locks, this should be different from the user that queries them (ALF-19465)
        lockOwner = "jbloggs";
        // The 'current' user.
        userName = AuthenticationUtil.getAdminUserName();
        AuthenticationUtil.setFullyAuthenticatedUser(userName);
        transactionService = (TransactionService) appCtx.getBean("TransactionService");
        nodeService = (NodeService) appCtx.getBean("NodeService");
        rawNodeService = (NodeService) appCtx.getBean("dbNodeService");
        lockStore = (LockStore) appCtx.getBean("lockStore");
        rootNode = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        interceptor = (LockableAspectInterceptor) appCtx.getBean("lockableAspectInterceptor");
        lockService = (LockService) appCtx.getBean("lockService");
        fileFolderService = (FileFolderService) appCtx.getBean("FileFolderService");
        actionService = (ActionService) appCtx.getBean("ActionService");
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
        lockProps.put(ContentModel.PROP_LOCK_ADDITIONAL_INFO, "{ \"fieldName\": \"extra lock info\" }");
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_LOCKABLE, lockProps);

        Map<QName, Serializable> readProps = nodeService.getProperties(nodeRef);

        assertEquals("jbloggs", readProps.get(ContentModel.PROP_LOCK_OWNER));
        assertEquals(LockType.READ_ONLY_LOCK.toString(), readProps.get(ContentModel.PROP_LOCK_TYPE));
        assertEquals(now, readProps.get(ContentModel.PROP_EXPIRY_DATE));
        // Spoofed - wasn't explicitly added.
        assertEquals(Lifetime.PERSISTENT.toString(), readProps.get(ContentModel.PROP_LOCK_LIFETIME));
        // Double check - not really present
        ensurePropertyNotPresent(nodeRef, ContentModel.PROP_LOCK_LIFETIME);
        assertEquals("{ \"fieldName\": \"extra lock info\" }", readProps.get(ContentModel.PROP_LOCK_ADDITIONAL_INFO));
    }

    /**
     * Uses the raw NodeService to ensure that the given property is not present
     */
    private void ensurePropertyNotPresent(final NodeRef nodeRef, final QName propQName)
    {
        RetryingTransactionCallback<Boolean> check = new RetryingTransactionCallback<Boolean>() {
            @Override
            public Boolean execute() throws Throwable
            {
                return rawNodeService.getProperties(nodeRef).containsKey(propQName);
            }
        };
        assertFalse(
                "Node should not have the " + propQName + " property present.",
                transactionService.getRetryingTransactionHelper().doInTransaction(check));
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

        // Set a lock on the node and reload the properties
        // Make sure the ephemeral lock won't expire before asserting cm:lockable properties (spoofed).
        Date expire = makeExpiryDate(2);
        lockStore.set(nodeRef,
                LockState.createLock(nodeRef, LockType.WRITE_LOCK, lockOwner, expire, Lifetime.EPHEMERAL, null));
        properties = nodeService.getProperties(nodeRef);

        // cm:lockable properties should be spoofed
        assertEquals(lockOwner, properties.get(ContentModel.PROP_LOCK_OWNER));
        assertEquals(LockType.WRITE_LOCK.toString(), properties.get(ContentModel.PROP_LOCK_TYPE));
        assertEquals(expire, properties.get(ContentModel.PROP_EXPIRY_DATE));
        // Spoofed - wasn't explicitly added.
        assertEquals(Lifetime.EPHEMERAL.toString(), properties.get(ContentModel.PROP_LOCK_LIFETIME));

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

        // Set a lock on the node
        // Make sure the ephemeral lock won't expire before asserting cm:lockable properties (spoofed).
        Date expire = makeExpiryDate(2);
        lockStore.set(nodeRef,
                LockState.createLock(nodeRef, LockType.WRITE_LOCK, lockOwner, expire, Lifetime.EPHEMERAL, null));

        // cm:lockable properties should be spoofed
        assertEquals(lockOwner, nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_OWNER));
        assertEquals(LockType.WRITE_LOCK.toString(), nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_TYPE));
        assertEquals(expire, nodeService.getProperty(nodeRef, ContentModel.PROP_EXPIRY_DATE));
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
        // Double check - not really present
        ensurePropertyNotPresent(nodeRef, ContentModel.PROP_LOCK_LIFETIME);
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

        Callable<Boolean> callable = new Callable<Boolean>() {
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

        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

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
        properties = nodeService.getProperties(nodeRef);
        assertEquals("Joe Bloggs", properties.get(ContentModel.PROP_AUTHOR));
        assertEquals("A Name", properties.get(ContentModel.PROP_NAME));
        assertEquals(LockType.NODE_LOCK.toString(), properties.get(ContentModel.PROP_LOCK_TYPE));
        assertEquals("Alison Bloggs", properties.get(ContentModel.PROP_LOCK_OWNER));
        assertEquals(expiryDate, properties.get(ContentModel.PROP_EXPIRY_DATE));

        // cm:lockLifetime is not persisted.
        ensurePropertyNotPresent(nodeRef, ContentModel.PROP_LOCK_LIFETIME);
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

        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        // Non-lock properties should be unaffected after setProperties()
        properties.put(ContentModel.PROP_AUTHOR, "Joe Bloggs");
        properties.put(ContentModel.PROP_NAME, "A Name");
        // Lock properties should not be persisted (filtered out by interceptor)
        properties.put(ContentModel.PROP_LOCK_TYPE, LockType.NODE_LOCK);
        properties.put(ContentModel.PROP_LOCK_LIFETIME, Lifetime.EPHEMERAL.toString());
        properties.put(ContentModel.PROP_LOCK_OWNER, "Alison Bloggs");
        Date expiryDate = new Date();
        properties.put(ContentModel.PROP_EXPIRY_DATE, expiryDate);

        // Set the properties
        nodeService.setProperties(nodeRef, properties);

        // Check the persisted properties
        properties = nodeService.getProperties(nodeRef);
        assertEquals("Joe Bloggs", properties.get(ContentModel.PROP_AUTHOR));
        assertEquals("A Name", properties.get(ContentModel.PROP_NAME));
        // Check the filtered properties
        assertNull(properties.get(ContentModel.PROP_LOCK_TYPE));
        assertNull(properties.get(ContentModel.PROP_LOCK_LIFETIME));
        assertNull(properties.get(ContentModel.PROP_LOCK_OWNER));
        assertNull(properties.get(ContentModel.PROP_EXPIRY_DATE));
    }

    /* MNT-12049 1) Create Ephemeral lock 2) Put data to node 3) Unlock node 4) Check for lock - node must be unlocked */
    @Test
    public void testEphemeralLock()
    {
        // create a node
        String fName = GUID.generate() + ".doc";
        QName nodeName = QName.createQName("http://www.alfresco.org/test/" + getClass().getSimpleName(), fName);
        final NodeRef nodeRef = nodeService.createNode(
                rootNode,
                ContentModel.ASSOC_CHILDREN,
                nodeName,
                ContentModel.TYPE_CONTENT).getChildRef();

        // lock node
        RetryingTransactionCallback<Void> lockExecuteImplCallBack = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                lockService.lock(nodeRef, LockType.WRITE_LOCK, (int) 3600, Lifetime.EPHEMERAL, "someInfo");
                return null;
            }

        };
        this.transactionService.getRetryingTransactionHelper().doInTransaction(lockExecuteImplCallBack);

        writeData(fName, nodeRef);

        // unlock node
        RetryingTransactionCallback<Void> unlockExecuteImplCallBack = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                lockService.unlock(nodeRef);
                return null;
            }

        };
        this.transactionService.getRetryingTransactionHelper().doInTransaction(unlockExecuteImplCallBack);

        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        assertNull(properties.get(ContentModel.PROP_LOCK_TYPE));
        assertNull(properties.get(ContentModel.PROP_LOCK_LIFETIME));
    }

    private void writeData(String fileName, final NodeRef fileNodeRef)
    {
        nodeService.addAspect(fileNodeRef, ContentModel.ASPECT_NO_CONTENT, null);
        // Access the content
        ContentWriter writer = fileFolderService.getWriter(fileNodeRef);

        // set content properties
        writer.guessMimetype(fileName);
        writer.guessEncoding();

        // Get the input stream from the request data
        InputStream is = getClass().getClassLoader().getResourceAsStream(
                "farmers_markets_list_2003.doc");

        // Write the new data to the content node
        writer.putContent(is);

        // write info about author
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.SYSTEM_USER_NAME);
        this.transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Void>() {
                    public Void execute() throws Throwable
                    {
                        // Create the action
                        Action action = actionService.createAction(ContentMetadataExtracter.EXECUTOR_NAME);
                        try
                        {
                            actionService.executeAction(action, fileNodeRef);
                        }
                        catch (Throwable th)
                        {
                            // do nothing
                        }
                        return null;
                    }
                });
        AuthenticationUtil.popAuthentication();
    }

    private Date makeExpiryDate(int timeToExpireInSeconds)
    {
        // Set the expiry date
        Date expiryDate = null;
        if (timeToExpireInSeconds > 0)
        {
            expiryDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expiryDate);
            calendar.add(Calendar.SECOND, timeToExpireInSeconds);
            expiryDate = calendar.getTime();
        }
        return expiryDate;
    }
}
