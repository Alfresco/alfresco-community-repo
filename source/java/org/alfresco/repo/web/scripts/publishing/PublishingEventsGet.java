/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.web.scripts.publishing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.WebScriptUtil;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class PublishingEventsGet extends PublishingWebScript
{
    /**
    * {@inheritDoc}
    */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        NodeRef node = WebScriptUtil.getNodeRef(params);
        if (node == null)
        {
            String msg = "A valid NodeRef must be specified!";
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
        try
        {
            ArrayList<PublishingEvent> events = getSortedPublishingEvents(node);
            List<Map<String, Object>> model = builder.buildPublishingEventsForNode(events, node, channelService);
            return WebScriptUtil.createBaseModel(model);
        }
        catch (Exception e)
        {
            String msg = "Failed to query for publishing events for node: " + node;
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg, e);
        }
    }

    private ArrayList<PublishingEvent> getSortedPublishingEvents(NodeRef node)
    {
        List<PublishingEvent> publishedEvents = publishingService.getPublishEventsForNode(node);
        List<PublishingEvent> unpublishedEvents = publishingService.getUnpublishEventsForNode(node);
        ArrayList<PublishingEvent> allEvents = new ArrayList<PublishingEvent>(publishedEvents);
        allEvents.addAll(unpublishedEvents);
        Collections.sort(allEvents);
        return allEvents;
    }
}