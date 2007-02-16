/*
 * Copyright (C) 2005 Alfresco, Inc.
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
