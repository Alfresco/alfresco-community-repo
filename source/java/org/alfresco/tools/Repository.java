/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.tools;

import java.io.IOException;

/**
 * Initializes the full application context and then waits for a
 * keypress to exit.
 * 
 * @author Derek Hulley
 */
public class Repository extends Tool
{
    protected @Override
    String getToolName()
    {
        return "Repository";
    }

    protected @Override
    ToolContext processArgs(String[] args) throws ToolException
    {
        ToolContext context = new ToolContext();
        context.setLogin(true);

        int i = 0;
        while (i < args.length)
        {
            if (args[i].equals("-h") || args[i].equals("-help"))
            {
                context.setHelp(true);
                break;
            }
            else if (args[i].equals("-user"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolArgumentException("The value <user> for the option -user must be specified");
                }
                context.setUsername(args[i]);
            }
            else if (args[i].equals("-pwd"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolArgumentException("The value <password> for the option -pwd must be specified");
                }
                context.setPassword(args[i]);
            }
            else
            {
                throw new ToolArgumentException("Unknown option " + args[i] + ".  Use -help for options.");
            }

            // next argument
            i++;
        }

        return context;
    }

    protected @Override
    void displayHelp()
    {
        logError(
                "usage: repository [OPTIONS] \n" +
                "\n" +
                "Initialize the Alfresco application context, initiating any \n" +
                "configured server beans (e.g. CIFS server, FTP server, etc). \n" +
                "\n" +
                "Options: \n" +
                " -h -help         Display this help \n" +
                " -user USER       Login as USER \n" +
                " -pwd PASSWORD    Use PASSWORD to login");        
    }

    @Override
    protected synchronized int execute() throws ToolException
    {
        try
        {
            System.out.println("\nRepository initialized.\nPress ENTER to exit...");
            System.in.read();
            System.out.println("\nShutting down the repository.");
            // start the ticker
            new ShutdownNotifierThread().start();
            try { wait(3000L); } catch (InterruptedException e) {}
        }
        catch (IOException e)
        {
            // just ignore
        }
        
        return 0;
    }
    
    /**
     * Start the repository and wait for a keypress to stop
     * 
     * @param args not used
     */
    public static void main(String[] args)
    {
        new Repository().start(args);
    }
    
    private static class ShutdownNotifierThread extends Thread
    {
        private ShutdownNotifierThread()
        {
            this.setDaemon(true);
        }
        @Override
        public synchronized void run()
        {
            while (true)
            {
                System.out.print('.');
                try
                {
                    wait(500L);
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    }
}
