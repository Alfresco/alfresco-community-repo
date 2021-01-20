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
package org.alfresco.repo.content;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import javax.transaction.RollbackException;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.node.ContentPropertyRestrictionInterceptor;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.content.RoutingContentService
 * 
 * @author Derek Hulley
 */
@Category(OwnJVMTestsCategory.class)
public class RoutingContentServiceTest extends TestCase
{
    private ApplicationContext ctx;
    
    private static final String SOME_CONTENT = "ABC";
        
    private static final String TEST_NAMESPACE = "http://www.alfresco.org/test/RoutingContentServiceTest";
    
    private TransactionService transactionService;
    private ContentService contentService;
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private CopyService copyService;
    private AuthenticationComponent authenticationComponent;
    private UserTransaction txn;
    private NodeRef rootNodeRef;
    private NodeRef contentNodeRef;
    
    public RoutingContentServiceTest()
    {
    }
    
    @Override
    public void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        nodeService = (NodeService) ctx.getBean("NodeService");
        contentService = (ContentService) ctx.getBean(ServiceRegistry.CONTENT_SERVICE.getLocalName());
        copyService = (CopyService) ctx.getBean("CopyService");
        this.policyComponent = (PolicyComponent) ctx.getBean("policyComponent");
        this.authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        
        // authenticate
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        // start the transaction
        txn = getUserTransaction();
        txn.begin();
        
