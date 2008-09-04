/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import junit.framework.TestCase;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author Michael Shavnev
 */
public abstract class AbstractServiceTest extends TestCase
{
    protected ServiceRegistry serviceRegistry;
    protected static NodeRef ALFRESCO_TUTORIAL_NODE_REF = null;
    protected static NodeRef COMPANY_HOME_NODE_REF = null;

    protected static FileSystemXmlApplicationContext fContext = null;

    protected Object servicePort = null;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        if (fContext == null)
        {
            fContext = new FileSystemXmlApplicationContext("classpath:alfresco/application-context.xml");

            AuthenticationComponent authenticationComponent = (AuthenticationComponent) fContext.getBean("authenticationComponent");
            authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

            serviceRegistry = (ServiceRegistry) fContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
            ResultSet resultSet = serviceRegistry.getSearchService().query(new StoreRef("workspace://SpacesStore"), "lucene", "@cm\\:name:Alfresco-Tutorial.pdf");
            ALFRESCO_TUTORIAL_NODE_REF = resultSet.getNodeRef(0);

            resultSet = serviceRegistry.getSearchService().query(new StoreRef("workspace://SpacesStore"), "lucene", "@cm\\:name:Company Home");
            COMPANY_HOME_NODE_REF = resultSet.getNodeRef(0);

            authenticationComponent.clearCurrentSecurityContext();
        }
    }


    public AbstractServiceTest()
    {
        servicePort = getServicePort();

        Map<String, Object> wss4jOutInterceptorProp = new HashMap<String, Object>();
        wss4jOutInterceptorProp.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN + " " + WSHandlerConstants.TIMESTAMP);

        wss4jOutInterceptorProp.put(WSHandlerConstants.USER, getUserName());
        wss4jOutInterceptorProp.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_DIGEST);

        wss4jOutInterceptorProp.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler()
        {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
            {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
                pc.setPassword(getPassword());
            }
        });

        WSS4JOutInterceptor wss4jOutInterceptor = new WSS4JOutInterceptor(wss4jOutInterceptorProp);

        Client client = ClientProxy.getClient(servicePort);
        client.getEndpoint().getOutInterceptors().add(new SAAJOutInterceptor());
        client.getEndpoint().getOutInterceptors().add(wss4jOutInterceptor);
    }

    protected abstract Object getServicePort();

    protected String getUserName()
    {
        return "admin";
    }

    protected String getPassword()
    {
        return "admin";
    }

}
