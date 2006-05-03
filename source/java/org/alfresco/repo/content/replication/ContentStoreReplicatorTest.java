/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Alfresco Network License. You may obtain a
 * copy of the License at
 *
 *   http://www.alfrescosoftware.com/legal/
 *
 * Please view the license relevant to your network subscription.
 *
 * BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
 * READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
 * YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
 * ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
 * THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
 * AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
 * TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
 * BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
 * HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
 * SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
 * TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
 * CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
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
        replicator.setRunContinuously(false);       // replicate once
        replicator.setWaitTime(0);
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
        replicator.setRunContinuously(true);
        replicator.setWaitTime(0L);
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
}
