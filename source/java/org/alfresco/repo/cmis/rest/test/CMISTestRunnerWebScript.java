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
package org.alfresco.repo.cmis.rest.test;

import java.io.IOException;
import java.io.PrintStream;

import org.alfresco.repo.cmis.rest.test.CMISTest.CMISTestListener;
import org.alfresco.repo.web.scripts.BaseWebScriptTest.WebScriptTestListener;
import org.alfresco.web.scripts.AbstractWebScript;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptResponse;

/**
 * Execute CMIS Tests
 * 
 * @author davidc
 */
public class CMISTestRunnerWebScript extends AbstractWebScript
{
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScript#execute(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    public void execute(WebScriptRequest req, WebScriptResponse res)
        throws IOException
    {
        // setup CMIS tests
        PrintStream printStream = new PrintStream(res.getOutputStream());
        WebScriptTestListener testListener = new CMISTestListener(printStream);
        CMISTestRunner runner = new CMISTestRunner();
        runner.setListener(testListener);

        // process test parameters
        String serviceUrl = req.getParameter("url");
        if (serviceUrl != null && serviceUrl.length() > 0)
        {
            runner.setServiceUrl(serviceUrl);
        }
        String userpass = req.getParameter("user");
        if (userpass != null && userpass.length() > 0)
        {
            runner.setUserPass(userpass);
        }
        String args = req.getParameter("args");
        if (args != null && args.length() > 0)
        {
            runner.setArguments(args);
        }
        String validate = req.getParameter("validate");
        if (validate != null && validate.length() > 0)
        {
            runner.setValidateResponse(Boolean.valueOf(validate));
        }
        String trace = req.getParameter("trace");
        if (trace != null && trace.length() > 0)
        {
            runner.setTraceReqRes(Boolean.valueOf(trace));
        }
        String match = req.getParameter("tests");
        if (match != null && match.length() > 0)
        {
            runner.setMatch(match);
        }
        
        // execute tests
        runner.execute();
    }

}
