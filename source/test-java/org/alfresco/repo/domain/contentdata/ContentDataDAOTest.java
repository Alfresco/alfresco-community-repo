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
package org.alfresco.repo.domain.contentdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.domain.contentdata.ContentDataDAO.ContentUrlHandler;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * @see ContentDataDAO
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class ContentDataDAOTest extends TestCase
{
    private ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private ContentDataDAO contentDataDAO;
    private ContentStore contentStore;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        contentDataDAO = (ContentDataDAO) ctx.getBean("contentDataDAO");
        contentStore = new FileContentStore(ctx, TempFileProvider.getTempDir());
    }
    
    private Pair<Long, ContentData> create(final ContentData contentData)
    {
        RetryingTransactionCallback<Pair<Long, ContentData>> callback = new RetryingTransactionCallback<Pair<Long, ContentData>>()
        {
            public Pair<Long, ContentData> execute() throws Throwable
            {
                Pair<Long, ContentData> contentDataPair = contentDataDAO.createContentData(contentData);
                return contentDataPair;
            }
        };
        return txnHelper.doInTransaction(callback, false, false);
    }
    
    private Pair<Long, ContentData> update(final Long id, final ContentData contentData)
    {
        RetryingTransactionCallback<Pair<Long, ContentData>> callback = new RetryingTransactionCallback<Pair<Long, ContentData>>()
        {
            public Pair<Long, ContentData> execute() throws Throwable
            {
                contentDataDAO.updateContentData(id, contentData);
                return new Pair<Long, ContentData>(id, contentData);
            }
        };
        return txnHelper.doInTransaction(callback, false, false);
    }
    
    private void delete(final Long id)
    {
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                contentDataDAO.deleteContentData(id);
                return null;
            }
        };
        txnHelper.doInTransaction(callback, false, false);
    }
    
    /**
     * Retrieves and checks the ContentData for equality
     */
    private void getAndCheck(final Long contentDataId, ContentData checkContentData)
    {
        RetryingTransactionCallback<Pair<Long, ContentData>> callback = new RetryingTransactionCallback<Pair<Long, ContentData>>()
        {
            public Pair<Long, ContentData> execute() throws Throwable
            {
                Pair<Long, ContentData> contentDataPair = contentDataDAO.getContentData(contentDataId);
                return contentDataPair;
            }
        };
        Pair<Long, ContentData> resultPair = txnHelper.doInTransaction(callback, true, false);
        assertNotNull("Failed to find result for ID " + contentDataId, resultPair);
        assertEquals("ContentData retrieved not the same as persisted: ", checkContentData, resultPair.getSecond());
    }
    
    private ContentData getContentData()
    {
        ContentContext contentCtx = new ContentContext(null, null);
        String contentUrl = contentStore.getWriter(contentCtx).getContentUrl();
        ContentData contentData = new ContentData(
                contentUrl,
                MimetypeMap.MIMETYPE_TEXT_PLAIN,
                12335L,
                "UTF-8",
                Locale.FRENCH);
        return contentData;
    }
    
    public void testGetWithInvalidId()
    {
        try
        {
            contentDataDAO.getContentData(-1L);
            fail("Invalid ContentData IDs must generate DataIntegrityViolationException.");
        }
        catch (DataIntegrityViolationException e)
        {
            // Expected
        }
    }
    
    /**
     * Check that the <code>ContentData</code> is decoded and persisted correctly.
     */
    public void testCreateContentDataSimple() throws Exception
    {
        ContentData contentData = getContentData();
        
        Pair<Long, ContentData> resultPair = create(contentData);
        getAndCheck(resultPair.getFirst(), contentData);
    }
    
    /**
     * Check that the <code>ContentData</code> is decoded and persisted correctly.
     */
    public void testCreateContentDataNulls() throws Exception
    {
        ContentData contentData = new ContentData(null, null, 0L, null, null);
        
        Pair<Long, ContentData> resultPair = create(contentData);
        getAndCheck(resultPair.getFirst(), contentData);
    }
    
    /**
     * Ensure that upper and lowercase URLs don't clash
     * @throws Exception
     */
    public void testEnsureCaseSensitiveStorage() throws Exception
    {
        ContentData contentData = getContentData();
        String contentUrlUpper = contentData.getContentUrl().toUpperCase();
        ContentData contentDataUpper = new ContentData(
                contentUrlUpper, MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, "UTF-8", new Locale("FR"));
        String contentUrlLower = contentData.getContentUrl().toLowerCase();
        ContentData contentDataLower = new ContentData(
                contentUrlLower, MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, "utf-8", new Locale("fr"));
        
        Pair<Long, ContentData> resultPairUpper = create(contentDataUpper);
        getAndCheck(resultPairUpper.getFirst(), contentDataUpper);
        
        Pair<Long, ContentData> resultPairLower = create(contentDataLower);
        getAndCheck(resultPairLower.getFirst(), contentDataLower);
    }
    
    public void testUpdate() throws Exception
    {
        ContentData contentData = getContentData();
        Pair<Long, ContentData> resultPair = create(contentData);
        Long id = resultPair.getFirst();
        // Update
        contentData = ContentData.setMimetype(contentData, MimetypeMap.MIMETYPE_HTML);
        contentData = ContentData.setEncoding(contentData, "UTF-16");
        // Don't update the content itself
        update(id, contentData);
        // Check
        getAndCheck(id, contentData);
    }
    
    public void testDelete() throws Exception
    {
        ContentData contentData = getContentData();
        
        Pair<Long, ContentData> resultPair = create(contentData);
        getAndCheck(resultPair.getFirst(), contentData);
        delete(resultPair.getFirst());
        try
        {
            getAndCheck(resultPair.getFirst(), contentData);
            fail("Entity still exists");
        }
        catch (Throwable e)
        {
            // Expected
        }
    }
    
    /**
     * Check that orphaned content can be re-instated.
     */
    public void testReinstate_ALF3867()
    {
        ContentData contentData = getContentData();
        Pair<Long, ContentData> resultPair = create(contentData);
        getAndCheck(resultPair.getFirst(), contentData);
        delete(resultPair.getFirst());
        // Now create a ContentData with the same URL
        create(contentData);
    }
    
    public void testContentUrl_FetchingOrphansNoLimit() throws Exception
    {
        ContentData contentData = getContentData();
        Pair<Long, ContentData> resultPair = create(contentData);
        getAndCheck(resultPair.getFirst(), contentData);
        delete(resultPair.getFirst());
        // The content URL is orphaned
        final String contentUrlOrphaned = contentData.getContentUrl();
        final boolean[] found = new boolean[] {false}; 
        
        // Iterate over all orphaned content URLs and ensure that we hit the one we just orphaned
        ContentUrlHandler handler = new ContentUrlHandler()
        {
            public void handle(Long id, String contentUrl, Long orphanTime)
            {
                // Check
                if (id == null || contentUrl == null || orphanTime == null)
                {
                    fail("Invalid orphan data returned to handler: " + id + "-" + contentUrl + "-" + orphanTime);
                }
                // Did we get the one we wanted?
                if (contentUrl.equals(contentUrlOrphaned))
                {
                    found[0] = true;
                }
            }
        };
        contentDataDAO.getContentUrlsOrphaned(handler, Long.MAX_VALUE, Integer.MAX_VALUE);
        assertTrue("Newly-orphaned content URL not found", found[0]);
    }
    
    public void testContentUrl_FetchingOrphansWithLimit() throws Exception
    {
        // Orphan some content
        for (int i = 0; i < 5; i++)
        {
            ContentData contentData = getContentData();
            Pair<Long, ContentData> resultPair = create(contentData);
            getAndCheck(resultPair.getFirst(), contentData);
            delete(resultPair.getFirst());
        }
        final int[] count = new int[] {0}; 
        
        // Iterate over all orphaned content URLs and ensure that we hit the one we just orphaned
        ContentUrlHandler handler = new ContentUrlHandler()
        {
            public void handle(Long id, String contentUrl, Long orphanTime)
            {
                // Check
                if (id == null || contentUrl == null || orphanTime == null)
                {
                    fail("Invalid orphan data returned to handler: " + id + "-" + contentUrl + "-" + orphanTime);
                }
                count[0]++;
            }
        };
        contentDataDAO.getContentUrlsOrphaned(handler, Long.MAX_VALUE, 5);
        assertEquals("Expected exactly 5 results callbacks", 5, count[0]);
    }
    
    private static final String[] MIMETYPES = new String[]
                                                         {
                                                            MimetypeMap.MIMETYPE_ACP,
                                                            MimetypeMap.MIMETYPE_EXCEL,
                                                            MimetypeMap.MIMETYPE_IMAGE_JPEG,
                                                            MimetypeMap.MIMETYPE_JAVASCRIPT,
                                                            MimetypeMap.MIMETYPE_RSS
                                                         };
    private static final String[] ENCODINGS = new String[]
                                                         {
                                                            "utf-8",
                                                            "ascii",
                                                            "latin1",
                                                            "wibbles",
                                                            "iso-whatever"
                                                         };
    private static final Locale[] LOCALES = new Locale[]
                                                         {
                                                            Locale.FRENCH,
                                                            Locale.CHINESE,
                                                            Locale.ITALIAN,
                                                            Locale.JAPANESE,
                                                            Locale.ENGLISH
                                                         };
    
    private List<Pair<Long, ContentData>> speedTestWrite(String name, int total)
    {
        System.out.println("Starting write speed test: " + name);
        long start = System.nanoTime();
        List<Pair<Long, ContentData>> pairs = new ArrayList<Pair<Long, ContentData>>(100000);
        // Loop and check for performance degradation
        for (int i = 0; i < (total / 200 / 5); i++)
        {
            for (int j = 0; j < 200; j++)
            {
                for (int k = 0; k < 5; k++)
                {
                    ContentData contentData = getContentData();
                    String contentUrl = contentData.getContentUrl();
                    contentData = new ContentData(
                            contentUrl,
                            MIMETYPES[k],
                            (long) j*k,
                            ENCODINGS[k],
                            LOCALES[k]);
                    Pair<Long, ContentData> pair = create(contentData);
                    pairs.add(pair);
                }
            }
            // That's 1000
            long now = System.nanoTime();
            double diffMs = (double) (now - start) / 1E6;
            double aveMs = diffMs / (double) pairs.size();
            String msg = String.format(
                    "   Wrote %7d rows; average is %5.2f ms per row or %5.2f rows per second",
                    pairs.size(),
                    aveMs,
                    1000.0 / aveMs);
            System.out.println(msg);
        }
        // Done
        return pairs;
    }
    
    private void speedTestRead(String name, List<Pair<Long, ContentData>> pairs)
    {
        System.out.println("Starting read speed test: " + name);
        long start = System.nanoTime();
        // Loop and check for performance degradation
        int num = 1;
        for (Pair<Long, ContentData> pair : pairs)
        {
            Long id = pair.getFirst();
            ContentData contentData = pair.getSecond();
            // Retrieve it
            getAndCheck(id, contentData);
            // Report
            if (num % 1000 == 0)
            {
                long now = System.nanoTime();
                double diffMs = (double) (now - start) / 1E6;
                double aveMs = diffMs / (double) num;
                String msg = String.format(
                        "   Read %7d rows; average is %5.2f ms per row or %5.2f rows per second",
                        num,
                        aveMs,
                        1000.0 / aveMs);
                System.out.println(msg);
            }
            num++;
        }
        // Done
    }
    
    public void testCreateSpeedIndividualTxns()
    {
        List<Pair<Long, ContentData>> pairs = speedTestWrite(getName(), 2000);
        speedTestRead(getName(), pairs);
    }
    
    public void testCreateSpeedSingleTxn()
    {
        RetryingTransactionCallback<List<Pair<Long, ContentData>>> writeCallback = new RetryingTransactionCallback<List<Pair<Long, ContentData>>>()
        {
            public List<Pair<Long, ContentData>> execute() throws Throwable
            {
                return speedTestWrite(getName(), 10000);
            }
        };
        final List<Pair<Long, ContentData>> pairs = txnHelper.doInTransaction(writeCallback, false, false);
        RetryingTransactionCallback<Void> readCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                speedTestRead(getName(), pairs);
                return null;
            }
        };
        txnHelper.doInTransaction(readCallback, false, false);
    }
}
