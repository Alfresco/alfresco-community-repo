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
package org.alfresco.repo.activities.feed;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.ObjectFactory;

/**
 * @since 4.0
 * 
 * @author Alex Miller
 */
public abstract class AbstractUserNotifier implements UserNotifier
{
    protected static Log logger = LogFactory.getLog(FeedNotifier.class);

    protected ActivityService activityService;
    protected NamespaceService namespaceService;
    protected RepoAdminService repoAdminService;
    protected NodeService nodeService;
    protected SiteService siteService;
    protected ObjectFactory<ActivitiesFeedModelBuilder> activitiesFeedModelBuilderFactory;

    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setRepoAdminService(RepoAdminService repoAdminService)
    {
        this.repoAdminService = repoAdminService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setActivitiesFeedModelBuilderFactory(ObjectFactory<ActivitiesFeedModelBuilder> activitiesFeedModelBuilder) 
    {
        this.activitiesFeedModelBuilderFactory = activitiesFeedModelBuilder;    
    }
    
    /**
     * Perform basic checks to ensure that the necessary dependencies were injected.
     */
    protected void checkProperties()
    {
        PropertyCheck.mandatory(this, "activitiesFeedModelBuilderFactory", activitiesFeedModelBuilderFactory);
        PropertyCheck.mandatory(this, "activityService", activityService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "siteService", siteService);
    }

    protected abstract boolean skipUser(NodeRef personNodeRef);
    protected abstract Long getFeedId(NodeRef personNodeRef);
    protected abstract void notifyUser(NodeRef personNodeRef, String subjectLine, Object[] subjectParams, Map<String, Object> model, String templateNodeRef);
    
    private void addSiteName(String siteId, Map<String, String> siteNames)
    {
        if (siteId == null)
        {
            return;
        }
        
        String siteName = siteNames.get(siteId);
        if (siteName == null)
        {
            SiteInfo site = siteService.getSite(siteId);
            if (site == null)
            {
                return;
            }
            
            String siteTitle = site.getTitle();
            if (siteTitle != null && siteTitle.length() > 0)
            {
                siteName = siteTitle;
            }
            else
            {
                siteName = siteId;
            }
            
            siteNames.put(siteId, siteName);
        }
    }
    
    public Pair<Integer, Long> notifyUser(final NodeRef personNodeRef, String subject, Object[] subjectParams, Map<String, String> siteNames,
            String shareUrl, int repeatIntervalMins, String templateNodeRef)
    {
        Map<QName, Serializable> personProps = nodeService.getProperties(personNodeRef);

        String feedUserId = (String)personProps.get(ContentModel.PROP_USERNAME);

        if (skipUser(personNodeRef))
        {
            // skip
            return null;
        }

        // where did we get up to ?
        Long feedDBID = getFeedId(personNodeRef);

        // own + others (note: template can be changed to filter out user's own activities if needed)
        if (logger.isDebugEnabled())
        {
            logger.debug("Get user feed entries: " + feedUserId + ", " + feedDBID);
        }
        List<ActivityFeedEntity> feedEntries = activityService.getUserFeedEntries(feedUserId, null, false, false, null, null, feedDBID);
        
        if (feedEntries.size() > 0)
        {
            ActivitiesFeedModelBuilder modelBuilder;
            try
            {
                modelBuilder = activitiesFeedModelBuilderFactory.getObject();
            }
            catch (Exception error)
            {
                logger.warn("Unable to create model builder: " + error.getMessage());
                return null;
            }
            
            for (ActivityFeedEntity feedEntry : feedEntries)
            {
                try
                {
                    modelBuilder.addActivityFeedEntry(feedEntry);

                    String siteId = feedEntry.getSiteNetwork();
                    addSiteName(siteId, siteNames);
                }
                catch (JSONException je)
                {
                    // skip this feed entry
                    logger.warn("Skip feed entry for user ("+feedUserId+"): " + je.getMessage());
                    continue;
                }
            }

            final int activityCount = modelBuilder.activityCount();
            if (activityCount > 0)
            {
                Map<String, Object> model = modelBuilder.buildModel();
                
                model.put("siteTitles", siteNames);
                model.put("repeatIntervalMins", repeatIntervalMins);
                model.put("feedItemsMax", activityService.getMaxFeedItems());

                // add Share info to model
                model.put(TemplateService.KEY_PRODUCT_NAME, ModelUtil.getProductName(repoAdminService));

                Map<String, Serializable> personPrefixProps = new HashMap<String, Serializable>(personProps.size());
                for (QName propQName : personProps.keySet())
                {
                    try
                    {
                        String propPrefix = propQName.toPrefixString(namespaceService);
                        personPrefixProps.put(propPrefix, personProps.get(propQName));
                    }
                    catch (NamespaceException ne)
                    {
                        // ignore properties that do not have a registered namespace
                        logger.warn("Ignoring property '" + propQName + "' as it's namespace is not registered");
                    }
                }

                model.put("personProps", personPrefixProps);

                // send
                notifyUser(personNodeRef, subject, subjectParams, model, templateNodeRef);

                return new Pair<Integer, Long>(activityCount, modelBuilder.getMaxFeedId());
            }
        }

        return null;
    }
}
