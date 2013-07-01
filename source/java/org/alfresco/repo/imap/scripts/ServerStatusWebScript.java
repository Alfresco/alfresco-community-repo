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
package org.alfresco.repo.imap.scripts;

import java.io.IOException;

import org.alfresco.repo.imap.ImapService;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Shows the availability of the IMAP server via web script request.
 */
public class ServerStatusWebScript extends AbstractWebScript implements ApplicationContextAware
{
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        ChildApplicationContextFactory subsystem = (ChildApplicationContextFactory)applicationContext.getBean("imap");
        
        // note: getting property (rather than getting imapService bean to check isEnabled) does not cause subsystem startup (if stopped)
        // hence providing ability for subsystem to be disabled (whilst still supporting ability to check status and/or dynamically start via JMX)
        String isEnabled = (String)subsystem.getProperty("imap.server.enabled");
        
        if (new Boolean(isEnabled).booleanValue())
        {
            res.getWriter().write("enabled");
        }
        else
        {
            res.getWriter().write("disabled");
        }
        res.getWriter().flush();
        res.getWriter().close();
    }
}
