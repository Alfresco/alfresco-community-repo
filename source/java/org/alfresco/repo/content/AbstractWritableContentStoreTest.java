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

import org.alfresco.repo.content.ContentStore.ContentUrlHandler;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public abstract class AbstractWritableContentStoreTest extends AbstractReadOnlyContentStoreTest
{
    private static Log logger = LogFactory.getLog(AbstractWritableContentStoreTest.class);
    
    public AbstractWritableContentStoreTest()
    {
        super();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation creates some content in the store and returns the new content URL.
     */
    protected String getExistingContentUrl()
    {
        ContentWriter writer = getWriter();
        writer.putContent("Content for " + getName());
        return writer.getContentUrl();
    }
    
    /**
     * Get a writer into the store.  This test class assumes that the store is writable and
     * that it therefore supports the ability to write content.
     * 
     * @return
     *      Returns a writer for new content
     */
    protected ContentWriter getWriter()
    {
        ContentStore store = getStore();
        return store.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
    }
    
    public void testSetUp() throws Exception
    {
        // check that the store remains the same
        ContentStore store = getStore();
        assertNotNull("No store provided", store);
        assertTrue("The same instance of the store must be returned for getStore", store == getStore());
    }
    
    public void testWritable() throws Exception
    {
        ContentStore store = getStore();
        assertTrue("The store cannot be read-only", store.isWriteSupported());
    }
    
    /**
     * Just check that the method doesn't blow up
     */
    public void testSpaceUsed() throws Exception
    {
        ContentStore store = getStore();
        store.getSpaceUsed();
    }
    
    /**
     * Just checks that the method doesn't blow up
     */
    public void testSpaceFree() throws Exception
    {
        ContentStore store = getStore();
        store.getSpaceFree();
    }
    
    /**
     * Just checks that the method doesn't blow up
     */
    public void testSpaceTotal() throws Exception
    {
        ContentStore store = getStore();
        store.getSpaceTotal();
    }
    
    /**
     * Just check that the method doesn't blow up
     */
    public void testRootLocation() throws Exception
    {
        ContentStore store = getStore();
        String rootLocation = store.getRootLocation();
        assertNotNull("The root location may not be null", rootLocation);
    }

    /**
     * Helper to ensure that illegal content URLs are flagged for <b>getWriter</b> requests
     */
    private void checkIllegalWritableContentUrl(ContentStore store, String contentUrl)
    {
        assertFalse("This check is for unsupported content URLs only", store.isContentUrlSupported(contentUrl));
        ContentContext bogusContentCtx = new ContentContext(null, contentUrl);
        try
        {
            store.getWriter(bogusContentCtx);
            fail("Expected UnsupportedContentUrlException, but got nothing");
        }
        catch (UnsupportedContentUrlException e)
        {
            // Expected
        }
    }
    
    /**
     * Checks that the error handling for <i>inappropriate</i> content URLs
     */
    public void testIllegalWritableContentUrls()
    {
        ContentStore store = getStore();
        checkIllegalWritableContentUrl(store, "://bogus");
        checkIllegalWritableContentUrl(store, "bogus://");
        checkIllegalWritableContentUrl(store, "bogus://bogus");
    }
    
    /**
     * Get a writer and write a little bit of content before reading it.
     */
    public void testSimpleUse()
    {
        ContentStore store = getStore();
        String content = "Content for " + getName();
        
        ContentWriter writer = store.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
        assertNotNull("Writer may not be null", writer);
        // Ensure that the URL is available
        String contentUrlBefore = writer.getContentUrl();
        assertNotNull("Content URL may not be null for unused writer", contentUrlBefore);
        assertTrue("URL is not valid: " + contentUrlBefore, AbstractContentStore.isValidContentUrl(contentUrlBefore));
        // Write something
        writer.putContent(content);
        String contentUrlAfter = writer.getContentUrl();
        assertTrue("URL is not valid: " + contentUrlBefore, AbstractContentStore.isValidContentUrl(contentUrlAfter));
        assertEquals("The content URL may not change just because the writer has put content", contentUrlBefore, contentUrlAfter);
        // Get the readers
        ContentReader reader = store.getReader(contentUrlBefore);
        assertNotNull("Reader from store is null", reader);
        assertEquals(reader.getContentUrl(), writer.getContentUrl());
        String checkContent = reader.getContentString();
        assertEquals("Content is different", content, checkContent);
    }
    
    /**
     * Checks that the various methods of obtaining a reader are supported.
     */
    public void testGetReader() throws Exception
    {
        ContentStore store = getStore();
        ContentWriter writer = store.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
        String contentUrl = writer.getContentUrl();
        
        // Check that a reader is available from the store
        ContentReader readerFromStoreBeforeWrite = store.getReader(contentUrl);
        assertNotNull("A reader must always be available from the store", readerFromStoreBeforeWrite);
        
        // check that a reader is available from the writer
        ContentReader readerFromWriterBeforeWrite = writer.getReader();
        assertNotNull("A reader must always be available from the writer", readerFromWriterBeforeWrite);
        
        String content = "Content for " + getName();
        
        // write some content
        long before = System.currentTimeMillis();
        writer.setMimetype("text/plain");
        writer.setEncoding("UTF-8");
        writer.setLocale(Locale.CHINESE);
        writer.putContent(content);
        long after = System.currentTimeMillis();
        
        // get a reader from the store
        ContentReader readerFromStore = store.getReader(contentUrl);
        assertNotNull(readerFromStore);
        assertTrue(readerFromStore.exists());
        // Store-provided readers don't have context other than URLs
        // assertEquals(writer.getContentData(), readerFromStore.getContentData());
        assertEquals(content, readerFromStore.getContentString());
        
        // get a reader from the writer
        ContentReader readerFromWriter = writer.getReader();
        assertNotNull(readerFromWriter);
        assertTrue(readerFromWriter.exists());
        assertEquals(writer.getContentData(), readerFromWriter.getContentData());
        assertEquals(content, readerFromWriter.getContentString());
        
        // get another reader from the reader
        ContentReader readerFromReader = readerFromWriter.getReader();
        assertNotNull(readerFromReader);
        assertTrue(readerFromReader.exists());
        assertEquals(writer.getContentData(), readerFromReader.getContentData());
        assertEquals(content, readerFromReader.getContentString());
        
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
    
    /**
     * Check that a reader is immutable, i.e. that a reader fetched before a
     * write doesn't suddenly become aware of the content once it has been written.
     */
    public void testReaderImmutability()
    {
        ContentWriter writer = getWriter();
        
        ContentReader readerBeforeWrite = writer.getReader();
        assertNotNull(readerBeforeWrite);
        assertFalse(readerBeforeWrite.exists());
        
        // Write some content
        writer.putContent("Content for " + getName());
        assertFalse("Reader's state changed after write", readerBeforeWrite.exists());
        try
        {
            readerBeforeWrite.getContentString();
            fail("Reader's state changed after write");
        }
        catch (ContentIOException e)
        {
            // Expected
        }
        
        // A new reader should work
        ContentReader readerAfterWrite = writer.getReader();
        assertTrue("New reader after write should be directed to new content", readerAfterWrite.exists());
    }
    
    public void testMimetypAndEncodingAndLocale() throws Exception
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
    
    public void testClosedState() throws Exception
    {
        ContentWriter writer = getWriter();
        ContentReader readerBeforeWrite = writer.getReader();
        
        // check that streams are not flagged as closed
        assertFalse("Reader stream should not be closed", readerBeforeWrite.isClosed());
        assertFalse("Writer stream should not be closed", writer.isClosed());
        
        // write some stuff
        writer.putContent("ABC");
        // check that the write has been closed
        assertTrue("Writer stream should be closed", writer.isClosed());
        
        // check that we can get a reader from the writer
        ContentReader readerAfterWrite = writer.getReader();
        assertNotNull("No reader given by closed writer", readerAfterWrite);
        assertFalse("Before-content reader should not be affected by content updates", readerBeforeWrite.isClosed());
        assertFalse("After content reader should not be closed", readerAfterWrite.isClosed());
        
        // check that the instance is new each time
        ContentReader newReaderA = writer.getReader();
        ContentReader newReaderB = writer.getReader();
        assertFalse("Reader must always be a new instance", newReaderA == newReaderB);
        
        // check that the readers refer to the same URL
        assertEquals("Readers should refer to same URL",
                readerBeforeWrite.getContentUrl(), readerAfterWrite.getContentUrl());
        
        // read their content
        try
        {
            readerBeforeWrite.getContentString();
        }
        catch (Throwable e)
        {
            // The content doesn't exist for this reader
        }
        String contentCheck = readerAfterWrite.getContentString();
        assertEquals("Incorrect content", "ABC", contentCheck);
        
        // check closed state of readers
        assertFalse("Before-content reader stream should not be closed", readerBeforeWrite.isClosed());
        assertTrue("After-content reader should be closed after reading", readerAfterWrite.isClosed());
    }
    
    /**
     * Helper method to check if a store contains a particular URL using the getUrl method
     */
    private boolean searchForUrl(ContentStore store, final String contentUrl, Date from, Date to)
    {
        final boolean[] found = new boolean[] {false};
        ContentUrlHandler handler = new ContentUrlHandler()
        {
            public void handle(String checkContentUrl)
            {
                if (contentUrl.equals(checkContentUrl))
                {
                    found[0] = true;
                }
            }
        };
        getStore().getUrls(from, to, handler);
        return found[0];
    }
    
    public void testGetUrls()
    {
        ContentWriter writer = getWriter();
        writer.putContent("Content for " + getName());
        final String contentUrl = writer.getContentUrl();
        ContentStore store = getStore();
        boolean inStore = searchForUrl(store, contentUrl, null, null);
        assertTrue("New content not found in URL set", inStore);
    }
    
    public void testDeleteSimple() throws Exception
    {
        ContentStore store = getStore();
        ContentWriter writer = getWriter();
        writer.putContent("Content for " + getName());
        String contentUrl = writer.getContentUrl();
        assertTrue("Content must now exist", store.exists(contentUrl));
        try
        {
            store.delete(contentUrl);
        }
        catch (UnsupportedOperationException e)
        {
            logger.warn("Store test " + getName() + " not possible on " + store.getClass().getName());
            return;
        }
        assertFalse("Content must now be removed", store.exists(contentUrl));
    }
    
    /**
     * Tests deletion of content.
     * <p>
     * Only applies when {@link #getStore()} returns a value.
     */
    public void testDeleteReaderStates() throws Exception
    {
        ContentStore store = getStore();
        ContentWriter writer = getWriter();
        
        String content = "Content for " + getName();
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
        ContentWriter writer = getWriter();
        
        String content = "ABC";
        writer.putContent(content);
        assertTrue("Stream close not detected", writer.isClosed());

        ContentReader reader = writer.getReader();
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
        ContentWriter writer = getWriter();
        
        File sourceFile = TempFileProvider.createTempFile(getName(), ".txt");
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
        File sinkFile = TempFileProvider.createTempFile(getName(), ".txt");
        sinkFile.deleteOnExit();
        
        // get the content into our temp file
        ContentReader reader = writer.getReader();
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
        ContentWriter writer = getWriter();

        String content = "ABC";
        // put the content using a stream
        InputStream is = new ByteArrayInputStream(content.getBytes());
        writer.putContent(is);
        assertTrue("Stream close not detected", writer.isClosed());
        
        // get the content using a stream
        ByteArrayOutputStream os = new ByteArrayOutputStream(100);
        ContentReader reader = writer.getReader();
        reader.getContent(os);
        byte[] bytes = os.toByteArray();
        String check = new String(bytes);
        
        assertEquals("Write out and read in using streams failed", content, check);
    }
    
    public void testReadAndWriteStreamByPush() throws Exception
    {
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
        ContentReader reader = writer.getReader();
        InputStream is = reader.getContentInputStream();
        byte[] buffer = new byte[100];
        int count = is.read(buffer);
        assertEquals("No content read", 3, count);
        is.close();
        String check = new String(buffer, 0, count);
        
        assertEquals("Write out of and read into files failed", content, check);
    }
    
    /**
     * Tests retrieval of all content URLs
     * <p>
     * Only applies when {@link #getStore()} returns a value.
     */
    public void testListUrls() throws Exception
    {
        ContentStore store = getStore();
        // Ensure that this test can be done
        try
        {
            searchForUrl(store, "abc", null, null);
        }
        catch (UnsupportedOperationException e)
        {
            logger.warn("Store test " + getName() + " not possible on " + store.getClass().getName());
            return;
        }
        // Proceed with the test
        ContentWriter writer = getWriter();
        String contentUrl = writer.getContentUrl();

        boolean inStore = searchForUrl(store, contentUrl, null, null);
        assertTrue("Writer URL not listed by store", inStore);

        Date yesterday = new Date(System.currentTimeMillis() - 3600L * 1000L * 24L);
        
        // write some data
        writer.putContent("The quick brown fox...");

        // check again
        inStore = searchForUrl(store, contentUrl, null, null);
        assertTrue("Writer URL not listed by store", inStore);
        
        // check that the query for content created before this time yesterday doesn't return the URL
        inStore = searchForUrl(store, contentUrl, null, yesterday);
        assertFalse("URL was younger than required, but still shows up", inStore);
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
        ContentContext writerTruncateCtx = new ContentContext(writer.getReader(), null);
        ContentWriter writerTruncate = getStore().getWriter(writerTruncateCtx);
        assertEquals("Content size incorrect", 0, writerTruncate.getSize());
        // get the channel with truncation
        FileChannel fcTruncate = writerTruncate.getFileChannel(true);
        fcTruncate.close();
        assertEquals("Content not truncated", 0, writerTruncate.getSize());
        
        // get a new writer from the store, using the existing content and perform a non-truncation check
        ContentContext writerNoTruncateCtx = new ContentContext(writer.getReader(), null);
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
