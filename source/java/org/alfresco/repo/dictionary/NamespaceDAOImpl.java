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
    private SimpleCache<String, NamespaceData> namespaceDataCache;
    
    // used to reset the cache
    private ThreadLocal<NamespaceData> namespaceDataThreadLocal = new ThreadLocal<NamespaceData>();
    private ThreadLocal<NamespaceData> defaultNamespaceDataThreadLocal = new ThreadLocal<NamespaceData>();
    
    // Dependencies
    private TenantService tenantService;
    private DictionaryDAO dictionaryDAO;
    
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setNamespaceDataCache(SimpleCache<String, NamespaceData> namespaceDataCache)
    {
        this.namespaceDataCache = namespaceDataCache;
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
        
        removeNamespaceData(tenantDomain);
        
        if (logger.isDebugEnabled()) 
        {
            logger.debug("Namespaces destroyed");
        }
    }
    
    /**
     * Resets the namespaces (by re-initialising the dictionary)
     */
    private NamespaceData reset(String tenantDomain)
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
        
        NamespaceData namespaceData = getNamespaceData(tenantDomain);
        
        if (logger.isDebugEnabled()) 
        {
            logger.debug("... resetting namespaces completed");
        }
        
        return namespaceData;
    }
    
    private NamespaceData initNamespace(String tenantDomain)
    {
        try
        {
            createNamespaceLocal(tenantDomain);
            
            NamespaceData namespaceData = initNamespaceData(tenantDomain);
            
            if (namespaceData == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to init namespaceData " + tenantDomain);
            }
            
            try
            {
                writeLock.lock();        
                namespaceDataCache.put(tenantDomain, namespaceData);
            }
            finally
            {
                writeLock.unlock();
            }
            
            return namespaceData;
        }
        finally
        {
            try
            {
                readLock.lock();
                if (namespaceDataCache.get(tenantDomain) != null)
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
    
    private NamespaceData initNamespaceData(String tenantDomain)
    {
        getNamespaceData(tenantDomain).setUrisCache(new ArrayList<String>());
        getNamespaceData(tenantDomain).setPrefixesCache(new HashMap<String, String>());
        
        if (logger.isDebugEnabled()) 
        {
            logger.debug("Empty namespaces initialised");
        }
        
        return getNamespaceDataLocal(tenantDomain);
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
    private NamespaceData getNamespaceData(String tenantDomain)
    {
        NamespaceData namespaceData =  null;
        try
        {
            // check cache first - return if set
            readLock.lock();
            namespaceData = namespaceDataCache.get(tenantDomain);
            
            if (namespaceData != null)
            {
                return namespaceData; // return cached config
            }
        }
        finally
        {
            readLock.unlock();
        }
        
        // check threadlocal second - return if set
        namespaceData = getNamespaceDataLocal(tenantDomain);
        if (namespaceData != null)
        {
            return namespaceData; // return local namespaceData
        }
        
        // reset caches - may have been invalidated (e.g. in a cluster)
        namespaceData = reset(tenantDomain);
        
        if (namespaceData == null)
        {     
            // unexpected
            throw new AlfrescoRuntimeException("Failed to get namespaceData " + tenantDomain);
        }
        
        return namespaceData;
    }
    
    // create threadlocal
    private void createNamespaceLocal(String tenantDomain)      
    {
         // create threadlocal, if needed
        NamespaceData namespaceData = getNamespaceDataLocal(tenantDomain);
        if (namespaceData == null)
        {
            namespaceData = new NamespaceData(tenantDomain);
            
            if (tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
            {
                defaultNamespaceDataThreadLocal.set(namespaceData);
            }
            else
            {
                namespaceDataThreadLocal.set(namespaceData);
            }
        }
    }
    
    // get threadlocal 
    private NamespaceData getNamespaceDataLocal(String tenantDomain)
    {
        NamespaceData namespaceData = null;
        
        if (tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
        {
            namespaceData = this.defaultNamespaceDataThreadLocal.get();
        }
        else
        {
            namespaceData = this.namespaceDataThreadLocal.get();
        }
        
        // check to see if domain switched (eg. during login)
        if ((namespaceData != null) && (tenantDomain.equals(namespaceData.getTenantDomain())))
        {
            return namespaceData; // return threadlocal, if set
        }   
        
        return null;
    }
    
    // remove threadlocal
    private void removeNamespaceLocal(String tenantDomain)
    {
        if (tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
        {
            defaultNamespaceDataThreadLocal.set(null); // it's in the cache, clear the threadlocal
        }
        else
        {
            namespaceDataThreadLocal.set(null); // it's in the cache, clear the threadlocal
        }
    }
    
    private void removeNamespaceData(String tenantDomain)
    {
        try
        {
            writeLock.lock();
            if (namespaceDataCache.get(tenantDomain) != null)
            {
                namespaceDataCache.remove(tenantDomain);
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
        return getNamespaceData(tenantDomain).getUrisCache();
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
        return getNamespaceData(tenantDomain).getPrefixesCache();
    }
    
    /**
     * Local helper - returns tenant domain (or empty string if default non-tenant)
     */
    private String getTenantDomain()
    {
        return tenantService.getCurrentUserDomain();
    }
    
    /* package */ class NamespaceData
    {
        private List<String> urisCache;
        private Map<String, String> prefixesCache;
        
        private String tenantDomain;
        
        public NamespaceData(String tenantDomain)
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
