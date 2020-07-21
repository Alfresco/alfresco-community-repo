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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.service.cmr.email.EmailService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class EmailServerTest extends TestCase
{
    /**
     * Services used by the tests
     */
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private EmailServer emailServer;
    
    // Linux-based env. should use port bigger than 1024
    private final int DEFAULT_TEST_PORT = 2225;
    private final String TEST_HOST = "localhost";
    private final int TEST_CLIENT_TIMEOUT = 20000;
    
    private final short SHUTDOWN_SLEEP_TIME = 1000;

    private EmailService emailService;
    private int currentPort;
    
    @Before
    @Override
    public void setUp() throws Exception
    {
        ChildApplicationContextFactory emailSubsystem = (ChildApplicationContextFactory) ctx.getBean("InboundSMTP");
        assertNotNull("emailSubsystem", emailSubsystem);
        ApplicationContext emailCtx = emailSubsystem.getApplicationContext();
        emailServer = (EmailServer) emailCtx.getBean("emailServer");
        emailService = (EmailService)emailCtx.getBean("emailService");       
        assertNotNull("emailService", emailService);
        
        // MNT-14417
        shutdownServer();
    }

    @After
    public void tearDown() throws Exception
    {
    	// MNT-14417
        shutdownServer();
    }

    /*
     * Check null reverse-path if 
     * email.server.auth.enabled=false
     *  and 
     * email.inbound.unknownUser isn't set
     * 
     * Null reverse-path must be disallowed
     */
    @Test
    public void testDisallowedNulableFromUser() throws Exception
    {
        emailServer.setEnableTLS(false);
        
        emailServer.setAuthenticate(false);
        emailServer.setUnknownUser(null);
        
        // MNT-14417
        startupServer();

        String[] response = getMailFromNullableResponse(TEST_HOST, getServerPort());
        checkResponse(response);
        
        // expects smth. like: "504 some data"
        // we are expect error code first
        assertTrue("Response should have error code", response[1].indexOf("5") == 0);
    }
    
    /*
     * Check null reverse-path if 
     * email.server.auth.enabled=true
     *  and 
     * email.inbound.unknownUser isn't set
     * 
     * Null reverse-path must be allowed
     */
    @Test
    public void testAllowedNulableFromUserWithAuth() throws Exception
    {
        emailServer.setEnableTLS(false);
        
        emailServer.setAuthenticate(true);
        emailServer.setUnknownUser(null);
        
        // MNT-14417
        startupServer();

        String[] response = getMailFromNullableResponse(TEST_HOST, getServerPort());
        checkResponse(response);
        
        // expects smth. like: "250 some data"
        // we aren't expect error code
        assertTrue("Response should have error code", response[1].indexOf("2") == 0);
    }
    
    /*
     * Check null reverse-path if 
     * email.server.auth.enabled=false
     *  and 
     * email.inbound.unknownUser is set
     * 
     * Null reverse-path must be allowed
     */
    @Test
    public void testAllowedNulableFromUserWithAnonymous() throws Exception
    {
        emailServer.setEnableTLS(false);
        
        emailServer.setAuthenticate(false);
        emailServer.setUnknownUser("anonymous");
        
        // MNT-14417
        startupServer();

        String[] response = getMailFromNullableResponse(TEST_HOST, getServerPort());
        checkResponse(response);
        
        // expects smth. like: "250 some data"
        // we aren't expect error code
        assertTrue("Response should have error code", response[1].indexOf("2") == 0);
    }
    
    /*
     * Check for data accepting if "From" user absent
     * and 
     * "email.inbound.unknownUser" isn't set
     * email.server.auth.enabled=true
     */
    @Test
    public void testForDataAcceptingIfUserIsEmpty() throws Exception
    {
        // we need to delete value from "email.inbound.unknownUser" in service too
        if (emailService instanceof EmailServiceImpl)
        {
            ((EmailServiceImpl) emailService).setUnknownUser("");
        }
        emailServer.setUnknownUser(null);

        emailServer.setAuthenticate(true);
        emailServer.setEnableTLS(false);
        
        // MNT-14417
        startupServer();
        
        String[] request = new String[] 
                {
                   "MAIL FROM:<>\r\n",
                   "RCPT TO:<buffy@sunnydale.high>\r\n",
                   "DATA\r\n",
                   "Hello world\r\n.\r\n",
                   "QUIT\r\n"
                };
        String[] response = getResponse(TEST_HOST, getServerPort(), request);
        
        checkResponse(response);
        
        assertTrue("Response incorrect", response.length > 4);
        // expects smth. like: "554 some data"
        // we are expect error code
        assertTrue("Response should have error code", response[4].indexOf("5") == 0);
    }
    
    private void startupServer()
    {
        currentPort = DEFAULT_TEST_PORT;
        boolean started = false;
        while (!started && currentPort < 65535)
        {
            try
            {
                emailServer.setEnabled(true);
                emailServer.setPort(currentPort);
                emailServer.onBootstrap(null);
                started = true;
            }
            catch (Exception exc)
            {
                // There is RuntimeException. We need to extract cause of error
                if (exc.getCause() instanceof java.net.BindException)
                {
                    currentPort++;
                }
                else
                {
                    throw exc;
                }
            }
        }
        if (!started)
        {
            throw new RuntimeException("Unable to start email server");
        }
    }
    
    // MNT-14417: wait for a while to avoid "java.net.BindException: Address already in use"
    private void shutdownServer() throws InterruptedException
    {
    	emailServer.onShutdown(null);
    	Thread.sleep(SHUTDOWN_SLEEP_TIME);
    	emailServer.setEnabled(false);
    }
    
    private int getServerPort()
    {
        return currentPort;
    }
    
    private void checkResponse(String[] response)
    {
        assertNotNull("Client hasn't response", response);
        assertNotNull("Client hasn empty response", response[0]);
    }

    private String[] getMailFromNullableResponse(String host, int port) throws Exception
    {
        // Send null reverse-path
        return getResponse(host, port, new String[]{"MAIL FROM:<>\r\n", "QUIT\r\n"});
    }
    
    private String[] getResponse(String host, int port, String requestStrings[]) throws Exception
    {
        Socket sock = new Socket(host, port);
        sock.setSoTimeout(TEST_CLIENT_TIMEOUT);
        PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        ArrayList<String> response = new ArrayList<String>();

        try
        {
            // Read first server response. It is smth. like: ESMTP SubEthaSMTP 3.1.6
            response.add(in.readLine());
            
            for (String reqStr : requestStrings)
            {
                out.print(reqStr);
                out.flush();
                response.add(in.readLine());
            }

//            for (String s : response)
//            {
//                System.out.println(s);
//            }
            return response.toArray(new String[response.size()]);
        }
        finally
        {
            in.close();
            out.close();
            sock.close();
        }
    }
}
