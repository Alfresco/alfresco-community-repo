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
package org.alfresco.repo.descriptor;

import java.security.Principal;
import java.util.Date;
import java.util.Map;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;


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
        Map properties = System.getProperties();
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
            String subject = license.getSubject();
            String msg = "Alfresco license: " + subject;
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
                
                msg += " limited to " + days + " days expiring " + validUntil + " (" + remainingDays + " days remaining)";
            }
            else
            {
                msg += " (does not expire)";
            }
            
            
            logger.info(msg);
        }
        
        // Log Repository Descriptors
        if (logger.isInfoEnabled())
        {
            Descriptor serverDescriptor = descriptorService.getServerDescriptor();
            Descriptor installedRepoDescriptor = descriptorService.getInstalledRepositoryDescriptor();
            String serverEdition = serverDescriptor.getEdition();
            
            if (tenantService.isEnabled())
            {
                serverEdition = serverEdition + " - Multi-Tenant";
            }
            
            String serverVersion = serverDescriptor.getVersion();
            int serverSchemaVersion = serverDescriptor.getSchema();
            String installedRepoVersion = installedRepoDescriptor.getVersion();
            int installedSchemaVersion = installedRepoDescriptor.getSchema();
            logger.info(String.format("Alfresco started (%s): Current version %s schema %d - Installed version %s schema %d",
               serverEdition, serverVersion, serverSchemaVersion, installedRepoVersion, installedSchemaVersion));
        }
    }

    
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }
    
}