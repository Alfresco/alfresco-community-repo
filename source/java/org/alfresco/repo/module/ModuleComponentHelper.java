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
package org.alfresco.repo.module;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.registry.RegistryKey;
import org.alfresco.repo.admin.registry.RegistryService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.module.ModuleDependency;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.VersionNumber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class to split up some of the code for managing module components.  This class handles
 * the execution of the module components.
 * 
 * @author Derek Hulley
 */
public class ModuleComponentHelper
{
    public static final String URI_MODULES_1_0 = "http://www.alfresco.org/system/modules/1.0";
    private static final String REGISTRY_PATH_MODULES = "modules";
    private static final String REGISTRY_PROPERTY_INSTALLED_VERSION = "installedVersion";
    private static final String REGISTRY_PROPERTY_CURRENT_VERSION = "currentVersion";
    private static final String REGISTRY_PATH_COMPONENTS = "components";
    private static final String REGISTRY_PROPERTY_EXECUTION_DATE = "executionDate";
    
    private static final String MSG_FOUND_MODULES = "module.msg.found_modules";
    private static final String MSG_STARTING = "module.msg.starting";
    private static final String MSG_INSTALLING = "module.msg.installing";
    private static final String MSG_UPGRADING = "module.msg.upgrading";
    private static final String MSG_DEPENDENCIES = "module.msg.dependencies";
    private static final String MSG_MISSING = "module.msg.missing";
    private static final String WARN_NO_INSTALL_VERSION = "module.warn.no_install_version";
    private static final String ERR_MISSING_DEPENDENCY = "module.err.missing_dependency";
    private static final String ERR_UNSUPPORTED_REPO_VERSION = "module.err.unsupported_repo_version";
    private static final String ERR_NO_DOWNGRADE = "module.err.downgrading_not_supported";
    private static final String ERR_COMPONENT_ALREADY_REGISTERED = "module.err.component_already_registered";
    private static final String ERR_COMPONENT_IN_MISSING_MODULE = "module.err.component_in_missing_module";
    private static final String ERR_ORPHANED_COMPONENTS = "module.err.orphaned_components";
    
    private static Log logger = LogFactory.getLog(ModuleComponentHelper.class);
    private static Log loggerService = LogFactory.getLog(ModuleServiceImpl.class);
    
    private ServiceRegistry serviceRegistry;
    private DescriptorService descriptorService;
    private RegistryService registryService;
    private ModuleService moduleService;
    private TenantAdminService tenantAdminService;
    private Map<String, Map<String, ModuleComponent>> componentsByNameByModule;

    /** Default constructor */
    public ModuleComponentHelper()
    {
        componentsByNameByModule = new HashMap<String, Map<String, ModuleComponent>>(7);
    }

    /**
     * @param serviceRegistry provides access to the service APIs
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * @param descriptorService gives access to the current repository version
     */
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    /**
     * @param registryService the service used to persist component execution details.
     */
    public void setRegistryService(RegistryService registryService)
    {
        this.registryService = registryService;
    }

