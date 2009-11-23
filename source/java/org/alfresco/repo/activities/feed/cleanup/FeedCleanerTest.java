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
package org.alfresco.repo.activities.feed.cleanup;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import junit.framework.TestCase;

import org.alfresco.repo.domain.activities.ActivityFeedDAO;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.activities.ActivityService;
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
    private ActivityService activityService;
    private SiteService siteService;
    
    @Override
    public void setUp() throws Exception
    {
        activityService = (ActivityService) ctx.getBean("activityService");
        siteService = (SiteService) ctx.getBean("SiteService");
        feedDAO = (ActivityFeedDAO) ctx.getBean("feedDAO");
        
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
        // clean out any remaining feed entries (allows test to be re-runnable)
        feedDAO.deleteFeedEntries(new Date(System.currentTimeMillis()+(120*1000L)));
        
        for (int i = 1; i <= 7; i++)
        {
            siteService.deleteSite("testSite"+i);
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
        
        assertEquals(2, activityService.getSiteFeedEntries("testSite1", "json").size());
        assertEquals(2, activityService.getUserFeedEntries("testUserB", "json", null).size());
        
        // fire the cleaner
        cleaner.setMaxAgeMins(10);
        cleaner.execute();
        
        assertEquals(1, activityService.getSiteFeedEntries("testSite1", "json").size());
        assertEquals(1, activityService.getUserFeedEntries("testUserB", "json", null).size());
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
        
        assertEquals(10, activityService.getSiteFeedEntries("testSite4", "json").size());
        assertEquals(10, activityService.getUserFeedEntries("testUserD", "json", null).size());
        
        // fire the cleaner
        cleaner.setMaxFeedSize(2);
        cleaner.execute();
        
        assertEquals(2, activityService.getSiteFeedEntries("testSite4", "json").size());
        assertEquals(2, activityService.getUserFeedEntries("testUserD", "json", null).size());
        
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
        
        assertEquals(10, activityService.getSiteFeedEntries("testSite6", "json").size());
        assertEquals(10, activityService.getUserFeedEntries("testUserF", "json", null).size());
        
        // fire the cleaner
        cleaner.setMaxFeedSize(2);
        cleaner.execute();
        
        // note: no effect, since entries at max feed size have same time (eg. to nearest minute)
        assertEquals(10, activityService.getSiteFeedEntries("testSite6", "json").size());
        assertEquals(10, activityService.getUserFeedEntries("testUserF", "json", null).size());
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
                    
                    System.out.println("["+i+"] Inserted "+insertCount+" entries");
                }
                
                if (type == 1)
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                    
                    // query some entries
                    int selectCount = activityService.getSiteFeedEntries("testSite4", "json").size();
                    
                    System.out.println("["+i+"] Selected "+selectCount+" entries");
                }
                
                if (type == 2)
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                    
                    // clean some entries
                    int deleteCount = cleaner.execute();
                    
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
