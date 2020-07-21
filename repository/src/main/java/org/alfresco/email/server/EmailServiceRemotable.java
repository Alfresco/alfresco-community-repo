/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.email.server;

import org.alfresco.email.server.impl.subetha.SubethaEmailMessage;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.email.EmailDelivery;
import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.email.EmailService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
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

    public void importMessage(EmailDelivery delivery, EmailMessage message)
    {
        if (message instanceof SubethaEmailMessage)
        {
            ((SubethaEmailMessage) message).setRmiRegistry(rmiRegistryHost, rmiRegistryPort);
        }
        emailServiceProxy.importMessage(delivery, message);
    }

    public void importMessage(EmailDelivery delivery, NodeRef nodeRef, EmailMessage message)
    {
        if (message instanceof SubethaEmailMessage)
        {
            ((SubethaEmailMessage) message).setRmiRegistry(rmiRegistryHost, rmiRegistryPort);
        }
        emailServiceProxy.importMessage(delivery, nodeRef, message);
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
