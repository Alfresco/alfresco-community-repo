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
package org.alfresco.repo.activities.feed.cleanup;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import junit.framework.TestCase;

import org.alfresco.repo.domain.activities.ActivityFeedDAO;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.activities.feed.cleanup.FeedCleaner
 * 
 * @author janv
 */
public class FeedCleanerTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private ActivityFeedDAO feedDAO;
    private FeedCleaner cleaner;
    private SiteService siteService;
    protected RetryingTransactionHelper transactionHelper;
    
    @Override
    public void setUp() throws Exception
    {
        siteService = (SiteService) ctx.getBean("SiteService");
        feedDAO = (ActivityFeedDAO) ctx.getBean("feedDAO");
        transactionHelper = (RetryingTransactionHelper)ctx.getBean("retryingTransactionHelper");
        
        tearDown();
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        for (int i = 1; i <= 7; i++)
        {
            siteService.createSite("myPreset", "testSite"+i, null, null, SiteVisibility.PUBLIC);
        }
        
        AuthenticationUtil.setRunAsUserSystem();
        
        // construct the test cleaner
        cleaner = new FeedCleaner();
        cleaner.setFeedDAO(feedDAO);
    }
    
    public void tearDown() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // clean out any remaining feed entries (allows test to be re-runnable)
        feedDAO.deleteFeedEntries(new Date(System.currentTimeMillis()+(120*1000L)));
        
        for (int i = 1; i <= 7; i++)
        {
            if (siteService.getSite("testSite"+i) != null)
            {
                siteService.deleteSite("testSite"+i);
            }
        }
        
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    public void testSetup() throws Exception
    {
        // NOOP
    }
    
    public void testMaxAge() throws Exception
    {
        cleaner.setMaxFeedSize(0);
        
        // insert site feed entries for "testSite1"
        
        ActivityFeedEntity feedEntry = new ActivityFeedEntity();
        
        feedEntry.setPostDate(new Date(System.currentTimeMillis()-(20*60*1000L))); // 20 mins ago
        feedEntry.setActivitySummaryFormat("json");
        feedEntry.setSiteNetwork("testSite1");
        feedEntry.setActivityType("testActivityType");
        feedEntry.setPostUserId("testUserA");
        feedEntry.setFeedUserId("");
        feedEntry.setFeedDate(new Date());
        
        feedDAO.insertFeedEntry(feedEntry);
        
        feedEntry = new ActivityFeedEntity();
        
        feedEntry.setPostDate(new Date()); // now
        feedEntry.setActivitySummaryFormat("json");
        feedEntry.setSiteNetwork("testSite1");
        feedEntry.setActivityType("testActivityType");
        feedEntry.setPostUserId("testUserA");
        feedEntry.setFeedUserId("");
        feedEntry.setFeedDate(new Date());
        
        // insert user feed entries for "testUserB"
        
        feedDAO.insertFeedEntry(feedEntry);
        
        feedEntry = new ActivityFeedEntity();
        
        feedEntry.setPostDate(new Date(System.currentTimeMillis()-(20*60*1000L))); // 20 mins ago
        feedEntry.setActivitySummaryFormat("json");
        feedEntry.setSiteNetwork("testSite2");
        feedEntry.setActivityType("testActivityType");
        feedEntry.setPostUserId("testUserA");
        feedEntry.setFeedUserId("testUserB");
        feedEntry.setFeedDate(new Date());
        
        feedDAO.insertFeedEntry(feedEntry);
        
        feedEntry = new ActivityFeedEntity();
        
        feedEntry.setPostDate(new Date()); // now
        feedEntry.setActivitySummaryFormat("json");
        feedEntry.setSiteNetwork("testSite3");
        feedEntry.setActivityType("testActivityType");
        feedEntry.setPostUserId("testUserA");
        feedEntry.setFeedUserId("testUserB");
        feedEntry.setFeedDate(new Date());
        
        feedDAO.insertFeedEntry(feedEntry);
        
        assertEquals(2, feedDAO.selectSiteFeedEntries("testSite1", "json").size());
        assertEquals(2, feedDAO.selectUserFeedEntries("testUserB", "json", null, false, false).size());
        
        // fire the cleaner
        cleaner.setMaxAgeMins(10);
        cleaner.execute();
        
        assertEquals(1, feedDAO.selectSiteFeedEntries("testSite1", "json").size());
        assertEquals(1, feedDAO.selectUserFeedEntries("testUserB", "json", null, false, false).size());
    }
    
    public void testMaxSize() throws Exception
    {
        cleaner.setMaxAgeMins(0);
        
        // insert site feed entries for "testSite4"
        
        for (int i = 0; i < 10; i++)
        {
            ActivityFeedEntity feedEntry = new ActivityFeedEntity();
            
            feedEntry.setPostDate(new Date(System.currentTimeMillis()-(i*60*1000L)));
            feedEntry.setActivitySummaryFormat("json");
            feedEntry.setSiteNetwork("testSite4");
            feedEntry.setActivityType("testActivityType");
            feedEntry.setPostUserId("testUserC");
            feedEntry.setFeedUserId("");
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry);
        }
        
        // insert user feed entries for user "testUserD"
        
        for (int i = 0; i < 10; i++)
        {
            ActivityFeedEntity feedEntry = new ActivityFeedEntity();
            
            feedEntry.setPostDate(new Date(System.currentTimeMillis()-(i*60*1000L)));
            feedEntry.setActivitySummaryFormat("json");
            feedEntry.setSiteNetwork("testSite5");
            feedEntry.setActivityType("testActivityType");
            feedEntry.setPostUserId("testUserA");
            feedEntry.setFeedUserId("testUserD");
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry);
        }
        
        assertEquals(10, feedDAO.selectSiteFeedEntries("testSite4", "json").size());
        assertEquals(10, feedDAO.selectUserFeedEntries("testUserD", "json", null, false, false).size());
        
        // fire the cleaner
        cleaner.setMaxFeedSize(2);
        cleaner.execute();
        
        assertEquals(2, feedDAO.selectSiteFeedEntries("testSite4", "json").size());
        assertEquals(2, feedDAO.selectUserFeedEntries("testUserD", "json", null, false, false).size());
        
        Date sameTime = new Date();
        
        // insert site feed entries for "testSite6"
        
        for (int i = 0; i < 10; i++)
        {
            ActivityFeedEntity feedEntry = new ActivityFeedEntity();
            
            feedEntry.setPostDate(sameTime);
            feedEntry.setActivitySummaryFormat("json");
            feedEntry.setSiteNetwork("testSite6");
            feedEntry.setActivityType("testActivityType");
            feedEntry.setPostUserId("testUserE");
            feedEntry.setFeedUserId("");
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry);
        }
        
        // insert user feed entries for user "testUserF"
        
        for (int i = 0; i < 10; i++)
        {
            ActivityFeedEntity feedEntry = new ActivityFeedEntity();
            
            feedEntry.setPostDate(sameTime);
            feedEntry.setActivitySummaryFormat("json");
            feedEntry.setSiteNetwork("testSite7");
            feedEntry.setActivityType("testActivityType");
            feedEntry.setPostUserId("testUserA");
            feedEntry.setFeedUserId("testUserF");
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry);
        }
        
        assertEquals(10, feedDAO.selectSiteFeedEntries("testSite6", "json").size());
        assertEquals(10, feedDAO.selectUserFeedEntries("testUserF", "json", null, false, false).size());
        
        // fire the cleaner
        cleaner.setMaxFeedSize(2);
        cleaner.execute();
        
        // note: no effect, since entries at max feed size have same time (eg. to nearest minute)
        
        assertEquals(10, feedDAO.selectSiteFeedEntries("testSite6", "json").size());
        assertEquals(10, feedDAO.selectUserFeedEntries("testUserF", "json", null, false, false).size());
    }
    
    public void testConcurrentAccessAndRemoval() throws Exception
    {
        cleaner.setMaxAgeMins(1);
        cleaner.setMaxFeedSize(1);
        
        int typeCount = 3;
        int n = typeCount * 10;
        
        Thread[] threads = new Thread[n];
        Tester[] testers = new Tester[n];
        
        for (int i = 0; i < n; i++)
        {
            Tester tester = new Tester(i, typeCount);
            testers[i] = tester;
            
            threads[i] = new Thread(tester);
            threads[i].start();
        }
        for (int i = 0; i < n; i++)
        {
            threads[i].join();
            
            if (testers[i].getErrorStackTrace() != null)
            {
                fail(testers[i].getErrorStackTrace());
            }
        }
    }
    
    private class Tester implements Runnable
    {
        private int i;
        private int typeCount;
        private String errorStackTrace = null;
        
        public Tester(int i, int typeCount)
        {
            this.i = i;
            this.typeCount = typeCount;
        }
        
        public String getErrorStackTrace()
        {
            return errorStackTrace;
        }
        
        public void run()
        {
            try
            {
                int type = i % typeCount;
                
                if (type == 0)
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                    
                    int insertCount = 10;
                    
                    // insert some entries
                    for (int i = 0; i < insertCount; i++)
                    {
                        final ActivityFeedEntity feedEntry = new ActivityFeedEntity();
                        
                        feedEntry.setPostDate(new Date(System.currentTimeMillis()-(i*60*1000L)));
                        feedEntry.setActivitySummaryFormat("json");
                        feedEntry.setSiteNetwork("testSite4");
                        feedEntry.setActivityType("testActivityType");
                        feedEntry.setPostUserId("testUserC");
                        feedEntry.setFeedUserId("");
                        feedEntry.setFeedDate(new Date());
                        
                        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
                        {
                            public Object execute() throws Throwable
                            {
                                feedDAO.insertFeedEntry(feedEntry);
                                return null;
                            }
                        });
                    }
                    
                    System.out.println("["+i+"] Inserted "+insertCount+" entries");
                }
                
                if (type == 1)
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                    
                    int selectCount = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Integer>()
                    {
                        public Integer execute() throws Throwable
                        {
                            // query some entries
                            int selectCount = feedDAO.selectSiteFeedEntries("testSite4", "json").size();
                            return selectCount;
                        }
                    });
                    
                    System.out.println("["+i+"] Selected "+selectCount+" entries");
                }
                
                if (type == 2)
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                    
                    int deleteCount = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Integer>()
                    {
                        public Integer execute() throws Throwable
                        {
                            // clean some entries
                            int deleteCount = cleaner.execute();
                            return deleteCount;
                        }
                    });
                    
                    System.out.println("["+i+"] Deleted "+deleteCount+" entries");
                }
            }
            catch (Throwable t)
            {
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                errorStackTrace = sw.toString();
                
                fail(t.getMessage());
            }
        }
    }
}