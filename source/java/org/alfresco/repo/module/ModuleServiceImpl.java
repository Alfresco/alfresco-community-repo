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
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.descriptor.DescriptorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * This component controls the execution of
 * {@link org.alfresco.repo.module.runtime.ModuleComponent module startup components}.
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
public class ModuleServiceImpl implements ModuleService
{
    /** Error messages **/
    private static final String ERR_UNABLE_TO_OPEN_MODULE_PROPETIES = "module.err.unable_to_open_module_properties";

    /** The classpath search path for module properties */
    private static final String MODULE_CONFIG_SEARCH_ALL = "classpath*:alfresco/module/*/module.properties";
    
    private static Log logger = LogFactory.getLog(ModuleServiceImpl.class);

    private ServiceRegistry serviceRegistry;
    private DescriptorService descriptorService;
    private AuthenticationComponent authenticationComponent;
    private ModuleComponentHelper moduleComponentHelper;
    /** A cache of module details by module ID */
    private Map<String, ModuleDetails> moduleDetailsById;    

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
        this.descriptorService = descriptorService;
        this.moduleComponentHelper.setDescriptorService(descriptorService);
    }

    /**
     * @param authenticationComponent allows execution as system user.
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
        this.moduleComponentHelper.setAuthenticationComponent(this.authenticationComponent);
    }

    /**
     * @param registryService the service used to persist component execution details.
     */
    public void setRegistryService(RegistryService registryService)
    {
        this.moduleComponentHelper.setRegistryService(registryService);
    }
    
    /**
     * @see ModuleComponentHelper#registerComponent(ModuleComponent)
     */
    public void registerComponent(ModuleComponent component)
    {
        this.moduleComponentHelper.registerComponent(component);
    }
    
    /**
     * @inheritDoc
     * 
     * @see ModuleComponentHelper#startModules()
     */
    public void startModules()
    {
        moduleComponentHelper.startModules();
    }
    
    /**
     * @inheritDoc
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
     * @inheritDoc
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
            
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
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
