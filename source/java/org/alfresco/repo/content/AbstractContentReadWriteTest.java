/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.content;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Abstract base class that provides a set of tests for implementations
 * of the content readers and writers.
 * 
 * @see org.alfresco.service.cmr.repository.ContentReader
 * @see org.alfresco.service.cmr.repository.ContentWriter
 * 
 * @author Derek Hulley
 */
public abstract class AbstractContentReadWriteTest extends TestCase
{
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    protected TransactionService transactionService;
    private String contentUrl;
    private UserTransaction txn;
    
    public AbstractContentReadWriteTest()
    {
        super();
    }

    @Override
    public void setUp() throws Exception
    {
        contentUrl = AbstractContentStore.createNewUrl();
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        txn = transactionService.getUserTransaction();
        txn.begin();
    }
    
    public void tearDown() throws Exception
    {
        txn.rollback();
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
     * @see #getStore()
     */
    protected final ContentWriter getWriter()
    {
        ContentContext contentCtx = new ContentContext(null, contentUrl);
        return getStore().getWriter(contentCtx);
    }
    
    /**
     * @see #getStore()
     */
    protected final ContentReader getReader()
    {
        return getStore().getReader(contentUrl);
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull("setUp() not executed: no content URL present");
        
        // check that the store remains the same
        ContentStore store = getStore();
        assertNotNull("No store provided", store);
        assertTrue("The same instance of the store must be returned for getStore", store == getStore());
    }
    
    public void testContentUrl() throws Exception
    {
        ContentReader reader = getReader();
        ContentWriter writer = getWriter();
        
        // the contract is that both the reader and writer must refer to the same
        // content -> the URL must be the same
        String readerContentUrl = reader.getContentUrl();
        String writerContentUrl = writer.getContentUrl();
        assertNotNull("Reader url is invalid", readerContentUrl);
        assertNotNull("Writer url is invalid", writerContentUrl);
        assertEquals("Reader and writer must reference same content",
                readerContentUrl,
                writerContentUrl);
        
        // check that the content URL is correct
        assertTrue("Content URL doesn't start with correct prefix",
                readerContentUrl.startsWith(ContentStore.STORE_PROTOCOL));
    }
    
    public void testMimetypAbdEncodingAndLocale() throws Exception
    {
        ContentWriter writer = getWriter();
        // set mimetype and encoding
        writer.setMimetype("text/plain");
        writer.setEncoding("UTF-16");
        writer.setLocale(Locale.CHINESE);
        
        // create a UTF-16 string
        String content = "A little bit o' this and a little bit o' that";
        byte[] bytesUtf16 = content.getBytes("UTF-16");
        // write the bytes directly to the writer
        OutputStream os = writer.getContentOutputStream();
        os.write(bytesUtf16);
        os.close();
        
        // now get a reader from the writer
        ContentReader reader = writer.getReader();
        assertEquals("Writer -> Reader content URL mismatch", writer.getContentUrl(), reader.getContentUrl());
        assertEquals("Writer -> Reader mimetype mismatch", writer.getMimetype(), reader.getMimetype());
        assertEquals("Writer -> Reader encoding mismatch", writer.getEncoding(), reader.getEncoding());
        assertEquals("Writer -> Reader locale mismatch", writer.getLocale(), reader.getLocale());
        
        // now get the string directly from the reader
        String contentCheck = reader.getContentString();     // internally it should have taken care of the encoding
        assertEquals("Encoding and decoding of strings failed", content, contentCheck);
    }
    
    public void testExists() throws Exception
    {
        ContentStore store = getStore();
        
        // make up a URL
        String contentUrl = AbstractContentStore.createNewUrl();
        
        // it should not exist in the store
        assertFalse("Store exists fails with new URL", store.exists(contentUrl));
        
        // get a reader
        ContentReader reader = store.getReader(contentUrl);
        assertNotNull("Reader must be present, even for missing content", reader);
        assertFalse("Reader exists failure", reader.exists());
        
        // write something
        ContentContext contentContext = new ContentContext(null, contentUrl);
        ContentWriter writer = store.getWriter(contentContext);
        writer.putContent("ABC");
        
        assertTrue("Store exists should show URL to be present", store.exists(contentUrl));
    }
    
    public void testGetReader() throws Exception
    {
        ContentWriter writer = getWriter();
        
        // check that no reader is available from the writer just yet
        ContentReader nullReader = writer.getReader();
        assertNull("No reader expected", nullReader);
        
        String content = "ABC";
        // write some content
        long before = System.currentTimeMillis();
        writer.setMimetype("text/plain");
        writer.setEncoding("UTF-8");
        writer.setLocale(Locale.CHINESE);
        writer.putContent(content);
        long after = System.currentTimeMillis();
        
        // get a reader from the writer
        ContentReader readerFromWriter = writer.getReader();
        assertEquals("URL incorrect", writer.getContentUrl(), readerFromWriter.getContentUrl());
        assertEquals("Mimetype incorrect", writer.getMimetype(), readerFromWriter.getMimetype());
        assertEquals("Encoding incorrect", writer.getEncoding(), readerFromWriter.getEncoding());
        assertEquals("Locale incorrect", writer.getLocale(), readerFromWriter.getLocale());
        
        // get another reader from the reader
        ContentReader readerFromReader = readerFromWriter.getReader();
        assertEquals("URL incorrect", writer.getContentUrl(), readerFromReader.getContentUrl());
        assertEquals("Mimetype incorrect", writer.getMimetype(), readerFromReader.getMimetype());
        assertEquals("Encoding incorrect", writer.getEncoding(), readerFromReader.getEncoding());
        assertEquals("Locale incorrect", writer.getLocale(), readerFromReader.getLocale());
        
        // check the content
        String contentCheck = readerFromWriter.getContentString();
        assertEquals("Content is incorrect", content, contentCheck);
        
        // check that the length is correct
        int length = content.getBytes(writer.getEncoding()).length;
        assertEquals("Reader content length is incorrect", length, readerFromWriter.getSize());

        // check that the last modified time is correct
        long modifiedTimeCheck = readerFromWriter.getLastModified();

        // On some versionms of Linux (e.g. Centos) this test won't work as the 
        // modified time accuracy is only to the second.
        long beforeSeconds = before/1000L;
        long afterSeconds = after/1000L;
        long modifiedTimeCheckSeconds = modifiedTimeCheck/1000L;

        assertTrue("Reader last modified is incorrect", beforeSeconds <= modifiedTimeCheckSeconds);
        assertTrue("Reader last modified is incorrect", modifiedTimeCheckSeconds <= afterSeconds);
    }
    
    public void testClosedState() throws Exception
    {
        ContentReader reader = getReader();
        ContentWriter writer = getWriter();
        
        // check that streams are not flagged as closed
        assertFalse("Reader stream should not be closed", reader.isClosed());
        assertFalse("Writer stream should not be closed", writer.isClosed());
        
        // check that the write doesn't supply a reader
        ContentReader writerGivenReader = writer.getReader();
        assertNull("No reader should be available before a write has finished", writerGivenReader);
        
        // write some stuff
        writer.putContent("ABC");
        // check that the write has been closed
        assertTrue("Writer stream should be closed", writer.isClosed());
        
        // check that we can get a reader from the writer
        writerGivenReader = writer.getReader();
        assertNotNull("No reader given by closed writer", writerGivenReader);
        assertFalse("Readers should still be closed", reader.isClosed());
        assertFalse("Readers should still be closed", writerGivenReader.isClosed());
        
        // check that the instance is new each time
        ContentReader newReaderA = writer.getReader();
        ContentReader newReaderB = writer.getReader();
        assertFalse("Reader must always be a new instance", newReaderA == newReaderB);
        
        // check that the readers refer to the same URL
        assertEquals("Readers should refer to same URL",
                reader.getContentUrl(), writerGivenReader.getContentUrl());
        
        // read their content
        String contentCheck = reader.getContentString();
        assertEquals("Incorrect content", "ABC", contentCheck);
        contentCheck = writerGivenReader.getContentString();
        assertEquals("Incorrect content", "ABC", contentCheck);
        
        // check closed state of readers
        assertTrue("Reader should be closed", reader.isClosed());
        assertTrue("Reader should be closed", writerGivenReader.isClosed());
    }
    
    /**
     * Checks that the store disallows concurrent writers to be issued to the same URL.
     */
    @SuppressWarnings("unused")
    public void testConcurrentWriteDetection() throws Exception
    {
        String contentUrl = AbstractContentStore.createNewUrl();
        ContentStore store = getStore();

        ContentContext contentCtx = new ContentContext(null, contentUrl);
        ContentWriter firstWriter = store.getWriter(contentCtx);
        try
        {
            ContentWriter secondWriter = store.getWriter(contentCtx);
            fail("Store issued two writers for the same URL: " + store);
        }
        catch (ContentIOException e)
        {
            // expected
        }
    }
    
    /**
     * Checks that the writer can have a listener attached
     */
    public void testWriteStreamListener() throws Exception
    {
        ContentWriter writer = getWriter();
        
        final boolean[] streamClosed = new boolean[] {false};  // has to be final
        ContentStreamListener listener = new ContentStreamListener()
        {
            public void contentStreamClosed() throws ContentIOException
            {
                streamClosed[0] = true;
            }
        };
        writer.setRetryingTransactionHelper(null);
        writer.addListener(listener);
        
        // write some content
        writer.putContent("ABC");
        
        // check that the listener was called
        assertTrue("Write stream listener was not called for the stream close", streamClosed[0]);
    }
    
    /**
     * The simplest test.  Write a string and read it again, checking that we receive the same values.
     * If the resource accessed by {@link #getReader()} and {@link #getWriter()} is not the same, then
     * values written and read won't be the same.
     */
    public void testWriteAndReadString() throws Exception
    {
        ContentReader reader = getReader();
        ContentWriter writer = getWriter();
        
        String content = "ABC";
        writer.putContent(content);
        assertTrue("Stream close not detected", writer.isClosed());

        String check = reader.getContentString();
        assertTrue("Read and write may not share same resource", check.length() > 0);
        assertEquals("Write and read didn't work", content, check);
    }
    
    public void testStringTruncation() throws Exception
    {
        String content = "1234567890";
        
        ContentWriter writer = getWriter();
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");  // shorter format i.t.o. bytes used
        // write the content
        writer.putContent(content);
        
        // get a reader - get it in a larger format i.t.o. bytes
        ContentReader reader = writer.getReader();
        String checkContent = reader.getContentString(5);
        assertEquals("Truncated strings don't match", "12345", checkContent);
    }
    
    public void testReadAndWriteFile() throws Exception
    {
        ContentReader reader = getReader();
        ContentWriter writer = getWriter();
        
        File sourceFile = File.createTempFile(getName(), ".txt");
        sourceFile.deleteOnExit();
        // dump some content into the temp file
        String content = "ABC";
        FileOutputStream os = new FileOutputStream(sourceFile);
        os.write(content.getBytes());
        os.flush();
        os.close();
        
        // put our temp file's content
        writer.putContent(sourceFile);
        assertTrue("Stream close not detected", writer.isClosed());
        
        // create a sink temp file
        File sinkFile = File.createTempFile(getName(), ".txt");
        sinkFile.deleteOnExit();
        
        // get the content into our temp file
        reader.getContent(sinkFile);
        
        // read the sink file manually
        FileInputStream is = new FileInputStream(sinkFile);
        byte[] buffer = new byte[100];
        int count = is.read(buffer);
        assertEquals("No content read", 3, count);
        is.close();
        String check = new String(buffer, 0, count);
        
        assertEquals("Write out of and read into files failed", content, check);
    }
    
    public void testReadAndWriteStreamByPull() throws Exception
    {
        ContentReader reader = getReader();
        ContentWriter writer = getWriter();

        String content = "ABC";
        // put the content using a stream
        InputStream is = new ByteArrayInputStream(content.getBytes());
        writer.putContent(is);
        assertTrue("Stream close not detected", writer.isClosed());
        
        // get the content using a stream
        ByteArrayOutputStream os = new ByteArrayOutputStream(100);
        reader.getContent(os);
        byte[] bytes = os.toByteArray();
        String check = new String(bytes);
        
        assertEquals("Write out and read in using streams failed", content, check);
    }
    
    public void testReadAndWriteStreamByPush() throws Exception
    {
        ContentReader reader = getReader();
        ContentWriter writer = getWriter();

        String content = "ABC";
        // get the content output stream
        OutputStream os = writer.getContentOutputStream();
        os.write(content.getBytes());
        assertFalse("Stream has not been closed", writer.isClosed());
        // close the stream and check again
        os.close();
        assertTrue("Stream close not detected", writer.isClosed());
        
        // pull the content from a stream
        InputStream is = reader.getContentInputStream();
        byte[] buffer = new byte[100];
        int count = is.read(buffer);
        assertEquals("No content read", 3, count);
        is.close();
        String check = new String(buffer, 0, count);
        
        assertEquals("Write out of and read into files failed", content, check);
    }
    
    /**
     * Tests deletion of content.
     * <p>
     * Only applies when {@link #getStore()} returns a value.
     */
    public void testDelete() throws Exception
    {
        ContentStore store = getStore();
        ContentWriter writer = getWriter();
        
        String content = "ABC";
        String contentUrl = writer.getContentUrl();

        // write some bytes, but don't close the stream
        OutputStream os = writer.getContentOutputStream();
        os.write(content.getBytes());
        os.flush();                  // make sure that the bytes get persisted
        
        // close the stream
        os.close();
        
        // get a reader
        ContentReader reader = store.getReader(contentUrl);
        assertNotNull(reader);
        
        ContentReader readerCheck = writer.getReader();
        assertNotNull(readerCheck);
        assertEquals("Store and write provided readers onto different URLs",
                writer.getContentUrl(), reader.getContentUrl());
        
        // open the stream onto the content
        InputStream is = reader.getContentInputStream();
        
        // attempt to delete the content
        boolean deleted = store.delete(contentUrl);

        // close the reader stream
        is.close();
        
        // get a fresh reader
        reader = store.getReader(contentUrl);
        assertNotNull(reader);
        
        // the underlying system may or may not have deleted the content
        if (deleted)
        {
            assertFalse("Content should not exist", reader.exists());
            // drop out here
            return;
        }
        else
        {
            assertTrue("Content should exist", reader.exists());
        }
        
        // delete the content
        store.delete(contentUrl);
        
        // attempt to read from the reader
        try
        {
            is = reader.getContentInputStream();
            fail("Reader failed to detect underlying content deletion");
        }
        catch (ContentIOException e)
        {
            // expected
        }
        
        // get another fresh reader
        reader = store.getReader(contentUrl);
        assertNotNull("Reader must be returned even when underlying content is missing",
                reader);
        assertFalse("Content should not exist", reader.exists());
        try
        {
            is = reader.getContentInputStream();
            fail("Reader opened stream onto missing content");
        }
        catch (ContentIOException e)
        {
            // expected
        }
    }
    
    /**
     * Tests retrieval of all content URLs
     * <p>
     * Only applies when {@link #getStore()} returns a value.
     */
    public void testListUrls() throws Exception
    {
        ContentStore store = getStore();

        ContentWriter writer = getWriter();
        
        Set<String> contentUrls = store.getUrls();
        String contentUrl = writer.getContentUrl();
        assertTrue("Writer URL not listed by store", contentUrls.contains(contentUrl));

        Date yesterday = new Date(System.currentTimeMillis() - 3600L * 1000L * 24L);
        
        // write some data
        writer.putContent("The quick brown fox...");

        // check again
        contentUrls = store.getUrls();
        assertTrue("Writer URL not listed by store", contentUrls.contains(contentUrl));
        
        // check that the query for content created before this time yesterday doesn't return the URL
        contentUrls = store.getUrls(null, yesterday);
        assertFalse("URL was younger than required, but still shows up", contentUrls.contains(contentUrl));
        
        // delete the content
        boolean deleted = store.delete(contentUrl);
        if (deleted)
        {
            contentUrls = store.getUrls();
            assertFalse("Successfully deleted URL still shown by store", contentUrls.contains(contentUrl));
        }
    }
    
    /**
     * Tests random access writing
     * <p>
     * Only executes if the writer implements {@link RandomAccessContent}.
     */
    public void testRandomAccessWrite() throws Exception
    {
        ContentWriter writer = getWriter();
        
        FileChannel fileChannel = writer.getFileChannel(true);
        assertNotNull("No channel given", fileChannel);
        
        // check that no other content access is allowed
        try
        {
            writer.getWritableChannel();
            fail("Second channel access allowed");
        }
        catch (RuntimeException e)
        {
            // expected
        }
        
        // write some content in a random fashion (reverse order)
        byte[] content = new byte[] {1, 2, 3};
        for (int i = content.length - 1; i >= 0; i--)
        {
            ByteBuffer buffer = ByteBuffer.wrap(content, i, 1);
            fileChannel.write(buffer, i);
        }
        
        // close the channel
        fileChannel.close();
        assertTrue("Writer not closed", writer.isClosed());
        
        // check the content
        ContentReader reader = writer.getReader();
        ReadableByteChannel channelReader = reader.getReadableChannel();
        ByteBuffer buffer = ByteBuffer.allocateDirect(3);
        int count = channelReader.read(buffer);
        assertEquals("Incorrect number of bytes read", 3, count);
        for (int i = 0; i < content.length; i++)
        {
            assertEquals("Content doesn't match", content[i], buffer.get(i));
        }
        
        // get a new writer from the store, using the existing content and perform a truncation check
        ContentContext writerTruncateCtx = new ContentContext(writer.getReader(), AbstractContentStore.createNewUrl());
        ContentWriter writerTruncate = getStore().getWriter(writerTruncateCtx);
        assertEquals("Content size incorrect", 0, writerTruncate.getSize());
        // get the channel with truncation
        FileChannel fcTruncate = writerTruncate.getFileChannel(true);
        fcTruncate.close();
        assertEquals("Content not truncated", 0, writerTruncate.getSize());
        
        // get a new writer from the store, using the existing content and perform a non-truncation check
        ContentContext writerNoTruncateCtx = new ContentContext(writer.getReader(), AbstractContentStore.createNewUrl());
        ContentWriter writerNoTruncate = getStore().getWriter(writerNoTruncateCtx);
        assertEquals("Content size incorrect", 0, writerNoTruncate.getSize());
        // get the channel without truncation
        FileChannel fcNoTruncate = writerNoTruncate.getFileChannel(false);
        fcNoTruncate.close();
        assertEquals("Content was truncated", writer.getSize(), writerNoTruncate.getSize());
    }
    
    /**
     * Tests random access reading
     * <p>
     * Only executes if the reader implements {@link RandomAccessContent}.
     */
    public void testRandomAccessRead() throws Exception
    {
        ContentWriter writer = getWriter();
        // put some content
        String content = "ABC";
        byte[] bytes = content.getBytes();
        writer.putContent(content);
        ContentReader reader = writer.getReader();
        
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
        
        // read the content
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        int count = fileChannel.read(buffer);
        assertEquals("Incorrect number of bytes read", bytes.length, count);
        // transfer back to array
        buffer.rewind();
        buffer.get(bytes);
        String checkContent = new String(bytes);
        assertEquals("Content read failure", content, checkContent);
        fileChannel.close();
    }
}
