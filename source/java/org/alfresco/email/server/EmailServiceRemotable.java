/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.email.server;

import org.alfresco.email.server.impl.subetha.SubethaEmailMessage;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.email.EmailService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.AbstractLifecycleBean;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.remoting.rmi.RmiClientInterceptor;

/**
 * @author Michael Shavnev
 * @since 2.2
 */
public class EmailServiceRemotable extends AbstractLifecycleBean implements EmailService
{
    private String rmiRegistryHost;

    private int rmiRegistryPort;

    private EmailService emailServiceProxy;

    public void setRmiRegistryHost(String rmiRegistryHost)
    {
        this.rmiRegistryHost = rmiRegistryHost;
    }

    public void setRmiRegistryPort(int rmiRegistryPort)
    {
        this.rmiRegistryPort = rmiRegistryPort;
    }

    public void importMessage(EmailMessage message)
    {
        if (message instanceof SubethaEmailMessage)
        {
            ((SubethaEmailMessage) message).setRmiRegistry(rmiRegistryHost, rmiRegistryPort);
        }
        emailServiceProxy.importMessage(message);
    }

    public void importMessage(NodeRef nodeRef, EmailMessage message)
    {
        if (message instanceof SubethaEmailMessage)
        {
            ((SubethaEmailMessage) message).setRmiRegistry(rmiRegistryHost, rmiRegistryPort);
        }
        emailServiceProxy.importMessage(nodeRef, message);
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        if (rmiRegistryHost == null)
        {
            throw new AlfrescoRuntimeException("Property 'rmiRegistryHost' not set");
        }
        if (rmiRegistryPort == 0)
        {
            throw new AlfrescoRuntimeException("Property 'rmiRegistryPort' not set");
        }

        RmiClientInterceptor rmiClientInterceptor = new RmiClientInterceptor();
        rmiClientInterceptor.setRefreshStubOnConnectFailure(true);
        rmiClientInterceptor.setServiceUrl("rmi://" + rmiRegistryHost + ":" + rmiRegistryPort + "/emailService");
        emailServiceProxy = (EmailService) ProxyFactory.getProxy(EmailService.class, rmiClientInterceptor);
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }
}
