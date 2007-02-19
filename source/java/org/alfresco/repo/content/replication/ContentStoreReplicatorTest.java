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
package org.alfresco.repo.content.replication;

import java.io.File;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;

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
        
        File tempDir = TempFileProvider.getTempDir();
        // create the source file store
        String storeDir = tempDir.getAbsolutePath() + File.separatorChar + getName() + File.separatorChar + GUID.generate();
        sourceStore = new FileContentStore(storeDir);
        // create the target file store
        storeDir = tempDir.getAbsolutePath() + File.separatorChar + getName() + File.separatorChar + GUID.generate();
        targetStore = new FileContentStore(storeDir);
        
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
        ContentWriter writer = sourceStore.getWriter(null, null);
        writer.putContent("123");
        
        // replicate
        replicator.start();
        
        // wait a second
        synchronized(this)
        {
            this.wait(1000L);
        }
        
        assertTrue("Target store doesn't have content added to source",
                targetStore.exists(writer.getContentUrl()));
        
        // this was a single pass, so now more replication should be done
        writer = sourceStore.getWriter(null, null);
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
        
        String duplicateUrl = AbstractContentStore.createNewUrl();
        // start the replicator - it won't wait between iterations
        for (int i = 0; i < 10; i++)
        {
            // put some content into both the target and source
            duplicateUrl = AbstractContentStore.createNewUrl();
            ContentWriter duplicateTargetWriter = targetStore.getWriter(null, duplicateUrl); 
            ContentWriter duplicateSourceWriter = sourceStore.getWriter(null, duplicateUrl);
            duplicateTargetWriter.putContent("Duplicate Target Content: " + i);
            duplicateSourceWriter.putContent(duplicateTargetWriter.getReader());
            
            for (int j = 0; j < 100; j++)
            {
                // write content
                ContentWriter writer = sourceStore.getWriter(null, null);
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
        Set<String> sourceUrls = sourceStore.getUrls();
        Set<String> targetUrls = targetStore.getUrls();
        
        sourceUrls.containsAll(targetUrls);
        targetUrls.contains(sourceUrls);
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
