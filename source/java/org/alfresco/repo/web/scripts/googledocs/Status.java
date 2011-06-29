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
package org.alfresco.repo.web.scripts.googledocs;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.googledocs.GoogleDocsService;
import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Google Doc service status web script implementation
 */
public class Status extends DeclarativeWebScript implements ApplicationContextAware
{
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, org.springframework.extensions.webscripts.Status status, Cache cache)
    {
        ApplicationContextFactory subsystem = (ApplicationContextFactory)applicationContext.getBean("googledocs");
        ConfigurableApplicationContext childContext = (ConfigurableApplicationContext)subsystem.getApplicationContext();
        GoogleDocsService googleDocsService = (GoogleDocsService)childContext.getBean("googleDocsService");
        
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("enabled", googleDocsService.isEnabled());
        return model;
    }
}
