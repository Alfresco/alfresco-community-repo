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
package org.alfresco.repo.content.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * A read-only store using HTTP to access content from a remote Alfresco application.
 * <p>
 * The primary purpose of this component is to allow clustered content sharing without
 * having to have shared access to the binary data on the various machines.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class HttpAlfrescoStore extends AbstractContentStore
{
    private TransactionService transactionService;
    private AuthenticationService authenticationService;
    private String baseHttpUrl;

    /**
     * Default constructor for bean instantiation.
     */
    public HttpAlfrescoStore()
    {
    }

    /**
     * 
     * @param transactionService            used to ensure proper ticket propagation in a cluster
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param authenticationService         used to retrieve authentication ticket
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * Set the base HTTP URL of the remote Alfresco application.<br/>
     * For example: <pre>http://192.168.1.66:8080/alfresco</pre>.
     * 
     * @param baseHttpUrl       the remote HTTP address including the <b>.../alfresco</b>
     */
    public void setBaseHttpUrl(String baseHttpUrl)
    {
        if (baseHttpUrl.endsWith("/"))
        {
            baseHttpUrl = baseHttpUrl.substring(0, baseHttpUrl.length() - 1);
        }
        this.baseHttpUrl = baseHttpUrl;
    }
    
    /**
     * This <b>is</b> a read only store.
     * 
     * @return                  <tt>false</tt> always
     */
    public boolean isWriteSupported()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public ContentReader getReader(String contentUrl)
    {
        ContentReader reader = new HttpAlfrescoContentReader(
                transactionService,
                authenticationService,
                baseHttpUrl,
                contentUrl);
        return reader;
    }
    
    /**
     * Tests the HTTP store against a given server.<br>
     * Usage:
     * <pre>
     *    HttpAlfrescoStore help
     *       Print the usage message
     * </pre>
     * 
     * @param args  the program arguments
     */
    public static void main(String[] args)
    {
        String baseUrl = null;
        String contentUrl = null;
        try
        {
            if (args.length != 2)
            {
                printUsage(System.err);
                System.exit(1);
            }
            else if (args[0].equalsIgnoreCase("help"))
            {
                printUsage(System.out);
                System.exit(0);
            }
            baseUrl = args[0];
            contentUrl = args[1];
            
            // Start the application to get hold of all the beans
            ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
            
            System.out.println(
                    "Starting test of " + HttpAlfrescoStore.class.getName() + " using server " + baseUrl + ".");

            // Do the test
            doTest(ctx, baseUrl, contentUrl);
            
            System.out.println(
                    "Completed test of " + HttpAlfrescoStore.class.getName() + " using server " + baseUrl + ".");

            // Done
            System.exit(0);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.err.println(
                    "Test of " + HttpAlfrescoStore.class.getName() + " using server " + baseUrl + " failed.");
            System.exit(1);
        }
        finally
        {
            try
            {
                ApplicationContextHelper.closeApplicationContext();
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
        }
    }
    
    private static void doTest(ApplicationContext ctx, String baseUrl, String contentUrl) throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        TransactionService transactionService = serviceRegistry.getTransactionService();
        AuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
        // Construct the store
        HttpAlfrescoStore store = new HttpAlfrescoStore();
        store.setTransactionService(transactionService);
        store.setAuthenticationService(authenticationService);
        store.setBaseHttpUrl(baseUrl);
        
        // Now test
        System.out.println(
                "   Retrieving reader for URL " + contentUrl);
        ContentReader reader = store.getReader(contentUrl);
        System.out.println(
                "   Retrieved reader for URL " + contentUrl);
        // Check if the content exists
        boolean exists = reader.exists();
        if (!exists)
        {
            System.out.println(
                    "   Content doesn't exist: " + contentUrl);
            return;
        }
        else
        {
            System.out.println(
                    "   Content exists: " + contentUrl);
        }
        // Get the content data
        ContentData contentData = reader.getContentData();
        System.out.println(
                "   Retrieved content data: " + contentData);
        
        // Now get the content
        ByteBuffer buffer = ByteBuffer.allocate((int)reader.getSize());
        FileChannel channel = reader.getFileChannel();
        try
        {
            int count = channel.read(buffer);
            if (count != reader.getSize())
            {
                System.err.println("The number of bytes read was " + count + " but expected " + reader.getSize());
                return;
            }
        }
        finally
        {
            channel.close();
        }
    }
    
    private static void printUsage(OutputStream os) throws IOException
    {
        String msg = "Usage: \n" +
                "   HttpAlfrescoStore <server-ip> <content-url>\n" +
                "      server-ip: the remote HTTP server running Alfresco \n" +
                "      content-url: the content URL to retrieve \n" +
                "      Run the test against a server. \n" +
                "   HttpAlfrescoStore help \n" +
                "      Print the usage message";
        os.write(msg.getBytes());
        os.flush();
    }
}
