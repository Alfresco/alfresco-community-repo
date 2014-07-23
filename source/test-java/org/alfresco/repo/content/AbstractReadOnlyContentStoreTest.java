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
package org.alfresco.repo.content;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;
import java.util.Set;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.content.ContentStore.ContentUrlHandler;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * Abstract base class that provides a set of tests for implementations
 * of {@link ContentStore}.
 * 
 * @see ContentStore
 * @see org.alfresco.service.cmr.repository.ContentReader
 * @see org.alfresco.service.cmr.repository.ContentWriter
 * 
 * @author Derek Hulley
 */
public abstract class AbstractReadOnlyContentStoreTest extends TestCase
{
    protected static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private static Log logger = LogFactory.getLog(AbstractReadOnlyContentStoreTest.class);
    
    protected TransactionService transactionService;
    private UserTransaction txn;
    
    public AbstractReadOnlyContentStoreTest()
    {
        super();
    }

    /**
     * Starts a transaction
     */
    @Override
    public void setUp() throws Exception
    {
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        txn = transactionService.getUserTransaction();
        txn.begin();
    }
    
    /**
     * Rolls back the transaction
     */
    @Override
    public void tearDown() throws Exception
    {
        try { txn.rollback(); } catch (Throwable e) {e.printStackTrace();}
    }
    
    /**
     * Fetch the store to be used during a test.  This method is invoked once per test - it is
     * therefore safe to use <code>setUp</code> to initialise resources.
     * <p>
     * Usually tests will construct a static instance of the store to use throughout all the
     * tests.
     * 
     * @return Returns the <b>same instance</b> of a store for all invocations.
     */
    protected abstract ContentStore getStore();
    
    /**
     * Gets a reader for the given content URL from the store
     * 
     * @see #getStore()
     */
    protected final ContentReader getReader(String contentUrl)
    {
        return getStore().getReader(contentUrl);
    }
    
    /**
     * Fetch a valid URL from the store.  The default implementation will attempt to get
     * all the available URLs from the store and pick the first one.  Writable store tests
     * can create some content to be sure of its existence.
     * 
     * @return
     *      Return any valid URL for the store, or <tt>null</tt> if the store is empty.
     */
    @SuppressWarnings("deprecation")
    protected String getExistingContentUrl()
    {
        ContentStore store = getStore();
        try
        {
            final Set<String> contentUrls = new HashSet<String>(5);
            ContentUrlHandler handler = new ContentUrlHandler()
            {
                public void handle(String contentUrl)
                {
                    if (contentUrls.size() < 50)
                    {
                        contentUrls.add(contentUrl);
                    }
                }
            };
            store.getUrls(handler);
            if (contentUrls.size() > 0)
            {
                return (String) contentUrls.toArray()[0];
            }
            else
            {
                // We can't do anything with this
                return null;
            }
        }
        catch (UnsupportedOperationException e)
        {
            // The store doesn't support this
            return null;
        }
    }
    
    public void testSetUp() throws Exception
    {
        // check that the store remains the same
        ContentStore store = getStore();
        assertNotNull("No store provided", store);
        assertTrue("The same instance of the store must be returned for getStore", store == getStore());
    }

    /**
     * Helper to ensure that illegal content URLs are flagged for
     * <b>getReader()</b> and <b>exists()</b> requests.
     */
    private void checkIllegalReadContentUrl(ContentStore store, String contentUrl)
    {
        assertFalse("This check is for unsupported content URLs only", store.isContentUrlSupported(contentUrl));
        try
        {
            store.getReader(contentUrl);
            fail("Expected UnsupportedContentUrlException for getReader(), but got nothing: " + contentUrl);
        }
        catch (UnsupportedContentUrlException e)
        {
            // Expected
        }
        try
        {
            store.exists(contentUrl);
            fail("Expected UnsupportedContentUrlException for exists(), but got nothing: " + contentUrl);
        }
        catch (UnsupportedContentUrlException e)
        {
            // Expected
        }
    }
    
    /**
     * Checks that the error handling for <i>inappropriate</i> content URLs
     */
    public void testIllegalReadableContentUrls()
    {
        ContentStore store = getStore();
        checkIllegalReadContentUrl(store, "://bogus");
        checkIllegalReadContentUrl(store, "bogus://");
        checkIllegalReadContentUrl(store, "bogus://bogus");
    }
    
    /**
     * Checks that the various methods of obtaining a reader are supported.
     */
    public void testGetReaderForExistingContentUrl() throws Exception
    {
        ContentStore store = getStore();
        String contentUrl = getExistingContentUrl();
        if (contentUrl == null)
        {
            logger.warn("Store test " + getName() + " not possible on " + store.getClass().getName());
            return;
        }
        // Get the reader
        assertTrue("URL returned in set seems to no longer exist", store.exists(contentUrl));
        ContentReader reader = store.getReader(contentUrl);
        assertNotNull("Reader should never be null", reader);
        assertTrue("Reader says content doesn't exist", reader.exists());
        assertFalse("Reader should not be closed before a read", reader.isClosed());
        assertFalse("The reader channel should not be open yet", reader.isChannelOpen());
        
        // Open the channel
        ReadableByteChannel readChannel = reader.getReadableChannel();
        readChannel.read(ByteBuffer.wrap(new byte[500]));
        assertFalse("Reader should not be closed during a read", reader.isClosed());
        assertTrue("The reader channel should be open during a read", reader.isChannelOpen());
        
        // Close the channel
        readChannel.close();
        assertTrue("Reader should be closed after a read", reader.isClosed());
        assertFalse("The reader channel should be closed after a read", reader.isChannelOpen());
    }
    
    /**
     * Tests random access reading
     * <p>
     * Only executes if the reader implements {@link RandomAccessContent}.
     */
    public void testRandomAccessRead() throws Exception
    {
        ContentStore store = getStore();
        String contentUrl = getExistingContentUrl();
        if (contentUrl == null)
        {
            logger.warn("Store test " + getName() + " not possible on " + store.getClass().getName());
            return;
        }
        // Get the reader
        ContentReader reader = store.getReader(contentUrl);
        assertNotNull("Reader should never be null", reader);

        FileChannel fileChannel = reader.getFileChannel();
        assertNotNull("No channel given", fileChannel);
        
        // check that no other content access is allowed
        try
        {
            reader.getReadableChannel();
            fail("Second channel access allowed");
        }
        catch (RuntimeException e)
        {
            // expected
        }
        fileChannel.close();
    }
    
    public void testBlockedWriteOperations() throws Exception
    {
        ContentStore store = getStore();
        if (store.isWriteSupported())
        {
            // Just ignore this test
            return;
        }
        // Ensure that we can't get a writer
        try
        {
            store.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
            fail("Read-only store provided a writer: " + store);
        }
        catch (UnsupportedOperationException e)
        {
            // Expected
        }
        String contentUrl = getExistingContentUrl();
        if (contentUrl == null)
        {
            logger.warn("Store test " + getName() + " not possible on " + store.getClass().getName());
            return;
        }
        // Ensure that we can't delete a URL
        try
        {
            store.delete(contentUrl);
            fail("Read-only store allowed deletion: " + store);
        }
        catch (UnsupportedOperationException e)
        {
            // Expected
        }
    }
}
