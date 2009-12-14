/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.config.xml;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.transaction.UserTransaction;

import org.springframework.extensions.config.ConfigDeployment;
import org.springframework.extensions.config.ConfigImpl;
import org.springframework.extensions.config.ConfigSection;
import org.springframework.extensions.config.ConfigSource;
import org.springframework.extensions.config.evaluator.Evaluator;
import org.springframework.extensions.config.xml.XMLConfigService;
import org.springframework.extensions.config.xml.elementreader.ConfigElementReader;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

/**
 * XML-based configuration service which can optionally read config from the Repository
 *
 */
public class RepoXMLConfigService extends XMLConfigService implements TenantDeployer
{
    private static final Log logger = LogFactory.getLog(RepoXMLConfigService.class);
    
    /**
     * Lock objects
     */
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();
    
    // Dependencies
    private TransactionService transactionService;
    private AuthenticationContext authenticationContext;
    private TenantAdminService tenantAdminService;
    
    // Internal cache (clusterable)
    private SimpleCache<String, ConfigData> configDataCache;

    // used to reset the cache
    private ThreadLocal<ConfigData> configDataThreadLocal = new ThreadLocal<ConfigData>();
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }
       
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }

    public void setConfigDataCache(SimpleCache<String, ConfigData> configDataCache)
    {
        this.configDataCache = configDataCache;
    }
        
    
    /**
     * Constructs an XMLConfigService using the given config source
     * 
     * @param configSource
     *            A ConfigSource
     */
    public RepoXMLConfigService(ConfigSource configSource)
    {
        super(configSource);
    }

    public List<ConfigDeployment> initConfig()
    {
        return resetRepoConfig().getConfigDeployments();
    }
    
    private ConfigData initRepoConfig(String tenantDomain)
    {
        ConfigData configData = null;
    	
    	// can be null e.g. initial login, after fresh bootstrap
        String currentUser = authenticationContext.getCurrentUserName();
        if (currentUser == null)
        {
            authenticationContext.setSystemUserAsCurrentUser();
        }
        
        UserTransaction userTransaction = transactionService.getUserTransaction();
        
        try
        {
            userTransaction.begin();
            
            // parse config
            List<ConfigDeployment> configDeployments = super.initConfig();
            
            configData = getConfigDataLocal(tenantDomain);
            if (configData != null)
            {
                configData.setConfigDeployments(configDeployments);
            }
            
            userTransaction.commit();
            
            logger.info("Config initialised");
        }
        catch(Throwable e)
        {
            try { userTransaction.rollback(); } catch (Exception ex) {}           
            throw new AlfrescoRuntimeException("Failed to initialise config service", e);
        }
        finally
        {
            if (currentUser == null)
            {
                authenticationContext.clearCurrentSecurityContext();
            }
        }
        
        return configData;
    }
    
    public void destroy()
    {
        super.destroy();
        
        logger.info("Config destroyed");
    }
    
    /**
     * Resets the config service
     */
    public void reset()
    {
       resetRepoConfig();
    }
    
    /**
     * Resets the config service
     */
    private ConfigData resetRepoConfig()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Resetting repo config service");
        }
       
        String tenantDomain = getTenantDomain();
        try
        {
            destroy();
            
            // create threadlocal, if needed
            ConfigData configData = getConfigDataLocal(tenantDomain);
            if (configData == null)
            {
                configData = new ConfigData(tenantDomain);
                this.configDataThreadLocal.set(configData);
            }
            
            configData = initRepoConfig(tenantDomain);
            
            if (configData == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to reset configData " + tenantDomain);
            }
            
            try
            {
                writeLock.lock();        
                configDataCache.put(tenantDomain, configData);
            }
            finally
            {
                writeLock.unlock();
            }

            return configData;
        }
        finally
        {
            try
            {
                readLock.lock();
                if (configDataCache.get(tenantDomain) != null)
                {
                    this.configDataThreadLocal.set(null); // it's in the cache, clear the threadlocal
                }
            }
            finally
            {
                readLock.unlock();
            }
        }
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
  
    public void onEnableTenant()
    {
        initConfig(); // will be called in context of tenant
    }
    
    public void onDisableTenant()
    {
        destroy(); // will be called in context of tenant
    }
    
    // re-entrant (eg. via reset)
    private ConfigData getConfigData()
    {
        String tenantDomain = getTenantDomain();
        
        // check threadlocal first - return if set
        ConfigData configData = getConfigDataLocal(tenantDomain);
        if (configData != null)
        {
            return configData; // return local config
        }

        try
        {
            // check cache second - return if set
            readLock.lock();
            configData = configDataCache.get(tenantDomain);

            if (configData != null)
            {
                return configData; // return cached config
            }
        }
        finally
        {
            readLock.unlock();
        }
        
        // reset caches - may have been invalidated (e.g. in a cluster)
        configData = resetRepoConfig(); 
        
        if (configData == null)
        {     
            // unexpected
            throw new AlfrescoRuntimeException("Failed to get configData " + tenantDomain);
        }
        
        return configData;
    }
    
    // get threadlocal 
    private ConfigData getConfigDataLocal(String tenantDomain)
    {
        ConfigData configData = this.configDataThreadLocal.get();
        
        // check to see if domain switched (eg. during login)
        if ((configData != null) && (tenantDomain.equals(configData.getTenantDomain())))
        {
            return configData; // return threadlocal, if set
        }   
        
        return null;
    }
    
    private void removeConfigData()
    {
        try
        {
            writeLock.lock();
            String tenantDomain = getTenantDomain();
            if (configDataCache.get(tenantDomain) != null)
            {
                configDataCache.remove(tenantDomain);
            }
        }
        finally
        {
            writeLock.unlock();
        }          
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
        removeConfigData();
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
        removeConfigData();
    }
    
    @Override
    protected Map<String, List<ConfigSection>> getSectionsByArea()
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
        removeConfigData();
    }
    
    @Override
    protected List<ConfigSection> getSections()
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
        removeConfigData();
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
        removeConfigData();
    }
    
    // local helper - returns tenant domain (or empty string if default non-tenant)
    private String getTenantDomain()
    {
        return tenantAdminService.getCurrentUserDomain();
    }
    
    private class ConfigData
    {
        private ConfigImpl globalConfig;   
        private Map<String, Evaluator> evaluators;
        private Map<String, List<ConfigSection>> sectionsByArea;
        private List<ConfigSection> sections;
        private Map<String, ConfigElementReader> elementReaders;
        
        private List<ConfigDeployment> configDeployments;
        
        private String tenantDomain;
        
        public ConfigData(String tenantDomain)
        {
            this.tenantDomain = tenantDomain;
        }
        
        public String getTenantDomain()
        {
            return tenantDomain;
        }
        
        public ConfigImpl getGlobalConfig()
        {
            return globalConfig;
        }
        public void setGlobalConfig(ConfigImpl globalConfig)
        {
            this.globalConfig = globalConfig;
        }
        public Map<String, Evaluator> getEvaluators()
        {
            return evaluators;
        }
        public void setEvaluators(Map<String, Evaluator> evaluators)
        {
            this.evaluators = evaluators;
        }
        public Map<String, List<ConfigSection>> getSectionsByArea()
        {
            return sectionsByArea;
        }
        public void setSectionsByArea(Map<String, List<ConfigSection>> sectionsByArea)
        {
            this.sectionsByArea = sectionsByArea;
        }
        public List<ConfigSection> getSections()
        {
            return sections;
        }
        public void setSections(List<ConfigSection> sections)
        {
            this.sections = sections;
        }
        public Map<String, ConfigElementReader> getElementReaders()
        {
            return elementReaders;
        }
        public void setElementReaders(Map<String, ConfigElementReader> elementReaders)
        {
            this.elementReaders = elementReaders;
        }
        public List<ConfigDeployment> getConfigDeployments()
        {
            return configDeployments;
        }
        public void setConfigDeployments(List<ConfigDeployment> configDeployments)
        {
            this.configDeployments = configDeployments;
        }
    }
}
