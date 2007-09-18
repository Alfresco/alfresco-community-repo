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
package org.alfresco.web.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.admin.BaseInterpreter;
import org.alfresco.repo.config.xml.RepoXMLConfigService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * A simple interactive console for (first cut) Web Client Config Admin.
 *
 */
public class ConfigAdminInterpreter extends BaseInterpreter
{
    // dependencies
    private RepoXMLConfigService webClientConfigService;
    
    public void setRepoXMLConfigService(RepoXMLConfigService webClientConfigService)
    {
        this.webClientConfigService = webClientConfigService;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args)
    {
    	ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"classpath:alfresco/application-context.xml","classpath:alfresco/web-client-application-context.xml"});
        runMain(context, "webClientConfigAdminInterpreter");
    }
    
    protected boolean hasAuthority(String username)
    {
        return ((username != null) && (tenantService.getBaseNameUser(username).equals(BaseInterpreter.DEFAULT_ADMIN)));
    }

    /**
     * Execute a single command using the BufferedReader passed in for any data needed.
     *
     * TODO: Use decent parser!
     *
     * @param line The unparsed command
     * @return The textual output of the command.
     */
    protected String executeCommand(String line)
        throws IOException
    {
        String[] command = line.split(" ");
        if (command.length == 0)
        {
            command = new String[1];
            command[0] = line;
        }

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);

        // repeat last command?
        if (command[0].equals("r"))
        {
            if (lastCommand == null)
            {
                return "No command entered yet.";
            }
            return "repeating command " + lastCommand + "\n\n" + executeCommand(lastCommand);
        }

        // remember last command
        lastCommand = line;

        // execute command
        if (command[0].equals("help"))
        {
            String helpFile = I18NUtil.getMessage("configadmin_console.help");
            ClassPathResource helpResource = new ClassPathResource(helpFile);
            byte[] helpBytes = new byte[500];
            InputStream helpStream = helpResource.getInputStream();
            try
            {
                int read = helpStream.read(helpBytes);
                while (read != -1)
                {
                    bout.write(helpBytes, 0, read);
                    read = helpStream.read(helpBytes);
                }
            }
            finally
            {
                helpStream.close();
            }
        }
        
        else if (command[0].equals("reload"))
        {
            if (command.length > 1)
            {
                return "Syntax Error.\n";
            }
            
            // destroy and re-initialise config service
            webClientConfigService.reset();
            
            out.println("Web Client config has been reloaded");
        }
        
        else
        {
            return "No such command, try 'help'.\n";
        }

        out.flush();
        String retVal = new String(bout.toByteArray());
        out.close();
        return retVal;
    }
}