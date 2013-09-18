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
package org.alfresco.repo.config.xml;

import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.config.ConfigDataCache;
import org.alfresco.repo.config.ConfigDataCache.ConfigData;
import org.alfresco.repo.config.ConfigDataCache.ImmutableConfigData;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.config.ConfigDeployment;
import org.springframework.extensions.config.ConfigImpl;
import org.springframework.extensions.config.ConfigSection;
import org.springframework.extensions.config.ConfigSource;
import org.springframework.extensions.config.evaluator.Evaluator;
import org.springframework.extensions.config.xml.XMLConfigService;
import org.springframework.extensions.config.xml.elementreader.ConfigElementReader;

/**
 * XML-based configuration service which can optionally read config from the Repository
 */
public class RepoXMLConfigService extends XMLConfigService implements TenantDeployer
{
    private static final Log logger = LogFactory.getLog(RepoXMLConfigService.class);
    
    /**
     * Configuration that is manipulated by the current thread.<br/>
     * This is required because the super classes call back into this mechanism after
     * receiving the initial object, etc.
     */
    private final ThreadLocal<ConfigData> configUnderConstruction = new ThreadLocal<ConfigData>();
    
    // Dependencies
    private TransactionService transactionService;
    private TenantAdminService tenantAdminService;
    private ConfigDataCache configDataCache;

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }

    /**
     * Set the asynchronously-controlled cache.
     */
    public void setConfigDataCache(ConfigDataCache configDataCache)
    {
        this.configDataCache = configDataCache;
    }
        
    /**
     * Constructs an XMLConfigService using the given config source
     */
    public RepoXMLConfigService(ConfigSource configSource)
    {
        super(configSource);
    }

    @Override
    public List<ConfigDeployment> initConfig()
    {
        configDataCache.refresh();
        // Just return whatever is there (no-one uses it)
        ConfigData configData = configDataCache.get();
        return configData.getConfigDeployments();
    }
    
    /**
     * Get the repository configuration data for a given tenant.
     * This does the low-level initialization of the configuration and does not do any
     * caching.
     * 
     * @param tenantDomain              the current tenant domain
     * @return                          return the repository configuration for the given tenant (never <tt>null</tt>)
     */
    public ConfigData getRepoConfig(final String tenantDomain)
    {
        final RetryingTransactionCallback<Void> getConfigWork = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // parse config
                RepoXMLConfigService.super.initConfig();
                return null;
            }
        };
        TenantUtil.TenantRunAsWork<Void> getConfigRunAs = new TenantUtil.TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(getConfigWork, true);
                return null;
            }
        };

        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Fetching repository config data for tenant: \n" +
                        "   Tenant Domain: " + tenantDomain);
            }
            // Put some mutable config onto the current thread and have the superclasses mess with that.
            ConfigData configData = new ConfigData();
            configUnderConstruction.set(configData);
            // Do the work as system tenant, see ALF-19922
            TenantUtil.runAsSystemTenant(getConfigRunAs, tenantDomain);
            // Now wrap the config so that it cannot be changed
            configData = new ImmutableConfigData(configData);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Fetched repository config data for tenant: \n" +
                        "   Tenant Domain: " + tenantDomain + "\n" +
                        "   Config:        " + configData);
            }
            return configData;
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to fetch repository config data for tenant \n" +
                    "   Tenant Domain: " + tenantDomain,
                    e);
        }
        finally
        {
            configUnderConstruction.remove();
        }
    }
    
    @Override
    public void destroy()
    {
        reset();
    }
    
    /**
     * Resets the config values for the current tenant
     */
    @Override
    public void reset()
    {
        configDataCache.refresh();
    }
    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // run as System on bootstrap
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {            
                initConfig();
                return null;
            }                               
        }, AuthenticationUtil.getSystemUserName());
        
        if ((tenantAdminService != null) && (tenantAdminService.isEnabled()))
        {
            tenantAdminService.deployTenants(this, logger);            
            tenantAdminService.register(this);
        } 
    }
    
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }
  
    @Override
    public void onEnableTenant()
    {
        initConfig(); // will be called in context of tenant
    }
    
    @Override
    public void onDisableTenant()
    {
        destroy(); // will be called in context of tenant
    }
    
    /**
     * Fetch the tenant-specific config data from the cache.  If nothing is
     * available then the config will be created.
     * <p/>
     * Note that this method is used during construction of the config as well.
     * When this occurs, a thread local value is used in place of the cached
     * value.  It's not pretty.
     * 
     * @return          the tenant-specific configuration (never <tt>null</tt>)
     */
    private ConfigData getConfigData()
    {
        if (configUnderConstruction.get() != null)
        {
            // We are busy building some config so we can return it
            return configUnderConstruction.get();
        }
        // Go to the backing cache
        return configDataCache.get();
    }
    
    @Override
    protected ConfigImpl getGlobalConfigImpl()
    {
        return getConfigData().getGlobalConfig();
    }
    
    @Override
    protected void putGlobalConfig(ConfigImpl globalConfig)
    {
        getConfigData().setGlobalConfig(globalConfig);
    }
    
    @Override
    protected void removeGlobalConfig()
    {
        throw new UnsupportedOperationException("'destroy' method must destroy all config.  Piecemeal destruction is not supported.");
    }
    
    @Override
    protected Map<String, Evaluator> getEvaluators()
    {
        return getConfigData().getEvaluators();
    }
    
    @Override
    protected void putEvaluators(Map<String, Evaluator> evaluators)
    {
        getConfigData().setEvaluators(evaluators);
    }
    
    @Override
    protected void removeEvaluators()
    {
        throw new UnsupportedOperationException("'destroy' method must destroy all config.  Piecemeal destruction is not supported.");
    }
    
    @Override
    public Map<String, List<ConfigSection>> getSectionsByArea()
    {
        return getConfigData().getSectionsByArea();
    }
    
    @Override
    protected void putSectionsByArea(Map<String, List<ConfigSection>> sectionsByArea)
    {
        getConfigData().setSectionsByArea(sectionsByArea);
    }
    
    @Override
    protected void removeSectionsByArea()
    {
        throw new UnsupportedOperationException("'destroy' method must destroy all config.  Piecemeal destruction is not supported.");
    }
    
    @Override
    public List<ConfigSection> getSections()
    {
        return getConfigData().getSections();
    }
    
    @Override
    protected void putSections(List<ConfigSection> sections)
    {
        getConfigData().setSections(sections);
    }
    
    @Override
    protected void removeSections()
    {
        throw new UnsupportedOperationException("'destroy' method must destroy all config.  Piecemeal destruction is not supported.");
    }

    @Override
    protected Map<String, ConfigElementReader> getElementReaders()
    {
        return getConfigData().getElementReaders();
    }
    
    @Override
    protected void putElementReaders(Map<String, ConfigElementReader> elementReaders)
    {
        getConfigData().setElementReaders(elementReaders);
    }
    
    @Override
    protected void removeElementReaders()
    {
        throw new UnsupportedOperationException("'destroy' method must destroy all config.  Piecemeal destruction is not supported.");
    }
}
