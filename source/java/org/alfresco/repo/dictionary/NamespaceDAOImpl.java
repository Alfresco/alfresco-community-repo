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
    
    private static final long serialVersionUID = -1085431310721591548L;
    
    /**
     * Lock objects
     */
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();
    
    // internal caches that are clusterable
    private SimpleCache<String, List<String>> urisCache;
    private SimpleCache<String, Map<String, String>> prefixesCache;

    // Dependencies
    private TenantService tenantService;
    
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setUrisCache(SimpleCache<String, List<String>> urisCache)
    {
        this.urisCache = urisCache;
    }
    
    public void setPrefixesCache(SimpleCache<String, Map<String, String>> prefixesCache)
    {
        this.prefixesCache = prefixesCache;
    }
    
    
    private DictionaryDAO dictionaryDAO;
    
    public void registerDictionary(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }

    
    /**
     * Initialise empty namespaces
     */
    public void init()
    {     
        String tenantDomain = getTenantDomain();
            
        putUrisCtx(tenantDomain, new ArrayList<String>());
        putPrefixesCtx(tenantDomain, new HashMap<String, String>());

        if (logger.isDebugEnabled()) 
        {
            logger.debug("Empty namespaces initialised");
        }
    }
    
    /**
     * Destroy the namespaces
     */
    public void destroy()
    {
        String tenantDomain = getTenantDomain();

        removeUrisCtx(tenantDomain);
        removePrefixesCtx(tenantDomain);
        
        if (logger.isDebugEnabled()) 
        {
            logger.debug("Namespaces destroyed");
        }
    }
    
    /**
     * Resets the namespaces (by resetting the dictionary)
     */
    private void reset()
    {
       if (logger.isDebugEnabled()) 
       {
           logger.debug("Resetting namespaces ...");
       }
       
       if (dictionaryDAO == null)
       {
           // Unexpected
           throw new AlfrescoRuntimeException("Dictionary should be registered in order to perform reset");
       }
       else
       {
           dictionaryDAO.reset();
       }
       
       if (logger.isDebugEnabled()) 
       {
           logger.debug("... resetting namespaces completed");
       }
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
        List<String> uris = null;
        try
        {
            readLock.lock();
            uris = urisCache.get(tenantDomain);
        }
        finally
        {
            readLock.unlock();
        }
        
        
        if (uris == null)
        {
            reset(); // reset caches - may have been invalidated (e.g. in a cluster)
            
            try
            {
                readLock.lock();
                uris = urisCache.get(tenantDomain);
            }
            finally
            {
                readLock.unlock();
            }
            
            if (uris == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to re-initialise urisCache " + tenantDomain);
            }
        }
        return uris;
    }
    
    /**
     * Put URIs into the cache
     * 
     * @param tenantDomain
     * @param uris
     */
    private void putUrisCtx(String tenantDomain, List<String> uris)
    {
        try 
        {
            writeLock.lock();
            urisCache.put(tenantDomain, uris);
        }
        finally
        {
            writeLock.unlock();
        }          
    } 
    
    /**
     * Remove URIs from the cache
     * 
     * @param tenantDomain
     */
    private void removeUrisCtx(String tenantDomain)
    {
        try 
        {
            writeLock.lock();
            if (urisCache.get(tenantDomain) != null)
            {
                urisCache.get(tenantDomain).clear();
                urisCache.remove(tenantDomain);
            }
        }
        finally
        {
            writeLock.unlock();
        }  
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
        Map<String, String> prefixes = null;       
        try
        {
            readLock.lock();
            prefixes = prefixesCache.get(tenantDomain);
        }
        finally
        {
            readLock.unlock();
        }
        
        if (prefixes == null)
        {
            reset(); // reset caches - may have been invalidated (e.g. in a cluster)
            
            try
            {
                readLock.lock();
                prefixes = prefixesCache.get(tenantDomain);
            }
            finally
            {
                readLock.unlock();
            }
            
            if (prefixes == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to re-initialise prefixesCache " + tenantDomain);
            }
        }
            
        return prefixes;
    }  

    /**
     * Put prefixes into the cache
     * 
     * @param tenantDomain
     * @param prefixes
     */
    private void putPrefixesCtx(String tenantDomain, Map<String, String> prefixes)
    {
        try 
        {
            writeLock.lock();
            prefixesCache.put(tenantDomain, prefixes);
        }
        finally
        {
            writeLock.unlock();
        }               
    } 
    
    /**
     * Remove prefixes from the cache
     * 
     * @param tenantDomain
     */
    private void removePrefixesCtx(String tenantDomain)
    {
        try 
        {
            writeLock.lock();
            if (prefixesCache.get(tenantDomain) != null)
            {
                prefixesCache.get(tenantDomain).clear();
                prefixesCache.remove(tenantDomain);
            }
        }
        finally
        {
            writeLock.unlock();
        }        
    } 
    
    /**
     * Local helper - returns tenant domain (or empty string if default non-tenant)
     */
    private String getTenantDomain()
    {
        return tenantService.getCurrentUserDomain();
    }
}
