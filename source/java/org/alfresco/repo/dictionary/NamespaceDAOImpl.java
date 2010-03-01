/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.namespace.NamespaceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple in-memory namespace DAO
 */
public class NamespaceDAOImpl implements NamespaceDAO
{
    private static final Log logger = LogFactory.getLog(NamespaceDAOImpl.class);
    
    /**
     * Lock objects
     */
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();
    
    // Internal cache (clusterable)
    private SimpleCache<String, NamespaceRegistry> namespaceRegistryCache;
    
    // used to reset the cache
    private ThreadLocal<NamespaceRegistry> namespaceRegistryThreadLocal = new ThreadLocal<NamespaceRegistry>();
    private ThreadLocal<NamespaceRegistry> defaultNamespaceRegistryThreadLocal = new ThreadLocal<NamespaceRegistry>();
    
    // Dependencies
    private TenantService tenantService;
    private DictionaryDAO dictionaryDAO;
    
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setNamespaceRegistryCache(SimpleCache<String, NamespaceRegistry> namespaceRegistryCache)
    {
        this.namespaceRegistryCache = namespaceRegistryCache;
    }
    
    public void registerDictionary(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }
    
    
    /**
     * Initialise empty namespaces
     */
    public void init()
    {     
        initNamespace(getTenantDomain());
    }
    
    /**
     * Destroy the namespaces
     */
    public void destroy()
    {
        String tenantDomain = getTenantDomain();
        
        removeNamespaceRegistry(tenantDomain);
        
        if (logger.isDebugEnabled()) 
        {
            logger.debug("Namespaces destroyed");
        }
    }
    
    /**
     * Resets the namespaces (by re-initialising the dictionary)
     */
    private NamespaceRegistry reset(String tenantDomain)
    {
        if (dictionaryDAO == null)
        {
            // Unexpected
            throw new AlfrescoRuntimeException("Dictionary should be registered in order to perform reset");
        }
        
        if (logger.isDebugEnabled()) 
        {
            logger.debug("Resetting namespaces ...");
        }
        
        dictionaryDAO.init();
        
        NamespaceRegistry namespaceRegistry = getNamespaceRegistry(tenantDomain);
        
        if (logger.isDebugEnabled()) 
        {
            logger.debug("... resetting namespaces completed");
        }
        
        return namespaceRegistry;
    }
    
    private NamespaceRegistry initNamespace(String tenantDomain)
    {
        try
        {
            createNamespaceLocal(tenantDomain);
            
            NamespaceRegistry namespaceRegistry = initNamespaceRegistry(tenantDomain);
            
            if (namespaceRegistry == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to init namespaceRegistry " + tenantDomain);
            }
            
            try
            {
                writeLock.lock();        
                namespaceRegistryCache.put(tenantDomain, namespaceRegistry);
            }
            finally
            {
                writeLock.unlock();
            }
            
            return namespaceRegistry;
        }
        finally
        {
            try
            {
                readLock.lock();
                if (namespaceRegistryCache.get(tenantDomain) != null)
                {
                    removeNamespaceLocal(tenantDomain);
                }
            }
            finally
            {
                readLock.unlock();
            }
        }
    }
    
