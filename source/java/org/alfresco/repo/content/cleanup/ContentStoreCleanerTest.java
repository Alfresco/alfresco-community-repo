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
package org.alfresco.repo.content.cleanup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.avm.AVMNodeDAO;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.TempFileProvider;
import org.springframework.context.ApplicationContext;

import junit.framework.TestCase;

/**
 * @see org.alfresco.repo.content.cleanup.ContentStoreCleaner
 * 
 * @author Derek Hulley
 */
public class ContentStoreCleanerTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private ContentStoreCleaner cleaner;
    private ContentStore store;
    private ContentStoreCleanerListener listener;
    private List<String> deletedUrls;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        TransactionService transactionService = serviceRegistry.getTransactionService();
        DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
        NodeDaoService nodeDaoService = (NodeDaoService) ctx.getBean("nodeDaoService");
        AVMNodeDAO avmNodeDAO = (AVMNodeDAO) ctx.getBean("avmNodeDAO");
        
        // we need a store
        store = new FileContentStore(TempFileProvider.getTempDir().getAbsolutePath());
        // and a listener
        listener = new DummyCleanerListener();
        // initialise record of deleted URLs
        deletedUrls = new ArrayList<String>(5);
        
        // construct the test cleaner
        cleaner = new ContentStoreCleaner();
        cleaner.setTransactionService(transactionService);
        cleaner.setDictionaryService(dictionaryService);
        cleaner.setNodeDaoService(nodeDaoService);
        cleaner.setAvmNodeDAO(avmNodeDAO);
        cleaner.setStores(Collections.singletonList(store));
        cleaner.setListeners(Collections.singletonList(listener));
    }
    
    public void testImmediateRemoval() throws Exception
    {
        cleaner.setProtectDays(0);
        // add some content to the store
        ContentWriter writer = store.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
        writer.putContent("ABC");
        String contentUrl = writer.getContentUrl();
        
        // fire the cleaner
        cleaner.execute();
        
        // the content should have disappeared as it is not in the database
        assertFalse("Unprotected content was not deleted", store.exists(contentUrl));
        assertTrue("Content listener was not called with deletion", deletedUrls.contains(contentUrl));
    }
    
    public void testProtectedRemoval() throws Exception
    {
        cleaner.setProtectDays(1);
        // add some content to the store
        ContentWriter writer = store.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
        writer.putContent("ABC");
        String contentUrl = writer.getContentUrl();
        
        // fire the cleaner
        cleaner.execute();
        
        // the content should have disappeared as it is not in the database
        assertTrue("Protected content was deleted", store.exists(contentUrl));
        assertFalse("Content listener was called with deletion of protected URL", deletedUrls.contains(contentUrl));
    }
    
    private class DummyCleanerListener implements ContentStoreCleanerListener
    {
        public void beforeDelete(ContentReader reader) throws ContentIOException
        {
            deletedUrls.add(reader.getContentUrl());
        }
    }
}
