/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.subscriptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.feed.FeedGenerator;
import org.alfresco.repo.activities.feed.local.LocalFeedTaskProcessor;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.subscriptions.SubscriptionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;

public class SubscriptionServiceActivitiesTest extends TestCase
{
    // Location of activity type templates (for site activities)
    // assumes test-resources is on classpath
    protected static final String TEST_TEMPLATES_LOCATION = "activities";
    
    protected ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    protected SubscriptionService subscriptionService;
    protected PersonService personService;
    protected SiteService siteService;
    protected ActivityService activityService;
    protected NodeService nodeService;
    protected ContentService contentService;
    protected PostLookup postLookup;
    protected FeedGenerator feedGenerator;
    
    @Override
    public void setUp() throws Exception
    {
        // Let's shut down the scheduler so that we aren't competing with the
        // scheduled versions of the post lookup and
        // feed generator jobs
        Scheduler scheduler = (Scheduler) ctx.getBean("schedulerFactory");
        scheduler.shutdown();
        
        // Get the required services
        subscriptionService = (SubscriptionService) ctx.getBean("SubscriptionService");
        personService = (PersonService) ctx.getBean("PersonService");
        siteService = (SiteService) ctx.getBean("SiteService");
        activityService = (ActivityService) ctx.getBean("activityService");
        nodeService = (NodeService) ctx.getBean("NodeService");
        contentService = (ContentService) ctx.getBean("ContentService");
        
        ChildApplicationContextFactory activitiesFeed = (ChildApplicationContextFactory) ctx.getBean("ActivitiesFeed");
        ApplicationContext activitiesFeedCtx = activitiesFeed.getApplicationContext();
        postLookup = (PostLookup) activitiesFeedCtx.getBean("postLookup");
        feedGenerator = (FeedGenerator) activitiesFeedCtx.getBean("feedGenerator");
        
        LocalFeedTaskProcessor feedProcessor = (LocalFeedTaskProcessor) activitiesFeedCtx.getBean("feedTaskProcessor");
        
        List<String> templateSearchPaths = new ArrayList<String>(1);
        templateSearchPaths.add(TEST_TEMPLATES_LOCATION);
        feedProcessor.setTemplateSearchPaths(templateSearchPaths);
        feedProcessor.setUseRemoteCallbacks(false);
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }
    
    protected void deletePerson(String userId)
    {
        personService.deletePerson(userId);
    }
    
    protected NodeRef createPerson(String userId)
    {
        deletePerson(userId);
        
        PropertyMap properties = new PropertyMap(5);
        properties.put(ContentModel.PROP_USERNAME, userId);
        properties.put(ContentModel.PROP_FIRSTNAME, userId);
        properties.put(ContentModel.PROP_LASTNAME, "Test");
        properties.put(ContentModel.PROP_EMAIL, userId + "@email.com");
        
        return personService.createPerson(properties);
    }
    
    protected void deleteSite(String siteId)
    {
        if (siteService.getSite(siteId) != null)
        {
            siteService.deleteSite(siteId);
        }
    }
    
    protected SiteInfo createSite(String siteId, SiteVisibility visibility)
    {
        deleteSite(siteId);
        return siteService.createSite("sitePreset", siteId, null, null, visibility);
    }
    
    protected NodeRef addTextContent(String siteId, String name)
    {
        String textData = name;
        String mimeType = MimetypeMap.MIMETYPE_TEXT_PLAIN;
                
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, name);
        
        // ensure that the Document Library folder is pre-created so that test code can start creating content straight away.
        // At the time of writing V4.1 does not create this folder automatically, but Thor does
        NodeRef parentRef = siteService.getContainer(siteId, SiteService.DOCUMENT_LIBRARY);
        if (parentRef == null)
        {
            parentRef = siteService.createContainer(siteId, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
        }
        
        ChildAssociationRef association = nodeService.createNode(parentRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                ContentModel.TYPE_CONTENT,
                contentProps);
        
        NodeRef content = association.getChildRef();
        
        // add titled aspect (for Web Client display)
        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
        titledProps.put(ContentModel.PROP_TITLE, name);
        titledProps.put(ContentModel.PROP_DESCRIPTION, name);
        this.nodeService.addAspect(content, ContentModel.ASPECT_TITLED, titledProps);
        
        ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);
        
        writer.setMimetype(mimeType);
        writer.setEncoding("UTF-8");
        
        writer.putContent(textData);
        
        activityService.postActivity("org.alfresco.documentlibrary.file-added", siteId, "documentlibrary", content, name, ContentModel.PROP_CONTENT, parentRef);
        
