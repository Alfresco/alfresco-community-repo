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
package org.alfresco.repo.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.namespace.QName;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * An interactive console for (first cut) Repository Admin Service / API.
 *
 */
public class RepoAdminInterpreter extends BaseInterpreter
{
    // dependencies
    private RepoAdminService repoAdminService;   
      
    
    public void setRepoAdminService(RepoAdminService repoAdminService)
    {
        this.repoAdminService = repoAdminService;
    }

    
    /**
     * 
     */
    public static BaseInterpreter getConsoleBean(ApplicationContext context)
    {
        return (RepoAdminInterpreter)context.getBean("repoAdminInterpreter");
    }

    protected boolean hasAuthority(String username)
    {
        // must be an "admin" for repository administration
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
            String helpFile = I18NUtil.getMessage("repoadmin_console.help");
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

        else if (command[0].equals("show"))
        {
            if (command.length < 2)
            {
                return "Syntax Error.\n";
            }

            else if (command[1].equals("file"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.\n";
                }
                
                ClassPathResource file = new ClassPathResource(command[2]);
                InputStream fileStream = file.getInputStream();
                
                if (fileStream != null)
                {
                    byte[] fileBytes = new byte[500];
                    try
                    {
                        int read = fileStream.read(fileBytes);
                        while (read != -1)
                        {
                            bout.write(fileBytes, 0, read);
                            read = fileStream.read(fileBytes);
                        }
                    }
                    finally
                    {
                        fileStream.close();
                    }
                }
                else
                {
                    out.println("No matching file found: " + command[2]);
                }
                    
                out.println();
            }
            
            else if (command[1].equals("file-list"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.\n";
                }
                
                // note: classpath should be in form path1/path2/path3/name*
                // wildcard * is allowed, e.g. abc/def/workflow-messages*.properties               
                String pattern = "classpath*:" + command[2];                
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
               
                Resource[] resources = resolver.getResources(pattern);
                ArrayList<String> names = new ArrayList<String>();
           
                if (resources != null)
                {
                    for (int i = 0; i < resources.length; i++)
                    {
                        String filename = resources[i].getFilename();
                        if (! names.contains(filename))
                        {
                            out.println("resource: " + filename + ", url: " + resources[i].getURL());
                            names.add(filename);
                        }
                    }
                }
                else
                {
                    out.println("No matching files found: " + command[2]);
                }
            }            
            
            else if (command[1].equals("models"))
            {
                List<RepoModelDefinition> models = repoAdminService.getModels();
                
                if ((models != null) && (models.size() > 0))
                {
                    for (RepoModelDefinition model : models)
                    {
                        out.println(model.toString());
                    }
                }
                else
                {
                    out.println("No additional models have been deployed to the Alfresco Repository");
                }
            }
            
            else if (command[1].equals("messages"))
            {
                List<String> messageResources = repoAdminService.getMessageBundles();
                
                if ((messageResources != null) && (messageResources.size() > 0))
                {
                    for (String messageResourceName : messageResources)
                    {
                        out.println("message resource bundle: " + messageResourceName);
                    }
                }
                else
                {
                    out.println("No additional messages resource bundles have been deployed to the Alfresco Repository");
                }
            }

            else 
            {
                return "No such sub-command, try 'help'.\n";
            }
        }
            
        else if (command[0].equals("deploy"))
        {
            if (command.length != 3)
            {
                return "Syntax Error.\n";
            }
            
            if (command[1].equals("model"))
            {
                ClassPathResource file = new ClassPathResource(command[2]);
                
                InputStream fileStream = file.getInputStream();
                
                String modelFileName = file.getFilename();
                repoAdminService.deployModel(fileStream, modelFileName);
                out.println("Model deployed: " + modelFileName);
            }
            
            else if (command[1].equals("messages"))
            {                           
                String bundleBasePath = command[2];
                String bundleBaseName = repoAdminService.deployMessageBundle(bundleBasePath);
                out.println("Message resource bundle deployed: " + bundleBaseName);              
            }
     
            else 
            {
                return "No such sub-command, try 'help'.\n";
            }                    
        }
        
        else if (command[0].equals("activate"))
        {
            if (command.length != 3)
            {
                return "Syntax Error.\n";
            }
     
            else if (command[1].equals("model"))
            {            
                String modelFileName = command[2];
                QName modelQName = repoAdminService.activateModel(modelFileName);
                out.println("Model activated: " + modelFileName + " [" + modelQName + "]");
            }
        }
        
        else if (command[0].equals("deactivate"))
        {
            if (command.length != 3)
            {
                return "Syntax Error.\n";
            }
     
            else if (command[1].equals("model"))
            {            
                String modelFileName = command[2];
                QName modelQName = repoAdminService.deactivateModel(modelFileName);
                out.println("Model deactivated: " + modelFileName + " [" + modelQName + "]");
            }
        }
        
        else if (command[0].equals("reload"))
        {
            if (command.length != 3)
            {
                return "Syntax Error.\n";
            }
            
            else if (command[1].equals("messages"))
            {            
                String bundleBaseName = command[2];
                repoAdminService.reloadMessageBundle(bundleBaseName);
                out.println("Message resource bundle reloaded: " + bundleBaseName);
            }

            else 
            {
                return "No such sub-command, try 'help'.\n";
            }            
        }
        
        else if (command[0].equals("undeploy"))
        {
            if (command.length != 3)
            {
                return "Syntax Error.\n";
            }
            
            if (command[1].equals("model"))
            {         
                String modelFileName = command[2];
                QName modelQName = repoAdminService.undeployModel(modelFileName);
                out.println("Model undeployed: " + modelFileName + " [" + modelQName + "]");
                
                out.println("");
                out.println("Remaining models:");
                out.print(executeCommand("show models"));
            }
            
            else if (command[1].equals("messages"))
            {            
                String bundleBaseName = command[2];
                repoAdminService.undeployMessageBundle(bundleBaseName);
                out.println("Message resource bundle undeployed: " + bundleBaseName);
                
                out.println("");
                out.println("Remaining message resource bundles:");
                out.print(executeCommand("show messages"));
            }

            else 
            {
                return "No such sub-command, try 'help'.\n";
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