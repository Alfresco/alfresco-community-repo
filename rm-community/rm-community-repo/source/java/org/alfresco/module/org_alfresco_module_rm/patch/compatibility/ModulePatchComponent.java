/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.patch.compatibility;

import java.io.Serializable;

import org.alfresco.module.org_alfresco_module_rm.patch.ModulePatchExecuterImpl;
import org.alfresco.repo.admin.registry.RegistryKey;
import org.alfresco.repo.admin.registry.RegistryService;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.module.ModuleComponentHelper;
import org.alfresco.repo.module.ModuleVersionNumber;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Module patch component base class.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@Deprecated
public abstract class ModulePatchComponent extends AbstractModuleComponent
{
    private static final String REGISTRY_PATH_MODULES = "modules";
    private static final String REGISTRY_PROPERTY_INSTALLED_VERSION = "installedVersion";
    private static final String REGISTRY_PROPERTY_CURRENT_VERSION = "currentVersion";
    
    /** logger */
    protected static final Logger LOGGER = LoggerFactory.getLogger(ModulePatchComponent.class);

    /** Retrying transaction helper */
    protected RetryingTransactionHelper retryingTransactionHelper;

    /** Behaviour filter */
    protected BehaviourFilter behaviourFilter;

    /** module patch executer */
    protected ModulePatchExecuterImpl modulePatchExecuter;

    /** Registry service */
    protected RegistryService registryService;
    
    /**
     * @param retryingTransactionHelper retrying transaction helper
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    /**
     * @param behaviourFilter   behaviour filter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * @param modulePatchExecuter   module patch executer
     */
    public void setModulePatchExecuter(ModulePatchExecuterImpl modulePatchExecuter)
    {
        this.modulePatchExecuter = modulePatchExecuter;
    }

    /**
     * @param registryService   Registry service
     */
    public void setRegistryService(RegistryService registryService)
    {
        this.registryService = registryService;
    }

    /**
     * Init method
     */
    @Override
    public void init()
    {
        super.init();
        modulePatchExecuter.getDependsOn().add(this);
    }

    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executeInternal()
    {
        ModuleVersionNumber moduleInstalledVersionNumber = getModuleVersionNumber(REGISTRY_PROPERTY_INSTALLED_VERSION);
        ModuleVersionNumber moduleCurrentVersionNumber = getModuleVersionNumber(REGISTRY_PROPERTY_CURRENT_VERSION);
        
        String moduleName = getName();

        if (isVersionLaterThan(moduleInstalledVersionNumber, moduleCurrentVersionNumber))
        {
            LOGGER.info("Module patch component '{}' is skipped for upgrade from version {} to version {}",
                    moduleName, moduleInstalledVersionNumber, moduleCurrentVersionNumber);
        }
        else
        {
            try
            {
                LOGGER.info("Module patch component '{}' is executing ...", moduleName);

                // execute path within an isolated transaction
                retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    @Override
                    public Void execute()
                    {
                        behaviourFilter.disableBehaviour();
                        try
                        {
                            executePatch();
                        }
                        finally
                        {
                            behaviourFilter.enableBehaviour();
                        }
                        return null;
                    }

                }, false, true);

                LOGGER.info(" ... completed module patch '{}'", moduleName);
                
            } catch (Exception exception)
            {
                // record the exception otherwise it gets swallowed
                LOGGER.info("  ... error encountered.  {}", exception.getMessage(), exception);
                throw exception;
            }
        }
    }

    /**
     * Helper method to get the ModuleVersionNumber.
     */
    private ModuleVersionNumber getModuleVersionNumber(String registryProperty)
    {
        String moduleId = modulePatchExecuter.getModuleId();
        RegistryKey moduleKeyVersion = new RegistryKey(ModuleComponentHelper.URI_MODULES_1_0,
                new String[]{REGISTRY_PATH_MODULES, moduleId, registryProperty});
        Serializable moduleVersion = this.registryService.getProperty(moduleKeyVersion);
        
        return new ModuleVersionNumber(moduleVersion.toString());
    }

    /**
     * Helper method to determine if this is an upgrade from a version that already includes the early (v2.0, v2.1)
     * patches.
     *
     */
    private boolean isVersionLaterThan(ModuleVersionNumber installedModuleVersionNumber,
                                       ModuleVersionNumber currentModuleVersionNumber)
    {
        // assume that the v2.0 and v2.1 patches should be run
        boolean versionLaterThan = false;

        // if this is an upgrade as opposed to a fresh install
        if (installedModuleVersionNumber.compareTo(currentModuleVersionNumber) != 0)
        {
            // if the installed version is later than the minimum version number of this patch
            ModuleVersionNumber minVersion = this.getAppliesFromVersionNumber();
            if (installedModuleVersionNumber.compareTo(minVersion) >= 0)
            {
                versionLaterThan = true;
            }
        }

        return  versionLaterThan;
    }

    /**
     * Execute patch work.
     */
    protected abstract void executePatch();
}
