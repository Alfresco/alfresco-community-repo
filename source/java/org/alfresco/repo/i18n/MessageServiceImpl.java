package org.alfresco.repo.i18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Message Service to get localised messages/strings which have been loaded from resource bundles either dynamically 
 * deployed in the Repository and/or statically loaded from the Classpath.
 * 
 * Also provides methods (delegated to core utility class) to access the Locale of the current thread.
 */
public class MessageServiceImpl implements MessageService
{
    private static final Log logger = LogFactory.getLog(MessageServiceImpl.class);
    
    public static final String PROPERTIES_FILE_SUFFIX = ".properties";
    
    /**
     * Lock objects
     */
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();

    // dependencies
    private ServiceRegistry serviceRegistry;
    private TenantService tenantService;
    
    /**
     * List of registered bundles
     */
    private SimpleCache<String, Set<String>> resourceBundleBaseNamesCache;

    /**
     * Map of loaded bundles by Locale
     */
    private SimpleCache<String, Map<Locale, Set<String>>> loadedResourceBundlesCache;

    /**
     * Map of cached messaged by Locale
     */
    private SimpleCache<String, Map<Locale, Map<String, String>>> messagesCache;

    
    private List<MessageDeployer> messageDeployers = new ArrayList<MessageDeployer>();


    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
     
    public void setResourceBundleBaseNamesCache(SimpleCache<String, Set<String>> resourceBundleBaseNamesCache)
    {
        this.resourceBundleBaseNamesCache = resourceBundleBaseNamesCache;
    }

    public void setLoadedResourceBundlesCache(SimpleCache<String, Map<Locale, Set<String>>> loadedResourceBundlesCache)
    {
        this.loadedResourceBundlesCache = loadedResourceBundlesCache;
    }
    
