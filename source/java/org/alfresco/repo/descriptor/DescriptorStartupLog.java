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
package org.alfresco.repo.descriptor;

import java.security.Principal;
import java.util.Date;
import java.util.Properties;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.surf.util.I18NUtil;


/**
 * Provide a Repository Startup Log
 *  
 * @author davidc
 */
public class DescriptorStartupLog extends AbstractLifecycleBean
{
    // Logger
    private static final Log logger = LogFactory.getLog(DescriptorService.class);

    // Dependencies
    private DescriptorService descriptorService;
    private TenantService tenantService;
    private TransactionService transactionService;
    
    private final String SYSTEM_INFO_STARTUP = "system.info.startup";
    private final String SYSTEM_WARN_READONLY = "system.warn.readonly";

    /**
     * @param descriptorService  Descriptor Service
     */
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }
    
    /**
     * @param tenantService  Tenant Service
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * @param transactionService        service to tell about read-write mode
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Get Organisation from Principal
     * 
     * @param holderPrincipal
     * @return  organisation
     */
    private String getHolderOrganisation(Principal holderPrincipal)
    {
        String holder = null;
        if (holderPrincipal != null)
        {
            holder = holderPrincipal.getName();
            if (holder != null)
            {
                String[] properties = holder.split(",");
                for (String property : properties)
                {
                    String[] parts = property.split("=");
                    if (parts[0].equals("O"))
                    {
                        holder = parts[1];
                    }
                }
            }
        }
        
        return holder;
    }

    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        //
        // log output of VM stats
        //
        Properties properties = System.getProperties();
        String version = (properties.get("java.runtime.version") == null) ? "unknown" : (String)properties.get("java.runtime.version");
        long maxHeap = Runtime.getRuntime().maxMemory();
        float maxHeapMB = maxHeap / 1024l;
        maxHeapMB = maxHeapMB / 1024l;
        if (logger.isInfoEnabled())
        {
            logger.info(String.format("Alfresco JVM - v%s; maximum heap size %.3fMB", version, maxHeapMB));
        }
        if (logger.isWarnEnabled())
        {
            if (version.startsWith("1.2") || version.startsWith("1.3") || version.startsWith("1.4"))
            {
                logger.warn(String.format("Alfresco JVM - WARNING - v1.5 is required; currently using v%s", version));
            }
            if (maxHeapMB < 500)
            {
                logger.warn(String.format("Alfresco JVM - WARNING - maximum heap size %.3fMB is less than recommended 512MB", maxHeapMB));
            }
        }

        // Log License Descriptors (if applicable)
        LicenseDescriptor license = descriptorService.getLicenseDescriptor();
        if (license != null && logger.isInfoEnabled())
        {
            LicenseMode licenseMode = license.getLicenseMode();
            
            String msg = "Alfresco license: Mode " + licenseMode;
            
            String holder = getHolderOrganisation(license.getHolder());
            if (holder != null)
            {
                msg += " granted to " + holder;
            }
            
            Date validUntil = license.getValidUntil();
            
            if (validUntil != null)
            {
                Integer days = license.getDays();
                Integer remainingDays = license.getRemainingDays();
                
                msg += " limited to " + days + " days expiring " + validUntil + " (" + remainingDays + " days remaining).";
            }
            else
            {
                msg += " (does not expire).";
            }
            
            Long maxUsers = license.getMaxUsers();
            if (maxUsers != null)
            {
                msg += "  User limit is " + maxUsers + ".";
            }
            Long maxDocs = license.getMaxDocs();
            if (maxDocs != null)
            {
                msg += "  Document limit is " + maxDocs + ".";
            }
            
            /**
             * This is an important information logging since it logs the license
             */
            logger.info(msg);
        }
        
        // Log Repository Descriptors
        if (logger.isInfoEnabled())
        {
            Descriptor serverDescriptor = descriptorService.getServerDescriptor();
            Descriptor currentDescriptor = descriptorService.getCurrentRepositoryDescriptor();
            Descriptor installedRepoDescriptor = descriptorService.getInstalledRepositoryDescriptor();
            
            String serverEdition = serverDescriptor.getEdition();
            
            String currentVersion = currentDescriptor.getVersion();
            int currentSchemaVersion = currentDescriptor.getSchema();
            LicenseMode currentMode = currentDescriptor.getLicenseMode();
            
            String installedRepoVersion = installedRepoDescriptor.getVersion();
            int installedSchemaVersion = installedRepoDescriptor.getSchema();
            
            /**
             * Alfresco started 
             */
            Object[] params = new Object[] {
                    serverEdition,
                    currentMode != LicenseMode.TEAM ? "" : (" " + currentMode),     // only append TEAM
                    !tenantService.isEnabled() ? "" : (" Multi-Tenant"),
                    currentVersion, currentSchemaVersion, installedRepoVersion, installedSchemaVersion};
            logger.info(I18NUtil.getMessage(SYSTEM_INFO_STARTUP, params));
        }
        
        // Issue a warning if the system is in read-only mode
        if (!transactionService.getAllowWrite())
        {
            logger.warn(I18NUtil.getMessage(SYSTEM_WARN_READONLY));
        }
    }
    
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }
}