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
package org.alfresco.repo.cmis.rest;

import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * CMIS API Test Harness
 * 
 * @author davidc
 */
public class TestRemoteCMIS extends CMISTest
{
    // remote CMIS server
    private static String repositoryUrl = "http://localhost:8080/alfresco/service/api/repository";
    private static String username = "admin";
    private static String password = "admin";
    private static boolean argsAsHeaders = false;


    @Override
    protected void setUp() throws Exception
    {
        if (repositoryUrl != null)
        {
            setRepositoryUrl(repositoryUrl);
            RemoteServer server = new RemoteServer();
            server.username = username;
            server.password = password;
            setRemoteServer(server);
        }
        
        setArgsAsHeaders(argsAsHeaders);

        super.setUp();
    }

    /**
     * Execute Unit Tests as client to remote CMIS Server
     * 
     * args[0] = serverUrl
     * args[1] = username/password
     * args[2] = [params=url|headers]
     * 
     * @param args  args
     */
    public static void main(String[] args)
    {
        if (args.length > 0)
        {
            repositoryUrl = args[0];
        }
        
        if (args.length > 1)
        {
            String[] credentials = args[1].split("/");
            username = credentials[0];
            if (credentials.length > 1)
            {
                password = credentials[1];
            }
        }
        
        String params = "both";
        if (args.length > 2)
        {
            String[] paramSegment = args[1].split("=");
            if (paramSegment[0].equalsIgnoreCase("params"))
            {
                params = paramSegment[1].toLowerCase();
            }
        }
        
        // execute cmis tests with url arguments
        if (params.equals("both") || params.equals("url"))
        {
            TestRunner.run(new TestSuite(TestRemoteCMIS.class));
        }
        
        // execute cmis tests with headers
        if (params.equals("both") || params.equals("headers"))
        {
            argsAsHeaders = true;
            TestRunner.run(new TestSuite(TestRemoteCMIS.class));
        }            
    }
    
}