    private NamespaceRegistry initNamespaceRegistry(String tenantDomain)
    {
        getNamespaceRegistry(tenantDomain).setUrisCache(new ArrayList<String>());
        getNamespaceRegistry(tenantDomain).setPrefixesCache(new HashMap<String, String>());
        
        if (logger.isDebugEnabled()) 
        {
            logger.debug("Empty namespaces initialised");
        }
        
        return getNamespaceRegistryLocal(tenantDomain);
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.ref.NamespacePrefixResolver#getURIs()
     */
    public Collection<String> getURIs()
    {
        if (! tenantService.isTenantUser())
        {
            return Collections.unmodifiableCollection(getUrisCtx());
        }
        else
        {
            // Get tenant-specific URIs
            List<String> domainUris = getUrisCtx();
            
            // Get non-tenant-specific URIs (and filter out, if overridden)
            List<String> urisFiltered = new ArrayList<String>();
            for(String uri : getUrisCtx(""))
            {
                if (domainUris.contains(uri))
                {
                    // overridden, hence skip this default prefix
                    continue; 
                }
                urisFiltered.add(uri);
            }
            
            // default (non-overridden) + tenant-specific
            urisFiltered.addAll(domainUris);
            
            return Collections.unmodifiableCollection(urisFiltered);
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.ref.NamespacePrefixResolver#getPrefixes()
     */
    public Collection<String> getPrefixes()
    {      
        if (! tenantService.isTenantUser())
        {
            return Collections.unmodifiableCollection(getPrefixesCtx().keySet());
        }
        else
        {
            // Get tenant-specific prefixes
            Collection<String> domainPrefixes = getPrefixesCtx().keySet();
            
            // Get non-tenant-specific URIs (and filter out, if overridden)
            List<String> prefixesFiltered = new ArrayList<String>();
            for(String prefix : getPrefixesCtx("").keySet())
            {
                if (domainPrefixes.contains(prefix))
                {
                    // overridden, hence skip this default prefix
                    continue; 
                }
                prefixesFiltered.add(prefix);        
            }
            
            // default (non-overridden) + tenant-specific
            prefixesFiltered.addAll(domainPrefixes);
            
            return Collections.unmodifiableCollection(prefixesFiltered);
        }  
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.NamespaceDAO#addURI(java.lang.String)
     */
    public void addURI(String uri)
    {
        if (getUrisCtx().contains(uri))
        {
            throw new NamespaceException("URI " + uri + " has already been defined");
        }
        getUrisCtx().add(uri);
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.NamespaceDAO#addPrefix(java.lang.String, java.lang.String)
     */
    public void addPrefix(String prefix, String uri)
    {
        if (!getUrisCtx().contains(uri))
        {
            throw new NamespaceException("Namespace URI " + uri + " does not exist");
        }
        getPrefixesCtx().put(prefix, uri);
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.NamespaceDAO#removeURI(java.lang.String)
     */
    public void removeURI(String uri)
    {
        getUrisCtx().remove(uri);
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.NamespaceDAO#removePrefix(java.lang.String)
     */
    public void removePrefix(String prefix)
    {
        getPrefixesCtx().remove(prefix);
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.ref.NamespacePrefixResolver#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix)
    {
        if (! tenantService.isTenantUser())
        {
            return getPrefixesCtx().get(prefix);
        }
        else
        {
            // first look for tenant-specific prefix
            String uri = getPrefixesCtx().get(prefix);
            if (uri != null) 
            {
                // found tenant specific uri
                return uri;
            } 
            else
            {
                // try with default (non-tenant-specific) prefix
                return getPrefixesCtx("").get(prefix);
            }
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.ref.NamespacePrefixResolver#getPrefixes(java.lang.String)
     */
    public Collection<String> getPrefixes(String URI)
    {
        if (! tenantService.isTenantUser())
        {
            Collection<String> uriPrefixes = new ArrayList<String>();
            for (String key : getPrefixesCtx().keySet())
            {
                String uri = getPrefixesCtx().get(key);
                if ((uri != null) && (uri.equals(URI)))
                {
                    uriPrefixes.add(key);
                }
            }
            return uriPrefixes;
        }
        else
        {           
            // check domain prefixes  
            Collection<String> domainUriPrefixes = new ArrayList<String>();
            for (String key : getPrefixesCtx().keySet())
            {
                String uri = getPrefixesCtx().get(key);
                if ((uri != null) && (uri.equals(URI)))
                {
                    domainUriPrefixes.add(key);
                }
            }
            
            // check non-domain prefixes           
            Collection<String> uriPrefixes = new ArrayList<String>();
            for (String key : getPrefixesCtx("").keySet())
            {
                String uri = getPrefixesCtx("").get(key);
                if ((uri != null) && (uri.equals(URI)))
                {
                    if (domainUriPrefixes != null)
                    {                          
                        if (domainUriPrefixes.contains(key))
                        {
                            // overridden, hence skip this default prefix
                            continue; 
                        }
                    }
                    
                    uriPrefixes.add(key);
                }
            }
            
            if (domainUriPrefixes != null)
            {
                // default (non-overridden) + domain
                uriPrefixes.addAll(domainUriPrefixes);
            }
            
            return uriPrefixes;
        }
    }
    
    
    // re-entrant (eg. via reset)
    private NamespaceRegistry getNamespaceRegistry(String tenantDomain)
    {
        NamespaceRegistry namespaceRegistry =  null;
        
        // check threadlocal first - return if set
        namespaceRegistry = getNamespaceRegistryLocal(tenantDomain);
        if (namespaceRegistry != null)
        {
            return namespaceRegistry; // return local namespaceRegistry
        }
        
        try
        {
            // check cache second - return if set
            readLock.lock();
            namespaceRegistry = namespaceRegistryCache.get(tenantDomain);
            
            if (namespaceRegistry != null)
            {
                return namespaceRegistry; // return cached config
            }
        }
        finally
        {
            readLock.unlock();
        }
        
        // reset caches - may have been invalidated (e.g. in a cluster)
        namespaceRegistry = reset(tenantDomain);
        
        if (namespaceRegistry == null)
        {     
            // unexpected
            throw new AlfrescoRuntimeException("Failed to get namespaceRegistry " + tenantDomain);
        }
        
        return namespaceRegistry;
    }
    
    // create threadlocal
    private void createNamespaceLocal(String tenantDomain)      
    {
         // create threadlocal, if needed
        NamespaceRegistry namespaceRegistry = getNamespaceRegistryLocal(tenantDomain);
        if (namespaceRegistry == null)
        {
            namespaceRegistry = new NamespaceRegistry(tenantDomain);
            
            if (! tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
            {
                namespaceRegistryThreadLocal.set(namespaceRegistry);
            }
            
            if (defaultNamespaceRegistryThreadLocal.get() == null)
            {
                namespaceRegistry = new NamespaceRegistry(TenantService.DEFAULT_DOMAIN);
                defaultNamespaceRegistryThreadLocal.set(namespaceRegistry);
            }
        }
    }
    
    // get threadlocal 
    private NamespaceRegistry getNamespaceRegistryLocal(String tenantDomain)
    {
        NamespaceRegistry namespaceRegistry = null;
        
        if (tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
        {
            namespaceRegistry = this.defaultNamespaceRegistryThreadLocal.get();
        }
        else
        {
            namespaceRegistry = this.namespaceRegistryThreadLocal.get();
        }
        
        // check to see if domain switched (eg. during login)
        if ((namespaceRegistry != null) && (tenantDomain.equals(namespaceRegistry.getTenantDomain())))
        {
            return namespaceRegistry; // return threadlocal, if set
        }   
        
        return null;
    }
    
    // remove threadlocal
    private void removeNamespaceLocal(String tenantDomain)
    {
        if (tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
        {
            defaultNamespaceRegistryThreadLocal.set(null); // it's in the cache, clear the threadlocal
        }
        else
        {
            defaultNamespaceRegistryThreadLocal.set(null); // it's in the cache, clear the threadlocal
            namespaceRegistryThreadLocal.set(null); // it's in the cache, clear the threadlocal
        }
    }
    
    private void removeNamespaceRegistry(String tenantDomain)
    {
        try
        {
            writeLock.lock();
            if (namespaceRegistryCache.get(tenantDomain) != null)
            {
                namespaceRegistryCache.remove(tenantDomain);
            }
            
            removeNamespaceLocal(tenantDomain);
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * Get URIs from the cache (in the context of the current user's tenant domain)
     * 
     * @return URIs
     */
    private List<String> getUrisCtx()
    {
        return getUrisCtx(getTenantDomain());
    }

    /**
     * Get URIs from the cache
     * 
     * @param tenantDomain
     * @return URIs
     */
    private List<String> getUrisCtx(String tenantDomain)
    {
        return getNamespaceRegistry(tenantDomain).getUrisCache();
    }
    
    /**
     * Get prefixes from the cache
     * 
     * @return prefixes
     */
    private Map<String, String> getPrefixesCtx()
    {
        return getPrefixesCtx(getTenantDomain());
    }
    
    /**
     * Get prefixes from the cache
     * 
     * @param tenantDomain
     * @return prefixes
     */
    private Map<String, String> getPrefixesCtx(String tenantDomain)
    {
        return getNamespaceRegistry(tenantDomain).getPrefixesCache();
    }
    
    /**
     * Local helper - returns tenant domain (or empty string if default non-tenant)
     */
    private String getTenantDomain()
    {
        return tenantService.getCurrentUserDomain();
    }
    
    /* package */ class NamespaceRegistry
    {
        private List<String> urisCache = new ArrayList<String>(0);
        private Map<String, String> prefixesCache = new HashMap<String, String>(0);
        
        private String tenantDomain;
        
        public NamespaceRegistry(String tenantDomain)
        {
            this.tenantDomain = tenantDomain;
        }
        
        public String getTenantDomain()
        {
            return tenantDomain;
        } 
        
        public List<String> getUrisCache()
        {
            return urisCache;
        }
        
        public void setUrisCache(List<String> urisCache)
        {
            this.urisCache = urisCache;
        }
        
        public Map<String, String> getPrefixesCache()
        {
            return prefixesCache;
        }

        public void setPrefixesCache(Map<String, String> prefixesCache)
        {
            this.prefixesCache = prefixesCache;
        }
    }
}
