/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.domain.audit;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.domain.audit.AuditDAO.AuditApplicationInfo;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Pair;
import org.apache.commons.lang.mutable.MutableInt;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @see ContentDataDAO
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditDAOTest extends TestCase
{
    private ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private AuditDAO auditDAO;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        auditDAO = (AuditDAO) ctx.getBean("auditDAO");
    }
    
    public void testAuditModel() throws Exception
    {
        final File file = AbstractContentTransformerTest.loadQuickTestFile("pdf");
        assertNotNull(file);
        final URL url = new URL("file:" + file.getAbsolutePath());
        RetryingTransactionCallback<Pair<Long, ContentData>> callback = new RetryingTransactionCallback<Pair<Long, ContentData>>()
        {
            public Pair<Long, ContentData> execute() throws Throwable
            {
                Pair<Long, ContentData> auditModelPair = auditDAO.getOrCreateAuditModel(url);
                return auditModelPair;
            }
        };
        Pair<Long, ContentData> configPair = txnHelper.doInTransaction(callback);
        assertNotNull(configPair);
        // Now repeat.  The results should be exactly the same.
        Pair<Long, ContentData> configPairCheck = txnHelper.doInTransaction(callback);
        assertNotNull(configPairCheck);
        assertEquals(configPair, configPairCheck);
    }
    
    public void testAuditApplication() throws Exception
    {
        final File file = AbstractContentTransformerTest.loadQuickTestFile("pdf");
        assertNotNull(file);
        final URL url = new URL("file:" + file.getAbsolutePath());
        RetryingTransactionCallback<Long> createModelCallback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                return auditDAO.getOrCreateAuditModel(url).getFirst();
            }
        };
        final Long modelId = txnHelper.doInTransaction(createModelCallback);
        
        final String appName = getName() + "." + System.currentTimeMillis();
        final int count = 1000;
        RetryingTransactionCallback<Void> createAppCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                for (int i = 0; i < count; i++)
                {
                    AuditApplicationInfo appInfo = auditDAO.getAuditApplication(appName);
                    if (appInfo == null)
                    {
                        appInfo = auditDAO.createAuditApplication(appName, modelId);
                    }
                }
                return null;
            }
        };
        long before = System.nanoTime();
        txnHelper.doInTransaction(createAppCallback);
        long after = System.nanoTime();
        System.out.println(
                "Time for " + count + " application creations was " +
                ((double)(after - before)/(10E6)) + "ms");
    }
    
    public void testAuditEntry() throws Exception
    {
        doAuditEntryImpl(1000);
    }
    /**
     * @return              Returns the name of the application
     */
    private String doAuditEntryImpl(final int count) throws Exception
    {
        final File file = AbstractContentTransformerTest.loadQuickTestFile("pdf");
        assertNotNull(file);
        final URL url = new URL("file:" + file.getAbsolutePath());
        final String appName = getName() + "." + System.currentTimeMillis();

        RetryingTransactionCallback<Long> createAppCallback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                AuditApplicationInfo appInfo = auditDAO.getAuditApplication(appName);
                if (appInfo == null)
                {
                    Long modelId = auditDAO.getOrCreateAuditModel(url).getFirst();
                    appInfo = auditDAO.createAuditApplication(appName, modelId);
                }
                return appInfo.getId();
            }
        };
        final Long sessionId = txnHelper.doInTransaction(createAppCallback);
        
        final String username = "alexi";
        RetryingTransactionCallback<Void> createEntryCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                for (int i = 0; i < count; i++)
                {
                    Map<String, Serializable> values = Collections.singletonMap("/a/b/c", (Serializable) new Integer(i));
                    long now = System.currentTimeMillis();
                    auditDAO.createAuditEntry(sessionId, now, username, values);
                }
                return null;
            }
        };
        long before = System.nanoTime();
        txnHelper.doInTransaction(createEntryCallback);
        long after = System.nanoTime();
        System.out.println(
                "Time for " + count + " entry creations was " +
                ((double)(after - before)/(10E6)) + "ms");
        // Done
        return appName;
    }
    
    public synchronized void testAuditQuery() throws Exception
    {
        // Some entries
        doAuditEntryImpl(1);
        
        final MutableInt count = new MutableInt(0);
        final LinkedList<Long> timestamps = new LinkedList<Long>();
        // Find everything, but look for a specific key
        final AuditQueryCallback callback = new AuditQueryCallback()
        {
            public boolean handleAuditEntry(
                    Long entryId,
                    String applicationName,
                    String user,
                    long time,
                    Map<String, Serializable> values)
            {
                count.setValue(count.intValue() + 1);
                timestamps.add(time);
                return true;
            }

            public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
            {
                throw new AlfrescoRuntimeException(errorMsg, error);
            }
        };
        
        RetryingTransactionCallback<Void> findCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                auditDAO.findAuditEntries(callback, true, null, null, null, null, "/a/b/c", null, 2);
                return null;
            }
        };
        count.setValue(0);
        timestamps.clear();
        txnHelper.doInTransaction(findCallback);
        assertTrue("Expected at least one result", count.intValue() > 0);
        
        // Make sure that the last two entries are in forward order (ascending time)
        Long lastTimestamp = timestamps.removeLast();
        Long secondLastTimeStamp = timestamps.removeLast();
        assertTrue("The timestamps should be in ascending order", lastTimestamp.compareTo(secondLastTimeStamp) > 0);
        
        // Make sure that the last two entries differ in time
        wait(1000L);
        
        // Search in reverse order
        doAuditEntryImpl(1);
        RetryingTransactionCallback<Void> findReverseCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                auditDAO.findAuditEntries(callback, false, null, null, null, null, "/a/b/c", null, 2);
                return null;
            }
        };
        timestamps.clear();
        txnHelper.doInTransaction(findReverseCallback);
        
        // Make sure that the last two entries are in reverse order (descending time)
        lastTimestamp = timestamps.removeLast();
        secondLastTimeStamp = timestamps.removeLast();
        assertTrue("The timestamps should be in descending order", lastTimestamp.compareTo(secondLastTimeStamp) < 0);
    }
    
    public void testAuditDeleteEntries() throws Exception
    {
        final AuditQueryCallback noResultsCallback = new AuditQueryCallback()
        {
            public boolean handleAuditEntry(
                    Long entryId,
                    String applicationName,
                    String user,
                    long time,
                    Map<String, Serializable> values)
            {
                fail("Expected no results.  All entries should have been removed.");
                return false;
            }

            public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
            {
                throw new AlfrescoRuntimeException(errorMsg, error);
            }
        };
        
        // Some entries
        final String appName = doAuditEntryImpl(1);
        // Delete the entries
        RetryingTransactionCallback<Void> deletedCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                Long appId = auditDAO.getAuditApplication(appName).getId();
                auditDAO.deleteAuditEntries(appId, null, null);
                // There should be no entries
                auditDAO.findAuditEntries(noResultsCallback, true, appName, null, null, null, -1);
                return null;
            }
        };
        txnHelper.doInTransaction(deletedCallback);
    }
}
