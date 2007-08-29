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
package org.alfresco.repo.config.xml;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.transaction.UserTransaction;

import org.alfresco.config.ConfigImpl;
import org.alfresco.config.ConfigSection;
import org.alfresco.config.ConfigSource;
import org.alfresco.config.evaluator.Evaluator;
import org.alfresco.config.xml.XMLConfigService;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.tenant.TenantDeployerService;
import org.alfresco.repo.tenant.TenantService;
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
    private AuthenticationComponent authenticationComponent;
    private TenantDeployerService tenantDeployerService;
    private TenantService tenantService;
    
    // Internal caches that are clusterable
    private SimpleCache<String, ConfigImpl> globalConfigCache;   
    private SimpleCache<String, Map<String, Evaluator>> evaluatorsCache;
    private SimpleCache<String, Map<String, List<ConfigSection>>> sectionsByAreaCache;
    private SimpleCache<String, List<ConfigSection>> sectionsCache;
    private SimpleCache<String, Map<String, ConfigElementReader>> elementReadersCache;


    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }
       
    public void setTenantDeployerService(TenantDeployerService tenantDeployerService)
    {
        this.tenantDeployerService = tenantDeployerService;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    

    public void setGlobalConfigCache(SimpleCache<String, ConfigImpl> globalConfigCache)
    {
        this.globalConfigCache = globalConfigCache;
    }
    
    public void setEvaluatorsCache(SimpleCache<String, Map<String, Evaluator>> evaluatorsCache)
    {
        this.evaluatorsCache = evaluatorsCache;
    }
    
    public void setSectionsByAreaCache(SimpleCache<String, Map<String, List<ConfigSection>>> sectionsByAreaCache)
    {
        this.sectionsByAreaCache = sectionsByAreaCache;
    }
    
    public void setSectionsCache(SimpleCache<String, List<ConfigSection>> sectionsCache)
    {
        this.sectionsCache = sectionsCache;
    }

    public void setElementReadersCache(SimpleCache<String, Map<String, ConfigElementReader>> elementReadersCache)
    {
        this.elementReadersCache = elementReadersCache;
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

    public void initConfig()
    {
        // can be null e.g. initial login, after fresh bootstrap
        String currentUser = authenticationComponent.getCurrentUserName();
        if (currentUser == null)
        {
            authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        }
        
        UserTransaction userTransaction = transactionService.getUserTransaction();
        
        try
        {
            userTransaction.begin();
            
            // parse config and initialise caches
            super.initConfig();
                       
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
                authenticationComponent.clearCurrentSecurityContext();
            }
        }
    }
    
    public void destroy()
    {
        super.destroy();
        
        logger.info("Config destroyed");
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
        
        if (tenantService.isEnabled() && (tenantDeployerService != null))
        {
            tenantDeployerService.deployTenants(this, logger);            
            tenantDeployerService.register(this);
        } 
    }
    
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // run as System on shutdown
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {            
                destroy();
                return null;
            }                               
        }, AuthenticationUtil.getSystemUserName());
        
        if (tenantService.isEnabled() && (tenantDeployerService != null))
        {
            tenantDeployerService.undeployTenants(this, logger);           
            tenantDeployerService.unregister(this);
        } 
    }
  
    public void onEnableTenant()
    {
        initConfig(); // will be called in context of tenant
    }
    
    public void onDisableTenant()
    {
        destroy(); // will be called in context of tenant
    }
    
    
    @Override
    protected ConfigImpl getGlobalConfigImpl()
    {
        String tenantDomain = getTenantDomain();
        ConfigImpl globalConfig = null;
        try
        {
            readLock.lock();
            globalConfig = globalConfigCache.get(tenantDomain);
        }
        finally
        {
            readLock.unlock();
        }        
        
        if (globalConfig == null)
        {
            reset(); // reset caches - may have been invalidated (e.g. in a cluster)
         
            try
            {
                readLock.lock();           
                globalConfig = globalConfigCache.get(tenantDomain);
            }
            finally
            {
                readLock.unlock();
            }           
            
            if (globalConfig == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to reset caches (globalConfigCache) " + tenantDomain);
            }
        }
        return globalConfig;
    } 
    
    @Override
    protected void putGlobalConfig(ConfigImpl globalConfig)
    {
        try
        {
            writeLock.lock();        
            globalConfigCache.put(getTenantDomain(), globalConfig);
        }
        finally
        {
            writeLock.unlock();
        }          
    }  
    
    @Override
    protected void removeGlobalConfig()
    {
        try
        {
            writeLock.lock();
            String tenantDomain = getTenantDomain();
            if (globalConfigCache.get(tenantDomain) != null)
            {
                globalConfigCache.remove(tenantDomain);
            }
        }
        finally
        {
            writeLock.unlock();
        }          
    } 
    
    @Override
    protected Map<String, Evaluator> getEvaluators()
    {
        String tenantDomain = getTenantDomain();
        Map<String, Evaluator> evaluators = null;
        try
        {
            readLock.lock();
            evaluators = evaluatorsCache.get(tenantDomain);
        }
        finally
        {
            readLock.unlock();
        }  
        
        if (evaluators == null)
        {
            reset(); // reset caches - may have been invalidated (e.g. in a cluster) 

            try
            {
                readLock.lock();            
                evaluators = evaluatorsCache.get(tenantDomain);
            }
            finally
            {
                readLock.unlock();
            }   
            
            if (evaluators == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to reset caches (evaluatorsCache) " + tenantDomain);
            }
        }
        return evaluators;
    }  
    
    @Override
    protected void putEvaluators(Map<String, Evaluator> evaluators)
    {
        try
        {
            writeLock.lock();        
            evaluatorsCache.put(getTenantDomain(), evaluators);
        }
        finally
        {
            writeLock.unlock();
        }          
    } 
    
    @Override
    protected void removeEvaluators()
    {
        try
        {
            writeLock.lock();       
            String tenantDomain = getTenantDomain();
            if (evaluatorsCache.get(tenantDomain) != null)
            {
                evaluatorsCache.get(tenantDomain).clear();
                evaluatorsCache.remove(tenantDomain);
            }
        }
        finally
        {
            writeLock.unlock();
        }  
    } 
    
    @Override
    protected Map<String, List<ConfigSection>> getSectionsByArea()
    {
        String tenantDomain = getTenantDomain();
        Map<String, List<ConfigSection>> sectionsByArea = null;
        try
        {
            readLock.lock();       
            sectionsByArea = sectionsByAreaCache.get(tenantDomain);
        }
        finally
        {
            readLock.unlock();
        }         
        
        if (sectionsByArea == null)
        {
            reset(); // reset caches - may have been invalidated (e.g. in a cluster)
            
            try
            {
                readLock.lock();            
                sectionsByArea = sectionsByAreaCache.get(tenantDomain);
            }
            finally
            {
                readLock.unlock();
            } 
        
            if (sectionsByArea == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to reset caches (sectionsByAreaCache) " + tenantDomain);
            }
        }
        return sectionsByArea;
    }  
    
    @Override
    protected void putSectionsByArea(Map<String, List<ConfigSection>> sectionsByArea)
    {
        try
        {
            writeLock.lock();        
            sectionsByAreaCache.put(getTenantDomain(), sectionsByArea);
        }
        finally
        {
            writeLock.unlock();
        }          
    }  

    @Override
    protected void removeSectionsByArea()
    {
        try
        {
            writeLock.lock();        
            String tenantDomain = getTenantDomain();
            if (sectionsByAreaCache.get(tenantDomain) != null)
            {
                sectionsByAreaCache.get(tenantDomain).clear();
                sectionsByAreaCache.remove(tenantDomain);
            }
        }
        finally
        {
            writeLock.unlock();
        }         
    } 
    
    @Override
    protected List<ConfigSection> getSections()
    {
        String tenantDomain = getTenantDomain();
        List<ConfigSection> sections = null;
        try
        {
            readLock.lock();        
            sections = sectionsCache.get(tenantDomain);
        }
        finally
        {
            readLock.unlock();
        }         
        
        if (sections == null)
        {
            reset(); // reset caches - may have been invalidated (e.g. in a cluster) 
      
            try
            {
                readLock.lock();            
                sections = sectionsCache.get(tenantDomain);
            }
            finally
            {
                readLock.unlock();
            }            
            
            if (sections == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to reset caches (sectionsCache) " + tenantDomain);
            }
        }
        return sections;
    } 
    
    @Override
    protected void putSections(List<ConfigSection> sections)
    {
        try
        {
            writeLock.lock();        
            sectionsCache.put(getTenantDomain(), sections);
        }
        finally
        {
            writeLock.unlock();
        }         
    } 
    
    @Override
    protected void removeSections()
    {
        try
        {
            writeLock.lock();       
            String tenantDomain = getTenantDomain();
            if (sectionsCache.get(tenantDomain) != null)
            {
                sectionsCache.get(tenantDomain).clear();
                sectionsCache.remove(tenantDomain);
            }
        }
        finally
        {
            writeLock.unlock();
        }  
    } 

    @Override
    protected Map<String, ConfigElementReader> getElementReaders()
    {
        String tenantDomain = getTenantDomain();
        Map<String, ConfigElementReader> elementReaders = null;
        try
        {
            readLock.lock();
            elementReaders = elementReadersCache.get(tenantDomain);
        }
        finally
        {
            readLock.unlock();
        }         
        
        if (elementReaders == null)
        {
            reset(); // reset caches - may have been invalidated (e.g. in a cluster)
             
            try
            {
                readLock.lock();            
                elementReaders = elementReadersCache.get(tenantDomain);
            }
            finally
            {
                readLock.unlock();
            } 
        
            if (elementReaders == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to reset caches (elementReadersCache) " + tenantDomain);
            }
        }
        return elementReaders;
    } 
    
    @Override
    protected void putElementReaders(Map<String, ConfigElementReader> elementReader)
    {
        try
        {
            writeLock.lock();
            elementReadersCache.put(getTenantDomain(), elementReader);
        }
        finally
        {
            writeLock.unlock();
        }             
    } 
    
    @Override
    protected void removeElementReaders()
    {
        try
        {
            writeLock.lock();
            String tenantDomain = getTenantDomain();
            if (elementReadersCache.get(tenantDomain) != null)
            {
                elementReadersCache.get(tenantDomain).clear();
                elementReadersCache.remove(tenantDomain);
            }
        }
        finally
        {
            writeLock.unlock();
        }  
    } 
    
    // local helper - returns tenant domain (or empty string if default non-tenant)
    private String getTenantDomain()
    {
        return tenantService.getCurrentUserDomain();
    }
}
