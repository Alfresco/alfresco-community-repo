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
package org.alfresco.repo.content.replication;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.ContentStore.ContentUrlHandler;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.StaticApplicationContext;

/**
 * Tests the content store replicator.
 * 
 * @see org.alfresco.repo.content.replication.ContentStoreReplicator
 * 
 * @author Derek Hulley
 */
@SuppressWarnings("unused")
public class ContentStoreReplicatorTest extends TestCase
{
    private static final String SOME_CONTENT = "The No. 1 Ladies' Detective Agency";
    
    private ContentStoreReplicator replicator;
    private ContentStore sourceStore;
    private ContentStore targetStore;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        // Create a dummy context for message broadcasting
        StaticApplicationContext ctx = new StaticApplicationContext();
        ctx.refresh();
        
        File tempDir = TempFileProvider.getTempDir();
        // create the source file store
        String storeDir = tempDir.getAbsolutePath() + File.separatorChar + getName() + File.separatorChar + GUID.generate();
        sourceStore = new FileContentStore(ctx, storeDir);
        // create the target file store
        storeDir = tempDir.getAbsolutePath() + File.separatorChar + getName() + File.separatorChar + GUID.generate();
        targetStore = new FileContentStore(ctx, storeDir);
        
        // create the replicator 
        replicator = new ContentStoreReplicator();
        replicator.setSourceStore(sourceStore);
        replicator.setTargetStore(targetStore);
    }
    
    /**
     * Creates a source with some files and replicates in a single pass, checking the results.
     */
    public void testSinglePassReplication() throws Exception
    {
        ContentWriter writer = sourceStore.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
        writer.putContent("123");
        
        // replicate
        replicator.start();
        
        // wait a second
        synchronized(this)
        {
            this.wait(5000L);
        }
        
        assertTrue("Target store doesn't have content added to source",
                targetStore.exists(writer.getContentUrl()));
        
        // this was a single pass, so now more replication should be done
        writer = sourceStore.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
        writer.putContent("456");

        // wait a second
        synchronized(this)
        {
            this.wait(1000L);
        }
        
        assertFalse("Replication should have been single-pass",
                targetStore.exists(writer.getContentUrl()));
    }
    
    /**
     * Handler that merely records the URL
     * 
     * @author Derek Hulley
     * @since 2.0
     */
    private class UrlRecorder implements ContentUrlHandler
    {
        public Set<String> urls = new HashSet<String>(1027);
        public void handle(String contentUrl)
        {
            urls.add(contentUrl);
        }
    }
    
    /**
     * Adds content to the source while the replicator is going as fast as possible.
     * Just to make it more interesting, the content is sometimes put in the target
     * store as well.
     * <p>
     * Afterwards, some content is removed from the the target.
     * <p>
     * Then, finally, a check is performed to ensure that the source and target are
     * in synch.
     */
    public void testContinuousReplication() throws Exception
    {
        replicator.start();
        
        String duplicateUrl = null;
        // start the replicator - it won't wait between iterations
        for (int i = 0; i < 10; i++)
        {
            // put some content into both the target and source
            ContentWriter duplicateSourceWriter = sourceStore.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
            duplicateUrl = duplicateSourceWriter.getContentUrl();
            try
            {
                ContentContext targetContentCtx = new ContentContext(null, duplicateUrl);
                ContentWriter duplicateTargetWriter = targetStore.getWriter(targetContentCtx); 
                duplicateTargetWriter.putContent("Duplicate Target Content: " + i);
                duplicateSourceWriter.putContent(duplicateTargetWriter.getReader());
            }
            catch (ContentIOException e)
            {
                // This can happen because the replicator may have beaten us to it
            }
            
            for (int j = 0; j < 100; j++)
            {
                // write content
                ContentWriter writer = sourceStore.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
                writer.putContent("Repeated put: " + j);
            }
        }
        
        // remove the last duplicated URL from the target
        targetStore.delete(duplicateUrl);
        
        // allow time for the replicator to catch up
        synchronized(this)
        {
            this.wait(1000L);
        }
        
        // check that we have an exact match of URLs
        UrlRecorder sourceUrls = new UrlRecorder();
        UrlRecorder targetUrls = new UrlRecorder();
        sourceStore.getUrls(sourceUrls);
        targetStore.getUrls(targetUrls);
        
        sourceUrls.urls.containsAll(targetUrls.urls);
        targetUrls.urls.contains(sourceUrls.urls);
    }
    
    /**
     * Call the replicator repeatedly to check that it prevents concurrent use
     */
    public void testRepeatedReplication() throws Exception
    {
        for (int i = 0; i < 10; i++)
        {
            replicator.start();
        }
    }
}
