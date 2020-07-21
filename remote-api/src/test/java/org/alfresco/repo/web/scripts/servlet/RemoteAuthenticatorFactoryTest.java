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
package org.alfresco.repo.web.scripts.servlet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.management.subsystems.DefaultChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

/**
 * 
 * @author sglover
 *
 */
public class RemoteAuthenticatorFactoryTest
{
    private static final String[] contextLocations = new String[] {
            "classpath:alfresco/application-context.xml",
            "classpath:alfresco/web-scripts-application-context.xml",
            "classpath:alfresco/web-scripts-application-context-test.xml"
    };

    private static RemoteUserAuthenticatorFactory remoteUserAuthenticatorFactory;
    private static PersonService personService;
    private static TransactionService transactionService;
    private static MutableAuthenticationDao authenticationDAO;

    @BeforeClass
    public static void beforeClass() throws Exception
    {
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext(contextLocations);
        DefaultChildApplicationContextManager childApplicationContextManager = (DefaultChildApplicationContextManager) ctx.getBean("Authentication");
        remoteUserAuthenticatorFactory = (RemoteUserAuthenticatorFactory) ctx.getBean("webscripts.authenticator.remoteuser");
        personService = (PersonService)ctx.getBean("PersonService");
        transactionService = (TransactionService)ctx.getBean("TransactionService");
        authenticationDAO = (MutableAuthenticationDao)ctx.getBean("authenticationDao");

        childApplicationContextManager.stop();
        childApplicationContextManager.setProperty("chain", "external1:external");
        ChildApplicationContextFactory childApplicationContextFactory = childApplicationContextManager.getChildApplicationContextFactory("external1");
        childApplicationContextFactory.stop();
        childApplicationContextFactory.setProperty("external.authentication.proxyUserName", "");
    }

    private String createPerson(boolean enabled)
    {
        Map<QName, Serializable> properties = new HashMap<>();
        String username = "user" + GUID.generate();
        properties.put(ContentModel.PROP_USERNAME, username);
        properties.put(ContentModel.PROP_FIRSTNAME, username);
        properties.put(ContentModel.PROP_LASTNAME, username);
        if(!enabled)
        {
            properties.put(ContentModel.PROP_ENABLED, enabled);
        }
        personService.createPerson(properties);

        authenticationDAO.createUser(username, "password".toCharArray());
        authenticationDAO.setEnabled(username, enabled);

        return username;
    }

    @Test
    public void testDisabledUser() throws Exception
    {
        final String username = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<String>()
        {
            @Override
            public String execute() throws Throwable
            {
                return AuthenticationUtil.runAs(new RunAsWork<String>()
                {
                    @Override
                    public String doWork() throws Exception
                    {
                        return createPerson(false);
                    }
                }, AuthenticationUtil.SYSTEM_USER_NAME);
            }
        }, false, true);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                return AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    @Override
                    public Void doWork() throws Exception
                    {
                        // Mock a request with a username in the header
                        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
                        when(mockHttpRequest.getHeader("X-Alfresco-Remote-User")).thenReturn(username);
                        when(mockHttpRequest.getScheme()).thenReturn("http");
                        WebScriptServletRequest mockRequest = mock(WebScriptServletRequest.class);
                        when(mockRequest.getHttpServletRequest()).thenReturn(mockHttpRequest);

                        HttpServletResponse mockHttpResponse = mock(HttpServletResponse.class);
                        WebScriptServletResponse mockResponse = mock(WebScriptServletResponse.class);
                        when(mockResponse.getHttpServletResponse()).thenReturn(mockHttpResponse);

                        Authenticator authenticator = remoteUserAuthenticatorFactory.create(mockRequest, mockResponse);
                        assertFalse(authenticator.authenticate(RequiredAuthentication.user, false));

                        return null;
                    }
                }, AuthenticationUtil.SYSTEM_USER_NAME);
            }
        }, false, true);
    }

    @Test
    public void testEnabledUser() throws Exception
    {
        final String username = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<String>()
        {
            @Override
            public String execute() throws Throwable
            {
                return AuthenticationUtil.runAs(new RunAsWork<String>()
                {
                    @Override
                    public String doWork() throws Exception
                    {
                        return createPerson(true);
                    }
                }, AuthenticationUtil.SYSTEM_USER_NAME);
            }
        }, false, true);

        // Mock a request with a username in the header
        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getHeader("X-Alfresco-Remote-User")).thenReturn(username);
        when(mockHttpRequest.getScheme()).thenReturn("http");
        WebScriptServletRequest mockRequest = mock(WebScriptServletRequest.class);
        when(mockRequest.getHttpServletRequest()).thenReturn(mockHttpRequest);

        HttpServletResponse mockHttpResponse = mock(HttpServletResponse.class);
        WebScriptServletResponse mockResponse = mock(WebScriptServletResponse.class);
        when(mockResponse.getHttpServletResponse()).thenReturn(mockHttpResponse);

        Authenticator authenticator = remoteUserAuthenticatorFactory.create(mockRequest, mockResponse);
        assertTrue(authenticator.authenticate(RequiredAuthentication.user, false));
    }

    @Test
    public void testLogInWithNonExistingPerson()
    {
        // Random non existing person
        final String username = GUID.generate();

        // Mock a request with a username in the header
        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getHeader("X-Alfresco-Remote-User")).thenReturn(username);
        when(mockHttpRequest.getScheme()).thenReturn("http");
        WebScriptServletRequest mockRequest = mock(WebScriptServletRequest.class);
        when(mockRequest.getHttpServletRequest()).thenReturn(mockHttpRequest);

        HttpServletResponse mockHttpResponse = mock(HttpServletResponse.class);
        WebScriptServletResponse mockResponse = mock(WebScriptServletResponse.class);
        when(mockResponse.getHttpServletResponse()).thenReturn(mockHttpResponse);

        Authenticator authenticator = remoteUserAuthenticatorFactory.create(mockRequest, mockResponse);
        assertTrue("The non existing user should be authenticated.", authenticator.authenticate(RequiredAuthentication.user, false));
        assertTrue("The user should be auto created.", personService.personExists(username));
    }
}

