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
package org.alfresco.repo.cmis.rest.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.alfresco.repo.cmis.rest.test.CMISTest.CMISTestListener;
import org.alfresco.repo.web.scripts.BaseWebScriptTest.RemoteServer;
import org.alfresco.repo.web.scripts.BaseWebScriptTest.WebScriptTestListener;
import org.alfresco.util.CachingDateFormat;


/**
 * CMIS Test Runner
 * 
 * @author davidc
 */
public class CMISTestRunner
{
    private String match = null;
    private WebScriptTestListener listener = new CMISTestListener(System.out);
    private boolean traceReqRes = false;
    private String serviceUrl = null;
    private String userpass = null;
    private String arguments = "url";
    private boolean validateResponse = true;

    
    /**
     * @param match  test name to execute (* for wildcards)
     */
    public void setMatch(String match)
    {
        this.match = match;
    }

    /**
     * @param listener  test listener
     */
    public void setListener(WebScriptTestListener listener)
    {
        this.listener = listener;
    }

    /**
     * @param traceReqRes  true => trace requests / responses
     */
    public void setTraceReqRes(boolean traceReqRes)
    {
        this.traceReqRes = traceReqRes;
    }

    /**
     * @param serviceUrl  cmis service document url
     */
    public void setServiceUrl(String serviceUrl)
    {
        this.serviceUrl = serviceUrl;
    }

    /**
     * @param userpass  user name / password
     */
    public void setUserPass(String userpass)
    {
        this.userpass = userpass;
    }

    /**
     * @param arguments  "url" => url arguments, "headers" => request headers, "both" => url & headers
     */
    public void setArguments(String arguments)
    {
        this.arguments = arguments;
    }
    
    /**
     * @param validateResponse  true => test response against CMIS XSDs
     */
    public void setValidateResponse(boolean validateResponse)
    {
        this.validateResponse = validateResponse;
    }
    
    /**
     * Gets the names of CMIS tests
     * 
     * @param match  * for wildcard
     * @return  array of test names
     */
    public String[] getTestNames(String match)
    {
        List<String> namesList = new ArrayList<String>();
        TestSuite allSuite = new TestSuite(CMISTest.class);
        for (int i = 0; i < allSuite.countTestCases(); i++)
        {
            CMISTest test = (CMISTest)allSuite.testAt(i);
            if (match == null || match.equals("*") || test.getName().matches(match.replace("*", "[A-Za-z0-9]*")))
            {
                namesList.add(test.getName());
            }
        }
        String[] names = new String[namesList.size()];
        namesList.toArray(names);
        return names;
    }
    
    /**
     * Execute CMIS Tests
     */
    public void execute()
    {
        RemoteServer server = null;
        if (serviceUrl != null)
        {
            server = new RemoteServer();
            if (userpass != null)
            {
                String[] credentials = userpass.split("/");
                server.username = credentials[0];
                if (credentials.length > 1)
                {
                    server.password = credentials[1];
                }
            }
        }
        
        // dump test parameters
        if (listener != null)
        {
            Calendar today = Calendar.getInstance();
            SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd HH:mm:ss.SSS", true);
            listener.addLog(null, "Test Started at " + df.format(today.getTime()));
            listener.addLog(null, "Service URL: " + (serviceUrl == null ? "[not set]" : serviceUrl));
            listener.addLog(null, "User: " + (userpass == null ? "[not set]" : userpass));
            listener.addLog(null, "Args: " + (arguments == null ? "[not set]" : arguments));
            listener.addLog(null, "Validate Responses: " + validateResponse);
            listener.addLog(null, "Trace Requests/Responses: " + traceReqRes);
            listener.addLog(null, "Tests: " + (match == null ? "*" : match));
            listener.addLog(null, "");
        }
        
        // execute cmis tests with url arguments
        if (arguments.equals("both") || arguments.equals("url"))
        {
            executeSuite(match, server, false);
        }
        
        // execute cmis tests with headers
        if (arguments.equals("both") || arguments.equals("headers"))
        {
            executeSuite(match, server, true);
        }            
    }

    /**
     * Execute suite of CMIS Tests
     * 
     * @param match  tests to execute (* for wildcard)
     * @param server  remote server
     * @param argsAsHeaders  arguments passed in Headers
     */
    private void executeSuite(String match, RemoteServer server, boolean argsAsHeaders)
    {
        TestSuite allSuite = new TestSuite(CMISTest.class);
        TestSuite suite = new TestSuite();
        for (int i = 0; i < allSuite.countTestCases(); i++)
        {
            CMISTest test = (CMISTest)allSuite.testAt(i);
            if (match == null || match.equals("*") || test.getName().matches(match.replace("*", "[A-Za-z0-9]*")))
            {
                if (listener != null)
                {
                    test.setListener(listener);
                    test.setTraceReqRes(traceReqRes);
                }
                if (server != null)
                {
                    test.setServiceUrl(serviceUrl);
                    if (server != null)
                    {
                        test.setRemoteServer(server);
                    }
                }
                test.setArgsAsHeaders(argsAsHeaders);
                test.setValidateResponse(validateResponse);
                suite.addTest(test);
            }
        }
        TestResult result = new TestResult();
        if (listener != null)
        {
            result.addListener(listener);
        }
        suite.run(result);
    }
    
    /**
     * Execute CMIS Tests from command-line 
     * 
     * url={serviceUrl}
     * user={userpass}
     * args={"url"|"headers"|"both"}
     */
    public static void main(String[] args)
    {
        CMISTestRunner runner = new CMISTestRunner();

        for (String arg : args)
        {
            String[] argSegment = arg.split("=");
            if (argSegment[0].equals("url"))
            {
                runner.setServiceUrl(argSegment[1]);
            }
            else if (argSegment[0].equals("user"))
            {
                runner.setUserPass(argSegment[1]);
            }
            else if (argSegment[0].equalsIgnoreCase("args"))
            {
                runner.setArguments(argSegment[1].toLowerCase());
            }
        }

        // execute
        runner.execute();
        System.exit(0);
    }

}
