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
package org.alfresco.repo.domain.audit;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.domain.audit.AuditDAO.AuditApplicationInfo;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.repo.domain.dialect.MySQLClusterNDBDialect;
import org.alfresco.repo.domain.propval.PropValGenerator;
import org.alfresco.repo.domain.propval.PropertyValueDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.testing.category.DBTests;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.experimental.categories.Category;
import org.springframework.context.ConfigurableApplicationContext;

import junit.framework.TestCase;

/**
 * @see ContentDataDAO
 * 
 * @author Derek Hulley
 * @since 3.2
 */
@Category({OwnJVMTestsCategory.class, DBTests.class})
public class AuditDAOTest extends TestCase
{
    private ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private AuditDAO auditDAO;
    private PropertyValueDAO propertyValueDAO;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        auditDAO = (AuditDAO) ctx.getBean("auditDAO");
        propertyValueDAO = ctx.getBean(PropertyValueDAO.class);
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
            public boolean valuesRequired()
            {
                return false;
            }

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
        
        final AuditQueryParameters params = new AuditQueryParameters();
        params.addSearchKey("/a/b/c", null);
        
        RetryingTransactionCallback<Void> findCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                auditDAO.findAuditEntries(callback, params, 2);
                return null;
            }
        };
        count.setValue(0);
        timestamps.clear();
        txnHelper.doInTransaction(findCallback);
        assertTrue("Expected at least one result", count.intValue() > 0);
        
//        // Make sure that the last two entries are in forward order (ascending time)
//        Long lastTimestamp = timestamps.removeLast();
//        Long secondLastTimeStamp = timestamps.removeLast();
//        assertTrue("The timestamps should be in ascending order", lastTimestamp.compareTo(secondLastTimeStamp) > 0);
//        
        // Make sure that the last two entries differ in time
        wait(1000L);
        
        // Search in reverse order
        doAuditEntryImpl(1);
        RetryingTransactionCallback<Void> findReverseCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                params.setForward(false);
                auditDAO.findAuditEntries(callback, params, 2);
                params.setForward(true);
                return null;
            }
        };
        timestamps.clear();
        txnHelper.doInTransaction(findReverseCallback);