    /**
     * @param moduleService the service from which to get the available modules.
     */
    public void setModuleService(ModuleService moduleService)
    {
        this.moduleService = moduleService;
    }
    
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }

    /**
     * Add a managed module component to the registry of components.  These will be controlled
     * by the {@link #startModules()} method.
     * 
     * @param component a module component to be executed
     */
    public synchronized void registerComponent(ModuleComponent component)
    {
        String moduleId = component.getModuleId();
        String name = component.getName();
        // Get the map of components for the module
        Map<String, ModuleComponent> componentsByName = componentsByNameByModule.get(moduleId);
        if (componentsByName == null)
        {
            componentsByName = new HashMap<String, ModuleComponent>(11);
            componentsByNameByModule.put(moduleId, componentsByName);
        }
        // Check if the component has already been registered
        if (componentsByName.containsKey(name))
        {
            throw AlfrescoRuntimeException.create(ERR_COMPONENT_ALREADY_REGISTERED, name, moduleId);
        }
        // Add it
        componentsByName.put(name, component);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Registered component: " + component);
        }
    }
    
    /**
     * @return Returns the map of components keyed by name.  The map could be empty but
     *      will never be <tt>null</tt>.
     */
    private synchronized Map<String, ModuleComponent> getComponents(String moduleId)
    {
        Map<String, ModuleComponent> componentsByName = componentsByNameByModule.get(moduleId);
        if (componentsByName != null)
        {
            // Done
            return componentsByName;
        }
        else
        {
            // Done
            return Collections.emptyMap();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized void startModules()
    {
        // Check properties
        PropertyCheck.mandatory(this, "serviceRegistry", serviceRegistry);
        PropertyCheck.mandatory(this, "registryService", registryService);
        PropertyCheck.mandatory(this, "moduleService", moduleService);
        PropertyCheck.mandatory(this, "tenantAdminService", tenantAdminService);
        /*
         * Ensure transactionality and the correct authentication
         */
    	AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
    		public Object doWork() throws Exception
            {
	    		try
	    		{
		            TransactionService transactionService = serviceRegistry.getTransactionService();
		
		            // Get all the modules
		            List<ModuleDetails> modules = moduleService.getAllModules();
		            loggerService.info(I18NUtil.getMessage(MSG_FOUND_MODULES, modules.size()));
		            
		            // Process each module in turn.  Ordering is not important.
		            final Map<String, Set<ModuleComponent>> mapExecutedComponents = new HashMap<String, Set<ModuleComponent>>(1);
		            final Map<String, Set<String>> mapStartedModules = new HashMap<String, Set<String>>(1);

		            // Note: for system bootstrap this will be the default domain, else tenant domain for tenant create/import
		            final String tenantDomainCtx = tenantAdminService.getCurrentUserDomain();
		            
		            mapExecutedComponents.put(tenantDomainCtx, new HashSet<ModuleComponent>(10));
		            mapStartedModules.put(tenantDomainCtx, new HashSet<String>(2));
		            
		            final List<Tenant> tenants;
		            if (tenantAdminService.isEnabled() && (tenantDomainCtx.equals(TenantService.DEFAULT_DOMAIN)))
		            {
		            	tenants = tenantAdminService.getAllTenants();
		            	for (Tenant tenant : tenants)
		                {
		                    mapExecutedComponents.put(tenant.getTenantDomain(), new HashSet<ModuleComponent>(10));
		                    mapStartedModules.put(tenant.getTenantDomain(), new HashSet<String>(2));
		                }
		            }
		            else
		            {
		            	tenants = null;
		            }
		            
		            for (final ModuleDetails module : modules)
		            {
		                RetryingTransactionCallback<Object> startModuleWork = new RetryingTransactionCallback<Object>()
		                {
		                    public Object execute() throws Exception
		                    {
		                        startModule(module, mapStartedModules.get(tenantDomainCtx), mapExecutedComponents.get(tenantDomainCtx));
		                        
		                        if (tenants != null)
		                        {
		                            for (Tenant tenant : tenants)
		                            {
		                            	final String tenantDomain = tenant.getTenantDomain();
		                            	AuthenticationUtil.runAs(new RunAsWork<Object>()
		                                {
		                            		public Object doWork() throws Exception
		                                    {
		                            			startModule(module, mapStartedModules.get(tenantDomain), mapExecutedComponents.get(tenantDomain));
		                            			return null;
		                                    }
		                                }, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
		                            }
		                        }
		                        
		                        return null;
		                    }
		                };
                        transactionService.getRetryingTransactionHelper().doInTransaction(startModuleWork, transactionService.isReadOnly());
		            }
		            
		            // Check for missing modules.
		            checkForMissingModules();
		            
		            if (tenants != null)
		            {
		                for (Tenant tenant : tenants)
		                {
		                	AuthenticationUtil.runAs(new RunAsWork<Object>()
		                    {
		                		public Object doWork() throws Exception
		                        {
		                		    checkForMissingModules();
		                			return null;
		                        }
		                    }, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
		                }
		            }
		            
		            // Check that all components where executed, or considered for execution
		            checkForOrphanComponents(mapExecutedComponents.get(tenantDomainCtx));
		            
		            if (tenants != null)
		            {
		                for (Tenant tenant : tenants)
		                {
		                	final String tenantDomain = tenant.getTenantDomain();
		                	AuthenticationUtil.runAs(new RunAsWork<Object>()
		                    {
		                		public Object doWork() throws Exception
		                        {
		                			checkForOrphanComponents(mapExecutedComponents.get(tenantDomain));
		                			return null;
		                        }
		                    }, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
		                }
		            }
		        }
		        catch (Throwable e)
		        {
		            throw new AlfrescoRuntimeException("Failed to start modules", e);
		        }
		        
		        return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Checks that all components have been executed or considered for execution.
     * @param executedComponents
     */
    private void checkForOrphanComponents(Set<ModuleComponent> executedComponents)
    {
        Set<ModuleComponent> missedComponents = new HashSet<ModuleComponent>(executedComponents);
        
        // Iterate over each module registered by components
        for (Map.Entry<String, Map<String, ModuleComponent>> entry : componentsByNameByModule.entrySet())
        {
            String moduleId = entry.getKey();
            Map<String, ModuleComponent> componentsByName = entry.getValue();
            // Iterate over each component registered against the module ID
            for (Map.Entry<String, ModuleComponent> entryInner : componentsByName.entrySet())
            {
                String componentName = entryInner.getKey();
                ModuleComponent component = entryInner.getValue();
                // Check if it has been executed
                if (executedComponents.contains(component))
                {
                    // It was executed, so remove it from the missed components set
                    missedComponents.remove(component);
                }
                else
                {
                    String msg = I18NUtil.getMessage(
                            ERR_COMPONENT_IN_MISSING_MODULE,
                            componentName, moduleId);
                    logger.error(msg);
                }
            }
        }
        // Dump if there were orphans
        if (missedComponents.size() > 0)
        {
            throw AlfrescoRuntimeException.create(ERR_ORPHANED_COMPONENTS, missedComponents.size());
        }
    }
    
    /**
     * Checks to see if there are any modules registered as installed that aren't in the
     * list of modules taken from the WAR.
     * <p>
     * Currently, the behaviour specified is that a warning is generated only.
     */
    private void checkForMissingModules()
    {
        // Get the IDs of all modules from the registry
        RegistryKey moduleKeyAllIds = new RegistryKey(
                ModuleComponentHelper.URI_MODULES_1_0,
                REGISTRY_PATH_MODULES, null);
        Collection<String> moduleIds = registryService.getChildElements(moduleKeyAllIds);
        
        // Check that each module is present in the distribution
        for (String moduleId : moduleIds)
        {
            ModuleDetails moduleDetails = moduleService.getModule(moduleId);
            if (moduleDetails != null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Installed module found in distribution: " + moduleId);
                }
            }
            else
            {
                // Get the specifics of the missing module
                RegistryKey moduleKeyCurrentVersion = new RegistryKey(
                        ModuleComponentHelper.URI_MODULES_1_0,
                        REGISTRY_PATH_MODULES, moduleId, REGISTRY_PROPERTY_CURRENT_VERSION);
                VersionNumber versionCurrent = (VersionNumber) registryService.getProperty(moduleKeyCurrentVersion);
                // The module is missing, so warn
                loggerService.warn(I18NUtil.getMessage(MSG_MISSING, moduleId, versionCurrent));
            }
        }
    }
    
    /**
     * Copies, where necessary, the module registry details from the alias details
     * and removes the alias details.
     */
    private void renameModule(ModuleDetails module)
    {
        String moduleId = module.getId();
        List<String> moduleAliases = module.getAliases();
        
        // Get the IDs of all modules from the registry
        RegistryKey moduleKeyAllIds = new RegistryKey(
                ModuleComponentHelper.URI_MODULES_1_0,
                REGISTRY_PATH_MODULES, null);
        Collection<String> registeredModuleIds = registryService.getChildElements(moduleKeyAllIds);
        
        // Firstly, is the module installed?
        if (registeredModuleIds.contains(moduleId))
        {
            // It is there, so we do nothing
            return;
        }
        // Check if any of the registered modules are on the alias list
        for (String moduleAlias : moduleAliases)
        {
            // Is this alias registered?
            if (!registeredModuleIds.contains(moduleAlias))
            {
                // No alias registered
                continue;
            }
            // We found an alias and have to rename it to the new module ID
            RegistryKey moduleKeyNew = new RegistryKey(
                    ModuleComponentHelper.URI_MODULES_1_0,
                    REGISTRY_PATH_MODULES, moduleId, null);
            RegistryKey moduleKeyOld = new RegistryKey(
                    ModuleComponentHelper.URI_MODULES_1_0,
                    REGISTRY_PATH_MODULES, moduleAlias, null);
            // Copy it all
            registryService.copy(moduleKeyOld, moduleKeyNew);
            // Remove the source
            registryService.delete(moduleKeyOld);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("Moved old module alias to new module ID: \n" +
                        "   Alias:  " + moduleAlias + "\n" +
                        "   Module: " + moduleId);
            }
            break;
        }
    }
    
    /**
     * Does the actual work without fussing about transactions and authentication.
     * Module dependencies will be started first, but a module will only be started
     * once.
     * 
     * @param module                the module to start
     * @param startedModules        the IDs of modules that have already started
     * @param executedComponents    keep track of the executed components
     */
    private void startModule(ModuleDetails module, Set<String> startedModules, Set<ModuleComponent> executedComponents)
    {
        String moduleId = module.getId();
        VersionNumber moduleNewVersion = module.getVersion();
        
        // Double check whether we have done this module already
        if (startedModules.contains(moduleId))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Module '" + module + "' already started");
            }
            return;
        }
        
        // Start dependencies
        List<ModuleDependency> moduleDependencies = module.getDependencies();
        for (ModuleDependency moduleDependency : moduleDependencies)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Module '" + module + "' depends on: " + moduleDependency);
            }
            // Get the dependency
            String moduleDependencyId = moduleDependency.getDependencyId();
            ModuleDetails moduleDependencyDetails = moduleService.getModule(moduleDependencyId);
            // Check that it is there
            if (moduleDependencyDetails == null)
            {
                // The dependency is not there
                // List required dependencies
                StringBuilder sb = new StringBuilder(128);
                for (ModuleDependency dependency : moduleDependencies)
                {
                    sb.append("\n").append(dependency);
                }
                String msg = I18NUtil.getMessage(
                        MSG_DEPENDENCIES,
                        moduleId, moduleNewVersion, sb.toString());
                logger.info(msg);
                // Now fail
                throw AlfrescoRuntimeException.create(
                        ERR_MISSING_DEPENDENCY,
                        moduleId, moduleNewVersion, moduleDependency);
            }
            // The dependency is installed, so start it
            startModule(moduleDependencyDetails, startedModules, executedComponents);
        }
        
        // Check if the module needs a rename first
        renameModule(module);
        
        // First check that the module version is fundamentally compatible with the repository
        VersionNumber repoVersionNumber = descriptorService.getServerDescriptor().getVersionNumber();
        VersionNumber minRepoVersionNumber = module.getRepoVersionMin();
        VersionNumber maxRepoVersionNumber = module.getRepoVersionMax();
        if ((minRepoVersionNumber != null && repoVersionNumber.compareTo(minRepoVersionNumber) < 0) ||
            (maxRepoVersionNumber != null && repoVersionNumber.compareTo(maxRepoVersionNumber) > 0))
        {
            // The current repo version is not supported
            throw AlfrescoRuntimeException.create(
                    ERR_UNSUPPORTED_REPO_VERSION,
                    moduleId, moduleNewVersion, repoVersionNumber, minRepoVersionNumber, maxRepoVersionNumber);
        }
        
        // Get the module details from the registry
        RegistryKey moduleKeyInstalledVersion = new RegistryKey(
                ModuleComponentHelper.URI_MODULES_1_0,
                REGISTRY_PATH_MODULES, moduleId, REGISTRY_PROPERTY_INSTALLED_VERSION);
        RegistryKey moduleKeyCurrentVersion = new RegistryKey(
                ModuleComponentHelper.URI_MODULES_1_0,
                REGISTRY_PATH_MODULES, moduleId, REGISTRY_PROPERTY_CURRENT_VERSION);
        VersionNumber moduleInstallVersion = (VersionNumber) registryService.getProperty(moduleKeyInstalledVersion);
        VersionNumber moduleCurrentVersion = (VersionNumber) registryService.getProperty(moduleKeyCurrentVersion);
        String msg = null;
        if (moduleCurrentVersion == null)                                 // No previous record of it
        {
            msg = I18NUtil.getMessage(MSG_INSTALLING, moduleId, moduleNewVersion);
            // Record the install version
            registryService.addProperty(moduleKeyInstalledVersion, moduleNewVersion);
            moduleInstallVersion = moduleNewVersion;
            moduleCurrentVersion = moduleNewVersion;
        }
        else                                    // It is an upgrade or is the same
        {
            // Check that we have an installed version
            if (moduleInstallVersion == null)
            {
                // A current version, but no installed version
                logger.warn(I18NUtil.getMessage(WARN_NO_INSTALL_VERSION, moduleId, moduleCurrentVersion));
                // Record the install version
                registryService.addProperty(moduleKeyInstalledVersion, moduleCurrentVersion);
                moduleInstallVersion = moduleCurrentVersion;
            }
            
            if (moduleCurrentVersion.compareTo(moduleNewVersion) == 0)       // The current version is the same
            {
                msg = I18NUtil.getMessage(MSG_STARTING, moduleId, moduleNewVersion);
            }
            else if (moduleCurrentVersion.compareTo(moduleNewVersion) > 0)   // Downgrading not supported
            {
                throw AlfrescoRuntimeException.create(ERR_NO_DOWNGRADE, moduleId, moduleCurrentVersion, moduleNewVersion);
            }
            else                                                    // This is an upgrade
            {
                msg = I18NUtil.getMessage(MSG_UPGRADING, moduleId, moduleNewVersion, moduleCurrentVersion);
            }
        }
        loggerService.info(msg);
        // Record the current version
        registryService.addProperty(moduleKeyCurrentVersion, moduleNewVersion);
        
        Map<String, ModuleComponent> componentsByName = getComponents(moduleId);
        for (ModuleComponent component : componentsByName.values())
        {
            executeComponent(moduleId, moduleNewVersion, component, executedComponents);
        }
        
        // Keep track of the ID as it started successfully
        startedModules.add(moduleId);
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Started module '" + module + "' including " + executedComponents.size() + "components.");
        }
    }
    
    /**
     * Execute the component, respecting dependencies.
     */
    private void executeComponent(
            String moduleId,
            VersionNumber currentVersion,
            ModuleComponent component,
            Set<ModuleComponent> executedComponents)
    {
        // Ignore if it has been executed in this run already
        if (executedComponents.contains(component))
        {
            // Already done
            if (logger.isDebugEnabled())
            {
                logger.debug("Skipping component already executed in this run: \n" +
                        "   Component:      " + component);
            }
            return;
        }
        // Keep track of the fact that we considered it for execution
        executedComponents.add(component);
        
        // Check the version applicability
        VersionNumber minVersion = component.getAppliesFromVersionNumber();
        VersionNumber maxVersion = component.getAppliesToVersionNumber();
        if (currentVersion.compareTo(minVersion) < 0 || currentVersion.compareTo(maxVersion) > 0)
        {
            // It is out of the allowable range for execution so we just ignore it
            if (logger.isDebugEnabled())
            {
                logger.debug("Skipping component that doesn't apply to the module installation version: \n" +
                        "   Component:       " + component + "\n" +
                        "   Module:          " + moduleId + "\n" +
                        "   Current Version: " + currentVersion + "\n" +
                        "   Applies From :   " + minVersion + "\n" +
                        "   Applies To   :   " + maxVersion);
            }
            return;
        }
        
        // Construct the registry key to store the execution date
        String name = component.getName();
        RegistryKey executionDateKey = new RegistryKey(
                ModuleComponentHelper.URI_MODULES_1_0,
                REGISTRY_PATH_MODULES, moduleId, REGISTRY_PATH_COMPONENTS, name, REGISTRY_PROPERTY_EXECUTION_DATE);
        
        // Check if the component has been executed
        Date executionDate = (Date) registryService.getProperty(executionDateKey);
        if (executionDate != null && component.isExecuteOnceOnly())
        {
            // It has been executed and is scheduled for a single execution - leave it
            if (logger.isDebugEnabled())
            {
                logger.debug("Skipping already-executed module component: \n" +
                        "   Component:      " + component + "\n" +
                        "   Execution Time: " + executionDate);
            }
            return;
        }
        // It may have been executed, but not in this run and it is allowed to be repeated
        // Check for dependencies
        List<ModuleComponent> dependencies = component.getDependsOn();
        for (ModuleComponent dependency : dependencies)
        {
            executeComponent(moduleId, currentVersion, dependency, executedComponents);
        }
        // Execute the component itself
        component.execute();
        // Keep track of it in the registry
        registryService.addProperty(executionDateKey, new Date());
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Executed module component: \n" +
                    "   Component:      " + component);
        }
    }
}
