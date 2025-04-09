/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.discussion;

import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;

/**
 * This class is the controller for the discussions topics fetching forum-posts.get webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class ForumTopicsGet extends AbstractDiscussionWebScript
{
    @Override
    protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef,
            TopicInfo topic, PostInfo post, WebScriptRequest req, JSONObject json,
            Status status, Cache cache)
    {
        // They shouldn't be trying to list of an existing Post or Topic
        if (topic != null || post != null)
        {
            String error = "Can't list Topics inside an existing Topic or Post";
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
        }

        // Do we need to list or search?
        boolean tagSearch = false;
        String tag = req.getParameter("tag");
        if (tag != null && tag.length() > 0)
        {
            tagSearch = true;

            // Tags can be full unicode strings, so decode
            tag = URLDecoder.decode(tag);
        }

        // Get the topics
        boolean oldestTopicsFirst = false;
        PagingResults<TopicInfo> topics = null;
        PagingRequest paging = buildPagingRequest(req);
        if (tagSearch)
        {
            // Tag based is a search rather than a listing
            if (site != null)
            {
                topics = discussionService.findTopics(site.getShortName(), null, tag, oldestTopicsFirst, paging);
            }
            else
            {
                topics = discussionService.findTopics(nodeRef, null, tag, oldestTopicsFirst, paging);
            }
        }
        else
        {
            if (site != null)
            {
                topics = discussionService.listTopics(site.getShortName(), oldestTopicsFirst, paging);
            }
            else
            {
                topics = discussionService.listTopics(nodeRef, oldestTopicsFirst, buildPagingRequest(req));
            }
        }

        // If they did a site based search, and the component hasn't
        // been created yet, use the site for the permissions checking
        if (site != null && nodeRef == null)
        {
            nodeRef = site.getNodeRef();
        }

        // Build the common model parts
        Map<String, Object> model = buildCommonModel(site, topic, post, req);
        model.put("forum", nodeRef);

        // Have the topics rendered
        model.put("data", renderTopics(topics, paging, site));

        // All done
        return model;
    }
}
