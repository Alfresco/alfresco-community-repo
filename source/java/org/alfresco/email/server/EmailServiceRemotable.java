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
