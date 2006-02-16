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

import java.util.Map;

import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;


/**
 * Provide a Repository Startup Log
 *  
 * @author davidc
 */
public class DescriptorStartupLog implements ApplicationListener
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
     * @param event
     */
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (event instanceof ContextRefreshedEvent)
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
            
            // Log Repository Descriptors
            if (logger.isInfoEnabled())
            {
                Descriptor serverDescriptor = descriptorService.getServerDescriptor();
                Descriptor repoDescriptor = descriptorService.getRepositoryDescriptor();
                String serverEdition = serverDescriptor.getEdition();
                String serverVersion = serverDescriptor.getVersion();
                String repoVersion = repoDescriptor.getVersion();
                int schemaVersion = repoDescriptor.getSchema();
                logger.info(String.format("Alfresco started (%s) - v%s; repository v%s; schema %d",
                        serverEdition, serverVersion, repoVersion, schemaVersion));
            }
        }
    }
    
}
