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
package org.alfresco.repo.imap;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import com.icegreen.greenmail.Managers;
import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.imap.ImapServer;
import com.icegreen.greenmail.user.UserManager;
import com.icegreen.greenmail.util.ServerSetup;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLServerSocket;

/**
 * @author Mike Shavnev
 */
public class AlfrescoImapServer extends AbstractLifecycleBean
{
    private class SecureImapServer extends ImapServer
    {
        
        public SecureImapServer(ServerSetup setup, Managers managers)
        {
            super(setup, managers);
        }

        /**
         * @override
         * Use Java's default SSL Server SocketFactory
         * controlled via System Properties 
         * -Djavax.net.ssl.keyStore=mySrvKeystore 
         * -Djavax.net.ssl.keyStorePassword=123456 
         */
        // MER - also consider using SSLContext
        protected synchronized ServerSocket openServerSocket() throws IOException {
            ServerSocket ret;
            if (setup.isSecure()) 
            {
                try
                {
                    ret = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(
                    setup.getPort(), 0, bindTo);
                } 
                catch (IOException e)
                {
                    if(logger.isErrorEnabled())
                    {
                        logger.error("Unable to open socket bindTo:" + bindTo + "port " + setup.getPort(), e);
                    }
                    throw e;
                }
            } 
            else 
            {
                try
                {
                    ret = new ServerSocket(setup.getPort(), 0, bindTo);
                }
                catch (IOException e)
                {
                    if(logger.isErrorEnabled())
                    {
                        logger.error("Unable to open socket bindTo:" + bindTo + "port " + setup.getPort(), e);
                    }
                    throw e;
                }
            }
            return ret;
        }
    }

    private static Log logger = LogFactory.getLog(AlfrescoImapServer.class);

    private ImapServer serverImpl;
    private ImapServer secureServerImpl;

    private int port = 143;
    private int securePort = 993;
    private boolean imapsEnabled = false;
    private boolean imapEnabled = true;
    
    private String host = "0.0.0.0";

    private UserManager imapUserManager;
    private ImapService imapService;
    
    private boolean imapServerEnabled;
    
    
    public void setImapServerEnabled(boolean imapServerEnabled)
    {
        this.imapServerEnabled = imapServerEnabled;
    }
    
    public boolean isImapServerEnabled()
    {
        return imapServerEnabled;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setHost(String host)
    {
        this.host = host;
    }
    
    public int getPort()
    {
        return port;
    }
    
    public void setSecurePort(int securePort)
    {
        this.securePort = securePort;
    }

    public int getSecurePort()
    {
        return securePort;
    }

    public String getHost()
    {
        return host;
    }

    public void setImapService(ImapService imapService)
    {
        this.imapService = imapService;
    }

    public void setImapUserManager(UserManager imapUserManager)
    {
        this.imapUserManager = imapUserManager;
    }
    
    protected void onBootstrap(ApplicationEvent event)
    {
        if (imapServerEnabled)
        {
            startup();
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
        shutdown();

    }
    
    public void startup()
    {
        if(serverImpl == null)
        {
            final Managers imapManagers = new Managers()
            {
                // We create a new Host Manager instance per session to allow for session state tracking
                public ImapHostManager getImapHostManager()
                {
                    return new AlfrescoImapHostManager(AlfrescoImapServer.this.imapService);
                }
    
                public UserManager getUserManager()
                {
                    return imapUserManager;
                }
            };
            
            if(isImapEnabled())
            {
                serverImpl = new ImapServer(new ServerSetup(port, host, ServerSetup.PROTOCOL_IMAP), imapManagers);
                serverImpl.startService(null);
            
                if (logger.isInfoEnabled())
                {
                    logger.info("IMAP service started on host:port " + host + ":" + this.port);
                }
            }
            if(isImapsEnabled())
            {
                secureServerImpl = new SecureImapServer(new ServerSetup(securePort, host, ServerSetup.PROTOCOL_IMAPS), imapManagers);
                secureServerImpl.startService(null);    
                if (logger.isInfoEnabled())
                {
                    logger.info("IMAPS service started on host:port " + host + ":" + this.securePort );
                }
            }

        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("IMAP server already running.");
            }
        }
    }
    
    public void shutdown()
    {
        if (serverImpl != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("IMAP service stopping.");
            }
            serverImpl.stopService(null);
        }
        
        if (secureServerImpl != null)
        {   
            if (logger.isDebugEnabled())
            {
                logger.debug("IMAPS service stopping.");
            }
            secureServerImpl.stopService(null);
        }
    }

    public void setImapsEnabled(boolean imapsEnabled)
    {
        this.imapsEnabled = imapsEnabled;
    }

    public boolean isImapsEnabled()
    {
        return imapsEnabled;
    }

    public void setImapEnabled(boolean imapEnabled)
    {
        this.imapEnabled = imapEnabled;
    }

    public boolean isImapEnabled()
    {
        return imapEnabled;
    }
}