//        
//        // Make sure that the last two entries are in reverse order (descending time)
//        lastTimestamp = timestamps.removeLast();
//        secondLastTimeStamp = timestamps.removeLast();
//        assertTrue("The timestamps should be in descending order", lastTimestamp.compareTo(secondLastTimeStamp) < 0);
    }

    /*
     * Test combinations of fromId, toId, fromTime, toTime and maxResults
     */
    public synchronized void testAuditQueryCombos() throws Exception
    {
        // Some entries
        doAuditEntryImpl(10);

        final MutableInt count = new MutableInt(0);
        final LinkedList<Long> timestamps = new LinkedList<Long>();
        final List<Long> entryIds = new LinkedList<>();
        // Find everything
        final AuditQueryCallback callback = new AuditQueryCallback()
        {            
            public boolean valuesRequired()
            {
                return false;
            }

            public boolean handleAuditEntry(
                    Long entryId,
                    String applicationName,
                    String user,
                    long time,
                    Map<String, Serializable> values)
            {
                count.setValue(count.intValue() + 1);
                timestamps.add(time);
                entryIds.add(entryId);
                return true;
            }

            public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
            {
                throw new AlfrescoRuntimeException(errorMsg, error);
            }
        };
        
        final AuditQueryParameters params = new AuditQueryParameters();
        params.addSearchKey("/a/b/c", null);

        //. get them all
        RetryingTransactionCallback<Void> findCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                auditDAO.findAuditEntries(callback, params, 10);
                return null;
            }
        };
        count.setValue(0);
        timestamps.clear();
        txnHelper.doInTransaction(findCallback);
        assertEquals(10, count.intValue());

        // copy what we found so that we can compare subsequent audit queries
        List<Long> allEntryIds = new ArrayList<>(entryIds);
        List<Long> allTimestamps = new ArrayList<>(timestamps);

        // test fromId and maxResults
        entryIds.clear();
        timestamps.clear();
        params.setFromId(allEntryIds.get(2));
        findCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                auditDAO.findAuditEntries(callback, params, 2);
                return null;
            }
        };
        txnHelper.doInTransaction(findCallback);
        assertTrue(allEntryIds.subList(2, 2 + 2).equals(entryIds));

        // test toId and maxResults
        entryIds.clear();
        timestamps.clear();
        params.setFromId(null);
        params.setFromTime(null);
        params.setToTime(null);
        params.setToId(allEntryIds.get(2));
        findCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                auditDAO.findAuditEntries(callback, params, 2);
                return null;
            }
        };
        txnHelper.doInTransaction(findCallback);
        assertTrue(allEntryIds.subList(0, 2).equals(entryIds));

        // test fromId and toId and maxResults
        entryIds.clear();
        timestamps.clear();
        params.setFromId(allEntryIds.get(2));
        params.setToId(allEntryIds.get(5));
        params.setFromTime(null);
        params.setToTime(null);
        findCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                auditDAO.findAuditEntries(callback, params, 1);
                return null;
            }
        };
        txnHelper.doInTransaction(findCallback);
        assertTrue(allEntryIds.subList(2, 3).equals(entryIds));

        // test fromTime and maxResults
        entryIds.clear();
        timestamps.clear();
        params.setFromTime(allTimestamps.get(2));
        params.setFromId(null);
        params.setToTime(null);
        params.setToId(null);
        findCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                auditDAO.findAuditEntries(callback, params, 2);
                return null;
            }
        };
        txnHelper.doInTransaction(findCallback);
        assertTrue(allTimestamps.subList(2, 4).equals(timestamps));

        // test toTime and maxResults
        entryIds.clear();
        timestamps.clear();
        params.setFromTime(null);
        params.setFromId(null);
        params.setToTime(allTimestamps.get(4));
        params.setToId(null);
        findCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                auditDAO.findAuditEntries(callback, params, 2);
                return null;
            }
        };
        txnHelper.doInTransaction(findCallback);
        assertTrue(allTimestamps.subList(0, 2).equals(timestamps));

        // test fromTime and toTime and maxResults
        entryIds.clear();
        timestamps.clear();
        params.setFromTime(allTimestamps.get(2));
        params.setFromId(null);
        params.setToTime(allTimestamps.get(5));
        params.setToId(null);
        findCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                auditDAO.findAuditEntries(callback, params, 2);
                return null;
            }
        };
        txnHelper.doInTransaction(findCallback);
        assertTrue(allTimestamps.subList(2, 4).equals(timestamps));
    }

    public void testAuditDeleteEntries() throws Exception
    {
        final AuditQueryCallback noResultsCallback = new AuditQueryCallback()
        {
            public boolean valuesRequired()
            {
                return false;
            }

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

        final AuditQueryParameters params = new AuditQueryParameters();
        params.setApplicationName(appName);
        // Delete the entries
        RetryingTransactionCallback<Void> deletedCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                Long appId = auditDAO.getAuditApplication(appName).getId();
                auditDAO.deleteAuditEntries(appId, null, null);
                // There should be no entries
                auditDAO.findAuditEntries(noResultsCallback, params, Integer.MAX_VALUE);
                return null;
            }
        };
        txnHelper.doInTransaction(deletedCallback);
    }

    /**
     * Ensure that only the correct application's audit entries are deleted.
     * @throws Exception 
     */
    public void testAuditDeleteEntriesForApplication() throws Exception
    {
        final String app1 = doAuditEntryImpl(6);
        final String app2 = doAuditEntryImpl(18);
        
        final AuditQueryCallbackImpl resultsCallback = new AuditQueryCallbackImpl();
        
        RetryingTransactionCallback<Void> deletedCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                Long app1Id = auditDAO.getAuditApplication(app1).getId();
                auditDAO.deleteAuditEntries(app1Id, null, null);
                // There should be no entries for app1
                // but still entries for app2
                auditDAO.findAuditEntries(resultsCallback, new AuditQueryParameters(), Integer.MAX_VALUE);
                assertEquals("All entries should have been deleted from app1", 0, resultsCallback.numEntries(app1));
                assertEquals("No entries should have been deleted from app2", 18, resultsCallback.numEntries(app2));
                return null;
            }
        };
        txnHelper.doInTransaction(deletedCallback);
    }
    
    
    /**
     * Ensure that an application's audit entries can be deleted between 2 times.
     * @throws Exception
     */
    public void testAuditDeleteEntriesForApplicationBetweenTimes() throws Exception
    {
        RetryingTransactionCallback<Void> deletedCallback = new RetryingTransactionCallback<Void>()
        {
            AuditQueryCallbackImpl preDeleteCallback = new AuditQueryCallbackImpl();
            AuditQueryCallbackImpl resultsCallback = new AuditQueryCallbackImpl();
     
            
            public Void execute() throws Throwable
            {
                AuditApplicationInfo info1 = createAuditApp();
                String app1 = info1.getName();
                Long app1Id = info1.getId();
                AuditApplicationInfo info2 = createAuditApp();
                String app2 = info2.getName();
                
                // Create items 10, 11, 12, 13, 14 for application 1
                // Create items 21, 22 for application 2
                createItem(info1, 10);
                createItem(info1, 11);
                Thread.sleep(10); // stop previous statements being executed during t1
                Thread.sleep(10);
                final long t1 = System.currentTimeMillis();
                Thread.sleep(10);
                Thread.sleep(10);
                createItem(info2, 21);
                createItem(info1, 12);
                createItem(info1, 13);
                Thread.sleep(10);
                Thread.sleep(10);
                final long t2 = System.currentTimeMillis();
                Thread.sleep(10); // stop next statements being executed during t2
                Thread.sleep(10);
                createItem(info2, 22);
                createItem(info1, 14);
                
                
                auditDAO.findAuditEntries(preDeleteCallback, new AuditQueryParameters(), Integer.MAX_VALUE);
                assertEquals(5, preDeleteCallback.numEntries(app1));
                assertEquals(2, preDeleteCallback.numEntries(app2));
                
                auditDAO.deleteAuditEntries(app1Id, t1, t2);
                
                auditDAO.findAuditEntries(resultsCallback, new AuditQueryParameters(), Integer.MAX_VALUE);
                assertEquals("Two entries should have been deleted from app1", 3, resultsCallback.numEntries(app1));
                assertEquals("No entries should have been deleted from app2", 2, resultsCallback.numEntries(app2));
                return null;
            }
        };
        txnHelper.doInTransaction(deletedCallback);
    }

    /**
     * Ensure audit entries can be deleted between two times - for all applications.
     * @throws Exception
     */
    public void testAuditDeleteEntriesBetweenTimes() throws Exception
    {
        RetryingTransactionCallback<Void> deletedCallback = new RetryingTransactionCallback<Void>()
        {
            AuditQueryCallbackImpl preDeleteCallback = new AuditQueryCallbackImpl();
            AuditQueryCallbackImpl resultsCallback = new AuditQueryCallbackImpl();
     
            
            public Void execute() throws Throwable
            {
                AuditApplicationInfo info1 = createAuditApp();
                String app1 = info1.getName();
                AuditApplicationInfo info2 = createAuditApp();
                String app2 = info2.getName();
                
                // Create items 10, 11, 12, 13, 14 for application 1
                // Create items 21, 22 for application 2
                createItem(info1, 10);
                createItem(info1, 11);
                Thread.sleep(10);
                Thread.sleep(10); // stop previous statements being executed during t1
                final long t1 = System.currentTimeMillis();
                Thread.sleep(10);
                Thread.sleep(10);
                createItem(info2, 21);
                createItem(info1, 12);
                createItem(info1, 13);
                Thread.sleep(10);
                Thread.sleep(10);
                final long t2 = System.currentTimeMillis();
                Thread.sleep(10); // stop next statements being executed during t2
                Thread.sleep(10);
                createItem(info2, 22);
                createItem(info1, 14);

                auditDAO.findAuditEntries(preDeleteCallback, new AuditQueryParameters(), Integer.MAX_VALUE);
                assertEquals(5, preDeleteCallback.numEntries(app1));
                assertEquals(2, preDeleteCallback.numEntries(app2));
                
                // Delete audit entries between times - for all applications.
                auditDAO.deleteAuditEntries(null, t1, t2);
                
                auditDAO.findAuditEntries(resultsCallback, new AuditQueryParameters(), Integer.MAX_VALUE);
                assertEquals("Two entries should have been deleted from app1", 3, resultsCallback.numEntries(app1));
                assertEquals("One entry should have been deleted from app2", 1, resultsCallback.numEntries(app2));
                return null;
            }
        };
        txnHelper.doInTransaction(deletedCallback);
    }
    
    /**
     * Create an audit item
     * @param appInfo The audit application to create the item for.
     * @param value The value that will be stored against the path /a/b/c
     */
    private void createItem(final AuditApplicationInfo appInfo, final int value)
    {
        String username = "alexi";    
        Map<String, Serializable> values = Collections.singletonMap("/a/b/c", (Serializable) value);
        long now = System.currentTimeMillis();
        auditDAO.createAuditEntry(appInfo.getId(), now, username, values);
    }

    
    /**
     * Create an audit application.
     * @return AuditApplicationInfo for the new application.
     * @throws IOException 
     */
    private AuditApplicationInfo createAuditApp() throws IOException
    {
        String appName = getName() + "." + GUID.generate();
        File file = AbstractContentTransformerTest.loadQuickTestFile("pdf");
        assertNotNull(file);
        URL url = new URL("file:" + file.getAbsolutePath());
        
        AuditApplicationInfo appInfo = auditDAO.getAuditApplication(appName);
        if (appInfo == null)
        {
            Long modelId = auditDAO.getOrCreateAuditModel(url).getFirst();
            appInfo = auditDAO.createAuditApplication(appName, modelId);
        }
        return appInfo;
    }


    public class AuditQueryCallbackImpl implements AuditQueryCallback
    {
        private Map<String, Integer> countsByApp = new HashMap<String, Integer>();
        
        public boolean valuesRequired()
        {
            return false;
        }

        public boolean handleAuditEntry(
                Long entryId,
                String applicationName,
                String user,
                long time,
                Map<String, Serializable> values)
        {
            Integer count = countsByApp.get(applicationName);
            if (count == null)
                countsByApp.put(applicationName, 1);
            else
                countsByApp.put(applicationName, ++count);
            
            return true;
        }

        public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
        {
            throw new AlfrescoRuntimeException(errorMsg, error);
        }
        
        public int numEntries(String appName)
        {
            if (countsByApp.containsKey(appName))
                return countsByApp.get(appName);
            else
                return 0;
        }
    }
    
    
    /**
     * MNT-10067: use a script to delete the orphaned audit data (property values). 
     */
    public void testScriptCanDeleteOrphanedProps() throws Exception
    {
        Dialect dialect = (Dialect) ctx.getBean("dialect");
        if (dialect instanceof MySQLClusterNDBDialect)
        {
            throw new Exception("TODO review this test case with NDB - note: throw exeception here else causes later tests to fail (when running via AllDBTestTestSuite)");
        }
        
        // single test
        scriptCanDeleteOrphanedPropsWork(false);
    }

    public void testMaxResults() throws Exception
    {
        try
        {
            AuditQueryCallbackImpl callback = new AuditQueryCallbackImpl();
            auditDAO.findAuditEntries(callback, new AuditQueryParameters(), -1);
            fail("maxResults == -1 should be disallowed");
        }
        catch(IllegalArgumentException e)
        {
            // ok
        }
    }

    private void scriptCanDeleteOrphanedPropsWork(final boolean performance) throws Exception
    {
        final int iterationStep, maxIterations;
        if (performance)
        {
            iterationStep = 1000;
            maxIterations = 1000;
        }
        else
        {
            iterationStep = 1;
            maxIterations = 1;
        }
        
        UserTransaction txn;
        
        for (int i = iterationStep; i <= maxIterations*iterationStep; i+=iterationStep)
        {
            List<String> stringValues = new LinkedList<String>();
            List<Double> doubleValues = new LinkedList<Double>();
            List<Date> dateValues = new LinkedList<Date>();
       
            txn  = transactionService.getUserTransaction();
            long startCreate = System.currentTimeMillis();
            txn.begin();
            for (int j = 0; j < i; j++)
            {
                PropValGenerator valueGen = new PropValGenerator(propertyValueDAO);
                String stringValue = valueGen.createUniqueString();
                stringValues.add(stringValue);
                Double doubleValue = valueGen.createUniqueDouble();
                doubleValues.add(doubleValue);
                Date dateValue = valueGen.createUniqueDate();
                dateValues.add(dateValue);
                
                AuditQueryCallbackImpl preDeleteCallback = new AuditQueryCallbackImpl();
                AuditQueryCallbackImpl resultsCallback = new AuditQueryCallbackImpl();
                
                AuditApplicationInfo info1 = createAuditApp();
                String app1 = info1.getName();
                
                String username = "alexi";    
                Map<String, Serializable> values = new HashMap<String, Serializable>();
                values.put("/a/b/string-" + j, stringValue);
                values.put("/a/b/double-" + j, doubleValue);
                values.put("/a/b/date-" + j, dateValue);
                // TODO: how to deal with Serializable values which cannot be retrieved later in test by value alone?
                long now = System.currentTimeMillis();
                auditDAO.createAuditEntry(info1.getId(), now, username, values);

                auditDAO.findAuditEntries(preDeleteCallback, new AuditQueryParameters(), Integer.MAX_VALUE);
                assertEquals(1, preDeleteCallback.numEntries(app1));
                
                // Delete audit entries between times - for all applications.
                auditDAO.deleteAuditEntries(info1.getId(), null, null);
                
                if (!performance)
                {
                    auditDAO.findAuditEntries(resultsCallback, new AuditQueryParameters(), Integer.MAX_VALUE);
                    assertEquals("All entries should have been deleted from app1", 0, resultsCallback.numEntries(app1));
                }
            }
            txn.commit();
            System.out.println("Created values for " + i + " entries in " + (System.currentTimeMillis() - startCreate) + " ms.");
            
            if (!performance)
            {
                // Check there are some persisted values to delete.
                // Unlike PropertyValueDAOTest we're using the getPropertyValue() method here,
                // instead of the datatype-specific methods (e.g. getPropertyStringValue()).
                // This is because AuditDAO persists an entire map of values resulting in different behaviour
                // (i.e. dates are persisted as Serializable)
                for (String stringValue : stringValues)
                {
                    assertEquals(stringValue, propertyValueDAO.getPropertyValue(stringValue).getSecond());
                }
                for (Double doubleValue : doubleValues)
                {
                    assertEquals(doubleValue, propertyValueDAO.getPropertyValue(doubleValue).getSecond());
                }
                for (Date dateValue : dateValues)
                {
                    assertEquals(dateValue, propertyValueDAO.getPropertyValue(dateValue).getSecond());
                }
            }
            long startDelete = System.currentTimeMillis();
            RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    propertyValueDAO.cleanupUnusedValues();

                    return null;
                }
            };
            // use a new transaction so it will retry in that transaction
            txnHelper.doInTransaction(callback,false,true);

            System.out.println("Cleaned values for " + i + " entries in " + (System.currentTimeMillis() - startDelete) + " ms.");
            
            if (!performance)
            {
                // Check all the properties have been deleted.
                txn  = transactionService.getUserTransaction();
                txn.begin();
                
                for (String stringValue : stringValues)
                {
                    assertPropDeleted(propertyValueDAO.getPropertyValue(stringValue));
                }
                for (Double doubleValue : doubleValues)
                {
                    assertPropDeleted(propertyValueDAO.getPropertyValue(doubleValue));
                }
                for (Date dateValue : dateValues)
                {
                    assertPropDeleted(propertyValueDAO.getPropertyValue(dateValue));
                }
                
                txn.commit();
            }
        }        
    }
    
    private void assertPropDeleted(Pair<Long, ?> value)
    {
        if (value != null)
        {
            String msg = String.format("Property value [%s=%s] should have been deleted by cleanup script.",
                        value.getSecond().getClass().getSimpleName(), value.getSecond());
            fail(msg);
        }
    }
    
    public void scriptCanDeleteOrphanedPropsPerformance() throws Exception
    {
        scriptCanDeleteOrphanedPropsWork(true);
    }
    
    public static void main(String[] args)
    {
        try
        {
            AuditDAOTest test = new AuditDAOTest();
            test.setUp();
            System.out.println("Press any key to run performance test.");
            System.in.read();
            test.scriptCanDeleteOrphanedPropsPerformance();
            System.out.println("Press any key to shutdown.");
            System.in.read();
            test.tearDown();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        finally
        {
            ApplicationContextHelper.closeApplicationContext();
        }
    }
}
