/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.cmis.rest.test;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.apache.chemistry.tck.atompub.TCKMessageWriter;
import org.apache.chemistry.tck.atompub.tools.TCKRunner;
import org.apache.chemistry.tck.atompub.tools.TCKRunnerOptions;

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
        // setup default values
        Properties properties = new Properties();
        properties.put(TCKRunnerOptions.PROP_VALIDATE, "false");
        properties.put(TCKRunnerOptions.PROP_FAIL_ON_VALIDATION_ERROR, "false");
        properties.put(TCKRunnerOptions.PROP_TRACE_REQUESTS, "false");

        // apply form provided values
        TCKRunnerOptions options = new TCKRunnerOptions(properties);
        String[] names = req.getParameterNames();
        for (String name : names)
        {
            properties.setProperty(name, req.getParameter(name));
        }
        
        // execute tck
        TCKRunner runner = new TCKRunner(options, new ResponseMessageWriter(res));
        runner.execute(properties);
    }

    private static class ResponseMessageWriter implements TCKMessageWriter
    {
        private PrintStream printStream;
        
        public ResponseMessageWriter(WebScriptResponse res) throws IOException
        {
            printStream = new PrintStream(res.getOutputStream(), true, "UTF-8");
        }
        
        public void info(String message)
        {
            printStream.println("INFO  " + message);
        }

        public void trace(String message)
        {
            printStream.println("TRACE " + message);
        }

        public void warn(String message)
        {
            printStream.println("WARN  " + message);
        }
    }
}
