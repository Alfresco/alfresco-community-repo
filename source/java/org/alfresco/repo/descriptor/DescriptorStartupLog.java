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
package org.alfresco.repo.descriptor;

import java.security.Principal;
import java.util.Date;
import java.util.Map;

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

    /**
     * @param descriptorService  Descriptor Service
     */
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
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