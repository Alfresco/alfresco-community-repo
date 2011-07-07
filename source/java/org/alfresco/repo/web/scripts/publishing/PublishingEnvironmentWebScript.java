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

import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.ENVIRONMENT_ID;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.SITE_ID;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.publishing.Environment;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.util.Pair;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public abstract class PublishingEnvironmentWebScript extends DeclarativeWebScript
{
    protected final PublishingJsonParser jsonParser = new PublishingJsonParser();
    protected final PublishingModelBuilder builder= new PublishingModelBuilder();

    protected PublishingService publishingService;
    protected ChannelService channelService;
    private String defaultEnvironmentId;
    
    protected Pair<String, Environment> getSiteAndEnvironment(WebScriptRequest req)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        String siteId = params.get(SITE_ID);
        
        if(siteId == null)
        {
            String msg = "A Site ID must be specified!";
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
        String environmentId = params.get(ENVIRONMENT_ID);
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
        return new Pair<String, Environment>(siteId, environment);
    }
    
    /**
     * @param defaultEnvironmentId the defaultEnvironmentId to set
     */
    public void setDefaultEnvironmentId(String defaultEnvironmentId)
    {
        this.defaultEnvironmentId = defaultEnvironmentId;
    }
    
    /**
     * @param publishingService the publishingService to set
     */
    public void setPublishingService(PublishingService publishingService)
    {
        this.publishingService = publishingService;
    }
    
    /**
     * @param channelService the channelService to set
     */
    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }
}