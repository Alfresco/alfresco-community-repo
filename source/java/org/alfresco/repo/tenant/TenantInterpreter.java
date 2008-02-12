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
package org.alfresco.repo.tenant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.admin.BaseInterpreter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.core.io.ClassPathResource;

/**
 * An interactive console for Tenants.
 *
 */
public class TenantInterpreter extends BaseInterpreter
{
    // Service dependencies    
    
    private TenantAdminService tenantAdminService;
    
    private AuthenticationService authenticationService;

    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args)
    {
        runMain("tenantInterpreter");
    }
    
    
    protected boolean hasAuthority(String username)
    {
        // must be super "admin" for tenant administrator
        return ((username != null) && (username.equals(BaseInterpreter.DEFAULT_ADMIN)));
    }

    /**
     * Execute a single command using the BufferedReader passed in for any data needed.
     *
     * TODO: Use decent parser!
     *
     * @param line The unparsed command
     * @return The textual output of the command.
     */
    public String executeCommand(String line)
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
            String helpFile = I18NUtil.getMessage("tenant_console.help");
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
                return "Syntax Error, try 'help'.\n";
            }

            else if (command[1].equals("tenants"))
            {
                List<Tenant> tenants = tenantAdminService.getAllTenants();
                
                for (Tenant tenant : tenants)
                {
                    if (tenant.isEnabled())
                    {
                        String rootContentStoreDir = tenant.getRootContentStoreDir();
                        out.println("Enabled  - Tenant: " + tenant.getTenantDomain() + " (" + rootContentStoreDir + ")");
                    }
                }

                out.println("");
                
                for (Tenant tenant : tenants)
                {
                    if (! tenant.isEnabled())
                    {
                        String rootContentStoreDir = tenant.getRootContentStoreDir();
                        out.println("Disabled - Tenant: " + tenant.getTenantDomain() + " (" + rootContentStoreDir + ")");
                    }
                }                
            }
            
            else if (command[1].equals("tenant"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error, try 'help'.\n";
                }
                
                String tenantDomain = new String(command[2]).toLowerCase();
                Tenant tenant = tenantAdminService.getTenant(tenantDomain);
                
                String rootContentStoreDir = tenant.getRootContentStoreDir();
                if (tenant.isEnabled())
                {
                    out.println("Enabled - Tenant: " + tenant.getTenantDomain() + " (" + rootContentStoreDir + ")");
                }
                else
                {   
                    out.println("Disabled - Tenant: " + tenant.getTenantDomain() + " (" + rootContentStoreDir + ")");
                }
            }             
            
            else 
            {
                return "No such sub-command, try 'help'.\n";
            }
        }
            
        else if (command[0].equals("create"))
        {
            if ((command.length != 3) && (command.length != 4))
            {
                return "Syntax Error, try 'help'.\n";
            }
            
            String newTenant = new String(command[1]).toLowerCase();
            char[] tenantAdminRawPassword = new String(command[2]).toCharArray();
            String rootContentStoreDir = null;
            if (command.length == 4)
            {
                rootContentStoreDir = new String(command[3]);
            }
            
            tenantAdminService.createTenant(newTenant, tenantAdminRawPassword, rootContentStoreDir);
            
            out.println("created tenant: " + newTenant);      
        }
        
        else if (command[0].equals("import"))
        {
            if ((command.length != 3) && (command.length != 4))
            {
                return "Syntax Error, try 'help'.\n";
            }
            
            String newTenant = new String(command[1]).toLowerCase();       
            File directorySource = new File(command[2]);
            
            String rootContentStoreDir = null;
            if (command.length == 4)
            {
                rootContentStoreDir = new String(command[3]);
            }
            
            tenantAdminService.importTenant(newTenant, directorySource, rootContentStoreDir);
            
            out.println("imported tenant: " + newTenant);
        }  
        
        else if (command[0].equals("export"))
        {
            if (command.length != 3)
            {
                return "Syntax Error, try 'help'.\n";
            }
            
            String tenant = new String(command[1]).toLowerCase();      
            File directoryDestination = new File(command[2]);
            
            tenantAdminService.exportTenant(tenant, directoryDestination);
            
            out.println("exported tenant: " + tenant);      
        }
        
        // TODO - not fully working yet
        else if (command[0].equals("delete"))
        {
            if (command.length != 2)
            {
                return "Syntax Error, try 'help'.\n";
            }
            
            String tenantDomain = new String(command[1]).toLowerCase();
            
            tenantAdminService.deleteTenant(tenantDomain);
            out.println("Deleted tenant: " + tenantDomain);  
        }   
        
        else if (command[0].equals("enable"))
        {
            if (command.length != 2)
            {
                return "Syntax Error, try 'help'.\n";
            }
            
            String tenantDomain = new String(command[1]).toLowerCase();
        
            tenantAdminService.enableTenant(tenantDomain);
            out.println("Enabled tenant: " + tenantDomain);        
        }  
        
        else if (command[0].equals("disable"))
        {
            if (command.length != 2)
            {
                return "Syntax Error, try 'help'.\n";
            }
            
            String tenantDomain = new String(command[1]).toLowerCase();
        
            tenantAdminService.disableTenant(tenantDomain);
            out.println("Disabled tenant: " + tenantDomain);        
        } 
        
        else if (command[0].equals("changeAdminPassword"))
        {
            if (command.length != 3)
            {
                return "Syntax Error, try 'help'.\n";
            }
            
            String tenantDomain = new String(command[1]).toLowerCase();
            
            final String newPassword = new String(command[2]);        
            final String tenantAdminUsername = tenantService.getDomainUser(TenantService.ADMIN_BASENAME, tenantDomain);

            AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {
                            RetryingTransactionCallback<Object> txnWork = new RetryingTransactionCallback<Object>()
                            {
                                public Object execute() throws Exception
                                {
                                    authenticationService.setAuthentication(tenantAdminUsername, newPassword.toCharArray());
                                    return null;
                                }
                            };
                            return transactionService.getRetryingTransactionHelper().doInTransaction(txnWork);
                        }
                    }, tenantAdminUsername);           
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