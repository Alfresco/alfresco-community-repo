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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.activities.ActivityFeedDAO;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
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
    private PersonService personService;
    protected RetryingTransactionHelper transactionHelper;
    
    private static final String TEST_SITE = "testSite";
    
    private static final String TEST_SITE_1 = TEST_SITE+"1";
    private static final String TEST_SITE_2 = TEST_SITE+"2";
    private static final String TEST_SITE_3 = TEST_SITE+"3";
    private static final String TEST_SITE_4 = TEST_SITE+"4";
    private static final String TEST_SITE_5 = TEST_SITE+"5";
    private static final String TEST_SITE_6 = TEST_SITE+"6";
    private static final String TEST_SITE_7 = TEST_SITE+"7";
    
    private static final String TEST_USER_A = "testUserA";
    private static final String TEST_USER_B = "testUserB";
    private static final String TEST_USER_C = "testUserC";
    private static final String TEST_USER_D = "testUserD";
    private static final String TEST_USER_E = "testUserE";
    private static final String TEST_USER_F = "testUserF";
    
    @Override
    public void setUp() throws Exception
    {
        siteService = (SiteService) ctx.getBean("SiteService");
        personService = (PersonService) ctx.getBean("PersonService");
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
        
        // insert site feed entries for TEST_SITE_1
        
        ActivityFeedEntity feedEntry = new ActivityFeedEntity();
        
        feedEntry.setPostDate(new Date(System.currentTimeMillis()-(20*60*1000L))); // 20 mins ago
        feedEntry.setActivitySummaryFormat("json");
        feedEntry.setSiteNetwork(TEST_SITE_1);
        feedEntry.setActivityType("testActivityType");
        feedEntry.setPostUserId(TEST_USER_A);
        feedEntry.setFeedUserId("");
        feedEntry.setFeedDate(new Date());
        
        feedDAO.insertFeedEntry(feedEntry);
        
        feedEntry = new ActivityFeedEntity();
        
        feedEntry.setPostDate(new Date()); // now
        feedEntry.setActivitySummaryFormat("json");
        feedEntry.setSiteNetwork(TEST_SITE_1);
        feedEntry.setActivityType("testActivityType");
        feedEntry.setPostUserId(TEST_USER_A);
        feedEntry.setFeedUserId("");
        feedEntry.setFeedDate(new Date());
        
        // insert user feed entries for TEST_USER_B
        
        feedDAO.insertFeedEntry(feedEntry);
        
        feedEntry = new ActivityFeedEntity();
        
        feedEntry.setPostDate(new Date(System.currentTimeMillis()-(20*60*1000L))); // 20 mins ago
        feedEntry.setActivitySummaryFormat("json");
        feedEntry.setSiteNetwork(TEST_SITE_2);
        feedEntry.setActivityType("testActivityType");
        feedEntry.setPostUserId(TEST_USER_A);
        feedEntry.setFeedUserId(TEST_USER_B);
        feedEntry.setFeedDate(new Date());
        
        feedDAO.insertFeedEntry(feedEntry);
        
        feedEntry = new ActivityFeedEntity();
        
        feedEntry.setPostDate(new Date()); // now
        feedEntry.setActivitySummaryFormat("json");
        feedEntry.setSiteNetwork(TEST_SITE_3);
        feedEntry.setActivityType("testActivityType");
        feedEntry.setPostUserId(TEST_USER_A);
        feedEntry.setFeedUserId(TEST_USER_B);
        feedEntry.setFeedDate(new Date());
        
        feedDAO.insertFeedEntry(feedEntry);
        
        assertEquals(2, feedDAO.selectSiteFeedEntries(TEST_SITE_1, "json", -1).size());
        assertEquals(2, feedDAO.selectUserFeedEntries(TEST_USER_B, "json", null, false, false, -1L, -1).size());
        
        // fire the cleaner
        cleaner.setMaxAgeMins(10);
        cleaner.execute();
        
        assertEquals(1, feedDAO.selectSiteFeedEntries(TEST_SITE_1, "json", -1).size());
        assertEquals(1, feedDAO.selectUserFeedEntries(TEST_USER_B, "json", null, false, false, -1L, -1).size());
    }
    
    public void testMaxSize() throws Exception
    {
        cleaner.setMaxAgeMins(0);
        
        // insert site feed entries for TEST_SITE_4
        
        for (int i = 0; i < 10; i++)
        {
            ActivityFeedEntity feedEntry = new ActivityFeedEntity();
            
            feedEntry.setPostDate(new Date(System.currentTimeMillis()-(i*60*1000L)));
            feedEntry.setActivitySummaryFormat("json");
            feedEntry.setSiteNetwork(TEST_SITE_4);
            feedEntry.setActivityType("testActivityType");
            feedEntry.setPostUserId(TEST_USER_C);
            feedEntry.setFeedUserId("");
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry);
        }
        
        // insert user feed entries for user TEST_USER_D
        
        for (int i = 0; i < 10; i++)
        {
            ActivityFeedEntity feedEntry = new ActivityFeedEntity();
            
            feedEntry.setPostDate(new Date(System.currentTimeMillis()-(i*60*1000L)));
            feedEntry.setActivitySummaryFormat("json");
            feedEntry.setSiteNetwork(TEST_SITE_5);
            feedEntry.setActivityType("testActivityType");
            feedEntry.setPostUserId(TEST_USER_A);
            feedEntry.setFeedUserId(TEST_USER_D);
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry);
        }
        
        assertEquals(10, feedDAO.selectSiteFeedEntries(TEST_SITE_4, "json", -1).size());
        assertEquals(10, feedDAO.selectUserFeedEntries(TEST_USER_D, "json", null, false, false, -1L, -1).size());
        
        // fire the cleaner
        cleaner.setMaxFeedSize(2);
        cleaner.execute();
        
        assertEquals(2, feedDAO.selectSiteFeedEntries(TEST_SITE_4, "json", -1).size());
        assertEquals(2, feedDAO.selectUserFeedEntries(TEST_USER_D, "json", null, false, false, -1L, -1).size());
        
        Date sameTime = new Date();
        
        // insert site feed entries for TEST_SITE_6
        
        for (int i = 0; i < 10; i++)
        {
            ActivityFeedEntity feedEntry = new ActivityFeedEntity();
            
            feedEntry.setPostDate(sameTime);
            feedEntry.setActivitySummaryFormat("json");
            feedEntry.setSiteNetwork(TEST_SITE_6);
            feedEntry.setActivityType("testActivityType");
            feedEntry.setPostUserId(TEST_USER_E);
            feedEntry.setFeedUserId("");
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry);
        }
        
        // insert user feed entries for user TEST_USER_F
        
        for (int i = 0; i < 10; i++)
        {
            ActivityFeedEntity feedEntry = new ActivityFeedEntity();
            
            feedEntry.setPostDate(sameTime);
            feedEntry.setActivitySummaryFormat("json");
            feedEntry.setSiteNetwork(TEST_SITE_7);
            feedEntry.setActivityType("testActivityType");
            feedEntry.setPostUserId(TEST_USER_A);
            feedEntry.setFeedUserId(TEST_USER_F);
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry);
        }
        
        assertEquals(10, feedDAO.selectSiteFeedEntries(TEST_SITE_6, "json", -1).size());
        assertEquals(10, feedDAO.selectUserFeedEntries(TEST_USER_F, "json", null, false, false, -1L, -1).size());
        
        // fire the cleaner
        cleaner.setMaxFeedSize(2);
        cleaner.execute();
        
        // note: no effect, since entries at max feed size have same time (eg. to nearest minute)
        
        assertEquals(10, feedDAO.selectSiteFeedEntries(TEST_SITE_6, "json", -1).size());
        assertEquals(10, feedDAO.selectUserFeedEntries(TEST_USER_F, "json", null, false, false, -1L, -1).size());
    }
    
    public void testSiteDelete() throws Exception
    {
        cleaner.setMaxAgeMins(100);
        
        assertEquals(0, feedDAO.selectSiteFeedEntries(TEST_SITE_4, "json", -1).size());
        assertEquals(0, feedDAO.selectUserFeedEntries(TEST_USER_D, "json", null, false, false, -1L, -1).size());
        
        int site4FeedCnt = 10;
        
        // insert site / user feed entries (for TEST_SITE_4 and TEST_USER_D)
        for (int i = 0; i < site4FeedCnt; i++)
        {
            ActivityFeedEntity feedEntry = new ActivityFeedEntity();
            
            feedEntry.setPostDate(new Date(System.currentTimeMillis()-(i*60*1000L)));
            feedEntry.setActivitySummaryFormat("json");
            feedEntry.setSiteNetwork(TEST_SITE_4);
            feedEntry.setActivityType("testActivityType");
            feedEntry.setPostUserId(TEST_USER_C);
            feedEntry.setFeedUserId("");
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry); // for TEST_SITE_4 site feed
            
            feedEntry.setFeedUserId(TEST_USER_D); // for TEST_USER_D user feed
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry);
        }
        
        int site5FeedCnt = 5;
        
        // add some additional user feed entries (for TEST_SITE_5 and TEST_USER_D)
        for (int i = 0; i < site5FeedCnt; i++)
        {
            ActivityFeedEntity feedEntry = new ActivityFeedEntity();
            
            feedEntry.setPostDate(new Date(System.currentTimeMillis()-(i*60*1000L)));
            feedEntry.setActivitySummaryFormat("json");
            feedEntry.setSiteNetwork(TEST_SITE_5);
            feedEntry.setActivityType("testActivityType");
            feedEntry.setPostUserId(TEST_USER_C);
            feedEntry.setFeedUserId(TEST_USER_D);
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry);
        }
        
        assertEquals(site4FeedCnt, feedDAO.selectSiteFeedEntries(TEST_SITE_4, "json", -1).size());
        assertEquals(site4FeedCnt+site5FeedCnt, feedDAO.selectUserFeedEntries(TEST_USER_D, "json", null, false, false,-1L, -1).size());
        
        // delete the site
        siteService.deleteSite(TEST_SITE_4);
        
        assertEquals(0, feedDAO.selectSiteFeedEntries(TEST_SITE_4, "json", -1).size());
        assertEquals(site5FeedCnt, feedDAO.selectUserFeedEntries(TEST_USER_D, "json", null, false, false, -1L, -1).size());
        
        siteService.createSite("mypreset", TEST_SITE_4, TEST_SITE_4, TEST_SITE_4, SiteVisibility.PUBLIC);
        
        assertEquals(0, feedDAO.selectSiteFeedEntries(TEST_SITE_4, "json", -1).size());
    }
    
    public void testPersonDelete() throws Exception
    {
        cleaner.setMaxAgeMins(100);
        
        createPerson(TEST_USER_E); // ignore result
        
        assertEquals(0, feedDAO.selectSiteFeedEntries(TEST_SITE_6, "json", -1).size());
        assertEquals(0, feedDAO.selectUserFeedEntries(TEST_USER_E, "json", null, false, false, -1L, -1).size());
        
        int site6FeedCnt = 10;
        
        // insert site / user feed entries (for TEST_SITE_6 and TEST_USER_E)
        for (int i = 0; i < site6FeedCnt; i++)
        {
            ActivityFeedEntity feedEntry = new ActivityFeedEntity();
            
            feedEntry.setPostDate(new Date(System.currentTimeMillis()-(i*60*1000L)));
            feedEntry.setActivitySummaryFormat("json");
            feedEntry.setSiteNetwork(TEST_SITE_6);
            feedEntry.setActivityType("testActivityType");
            feedEntry.setPostUserId(TEST_USER_E);
            feedEntry.setFeedUserId("");
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry); // for TEST_SITE_6 site feed
            
            feedEntry.setFeedUserId(TEST_USER_E); // for TEST_USER_E user feed
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry);
        }
        
        int site7FeedCnt = 5;
        
        // insert site / user feed entries (for TEST_SITE_7 and TEST_USER_E)
        for (int i = 0; i < site7FeedCnt; i++)
        {
            ActivityFeedEntity feedEntry = new ActivityFeedEntity();
            
            feedEntry.setPostDate(new Date(System.currentTimeMillis()-(i*60*1000L)));
            feedEntry.setActivitySummaryFormat("json");
            feedEntry.setSiteNetwork(TEST_SITE_7);
            feedEntry.setActivityType("testActivityType");
            feedEntry.setPostUserId(TEST_USER_E);
            feedEntry.setFeedUserId("");
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry); // for TEST_SITE_7 site feed
            
            feedEntry.setFeedUserId(TEST_USER_E); // for TEST_USER_E user feed
            feedEntry.setFeedDate(new Date());
            
            feedDAO.insertFeedEntry(feedEntry);
        }
        
        assertEquals(site6FeedCnt, feedDAO.selectSiteFeedEntries(TEST_SITE_6, "json", -1).size());
        assertEquals(site7FeedCnt, feedDAO.selectSiteFeedEntries(TEST_SITE_7, "json", -1).size());
        assertEquals(site6FeedCnt+site7FeedCnt, feedDAO.selectUserFeedEntries(TEST_USER_E, "json", null, false, false, -1L, -1).size());
        
        // delete the person
        personService.deletePerson(TEST_USER_E);
        
        assertEquals(site6FeedCnt, feedDAO.selectSiteFeedEntries(TEST_SITE_6, "json", -1).size());
        assertEquals(site7FeedCnt, feedDAO.selectSiteFeedEntries(TEST_SITE_7, "json", -1).size());
        
        assertEquals(0, feedDAO.selectUserFeedEntries(TEST_USER_E, "json", null, false, false, -1L, -1).size());
        
        assertTrue(createPerson(TEST_USER_E));
        
        assertEquals(0, feedDAO.selectUserFeedEntries(TEST_USER_E, "json", null, false, false, -1L, -1).size());
    }
    
    private boolean createPerson(String userName)
    {
        if (this.personService.personExists(userName) == false)
        {
            // create person properties
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, userName);
            personProps.put(ContentModel.PROP_LASTNAME, userName);
            personProps.put(ContentModel.PROP_EMAIL, userName+"@email.com");
            
            // create person node for user
            this.personService.createPerson(personProps);
            
            return true;
        }
        
        return false; // already exists
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
                        feedEntry.setSiteNetwork(TEST_SITE_4);
                        feedEntry.setActivityType("testActivityType");
                        feedEntry.setPostUserId(TEST_USER_C);
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
                            int selectCount = feedDAO.selectSiteFeedEntries(TEST_SITE_4, "json", -1).size();
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