    public void setMessagesCache(SimpleCache<String, Map<Locale, Map<String, String>>>messagesCache)
    {
        this.messagesCache = messagesCache;
    }
    

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#setLocale(java.util.Locale)
     */
    public void setLocale(Locale locale)
    {
        I18NUtil.setLocale(locale);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#getLocale()
     */
    public Locale getLocale()
    {
        return I18NUtil.getLocale();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#setContentLocale(java.util.Locale)
     */
    public void setContentLocale(Locale locale)
    {
        I18NUtil.setContentLocale(locale);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#getContentLocale()
     */
    public Locale getContentLocale()
    {
        return I18NUtil.getContentLocale();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#getNearestLocale(java.util.Locale, java.util.Set)
     */
    public Locale getNearestLocale(Locale templateLocale, Set<Locale> options)
    {
        return I18NUtil.getNearestLocale(templateLocale, options);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#parseLocale(java.lang.String)
     */
    public Locale parseLocale(String localeStr)
    {
        return I18NUtil.parseLocale(localeStr);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#registerResourceBundle(java.lang.String)
     */
    public void registerResourceBundle(String resBundlePath)
    {
        String tenantDomain = getTenantDomain(); 
        Set<String> tenantResourceBundleBaseNames = null;
        
        try
        {
            readLock.lock();         
            tenantResourceBundleBaseNames = getResourceBundleBaseNames(tenantDomain);  
        }
        finally
        {
            readLock.unlock();
        }
        
        try
        {
            writeLock.lock();
            
            if (! tenantResourceBundleBaseNames.contains(resBundlePath))
            {
            	tenantResourceBundleBaseNames.add(resBundlePath);
            }

            logger.info("Registered message bundle '" + resBundlePath + "'");
            
            clearLoadedResourceBundles(tenantDomain); // force re-load of message cache
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#getMessage(java.lang.String)
     */
    public String getMessage(String messageKey)
    {
        return getMessage(messageKey, getLocale());
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#getMessage(java.lang.String, java.util.Locale)
     */
    public String getMessage(final String messageKey, final Locale locale)
    {
        String message = null;
                  
        // look for message, within context of tenant if applicable
        Map<String, String> props = getLocaleProperties(locale);
        if (props != null)
        {
            // get runtime/repo managed message (if it exists)
            message = props.get(messageKey);
        }
                            
        if (message == null)
        {           
            if (tenantService.isTenantUser())
            {
                // tenant user, with no tenant-specific message
                
                //look for non-tenant-specific message
                message = AuthenticationUtil.runAs(new RunAsWork<String>()
                        {
                            public String doWork() throws Exception
                            {
                                String message = null;
                                Map<String, String> props = getLocaleProperties(locale);
                                if (props != null)
                                {
                                    // get default runtime/repo managed message (if it exists)
                                    message = props.get(messageKey);
                                }                                            
                                return message;    
                            }
                        }, AuthenticationUtil.getSystemUserName());        
            }
          
            if (message == null)
            {
                // get default static message (if it exists)
                message = I18NUtil.getMessage(tenantService.getBaseName(messageKey));
            }
        }
        
        return message;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#getMessage(java.lang.String, java.lang.Object[])
     */
    public String getMessage(String messageKey, Object ... params)
    {
        return getMessage(messageKey, getLocale(), params);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#getMessage(java.lang.String, java.util.Locale, java.lang.Object[])
     */
    public String getMessage(String messageKey, Locale locale, Object ... params)
    {
        String message = getMessage(messageKey, locale);
        if (message != null && params != null)
        {
            message = MessageFormat.format(message, params);
        }
        return message;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#unregisterResourceBundle(java.lang.String)
     */
    public void unregisterResourceBundle(String resBundlePath)
    {
        Map<Locale, Set<String>> loadedResourceBundlesForAllLocales;
        Map<Locale, Map<String, String>> cachedMessagesForAllLocales;
        Set<String> resourceBundleBaseNamesForAllLocales;
        
        String tenantDomain = getTenantDomain();
        
        try
        {
            readLock.lock();

            // all locales
            loadedResourceBundlesForAllLocales = getLoadedResourceBundles(tenantDomain);
            cachedMessagesForAllLocales = getMessages(tenantDomain);
            resourceBundleBaseNamesForAllLocales = getResourceBundleBaseNames(tenantDomain);
        }    
        finally
        {
            readLock.unlock();
        }
        
        try
        {
            writeLock.lock();
                      
            // unload resource bundles for each locale (by tenant, if applicable)        
            if ((loadedResourceBundlesForAllLocales != null) && (cachedMessagesForAllLocales != null))
            {
                Iterator<Locale> itr = loadedResourceBundlesForAllLocales.keySet().iterator();
                
                while (itr.hasNext())
                {   
                    Locale locale = itr.next();
                    
                    Set<String> loadedBundles = loadedResourceBundlesForAllLocales.get(locale);
                    Map<String, String> props = cachedMessagesForAllLocales.get(locale);
                    
                    if ((loadedBundles != null) && (props != null))
                    {
                        if (loadedBundles.contains(resBundlePath))
                        {
                            ResourceBundle resourcebundle = null;
                
                            int idx1 = resBundlePath.indexOf(StoreRef.URI_FILLER);
                           
                            if (idx1 != -1)
                            {
                                // load from repository
                                int idx2 = resBundlePath.indexOf("/", idx1+3);
                
                                String store = resBundlePath.substring(0, idx2);
                                String path = resBundlePath.substring(idx2);
                
                                StoreRef storeRef = tenantService.getName(new StoreRef(store));
                                
                                try
                                {
                                    resourcebundle = getRepoResourceBundle(storeRef, path, locale);
                                }
                                catch (IOException ioe)
                                {
                                    throw new AlfrescoRuntimeException("Failed to read message resource bundle from repository " + resBundlePath + " : " + ioe);
                                }
                            }
                            else
                            {
                                // load from classpath
                                resourcebundle = ResourceBundle.getBundle(resBundlePath, locale);
                            }
                
                            if (resourcebundle != null)
                            {
                                // unload from the cached messages
                                Enumeration<String> enumKeys = resourcebundle.getKeys();
                                while (enumKeys.hasMoreElements() == true)
                                {
                                    String key = enumKeys.nextElement();
                                    props.remove(key);
                                }
                            }
                
                            loadedBundles.remove(resBundlePath);
                        }
                    }
                }
            }
            
            // unregister resource bundle
            if (resourceBundleBaseNamesForAllLocales != null)
            {
                resourceBundleBaseNamesForAllLocales.remove(resBundlePath);
                logger.info("Unregistered message bundle '" + resBundlePath + "'");
            }
                     
            clearLoadedResourceBundles(tenantDomain); // force re-load of message cache
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * Get the messages for a locale.
     * <p>
     * Will use cache where available otherwise will load into cache from bundles.
     *
     * @param locale    the locale
     * @return          message map
     */
    private Map<String, String> getLocaleProperties(Locale locale)
    {
        Set<String> loadedBundles = null;
        Map<String, String> props = null;

        int loadedBundleCount = 0;
             
        String tenantDomain = getTenantDomain();
        boolean init = false;
        
        Map<Locale, Set<String>> tenantLoadedResourceBundles = null;
        Map<Locale, Map<String, String>> tenantCachedMessages = null;
        Set<String> tenantResourceBundleBaseNames = null;
            
        try
        {
            readLock.lock();       

            tenantLoadedResourceBundles = getLoadedResourceBundles(tenantDomain);
            loadedBundles = tenantLoadedResourceBundles.get(locale);

            tenantCachedMessages = getMessages(tenantDomain);
            props = tenantCachedMessages.get(locale);

            tenantResourceBundleBaseNames = getResourceBundleBaseNames(tenantDomain);
            loadedBundleCount = tenantResourceBundleBaseNames.size();
        }
        finally
        {
            readLock.unlock();
        }

        if (loadedBundles == null)
        {
            try
            {
                writeLock.lock();
                loadedBundles = new HashSet<String>();
                tenantLoadedResourceBundles.put(locale, loadedBundles);
                init = true;
            }
            finally
            {
                writeLock.unlock();
            }
        }

        if (props == null)
        {
            try
            {
                writeLock.lock();
                
                props = new HashMap<String, String>();
                tenantCachedMessages.put(locale, props);
                init = true;
            }
            finally
            {
                writeLock.unlock();
            }
        }

        if ((loadedBundles.size() != loadedBundleCount) || (init == true))
        {
            try
            {
                writeLock.lock();

                // get registered resource bundles               
                Set<String> resBundleBaseNames = getResourceBundleBaseNames(tenantDomain);

                int count = 0;
                
                // load resource bundles for given locale (by tenant, if applicable)
                for (String resBundlePath : resBundleBaseNames)
                {
                    if (loadedBundles.contains(resBundlePath) == false)
                    {
                        ResourceBundle resourcebundle = null;

                        int idx1 = resBundlePath.indexOf(StoreRef.URI_FILLER);
                       
                        if (idx1 != -1)
                        {
                            // load from repository
                            int idx2 = resBundlePath.indexOf("/", idx1+3);

                            String store = resBundlePath.substring(0, idx2);
                            String path = resBundlePath.substring(idx2);

                            StoreRef storeRef = tenantService.getName(new StoreRef(store));
                            
                            try
                            {
                                resourcebundle = getRepoResourceBundle(storeRef, path, locale);
                            }
                            catch (IOException ioe)
                            {
                                throw new AlfrescoRuntimeException("Failed to read message resource bundle from repository " + resBundlePath + " : " + ioe);
                            }
                        }
                        else
                        {
                            // load from classpath
                            resourcebundle = ResourceBundle.getBundle(resBundlePath, locale);
                        }

                        if (resourcebundle != null)
                        {
                            Enumeration<String> enumKeys = resourcebundle.getKeys();
                            while (enumKeys.hasMoreElements() == true)
                            {
                                String key = enumKeys.nextElement();
                                props.put(key, resourcebundle.getString(key));
                            }
    
                            loadedBundles.add(resBundlePath);
                            count++;
                        }
                    }
                }
                
                logger.info("Message bundles (x " + count + ") loaded for locale " + locale);
            }
            finally
            {
                writeLock.unlock();
            }
        }

        return props;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#getRepoResourceBundle(org.alfresco.service.cmr.repository.StoreRef, java.lang.String, java.util.Locale)
     */
    public ResourceBundle getRepoResourceBundle(StoreRef storeRef, String path, Locale locale) throws IOException
    {   
        ResourceBundle resBundle = null;
        
        // TODO - need to replace basic strategy with a more complete
        // search & instantiation strategy similar to ResourceBundle.getBundle()
        // Consider search query with basename* and then apply strategy ...
  
        SearchService searchService = serviceRegistry.getSearchService();
        NamespaceService resolver = serviceRegistry.getNamespaceService();
            
        NodeRef rootNode = serviceRegistry.getNodeService().getRootNode(storeRef);        
        
        // first attempt - with locale        
        List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, path+"_"+locale+PROPERTIES_FILE_SUFFIX, null, resolver, false);
        
        if ((nodeRefs == null) || (nodeRefs.size() == 0))
        {
            // second attempt - basename 
            nodeRefs = searchService.selectNodes(rootNode, path+PROPERTIES_FILE_SUFFIX, null, resolver, false);
           
            if ((nodeRefs == null) || (nodeRefs.size() == 0))
            {
                logger.debug("Could not find message resource bundle " + storeRef + "/" + path);
                return null;
            }
        }
        
        if (nodeRefs.size() > 1)
        {
            throw new RuntimeException("Found more than one message resource bundle " + storeRef + path);
        }
        else
        {
            NodeRef messageResourceNodeRef = nodeRefs.get(0);
    
            ContentReader cr = serviceRegistry.getContentService().getReader(messageResourceNodeRef, ContentModel.PROP_CONTENT);
            resBundle = new MessagePropertyResourceBundle(new InputStreamReader(cr.getContentInputStream(), cr.getEncoding()));
        }
        
        return resBundle;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#onEnableTenant()
     */
    public void onEnableTenant()
    {
        // NOOP - refer to DictionaryRepositoryBootstrap
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#onDisableTenant()
     */
    public void onDisableTenant()
    {
        destroy(); // will be called in context of tenant
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#init()
     */
    public void init()
    { 
        // initialise empty message service       
        String tenantDomain = getTenantDomain();
        
        putResourceBundleBaseNames(tenantDomain, new HashSet<String>());
        putLoadedResourceBundles(tenantDomain, new HashMap<Locale, Set<String>>());
        putMessages(tenantDomain, new HashMap<Locale, Map<String, String>>());
        
        logger.info("Empty message service initialised");
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#destroy()
     */
    public void destroy()
    {
        // used by reset and also as callback when destroying tenant(s) during shutdown
        String tenantDomain = getTenantDomain();
        
        removeLoadedResourceBundles(tenantDomain);
        removeMessages(tenantDomain);
        removeResourceBundleBaseNames(tenantDomain);
        
        logger.info("Messages cache destroyed (all locales)");
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#getRegisteredBundles()
     */
    public Set<String> getRegisteredBundles()
    {
        try
        {
            readLock.lock();      
            return getResourceBundleBaseNames(getTenantDomain());
        }
        finally
        {
            readLock.unlock();
        }
    }  
    
    private Set<String> getResourceBundleBaseNames(String tenantDomain)
    {     
        Set<String> resourceBundleBaseNames = resourceBundleBaseNamesCache.get(tenantDomain);
        
        if (resourceBundleBaseNames == null)
        {     
            try 
            {
                // assume caller has read lock - upgrade lock manually
                readLock.unlock();
                writeLock.lock();
                
                reset(tenantDomain); // reset caches - may have been invalidated (e.g. in a cluster)
                resourceBundleBaseNames = resourceBundleBaseNamesCache.get(tenantDomain);             
            }
            finally
            {
                readLock.lock();  // reacquire read without giving up write lock
                writeLock.unlock(); // unlock write, still hold read - caller must unlock the read
            }
            
            if (resourceBundleBaseNames == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to re-initialise resourceBundleBaseNamesCache " + tenantDomain);
            }
        }
            
        return resourceBundleBaseNames;
    }  
    
    private void putResourceBundleBaseNames(String tenantDomain, Set<String> resourceBundleBaseNames)
    {
        resourceBundleBaseNamesCache.put(tenantDomain, resourceBundleBaseNames);
    } 
    
    private void removeResourceBundleBaseNames(String tenantDomain)
    {
        if (resourceBundleBaseNamesCache.get(tenantDomain) != null)
        {
            resourceBundleBaseNamesCache.get(tenantDomain).clear();
            resourceBundleBaseNamesCache.remove(tenantDomain);
        }
    } 
    
    private Map<Locale, Set<String>> getLoadedResourceBundles(String tenantDomain)
    {
        Map<Locale, Set<String>> loadedResourceBundles = loadedResourceBundlesCache.get(tenantDomain);
        
        if (loadedResourceBundles == null)
        {   
            try 
            {
                // assume caller has read lock - upgrade lock manually
                readLock.unlock();
                writeLock.lock();
                
                reset(tenantDomain); // reset caches - may have been invalidated (e.g. in a cluster)
                loadedResourceBundles = loadedResourceBundlesCache.get(tenantDomain);
            }
            finally
            {
                readLock.lock();  // reacquire read without giving up write lock
                writeLock.unlock(); // unlock write, still hold read - caller must unlock the read
            }
            
            if (loadedResourceBundles == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to re-initialise loadedResourceBundlesCache " + tenantDomain);
            }
        }
            
        return loadedResourceBundles;
    }  
    
    private void putLoadedResourceBundles(String tenantDomain, Map<Locale, Set<String>> loadedResourceBundles)
    {
        loadedResourceBundlesCache.put(tenantDomain, loadedResourceBundles);
    } 
    
    private void removeLoadedResourceBundles(String tenantDomain)
    {
        if (loadedResourceBundlesCache.get(tenantDomain) != null)
        {
            loadedResourceBundlesCache.get(tenantDomain).clear();
            loadedResourceBundlesCache.remove(tenantDomain);
        }
    } 
    
    private void clearLoadedResourceBundles(String tenantDomain)
    {
        if (loadedResourceBundlesCache.get(tenantDomain) != null)
        {
            loadedResourceBundlesCache.get(tenantDomain).clear();
        }
    } 
    
    private Map<Locale, Map<String, String>> getMessages(String tenantDomain)
    {
        Map<Locale, Map<String, String>> messages = messagesCache.get(tenantDomain);
        
        if (messages == null)
        {   
            try 
            {
                // assume caller has read lock - upgrade lock manually
                readLock.unlock();
                writeLock.lock();
                
                reset(tenantDomain); // reset caches - may have been invalidated (e.g. in a cluster)
                messages = messagesCache.get(tenantDomain);             
            }
            finally
            {
                readLock.lock();  // reacquire read without giving up write lock
                writeLock.unlock(); // unlock write, still hold read - caller must unlock the read
            }
            
            if (messages == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to re-initialise messagesCache " + tenantDomain);
            }
        }
            
        return messages;
    }  
    
    private void putMessages(String tenantDomain, Map<Locale, Map<String, String>> messages)
    {
        messagesCache.put(tenantDomain, messages);
    } 
    
    private void removeMessages(String tenantDomain)
    {
        if (messagesCache.get(tenantDomain) != null)
        {
            messagesCache.get(tenantDomain).clear();
            messagesCache.remove(tenantDomain);
        }
    } 
    
    // local helper - returns tenant domain (or empty string if default non-tenant)
    private String getTenantDomain()
    {
        return tenantService.getCurrentUserDomain();
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.i18n.MessageService#register(org.alfresco.repo.i18n.MessageDeployer)
     */
    public void register(MessageDeployer messageDeployer)
    {
        if (! messageDeployers.contains(messageDeployer))
        {
            messageDeployers.add(messageDeployer);
        }
    }
    
    /**
     * Resets the message service
     */      
    public void reset()
    {
        reset(getTenantDomain());
    }
    
    private void reset(String tenantDomain)
    {
        if (logger.isDebugEnabled()) 
        {
            logger.debug("Resetting messages ...");
        }
       
        String userName;
        if (tenantDomain == "")
        {
            userName = AuthenticationUtil.getSystemUserName();
        }
        else
        {
            userName = tenantService.getDomainUser(TenantService.ADMIN_BASENAME, tenantDomain);
        }
       
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {  
                destroy();
                init();
               
                for (final MessageDeployer messageDeployer : messageDeployers)
                {
                    messageDeployer.initMessages();
                }
               
                return null;
            }                               
        }, userName);
    
        if (logger.isDebugEnabled()) 
        {
            logger.debug("... resetting messages completed");
        }
    }
    
    /**
     * Message Resource Bundle
     * 
     * Custom message property resource bundle, to overcome known limitation of JDK 5.0 (and lower).
     *
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6204853
     * 
     * Note: JDK 6.0 provides the ability to construct a PropertyResourceBundle from a Reader.
     */
    private class MessagePropertyResourceBundle extends ResourceBundle
    {   
        private Properties properties = new Properties();
        
        public MessagePropertyResourceBundle(Reader reader) throws IOException
        {
            try
            {
                BufferedReader br = new BufferedReader(reader);
                String line = br.readLine();
                while (line != null)
                {
                    if ((line.length() > 0) && (line.charAt(0) != '#'))
                    {
                        String[] splits = line.split("=");
                        if (splits.length != 2)
                        {
                            throw new AlfrescoRuntimeException("Unexpected message properties file format: " + line);
                        }
                        properties.put(splits[0], splits[1]);
                    }
                    line = br.readLine();
                }
            }
            finally
            {
                reader.close();
            }
        }
        
        @Override
        public Enumeration<String> getKeys()
        {
           List<String> keys = new ArrayList<String>();
           Enumeration<Object> enums = properties.keys();
           while (enums.hasMoreElements())
           {
               keys.add((String)enums.nextElement());
           }
           return new StringIteratorEnumeration(keys.iterator());
        }

        @Override
        protected Object handleGetObject(String arg0)
        {
            return properties.get(arg0);
        }        
        
        private class StringIteratorEnumeration implements Enumeration<String>
        {
            private Iterator<String> enums;
            
            public StringIteratorEnumeration(Iterator<String> enums)
            {
                this.enums = enums;
            }
            
            public boolean hasMoreElements()
            {
                return enums.hasNext();
            }

            public String nextElement()
            {
                return enums.next();
            }
        }
    }
}
