/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.tenant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.alfresco.repo.admin.BaseInterpreter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.util.PropertyCheck;

/**
 * An interactive console for Tenants.
 *
 */
public class TenantInterpreter extends BaseInterpreter implements ApplicationContextAware, InitializingBean
{
    private static Log logger = LogFactory.getLog(TenantInterpreter.class);
    
    // Service dependencies    
    
    private ApplicationContext ctx;
    
    private TenantAdminService tenantAdminService;
    protected TenantService tenantService;
    private MutableAuthenticationService authenticationService;
    
    private String baseAdminUsername = null;
    
    private static final String WARN_MSG = "system.mt.warn.upgrade_mt_admin_context";
    
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    public void setAuthenticationService(MutableAuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setBaseAdminUsername(String baseAdminUsername)
    {
        this.baseAdminUsername = baseAdminUsername;
    }
    
    public String getBaseAdminUsername()
    {
        if (baseAdminUsername != null)
        {
            return baseAdminUsername;
        }
        return AuthenticationUtil.getAdminUserName();
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.ctx = applicationContext;
    }
    
    /**
     * Main entry point.
     */
    public static void main(String[] args)
    {
        runMain("tenantInterpreter");
    }
    
    public void afterPropertiesSet() throws Exception
    {
        // for upgrade/backwards compatibility with 3.0.x (mt-admin-context.xml)
        if (authorityService == null || baseAdminUsername == null)
        {
            logger.warn(I18NUtil.getMessage(WARN_MSG));
        }
        
        if (authorityService == null)
        {
            authorityService = (AuthorityService)ctx.getBean("AuthorityService");
        }
        
        PropertyCheck.mandatory(this, "TransactionService", transactionService);
        PropertyCheck.mandatory(this, "TenantService", tenantService);
    }
    
    protected boolean hasAuthority(String username)
    {
        // must be "super" admin for tenant administration
        return ((username != null) && (authorityService.isAdminAuthority(username)) && (! tenantService.isTenantUser(username)));
    }
    
    public String interpretCommand(final String line) throws IOException
    {
        String currentUserName = getCurrentUserName();
        if (hasAuthority(currentUserName))
        {
           RunAsWork<String> executeWork = new RunAsWork<String>()
           {
               public String doWork() throws Exception
               {
                   RetryingTransactionCallback<String> txnWork = new RetryingTransactionCallback<String>()
                   {
                       public String execute() throws Exception
                       {
                           return executeCommand(line);
                       }
                   };

                   // from Thor
                   RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                   txnHelper.setMaxRetries(1);
                   
                   return txnHelper.doInTransaction(txnWork);
               }
           };
           return AuthenticationUtil.runAs(executeWork, AuthenticationUtil.SYSTEM_USER_NAME);
        }
        else
        {
            return("Error: User '"+ currentUserName + "' not authorised");
        }
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
                        String contentRoot = tenant.getRootContentStoreDir();
                        out.println("Enabled  - Tenant: " + tenant.getTenantDomain() + " (" + contentRoot + ")");
                    }
                }

                out.println("");
                
                for (Tenant tenant : tenants)
                {
                    if (! tenant.isEnabled())
                    {
                        String contentRoot = tenant.getRootContentStoreDir();
                        out.println("Disabled - Tenant: " + tenant.getTenantDomain() + " (" + contentRoot + ")");
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
                
                String contentRoot = tenant.getRootContentStoreDir();
                if (tenant.isEnabled())
                {
                    out.println("Enabled - Tenant: " + tenant.getTenantDomain() + " (" + contentRoot + ")");
                }
                else
                {   
                    out.println("Disabled - Tenant: " + tenant.getTenantDomain() + " (" + contentRoot + ")");
                }
            }             
            
            else 
            {
                return "No such sub-command, try 'help'.\n";
            }
        }
            
        else if (command[0].equals("create"))
        {
            if ((command.length < 3) || (command.length > 5))
            {
                return "Syntax Error, try 'help'.\n";
            }
            
            String newTenant = new String(command[1]).toLowerCase();
            char[] tenantAdminRawPassword = new String(command[2]).toCharArray();
            String contentRoot = null;
            if (command.length >= 4)
            {
                contentRoot = new String(command[3]);
                if ("null".equals(contentRoot))
                {
                    contentRoot = null;
                }
            }
            
            String dbUrl = null;
            if (command.length >= 5)
            {
                // experimental (unsupported)
                dbUrl = new String(command[4]);
                if ("null".equals(dbUrl))
                {
                    dbUrl = null;
                }
            }
            
            tenantAdminService.createTenant(newTenant, tenantAdminRawPassword, contentRoot, dbUrl);
            
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
            
            String contentRoot = null;
            if (command.length == 4)
            {
                contentRoot = new String(command[3]);
            }
            
            tenantAdminService.importTenant(newTenant, directorySource, contentRoot);
            
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
            final String tenantAdminUsername = tenantService.getDomainUser(getBaseAdminUsername(), tenantDomain);

            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    authenticationService.setAuthentication(tenantAdminUsername, newPassword.toCharArray());
                    return null;
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