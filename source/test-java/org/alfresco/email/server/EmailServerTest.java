package org.alfresco.email.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.service.cmr.email.EmailService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import junit.framework.TestCase;

public class EmailServerTest extends TestCase
{
    /**
     * Services used by the tests
     */
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private EmailServer emailServer;
    
    // Linux-based env. should use port bigger than 1024
    private final int TEST_PORT = 2225;
    private final String TEST_HOST = "localhost";
    private final int TEST_CLIENT_TIMEOUT = 20000;

    private EmailService emailService;

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
    }

    @After
    public void tearDown() throws Exception
    {
        // nothing now
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
        emailServer.onShutdown(null);

        emailServer.setEnableTLS(false);
        emailServer.setEnabled(true);
        
        emailServer.setAuthenticate(false);
        emailServer.setUnknownUser(null);
        
        emailServer.setPort(TEST_PORT);
        emailServer.startup();

        String[] response = getMailFromNullableResponse(TEST_HOST, TEST_PORT);
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
        emailServer.onShutdown(null);

        emailServer.setEnableTLS(false);
        emailServer.setEnabled(true);
        
        emailServer.setAuthenticate(true);
        emailServer.setUnknownUser(null);
        
        emailServer.setPort(TEST_PORT);
        emailServer.startup();

        String[] response = getMailFromNullableResponse(TEST_HOST, TEST_PORT);
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
        emailServer.onShutdown(null);

        emailServer.setEnableTLS(false);
        emailServer.setEnabled(true);
        
        emailServer.setAuthenticate(false);
        emailServer.setUnknownUser("anonymous");
        
        emailServer.setPort(TEST_PORT);
        emailServer.startup();

        String[] response = getMailFromNullableResponse(TEST_HOST, TEST_PORT);
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
        emailServer.onShutdown(null);
        
        // we need to delete value from "email.inbound.unknownUser" in service too
        if (emailService instanceof EmailServiceImpl)
        {
            ((EmailServiceImpl) emailService).setUnknownUser("");
        }
        emailServer.setUnknownUser(null);

        emailServer.setAuthenticate(true);
        emailServer.setEnableTLS(false);
        emailServer.setEnabled(true);
        
        emailServer.setPort(TEST_PORT);
        emailServer.startup();
        
        String[] request = new String[] 
                {
                   "MAIL FROM:<>\r\n",
                   "RCPT TO:<buffy@sunnydale.high>\r\n",
                   "DATA\r\n",
                   "Hello world\r\n.\r\n",
                   "QUIT\r\n"
                };
        String[] response = getResponse(TEST_HOST, TEST_PORT, request);
        
        checkResponse(response);
        
        assertTrue("Response incorrect", response.length > 4);
        // expects smth. like: "554 some data"
        // we are expect error code
        assertTrue("Response should have error code", response[4].indexOf("5") == 0);
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
