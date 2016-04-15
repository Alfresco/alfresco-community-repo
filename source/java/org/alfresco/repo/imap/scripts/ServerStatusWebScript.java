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
