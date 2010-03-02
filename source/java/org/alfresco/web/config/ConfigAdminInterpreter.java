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
package org.alfresco.web.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.springframework.extensions.config.ConfigDeployment;
import org.springframework.extensions.surf.util.I18NUtil;
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
            webClientConfigService.destroy();
            List <ConfigDeployment> configDeployments = webClientConfigService.initConfig();

            if (configDeployments != null)
            {
            	out.println("Web Client config has been reloaded\n");
            	
	            for (ConfigDeployment configDeployment : configDeployments)
	            {
	            	out.println(configDeployment.getName() + " ---> " + configDeployment.getDeploymentStatus());
	            }
        	}
            else
            {
            	out.println("No config reloaded");
            }
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