        return content;
    }
    
    protected void generateFeed() throws Exception
    {
        postLookup.execute();
        feedGenerator.execute();
    }
    
    public void testFollowingActivity() throws Exception
    {
        final String userId1 = "bob" + GUID.generate();
        final String userId2 = "tom" + GUID.generate();
        
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                createPerson(userId1);
                createPerson(userId2);
                
                createSite(userId1+"pub", SiteVisibility.PUBLIC);
                siteService.setMembership(userId1+"pub", userId1, SiteModel.SITE_MANAGER);
                
                createSite(userId1+"priv1", SiteVisibility.PRIVATE);
                siteService.setMembership(userId1+"priv1", userId1, SiteModel.SITE_MANAGER);
                
                createSite(userId1+"priv2", SiteVisibility.PRIVATE);
                siteService.setMembership(userId1+"priv2", userId1, SiteModel.SITE_MANAGER);
                
                createSite(userId1+"mod1", SiteVisibility.MODERATED);
                siteService.setMembership(userId1+"mod1", userId1, SiteModel.SITE_MANAGER);
                
                createSite(userId1+"mod2", SiteVisibility.MODERATED);
                siteService.setMembership(userId1+"mod2", userId1, SiteModel.SITE_MANAGER);
                
                List<String> feed = activityService.getUserFeedEntries(userId1, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 0, feed.size());
                
                feed = activityService.getUserFeedEntries(userId2, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 0, feed.size());
                
                // userId1 + 5, userId2 + 0
                generateFeed();
                
                feed = activityService.getUserFeedEntries(userId1, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 5, feed.size());
                
                feed = activityService.getUserFeedEntries(userId2, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 0, feed.size());
                
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
        
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                subscriptionService.follow(userId1, userId2);
                return null;
            }
        }, userId1);
        
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                subscriptionService.follow(userId2, userId1);
                return null;
            }
        }, userId2);
        
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                // userId1 + 5, userId2 + 2
                generateFeed();
                
                List<String> feed = activityService.getUserFeedEntries(userId1, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 7, feed.size());
                
                feed = activityService.getUserFeedEntries(userId2, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 2, feed.size());
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
        
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                addTextContent(userId1+"pub", userId1+"pub-a");
                addTextContent(userId1+"priv1", userId1+"priv1-a");
                addTextContent(userId1+"priv2", userId1+"priv2-a");
                addTextContent(userId1+"mod1", userId1+"mod1-a");
                addTextContent(userId1+"mod2", userId1+"mod2-a");
                return null;
            }
        }, userId1);
        
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                // userId1 + 5, userId2 + 1
                generateFeed();
                
                List <String> feed = activityService.getUserFeedEntries(userId1, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 12, feed.size());
                
                // note: userId2 should not see activities from followers in moderated sites that they do not belong do (ALF-16460)
                feed = activityService.getUserFeedEntries(userId2, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 3, feed.size());

                siteService.setMembership(userId1+"priv2", userId2, SiteModel.SITE_CONSUMER);
                siteService.setMembership(userId1+"mod2", userId2, SiteModel.SITE_MANAGER);
                
                // userId1 + 2, userId2 + 2
                generateFeed();
                
                feed = activityService.getUserFeedEntries(userId1, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 14, feed.size());
                
                // note: userId2 should not see activities from followers in moderated sites that they do not belong do (ALF-16460)
                feed = activityService.getUserFeedEntries(userId2, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 5, feed.size());

                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
                
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                addTextContent(userId1+"pub", userId1+"pub-b");
                addTextContent(userId1+"priv1", userId1+"priv1-b");
                addTextContent(userId1+"priv2", userId1+"priv2-b");
                addTextContent(userId1+"mod1", userId1+"mod1-b");
                addTextContent(userId1+"mod2", userId1+"mod2-b");
                
                return null;
            }
        }, userId1);
        
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                // userId1 + 5, userId2 + 3
                generateFeed();
                
                List<String> feed = activityService.getUserFeedEntries(userId1, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 19, feed.size());
                
                // note: userId2 should not see activities from followers in moderated sites that they do not belong do (ALF-16460)
                feed = activityService.getUserFeedEntries(userId2, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 8, feed.size());
                
                deleteSite(userId1+"pub");
                deleteSite(userId1+"priv1");
                deleteSite(userId1+"priv2");
                deleteSite(userId1+"mod1");
                deleteSite(userId1+"mod2");
                
                feed = activityService.getUserFeedEntries(userId1, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 2, feed.size());
                
                feed = activityService.getUserFeedEntries(userId2, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 2, feed.size());
                
                deletePerson(userId1);
                deletePerson(userId2);
                
                feed = activityService.getUserFeedEntries(userId1, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 0, feed.size());
                
                feed = activityService.getUserFeedEntries(userId2, "json", null, false, false, null, null);
                assertEquals(feed.toString(), 0, feed.size());

                return null;
            }
        }, AuthenticationUtil.getAdminUserName());       
    }
}