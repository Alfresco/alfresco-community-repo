/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.module;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.registry.RegistryService;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.util.VersionNumber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * This component controls the execution of
 * {@link org.alfresco.repo.module.ModuleComponent module startup components}.
 * <p/>
 * All required startup executions are performed in a single transaction, so this
 * component guarantees that the module initialization is consistent.  Module components are
 * executed in dependency order <i>only</i>.  The version numbering is not to be used
 * for ordering purposes.
 * <p/>
 * Afterwards, execution details are persisted in the
 * {@link org.alfresco.repo.admin.registry.RegistryService service registry} to be used when the
 * server starts up again.
 *
 * @author Roy Wetherall
 * @author Derek Hulley
 * @since 2.0
 */
public class ModuleServiceImpl implements ApplicationContextAware, ModuleService
{
    /** Error messages **/
    private static final String ERR_UNABLE_TO_OPEN_MODULE_PROPETIES = "module.err.unable_to_open_module_properties";

    /** The classpath search path for module properties */
    private static final String MODULE_CONFIG_SEARCH_ALL = "classpath*:alfresco/module/*/module.properties";
    
    private static Log logger = LogFactory.getLog(ModuleServiceImpl.class);

    private ServiceRegistry serviceRegistry;
    private ModuleComponentHelper moduleComponentHelper;
    /** A cache of module details by module ID */
    private Map<String, ModuleDetails> moduleDetailsById;    

    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    
    /** Default constructor */
    public ModuleServiceImpl()
    {
        moduleComponentHelper = new ModuleComponentHelper();
        moduleComponentHelper.setModuleService(this);
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
        this.moduleComponentHelper.setServiceRegistry(this.serviceRegistry);
    }

    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.moduleComponentHelper.setDescriptorService(descriptorService);
    }

    /**
     * @param registryService the service used to persist component execution details.
     */
    public void setRegistryService(RegistryService registryService)
    {
        this.moduleComponentHelper.setRegistryService(registryService);
    }
    
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.moduleComponentHelper.setTenantAdminService(tenantAdminService);
    }
    
    /**
     * @throws UnsupportedOperationException This feature was never active and cannot be used (ALF-19207)
     */
    public void setApplyToTenants(boolean applyToTenants)
    {
        throw new UnsupportedOperationException("Applying modules to individual tenants is unsupported. See ALF-19207: MT module startup does not work");
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.resolver = applicationContext;
    }

    /**
     * @see ModuleComponentHelper#registerComponent(ModuleComponent)
     */
    public void registerComponent(ModuleComponent component)
    {
        this.moduleComponentHelper.registerComponent(component);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see ModuleComponentHelper#startModules()
     */
    public void startModules()
    {
        moduleComponentHelper.startModules();
    }

    /**
     * {@inheritDoc}
     * 
     * @see ModuleComponentHelper#shutdownModules()
     */
    public void shutdownModules()
    {
        moduleComponentHelper.shutdownModules();
    }
    
    /**
     * {@inheritDoc}
     */
    public ModuleDetails getModule(String moduleId)
    {
        cacheModuleDetails();
        // Get the details of the specific module
        ModuleDetails details = moduleDetailsById.get(moduleId);
        // Done
        return details;
    }

    /**
     * {@inheritDoc}
     */
    public List<ModuleDetails> getAllModules()
    {
        cacheModuleDetails();
        Collection<ModuleDetails> moduleDetails = moduleDetailsById.values();
        // Make a copy to avoid modification of cached data by clients (and to satisfy API)
        List<ModuleDetails> result = new ArrayList<ModuleDetails>(moduleDetails);
        // Done
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public List<ModuleDetails> getMissingModules()
    {
        cacheModuleDetails();
        
        // Get the IDs of all modules from the registry
        Collection<String> moduleIds = moduleComponentHelper.getRegistryModuleIDs();
        
        List<ModuleDetails> result = new ArrayList<ModuleDetails>();
        
        //Check for missing modules
        for (String moduleId : moduleIds)
        {
            ModuleDetails moduleDetails = getModule(moduleId);
            if (moduleDetails == null)
            {
                // Get the specifics of the missing module and add them to the list.
                ModuleVersionNumber currentVersion = moduleComponentHelper.getVersion(moduleId);
                
                ModuleDetails newModuleDetails = new ModuleDetailsImpl(moduleId, currentVersion, "", "");
                
                result.add(newModuleDetails);
            }
        }
        return result;
    }

    /**
     * Ensure that the {@link #moduleDetailsById module details} are populated.
     * <p/>
     * TODO: We will have to avoid caching or add context listening if we support reloading
     *       of beans one day.
     */
    private synchronized void cacheModuleDetails()
    {
        if (moduleDetailsById != null)
        {
            // There is nothing to do
            return;
        }
        try
        {
            moduleDetailsById = new HashMap<String, ModuleDetails>(13);
            
            Resource[] resources = resolver.getResources(MODULE_CONFIG_SEARCH_ALL);
            
            // Read each resource
            for (Resource resource : resources)
            {
                try
                {
                    InputStream is = new BufferedInputStream(resource.getInputStream());
                    Properties properties = new Properties();
                    properties.load(is);
                    ModuleDetails details = new ModuleDetailsImpl(properties);
                    moduleDetailsById.put(details.getId(), details);
                }
                catch (Throwable e)
                {
                    logger.error("Unable to use module information.",e);
                    throw AlfrescoRuntimeException.create(e, ERR_UNABLE_TO_OPEN_MODULE_PROPETIES, resource);
                }
            }
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Failed to retrieve module information", e);
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Found " + moduleDetailsById.size() + " modules: \n" +
                    "   Modules: " + moduleDetailsById);
        }
    }
}