        // create a store and get the root node
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, getName());
        if (!nodeService.exists(storeRef))
        {
            storeRef = nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
        }
        rootNodeRef = nodeService.getRootNode(storeRef);

        ChildAssociationRef assocRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(TEST_NAMESPACE, GUID.generate()),
                ContentModel.TYPE_CONTENT);
        contentNodeRef = assocRef.getChildRef();

        ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
        writer.setEncoding("UTF-16");
        writer.setLocale(Locale.CHINESE);
        writer.setMimetype("text/plain");
        writer.putContent("sample content");
    }
    
    @Override
    public void tearDown() throws Exception
    {
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            // ignore
        }
        try
        {
            if (txn != null)
            {
                txn.rollback();
            }
        }
        catch (Throwable e)
        {
            // ignore
        }
    }
    
    private UserTransaction getUserTransaction()
    {
        return (UserTransaction) transactionService.getUserTransaction();
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull(contentService);
        assertNotNull(nodeService);
        assertNotNull(rootNodeRef);
        assertNotNull(contentNodeRef);
        assertNotNull(getUserTransaction());
        assertFalse(getUserTransaction() == getUserTransaction());  // ensure txn instances aren't shared 
    }
    
    /**
     * Check that a valid writer into the content store can be retrieved and used.
     */
    public void testSimpleNonTempWriter() throws Exception
    {
        ContentWriter writer = contentService.getWriter(null, null, false);
        assertNotNull("Writer should not be null", writer);
        assertNotNull("Content URL should not be null", writer.getContentUrl());
        
        // write some content
        writer.putContent(SOME_CONTENT);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-16");
        writer.setLocale(Locale.CHINESE);

        // get the reader
        ContentReader reader = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
        assertNotNull("Reader should not be null", reader);
        assertNotNull("Content URL should not be null", reader.getContentUrl());
        assertEquals("Content Encoding was not set", "UTF-16", reader.getEncoding());
        assertEquals("Content Locale was not set", Locale.CHINESE, reader.getLocale());
    }
    
    /**
     * Checks that the URL, mimetype and encoding are automatically set on the readers
     * and writers
     */
    public void testAutoSettingOfProperties() throws Exception
    {
        // get a writer onto the node
        ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
        assertNotNull("Writer should not be null", writer);
        assertNotNull("Content URL should not be null", writer.getContentUrl());
        assertNotNull("Content mimetype should not be null", writer.getMimetype());
        assertNotNull("Content encoding should not be null", writer.getEncoding());
        assertNotNull("Content locale should not be null", writer.getLocale());
        
        // write some content
        writer.putContent(SOME_CONTENT);
        
        // get the reader
        ContentReader reader = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
        assertNotNull("Reader should not be null", reader);
        assertNotNull("Content URL should not be null", reader.getContentUrl());
        assertNotNull("Content mimetype should not be null", reader.getMimetype());
        assertNotNull("Content encoding should not be null", reader.getEncoding());
        assertNotNull("Content locale should not be null", reader.getLocale());
        
        // check that the content length is correct
        // - note encoding is important as we get the byte length
        long length = SOME_CONTENT.getBytes(reader.getEncoding()).length;  // ensures correct decoding
        long checkLength = reader.getSize();
        assertEquals("Content length incorrect", length, checkLength);

        // check the content - the encoding will come into effect here
        String contentCheck = reader.getContentString();
        assertEquals("Content incorrect", SOME_CONTENT, contentCheck);
    }
    
    public void testWriteToNodeWithoutAnyContentProperties() throws Exception
    {
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(TEST_NAMESPACE, GUID.generate()),
                ContentModel.TYPE_CONTENT).getChildRef();

        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

        assertNull(writer.getMimetype());
        assertEquals("UTF-8", writer.getEncoding());
        assertEquals(Locale.getDefault(), writer.getLocale());
        
        // now set it on the writer
        writer.setMimetype("text/plain");
        writer.setEncoding("UTF-16");
        writer.setLocale(Locale.FRENCH);
        
        String content = "The quick brown fox ...";
        writer.putContent(content);
        
        // the properties should have found their way onto the node
        ContentData contentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        assertEquals("metadata didn't get onto node", writer.getContentData(), contentData);
        
        // check that the reader's metadata is set
        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        assertEquals("Metadata didn't get set on reader", writer.getContentData(), reader.getContentData());
    }

    /**
     * It is not allowed to set content properties directly
     */
    public void ignoreTestNullReaderForNullUrl() throws Exception
    {
        // set the property, but with a null URL
        ContentData contentData = new ContentData(null, null, 0L, null);
        nodeService.setProperty(contentNodeRef, ContentModel.PROP_CONTENT, contentData);

        // get the reader
        ContentReader reader = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
        assertNull("Reader must be null if the content URL is null", reader);
    }
    
    @SuppressWarnings("unused")
    public void testContentStoreSizes() throws Exception
    {
        long contentTotalSize = contentService.getStoreFreeSpace();
        long contentAvailableSize = contentService.getStoreTotalSpace();
    }
    
    public void testGetRawReader() throws Exception
    {
        ContentReader reader = contentService.getRawReader("test://non-existence");
        assertNotNull("A reader is expected with content URL referencing no content", reader);
        assertFalse("Reader should not have any content", reader.exists());
        // Now write something
        ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, false);
        writer.putContent("ABC from " + getName());
        // Try again
        String contentUrl = writer.getContentUrl();
        reader = contentService.getRawReader(contentUrl);
        assertNotNull("Expected reader for live, raw content", reader);
        assertEquals("Content sizes don't match", writer.getSize(), reader.getSize());
    }
    
    /**
     * Checks what happens when the physical content disappears
     */
    public void testMissingContent() throws Exception
    {
        // whitelist the test as it has to manipulate content property directly
        ContentPropertyRestrictionInterceptor contentPropertyRestrictionInterceptor =
                (ContentPropertyRestrictionInterceptor) ctx.getBean("contentPropertyRestrictionInterceptor");

        contentPropertyRestrictionInterceptor.setGlobalContentPropertyRestrictionWhiteList(this.getClass().getName());
        try
        {
            File tempFile = TempFileProvider.createTempFile(getName(), ".txt");

            ContentWriter writer = new FileContentWriter(tempFile);
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
            writer.putContent("What about the others?  Buckwheats!");
            // check
            assertTrue("File does not exist", tempFile.exists());
            assertTrue("File not written to", tempFile.length() > 0);

            // update the node with this new info
            ContentData contentData = writer.getContentData();
            nodeService.setProperty(contentNodeRef, ContentModel.PROP_CONTENT, contentData);

            // delete the content
            tempFile.delete();
            assertFalse("File not deleted", tempFile.exists());

            // now attempt to get the reader for the node
            ContentReader reader = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
            assertFalse("Reader should indicate that content is missing", reader.exists());

            // check the indexing doesn't spank everthing
            txn.commit();
            txn = null;

            // cleanup
            txn = getUserTransaction();
            txn.begin();
            nodeService.deleteNode(contentNodeRef);
            txn.commit();
            txn = null;
        }
        finally
        {
            contentPropertyRestrictionInterceptor.setGlobalContentPropertyRestrictionWhiteList("");
        }
    }
	
	/**
	 * Tests simple writes that don't automatically update the node content URL
	 */
	public void testSimpleWrite() throws Exception
	{
		// get a writer to an arbitrary node
		ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, false);   // no updating of URL
		assertNotNull("Writer should not be null", writer);
		
		// put some content
		writer.putContent(SOME_CONTENT);
	}
	
	private boolean policyFired = false;
    private boolean readPolicyFired = false;
    private boolean newContent = true;
	
	/**
	 * Tests that the content update policy firs correctly
	 */
	public void testOnContentUpdatePolicy()
	{
		// Register interest in the content update event for a versionable node
		this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onContentUpdate"),
				ContentModel.ASPECT_VERSIONABLE,
				new JavaBehaviour(this, "onContentUpdateBehaviourTest"));
		
		// First check that the policy is not fired when the versionable aspect is not present
		ContentWriter contentWriter = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
		contentWriter.putContent("content update one");
		assertFalse(this.policyFired);
        
        this.newContent = false;
		
		// Now check that the policy is fired when the versionable aspect is present
		this.nodeService.addAspect(this.contentNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
		ContentWriter contentWriter2 = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
		contentWriter2.putContent("content update two");
		assertTrue(this.policyFired);
		this.policyFired = false;
		
		// Check that the policy is not fired when using a non updating content writer
		ContentWriter contentWriter3 = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, false);
		contentWriter3.putContent("content update three");
		assertFalse(this.policyFired);
	}
	
	public void onContentUpdateBehaviourTest(NodeRef nodeRef, boolean newContent)
	{
		assertEquals(this.contentNodeRef, nodeRef);
        assertEquals(this.newContent, newContent);
		assertTrue(this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE));
		this.policyFired = true;
	}
    
    public void testOnContentReadPolicy()
    {
        // Register interest in the content read event for a versionable node
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onContentRead"),
                ContentModel.ASPECT_VERSIONABLE,
                new JavaBehaviour(this, "onContentReadBehaviourTest"));
        
        // First check that the policy is not fired when the versionable aspect is not present
        this.contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
        assertFalse(this.readPolicyFired);
        
        // Write some content and check that the policy is still not fired
        ContentWriter contentWriter2 = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter2.putContent("content update two");
        this.contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
        assertFalse(this.readPolicyFired);
        
        // Now check that the policy is fired when the versionable aspect is present
        this.nodeService.addAspect(this.contentNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
        this.contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
        assertTrue(this.readPolicyFired);
    }
    
    public void onContentReadBehaviourTest(NodeRef nodeRef)
    {
        this.readPolicyFired = true;
    }
    
    public void testTempWrite() throws Exception
    {
        // get a temporary writer
        ContentWriter writer1 = contentService.getTempWriter();
        // and another
        ContentWriter writer2 = contentService.getTempWriter();
        
        // check
        assertNotSame("Temp URLs must be different",
                writer1.getContentUrl(), writer2.getContentUrl());
    }
    
	/**
	 * Tests the automatic updating of nodes' content URLs
	 */
    public void testUpdatingWrite() throws Exception
    {
        // check that the content URL property has not been set
        ContentData contentData = (ContentData) nodeService.getProperty(
                contentNodeRef,
                ContentModel.PROP_CONTENT); 

        // get the writer
        ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
        assertNotNull("No writer received", writer);
        // write some content directly
        writer.putContent(SOME_CONTENT);
        
        // make sure that we can't reuse the writer
        try
        {
            writer.putContent("DEF");
            fail("Failed to prevent repeated use of the content writer");
        }
        catch (ContentIOException e)
        {
            // expected
        }
        
        // check that there is a reader available
        ContentReader reader = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
        assertNotNull("No reader available for node", reader);
        String contentCheck = reader.getContentString();
        assertEquals("Content fetched doesn't match that written", SOME_CONTENT, contentCheck);

        // check that the content data was set
        contentData = (ContentData) nodeService.getProperty(
                contentNodeRef,
                ContentModel.PROP_CONTENT);
        assertNotNull("Content data not set", contentData);
        assertEquals("Mismatched URL between writer and node",
                writer.getContentUrl(), contentData.getContentUrl());
        
        // check that the content size was set
        assertEquals("Reader content length and node content length different",
                reader.getSize(), contentData.getSize());
        
        // check that the mimetype was set
        assertEquals("Mimetype not set on content data", "text/plain", contentData.getMimetype());
        // check encoding
        assertEquals("Encoding not set", "UTF-16", contentData.getEncoding());
    }
    
    /**
     * Checks that multiple writes can occur to the same node outside of any transactions.
     * <p>
     * It is only when the streams are closed that the node is updated.
     */
    public void testConcurrentWritesNoTxn() throws Exception
    {
        // ensure that the transaction is ended - ofcourse, we need to force a commit
        txn.commit();
        txn = null;
        
        ContentWriter writer1 = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
        ContentWriter writer2 = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
        ContentWriter writer3 = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
        
        writer1.putContent("writer1 wrote this");
        writer2.putContent("writer2 wrote this");
        writer3.putContent("writer3 wrote this");

        // get the content
        ContentReader reader = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
        String contentCheck = reader.getContentString();
        assertEquals("Content check failed", "writer3 wrote this", contentCheck);
    }
    
    public void testConcurrentWritesWithSingleTxn() throws Exception
    {
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(TEST_NAMESPACE, GUID.generate()),
                ContentModel.TYPE_CONTENT).getChildRef();

        // want to operate in a user transaction
        txn.commit();
        txn = null;
        
        UserTransaction txn = getUserTransaction();
        txn.begin();
        txn.setRollbackOnly();

        ContentWriter writer1 = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        ContentWriter writer2 = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        ContentWriter writer3 = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        
        writer1.putContent("writer1 wrote this");
        writer2.putContent("writer2 wrote this");
        writer3.putContent("writer3 wrote this");

        // get the content
        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        String contentCheck = reader.getContentString();
        assertEquals("Content check failed", "writer3 wrote this", contentCheck);
        
        try
        {
            txn.commit();
            fail("Transaction has been marked for rollback");
        }
        catch (RollbackException e)
        {
            // expected
        }
        
        // rollback and check that the content has 'disappeared'
        txn.rollback();
        
        // need a new transaction
        txn = getUserTransaction();
        txn.begin();
        txn.setRollbackOnly();

        reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        assertNull("Transaction was rolled back - no content should be visible", reader);
        
        txn.rollback();
    }
    
    /**
     * Create several threads that will attempt to write to the same node property.
     * The ContentWriter is handed to the thread, so this checks that the stream closure
     * uses the transaction that called <code>close</code> and not the transaction that
     * fetched the <code>ContentWriter</code>.
     */
    public synchronized void testConcurrentWritesWithMultipleTxns() throws Exception
    {
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(TEST_NAMESPACE, GUID.generate()),
                ContentModel.TYPE_CONTENT).getChildRef();
        // ensure that there is no content to read on the node
        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        assertNull("Reader should not be available", reader);

        // commit node so that threads can see node
        txn.commit();
        txn = null;
        
        String threadContent = "Thread content";
        WriteThread[] writeThreads = new WriteThread[5];
        for (int i = 0; i < writeThreads.length; i++)
        {
            ContentWriter threadWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            writeThreads[i] = new WriteThread(threadWriter, threadContent);
            // Kick each thread off
            writeThreads[i].start();
        }
        
        // Wait for all threads to be waiting
        outer:
        while (true)
        {
            // Wait for each thread to be in a transaction
            for (int i = 0; i < writeThreads.length; i++)
            {
                if (!writeThreads[i].isWaiting())
                {
                    wait(10);
                    continue outer;
                }
            }
            // All threads were waiting
            break outer;
        }
        
        // Kick each thread into the stream close phase
        for (int i = 0; i < writeThreads.length; i++)
        {
            synchronized(writeThreads[i])
            {
                writeThreads[i].notifyAll();
            }
        }
        // Wait for the threads to complete (one way or another)
        for (int i = 0; i < writeThreads.length; i++)
        {
            while (!writeThreads[i].isDone())
            {
                wait(10);
            }
        }

        // check content has taken on thread's content
        reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        assertNotNull("Reader should now be available", reader);
        String checkContent = reader.getContentString();
        assertEquals("Content check failed", threadContent, checkContent);
    }
    
    /**
     * Writes some content to the writer's output stream and then aquires
     * a lock on the writer, waits until notified and then closes the
     * output stream before terminating.
     * <p>
     * When firing thread up, be sure to call <code>notify</code> on the
     * Thread instance in order to let the thread run to completion.
     */
    private class WriteThread extends Thread
    {
        private ContentWriter writer;
        private String content;
        private volatile boolean isWaiting;
        private volatile boolean isDone;
        private volatile Throwable error;
        
        public WriteThread(ContentWriter writer, String content)
        {
            this.writer = writer;
            this.content = content;
            isWaiting = false;
            isDone = false;
            error = null;
        }
        
        public boolean isWaiting()
        {
            return isWaiting;
        }
        
        public boolean isDone()
        {
            return isDone;
        }
        
        @SuppressWarnings("unused")
        public Throwable getError()
        {
            return error;
        }

        public void run()
        {
            authenticationComponent.setSystemUserAsCurrentUser();
            
            synchronized (this)
            {
                isWaiting = true;
                try { this.wait(); } catch (InterruptedException e) {};   // wait until notified
            }

            final OutputStream os = writer.getContentOutputStream();
            // Callback to write to the content in a new transaction
            RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    try
                    {
                        // put the content
                        if (writer.getEncoding() == null)
                        {
                            os.write(content.getBytes());
                        }
                        else
                        {
                            os.write(content.getBytes(writer.getEncoding()));
                        }
                        os.close();
                    }
                    finally
                    {
                        if (os != null)
                        {
                            try { os.close(); } catch (IOException e) {}
                        }
                    }
                    return null;
                }
            };
            try
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(callback);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                error = e;
            }
            finally
            {
                isDone = true;
            }
        }
    }

    /**
     * Check that the system is able to handle the uploading of content with an unknown mimetype.
     * The unknown mimetype should be preserved, but treated just like an octet stream.
     */
    public void testUnknownMimetype() throws Exception
    {
        String bogusMimetype = "text/bamboozle";
        // get a writer onto the node
        ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(bogusMimetype);
        
        // write something in
        writer.putContent(SOME_CONTENT);
        
        // commit the transaction to ensure that it goes in OK
        txn.commit();
        
        // so far, so good
        ContentReader reader = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
        assertNotNull("Should be able to get reader", reader);
        assertEquals("Unknown mimetype was changed", bogusMimetype, reader.getMimetype());
    }
    
    /**
     * Checks that node copy and delete behaviour behaves correctly w.r.t. cleanup and shared URLs
     */
    public void testPostCopyContentRetrieval() throws Exception
    {
        ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.putContent("Some content");
        ContentData writerContentData = writer.getContentData();
        ContentData nodeContentData = (ContentData) nodeService.getProperty(contentNodeRef, ContentModel.PROP_CONTENT);
        assertNotNull(nodeContentData);
        assertEquals("ContentData not the same from NodeService and from ContentWriter", writerContentData, nodeContentData);
        
        Map<QName, Serializable> copyProperties = nodeService.getProperties(contentNodeRef);
        copyProperties.remove(ContentModel.PROP_NODE_UUID);
        // Copy the node
        NodeRef contentCopyNodeRef = copyService.copy(contentNodeRef, rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName(TEST_NAMESPACE, GUID.generate()));
        // Now get and check the ContentData for the copy
        ContentData copyNodeContentData = (ContentData) nodeService.getProperty(contentCopyNodeRef, ContentModel.PROP_CONTENT);
        assertNotNull(copyNodeContentData);
        // The copy should share the same URL even
        assertEquals("Copied node's cm:content ContentData was different", writerContentData, copyNodeContentData);
        
        // Delete the first node and ensure that the second valud remains good and the content is editable
        nodeService.deleteNode(contentNodeRef);
        copyNodeContentData = (ContentData) nodeService.getProperty(contentCopyNodeRef, ContentModel.PROP_CONTENT);
        assertNotNull(copyNodeContentData);
        assertEquals("Post-delete value didn't remain the same", writerContentData, copyNodeContentData);
        ContentReader copyNodeContentReader = contentService.getReader(contentCopyNodeRef, ContentModel.PROP_CONTENT);
        assertTrue("Physical content was removed", copyNodeContentReader.exists());
        
        txn.commit();
        txn = null;
    }
    
    /**
     * Ensure that content URLs outside of a transaction are not touched on rollback.
     */
    public void testRollbackCleanup_ALF2890() throws Exception
    {
        // whitelist the test for content property updates
        ContentPropertyRestrictionInterceptor contentPropertyRestrictionInterceptor =
                (ContentPropertyRestrictionInterceptor) ctx.getBean("contentPropertyRestrictionInterceptor");

        contentPropertyRestrictionInterceptor.setGlobalContentPropertyRestrictionWhiteList(this.getClass().getName());
        try
        {
            ContentWriter updatingWriter = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
            updatingWriter.putContent("STEP 1");

            txn.commit();
            txn = null;

            ContentReader readerStep1 = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
            assertEquals("Incorrect content", "STEP 1", readerStep1.getContentString());

            ContentWriter simpleWriter = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, false);
            simpleWriter.putContent("STEP 2");
            readerStep1 = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
            assertEquals("Incorrect content", "STEP 1", readerStep1.getContentString());

            // Update the content
            nodeService.setProperty(contentNodeRef, ContentModel.PROP_CONTENT, simpleWriter.getContentData());
            ContentReader readerStep2 = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
            assertEquals("Incorrect content", "STEP 2", readerStep2.getContentString());

            simpleWriter = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, false);
            simpleWriter.putContent("STEP 3");
            ContentReader readerStep3 = simpleWriter.getReader();
            assertEquals("Incorrect content", "STEP 3", readerStep3.getContentString());
            readerStep2 = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
            assertEquals("Incorrect content", "STEP 2", readerStep2.getContentString());

            // Now get a ex-transaction writer but set the content property in a failing transaction
            // Notice that we have already written "STEP 3" to an underlying binary
            final ContentData simpleWriterData = simpleWriter.getContentData();
            RetryingTransactionCallback<Void> failToSetPropCallback = new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    nodeService.setProperty(contentNodeRef, ContentModel.PROP_CONTENT, simpleWriterData);
                    throw new RuntimeException("aaa");
                }
            };
            try
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(failToSetPropCallback);
            }
            catch (RuntimeException e)
            {
                if (!e.getMessage().equals("aaa"))
                {
                    throw e;
                }
                // Expected
            }
            // The writer data should not have been cleaned up
            readerStep3 = simpleWriter.getReader();
            assertTrue("Content was cleaned up when it originated outside of the transaction", readerStep3.exists());
            assertEquals("Incorrect content", "STEP 3", readerStep3.getContentString());
            // The node's content must be unchanged
            readerStep2 = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
            assertEquals("Incorrect content", "STEP 2", readerStep2.getContentString());

            // Test that rollback cleanup works for writers fetched in the same transaction
            final ContentReader[] readers = new ContentReader[1];
            RetryingTransactionCallback<Void> rollbackCallback = new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                    writer.putContent("UNLUCKY CONTENT");
                    ContentReader reader = contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
                    assertEquals("Incorrect content", "UNLUCKY CONTENT", reader.getContentString());
                    assertEquals("Incorrect content", "UNLUCKY CONTENT", writer.getReader().getContentString());
                    readers[0] = reader;

                    throw new RuntimeException("aaa");
                }
            };
            try
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(rollbackCallback);
            }
            catch (RuntimeException e)
            {
                if (!e.getMessage().equals("aaa"))
                {
                    throw e;
                }
                // Expected
            }
            // Make sure that the content has been cleaned up
            assertFalse("Content was not cleaned up after having been created in-transaction", readers[0].exists());
        }
        finally
        {
            contentPropertyRestrictionInterceptor.setGlobalContentPropertyRestrictionWhiteList("");
        }
    }
}
