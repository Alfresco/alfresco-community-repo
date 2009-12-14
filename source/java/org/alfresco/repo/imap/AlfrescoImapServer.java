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
package org.alfresco.repo.imap;

import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

import com.icegreen.greenmail.Managers;
import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.imap.ImapServer;
import com.icegreen.greenmail.user.UserManager;
import com.icegreen.greenmail.util.ServerSetup;

/**
 * @author Mike Shavnev
 */
public class AlfrescoImapServer extends AbstractLifecycleBean
{

    private static Log logger = LogFactory.getLog(AlfrescoImapServer.class);

    private ImapServer serverImpl;

    private int port = 143;
    private String host = "localhost";
    private ImapHostManager imapHostManager;

    private UserManager imapUserManager;
    
    private boolean imapServerEnabled;
    
    public void setImapServerEnabled(boolean imapServerEnabled)
    {
        this.imapServerEnabled = imapServerEnabled;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setImapHostManager(ImapHostManager imapHostManager)
    {
        this.imapHostManager = imapHostManager;
    }

    public void setImapUserManager(UserManager imapUserManager)
    {
        this.imapUserManager = imapUserManager;
    }
    
    protected void onBootstrap(ApplicationEvent event)
    {
        if (imapServerEnabled)
        {
            Managers imapManagers = new Managers()
            {
                public ImapHostManager getImapHostManager()
                {
                    return imapHostManager;
                }
    
                public UserManager getUserManager()
                {
                    return imapUserManager;
                }
            };
            serverImpl = new ImapServer(new ServerSetup(port, host, ServerSetup.PROTOCOL_IMAP), imapManagers);
            serverImpl.startService(null);
            if (logger.isInfoEnabled())
            {
                logger.info("IMAP service started on host:port " + this.host + ":" + this.port + ".");
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("IMAP service is disabled.");
            }
        }
    }

    protected void onShutdown(ApplicationEvent event)
    {
        if (serverImpl != null)
        {
            serverImpl.stopService(null);
        }
    }

}
