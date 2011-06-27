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

package org.alfresco.repo.web.scripts.publishing;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.publishing.Environment;
import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class PublishingQueuePost extends DeclarativeWebScript
{
    private static final String ENVIRONMENT_ID = "environment_id";
    private static final String SITE_ID = "site_id";

    private final PublishingJsonParser jsonParser = new PublishingJsonParser();
    private PublishingService publishingService;
    private String defaultEnvironmentId;
    
    /**
    * {@inheritDoc}
    */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        String siteId = params.get(SITE_ID);
        String environmentId = params.get(ENVIRONMENT_ID);
        
        if(siteId == null)
        {
            String msg = "A Site ID must be specified!";
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
        if(environmentId == null)
        {
            if(defaultEnvironmentId == null)
            {
                String msg = "An Environment ID must be specified!";
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
            }
            environmentId = defaultEnvironmentId;
        }
        
        Environment environment = publishingService.getEnvironment(siteId, environmentId);
        if(environment == null)
        {
            String msg = "Environment " +environmentId + " does not exist in site " +siteId;
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
        PublishingQueue queue = environment.getPublishingQueue();
        
        String content = null;
        try
        {
            content = getContent(req);
            jsonParser.schedulePublishingEvent(queue, content);
        }
        catch(Exception e)
        {
            String msg = "Failed to schedule publishing event. POST body: " + content;
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg, e);
        }
        return null;
    }
    
    
    protected String getContent(WebScriptRequest request) throws IOException
    {
        Content content = request.getContent();
        return content.getContent();
    }

    /**
     * @param publishingService the publishingService to set
     */
    public void setPublishingService(PublishingService publishingService)
    {
        this.publishingService = publishingService;
    }
    
    /**
     * @param defaultEnvironmentId the defaultEnvironmentId to set
     */
    public void setDefaultEnvironmentId(String defaultEnvironmentId)
    {
        this.defaultEnvironmentId = defaultEnvironmentId;
    }
}
