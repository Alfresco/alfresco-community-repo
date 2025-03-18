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
package org.alfresco.tools;

import java.io.IOException;

/**
 * Initializes the full application context and then waits for a keypress to exit.
 * 
 * @author Derek Hulley
 */
public class Repository extends Tool
{
    protected @Override String getToolName()
    {
        return "Repository";
    }

    protected @Override ToolContext processArgs(String[] args) throws ToolException
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
            else if (args[i].equals("-verbose"))
            {
                context.setVerbose(true);
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

    protected @Override void displayHelp()
    {
        logError(
                "usage: repository [OPTIONS] \n" +
                        "\n" +
                        "Initialize the Alfresco application context, initiating any \n" +
                        "configured server beans (e.g. FTP server, etc). \n" +
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
            try
            {
                wait(3000L);
            }
            catch (InterruptedException e)
            {}
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
     * @param args
     *            not used
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
                {}
            }
        }
    }
}
