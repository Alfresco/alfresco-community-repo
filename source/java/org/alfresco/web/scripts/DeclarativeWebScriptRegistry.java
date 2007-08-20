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
package org.alfresco.web.scripts;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;

import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.util.AbstractLifecycleBean;
import org.alfresco.web.scripts.WebScriptDescription.FormatStyle;
import org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication;
import org.alfresco.web.scripts.WebScriptDescription.RequiredTransaction;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.context.ServletContextAware;


/**
 * Registry of declarative (scripted/template driven) Web Scripts
 * 
 * @author davidc
 */
public class DeclarativeWebScriptRegistry extends AbstractLifecycleBean 
    implements WebScriptRegistry, ServletContextAware, InitializingBean
{
    // Logger
    private static final Log logger = LogFactory.getLog(DeclarativeWebScriptRegistry.class);

    private ServletContext servletContext;
    private String defaultWebScript;
    private FormatRegistry formatRegistry;
    private WebScriptStorage storage;
    private TemplateImageResolver imageResolver;
    
    
    // map of web scripts by id
    // NOTE: The map is sorted by id (ascending order)
    private Map<String, WebScript> webscriptsById = new TreeMap<String, WebScript>();
    
    // map of web scripts by url
    // NOTE: The map is sorted by url (descending order)
    private Map<String, URLIndex> webscriptsByURL = new TreeMap<String, URLIndex>(Collections.reverseOrder());
    
    // map of web script packages by path
    private Map<String, Path> packageByPath = new TreeMap<String, Path>();

    // map of web script uris by path
    private Map<String, Path> uriByPath = new TreeMap<String, Path>();

    
    //
    // Initialisation
    // 
    
    /**
     * Sets the available Web Script Stores
     * 
     * @param storage
     */
    public void setStorage(WebScriptStorage storage)
    {
        this.storage = storage;
    }

    /**
     * Sets the default service implementation bean
     * 
     * @param defaultWebScript
     */
    public void setDefaultWebScript(String defaultWebScript)
    {
        this.defaultWebScript = defaultWebScript;
    }

    /**
     * Sets the response format registry
     * 
     * @param formatRegistry
     */
    public void setFormatRegistry(FormatRegistry formatRegistry)
    {
        this.formatRegistry = formatRegistry;
    }

    /* (non-Javadoc)
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext context)
    {
        this.servletContext = context;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        this.imageResolver = new TemplateImageResolver()
        {
            public String resolveImagePathForName(String filename, FileTypeImageSize size)
            {
                return Utils.getFileTypeImage(servletContext, filename, size);
            }  
        };        
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRegistry#reset()
     */
    public void reset()
    {
        long startTime = System.currentTimeMillis();
        try
        {
            getTemplateProcessor().resetCache();
            getScriptProcessor().resetCache();
            initWebScripts();
        }
        finally
        {
            if (logger.isInfoEnabled())
                logger.info("Registered " + webscriptsById.size() + " Web Scripts, " + webscriptsByURL.size() + " URLs (in " + (System.currentTimeMillis() - startTime) + "ms)");
        }        
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        reset();
    }

    /* (non-Javadoc)
     * @see org.alfresco.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }
    
    /**
     * Initialise Web Scripts
     *
     * Note: Each invocation of this method resets the list of the services
     */
    private void initWebScripts()
    {
        if (logger.isDebugEnabled())
            logger.debug("Initialising Web Scripts");
        
        // clear currently registered services
        webscriptsById.clear();
        webscriptsByURL.clear();
        packageByPath.clear();
        packageByPath.put("/", new Path("/"));
        uriByPath.clear();
        uriByPath.put("/", new Path("/"));
        
        // register services
        for (WebScriptStore apiStore : storage.getStores())
        {
            if (logger.isDebugEnabled())
                logger.debug("Locating Web Scripts within " + apiStore.getBasePath());
            
            String[] serviceDescPaths = apiStore.getDescriptionDocumentPaths();
            for (String serviceDescPath : serviceDescPaths)
            {
                try
                {
                    // build service description
                    WebScriptDescription serviceDesc = null;
                    InputStream serviceDescIS = null;
                    try
                    {
                        serviceDescIS = apiStore.getDocument(serviceDescPath);
                        serviceDesc = createDescription(apiStore, serviceDescPath, serviceDescIS);
                    }
                    catch(IOException e)
                    {
                        throw new WebScriptException("Failed to read Web Script description document " + apiStore.getBasePath() + serviceDescPath, e);
                    }
                    finally
                    {
                        try
                        {
                            if (serviceDescIS != null) serviceDescIS.close();
                        }
                        catch(IOException e)
                        {
                            // NOTE: ignore close exception
                        }
                    }
                    
                    // determine if service description has been registered
                    String id = serviceDesc.getId();
                    if (webscriptsById.containsKey(id))
                    {
                        // move to next service
                        if (logger.isDebugEnabled())
                        {
                            WebScript existingService = webscriptsById.get(id);
                            WebScriptDescription existingDesc = existingService.getDescription();
                            String msg = "Web Script description document " + serviceDesc.getStorePath() + "/" + serviceDesc.getDescPath();
                            msg += " overridden by " + existingDesc.getStorePath() + "/" + existingDesc.getDescPath();
                            logger.debug(msg);
                        }
                        continue;
                    }
                    
                    // construct service implementation
                    ApplicationContext applicationContext = getApplicationContext();
                    String beanName = "webscript." + id.replace('/', '.');
                    String serviceImplName = (applicationContext.containsBean(beanName)) ? beanName : defaultWebScript;
                    AbstractWebScript serviceImpl = (AbstractWebScript)applicationContext.getBean(serviceImplName);
                    serviceImpl.setDescription(serviceDesc);
                    serviceImpl.init(this);
                    
                    if (logger.isDebugEnabled())
                        logger.debug("Found Web Script " + id +  " (desc: " + serviceDescPath + ", impl: " + serviceImplName + ", auth: " + 
                                     serviceDesc.getRequiredAuthentication() + ", trx: " + serviceDesc.getRequiredTransaction() + ", format style: " + 
                                     serviceDesc.getFormatStyle() + ", default format: " + serviceDesc.getDefaultFormat() + ")");
                    
                    // register service and its urls
                    webscriptsById.put(id, serviceImpl);
                    for (String uriTemplate : serviceDesc.getURIs())
                    {
                        // establish static part of url template
                        boolean wildcard = false;
                        boolean extension = true;
                        int queryArgIdx = uriTemplate.indexOf('?');
                        if (queryArgIdx != -1)
                        {
                            uriTemplate = uriTemplate.substring(0, queryArgIdx);
                        }
                        int tokenIdx = uriTemplate.indexOf('{');
                        if (tokenIdx != -1)
                        {
                            uriTemplate = uriTemplate.substring(0, tokenIdx);
                            wildcard = true;
                        }
                        if (serviceDesc.getFormatStyle() != WebScriptDescription.FormatStyle.argument)
                        {
                            int extIdx = uriTemplate.lastIndexOf(".");
                            if (extIdx != -1)
                            {
                                uriTemplate = uriTemplate.substring(0, extIdx);
                            }
                            extension = false;
                        }
                        
                        // index service by static part of url (ensuring no other service has already claimed the url)
                        String uriIdx = serviceDesc.getMethod().toString() + ":" + uriTemplate;
                        if (webscriptsByURL.containsKey(uriIdx))
                        {
                            URLIndex urlIndex = webscriptsByURL.get(uriIdx);
                            WebScript existingService = urlIndex.script;
                            if (!existingService.getDescription().getId().equals(serviceDesc.getId()))
                            {
                                String msg = "Web Script document " + serviceDesc.getDescPath() + " is attempting to define the url '" + uriIdx + "' already defined by " + existingService.getDescription().getDescPath();
                                throw new WebScriptException(msg);
                            }
                        }
                        else
                        {
                            URLIndex urlIndex = new URLIndex(uriTemplate, wildcard, extension, serviceImpl);
                            webscriptsByURL.put(uriIdx, urlIndex);
                            
                            if (logger.isDebugEnabled())
                                logger.debug("Registered Web Script URL '" + uriIdx + "'");
                        }
                    }
    
                    // build path indexes to web script
                    registerPackage(serviceImpl);
                    registerURIs(serviceImpl);
                }
                catch(WebScriptException e)
                {
                    if (logger.isWarnEnabled())
                    {
                        Throwable c = e;
                        String cause = c.getMessage();
                        while (c.getCause() != null && !c.getCause().equals(c))
                        {
                            c = c.getCause();
                            cause += " ; " + c.getMessage(); 
                        }
                        String msg = "Unable to register script " + apiStore.getBasePath() + "/" + serviceDescPath + " due to error: " + cause;
                        logger.warn(msg);
                    }
                    else
                    {
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * Register a Web Script Package
     * 
     * @param script
     */
    private void registerPackage(WebScript script)
    {
        WebScriptDescription desc = script.getDescription();
        Path path = packageByPath.get("/");
        String[] parts = desc.getScriptPath().split("/");
        for (String part : parts)
        {
            Path subpath = packageByPath.get(Path.concatPath(path.getPath(), part));
            if (subpath == null)
            {
                subpath = path.createChildPath(part);
                packageByPath.put(subpath.getPath(), subpath);
            }      
            path = subpath;
        }
        path.addScript(script);
    }
    
    /**
     * Register a Web Script URI
     * 
     * @param script
     */
    private void registerURIs(WebScript script)
    {
        WebScriptDescription desc = script.getDescription();
        for (String uri : desc.getURIs())
        {
            Path path = uriByPath.get("/");
            String[] parts = uri.split("/");
            for (String part : parts)
            {
                if (part.indexOf("?") != -1)
                {
                    part = part.substring(0, part.indexOf("?"));
                }
                Path subpath = uriByPath.get(Path.concatPath(path.getPath(), part));
                if (subpath == null)
                {
                    subpath = path.createChildPath(part);
                    uriByPath.put(subpath.getPath(), subpath);
                }
                path = subpath;
            }
            path.addScript(script);
        }
    }
    
    /**
     * Create an Web Script Description
     * 
     * @param store
     * @param serviceDescPath
     * @param serviceDoc
     * 
     * @return  web script service description
     */
    private WebScriptDescription createDescription(WebScriptStore store, String serviceDescPath, InputStream serviceDoc)
    {
        SAXReader reader = new SAXReader();
        try
        {
            // retrieve script path
            int iPathIdx = serviceDescPath.lastIndexOf('/');
            String scriptPath = serviceDescPath.substring(0, iPathIdx == -1 ? 0 : iPathIdx);
            
            // retrieve script id
            String id = serviceDescPath.substring(0, serviceDescPath.lastIndexOf(".desc.xml"));
            
            // retrieve http method
            int methodIdx = id.lastIndexOf('.');
            if (methodIdx == -1 || (methodIdx == id.length() - 1))
            {
                throw new WebScriptException("Unable to establish HTTP Method from web script description: naming convention must be <name>.<method>.desc.xml");
            }
            String method = id.substring(methodIdx + 1).toUpperCase();

            // parse description document
            Document document = reader.read(serviceDoc);
            Element rootElement = document.getRootElement();
            if (!rootElement.getName().equals("webscript"))
            {
                throw new WebScriptException("Expected <webscript> root element - found <" + rootElement.getName() + ">");
            }

            // retrieve short name
            Element shortNameElement = rootElement.element("shortname");
            if (shortNameElement == null || shortNameElement.getTextTrim() == null || shortNameElement.getTextTrim().length() == 0)
            {
                throw new WebScriptException("Expected <shortname> value");
            }
            String shortName = shortNameElement.getTextTrim();

            // retrieve description
            String description = null;
            Element descriptionElement = rootElement.element("description");
            if (descriptionElement != null)
            {
                description = descriptionElement.getTextTrim();
            }
            
            // retrieve urls
            List urlElements = rootElement.elements("url");
            if (urlElements == null || urlElements.size() == 0)
            {
                throw new WebScriptException("Expected at least one <url> element");
            }
            List<String> uris = new ArrayList<String>();
            Iterator iterElements = urlElements.iterator();
            while(iterElements.hasNext())
            {
                // retrieve url element
                Element urlElement = (Element)iterElements.next();
                
                // retrieve url template
                String template = urlElement.getTextTrim();
                if (template == null || template.length() == 0)
                {
                    // NOTE: for backwards compatibility only
                    template = urlElement.attributeValue("template");
                    if (template == null || template.length() == 0)
                    {
                        throw new WebScriptException("Expected <url> element value");
                    }
                }
                uris.add(template);
            }
            
            // retrieve authentication
            RequiredAuthentication reqAuth = RequiredAuthentication.none;
            Element authElement = rootElement.element("authentication");
            if (authElement != null)
            {
                String reqAuthStr = authElement.getTextTrim();
                if (reqAuthStr == null || reqAuthStr.length() == 0)
                {
                    throw new WebScriptException("Expected <authentication> value");
                }
                reqAuth = RequiredAuthentication.valueOf(reqAuthStr);
                if (reqAuth == null)
                {
                    throw new WebScriptException("Authentication '" + reqAuthStr + "' is not a valid value");
                }
            }
            
            // retrieve transaction
            RequiredTransaction reqTrx = (reqAuth == RequiredAuthentication.none) ? RequiredTransaction.none : RequiredTransaction.required;
            Element trxElement = rootElement.element("transaction");
            if (trxElement != null)
            {
                String reqTrxStr = trxElement.getTextTrim();
                if (reqTrxStr == null || reqTrxStr.length() == 0)
                {
                    throw new WebScriptException("Expected <transaction> value");
                }
                reqTrx = RequiredTransaction.valueOf(reqTrxStr);
                if (reqTrx == null)
                {
                    throw new WebScriptException("Transaction '" + reqTrxStr + "' is not a valid value");
                }
            }
            
            // retrieve format
            String defaultFormat = "html";
            FormatStyle formatStyle = FormatStyle.any;
            Element formatElement = rootElement.element("format");
            if (formatElement != null)
            {
                // establish if default is set explicitly
                String attrDefaultValue = formatElement.attributeValue("default");
                if (attrDefaultValue != null)
                {
                    defaultFormat = (attrDefaultValue.length() == 0) ? null : attrDefaultValue;
                }
                // establish format declaration style
                String formatStyleStr = formatElement.getTextTrim();
                if (formatStyleStr != null && formatStyleStr.length() > 0)
                {
                    formatStyle = FormatStyle.valueOf(formatStyleStr);
                    if (formatStyle == null)
                    {
                        throw new WebScriptException("Format Style '" + formatStyle + "' is not a valid value");
                    }
                }
            }
            
            // retrieve caching
            WebScriptCache cache = new WebScriptCache();
            Element cacheElement = rootElement.element("cache");
            if (cacheElement != null)
            {
                Element neverElement = cacheElement.element("never");
                if (neverElement != null)
                {
                    String neverStr = neverElement.getTextTrim();
                    boolean neverBool = (neverStr == null || neverStr.length() == 0) ? true : Boolean.valueOf(neverStr);
                    cache.setNeverCache(neverBool);
                }
                Element publicElement = cacheElement.element("public");
                if (publicElement != null)
                {
                    String publicStr = publicElement.getTextTrim();
                    boolean publicBool = (publicStr == null || publicStr.length() == 0) ? true : Boolean.valueOf(publicStr);
                    cache.setIsPublic(publicBool);
                }
                Element revalidateElement = cacheElement.element("mustrevalidate");
                if (revalidateElement != null)
                {
                    String revalidateStr = revalidateElement.getTextTrim();
                    boolean revalidateBool = (revalidateStr == null || revalidateStr.length() == 0) ? true : Boolean.valueOf(revalidateStr);
                    cache.setMustRevalidate(revalidateBool);
                }
            }
            
            // construct service description
            WebScriptDescriptionImpl serviceDesc = new WebScriptDescriptionImpl();
            serviceDesc.setStore(store);
            serviceDesc.setScriptPath(scriptPath);
            serviceDesc.setDescPath(serviceDescPath);
            serviceDesc.setId(id);
            serviceDesc.setShortName(shortName);
            serviceDesc.setDescription(description);
            serviceDesc.setRequiredAuthentication(reqAuth);
            serviceDesc.setRequiredTransaction(reqTrx);
            serviceDesc.setRequiredCache(cache);
            serviceDesc.setMethod(method);
            serviceDesc.setUris(uris.toArray(new String[uris.size()]));
            serviceDesc.setDefaultFormat(defaultFormat);
            serviceDesc.setFormatStyle(formatStyle);
            return serviceDesc;
        }
        catch(DocumentException e)
        {
            throw new WebScriptException("Failed to parse web script description document " + serviceDescPath, e);
        }
        catch(WebScriptException e)
        {
            throw new WebScriptException("Failed to parse web script description document " + serviceDescPath, e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRegistry#getPackage(java.lang.String)
     */
    public WebScriptPath getPackage(String scriptPackage)
    {
        return packageByPath.get(scriptPackage);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRegistry#getUri(java.lang.String)
     */
    public WebScriptPath getUri(String scriptUri)
    {
        return uriByPath.get(scriptUri);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRegistry#getWebScripts()
     */
    public Collection<WebScript> getWebScripts()
    {
        return webscriptsById.values();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRegistry#getWebScript(java.lang.String)
     */
    public WebScript getWebScript(String id)
    {
        return webscriptsById.get(id);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRegistry#findWebScript(java.lang.String, java.lang.String)
     */
    public WebScriptMatch findWebScript(String method, String uri)
    {
        long startTime = System.currentTimeMillis();
        
        // TODO: Replace with more efficient approach
        String matchedPath = null;
        DeclarativeWebScriptMatch apiServiceMatch = null;
        String match = method.toString().toUpperCase() + ":" + uri;
        String matchNoExt = method.toString().toUpperCase() + ":" + ((uri.indexOf('.') != -1) ? uri.substring(0, uri.indexOf('.')) : uri);
        
        // locate full match - on URI and METHOD
        for (Map.Entry<String, URLIndex> entry : webscriptsByURL.entrySet())
        {
            URLIndex urlIndex = entry.getValue();
            String index = entry.getKey();
            String test = urlIndex.includeExtension ? match : matchNoExt; 
            if ((urlIndex.wildcardPath && test.startsWith(index)) || (!urlIndex.wildcardPath && test.equals(index)))
            {
                apiServiceMatch = new DeclarativeWebScriptMatch(urlIndex.path, urlIndex.script); 
                break;
            }
            else if ((urlIndex.wildcardPath && uri.startsWith(urlIndex.path)) || (!urlIndex.wildcardPath && uri.equals(urlIndex.path)))
            {
                matchedPath = urlIndex.path;
            }
        }
        
        // locate URI match
        if (apiServiceMatch == null && matchedPath != null)
        {
            apiServiceMatch = new DeclarativeWebScriptMatch(matchedPath);
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Web Script index lookup for uri " + uri + " took " + (System.currentTimeMillis() - startTime) + "ms");
        
        return apiServiceMatch;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRegistry#getContext()
     */
    public ServletContext getContext()
    {
        return servletContext;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRegistry#getFormatRegistry()
     */
    public FormatRegistry getFormatRegistry()
    {
        return this.formatRegistry;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRegistry#getTemplateProcessor()
     */
    public TemplateProcessor getTemplateProcessor()
    {
        return this.storage.getTemplateProcessor();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRegistry#getTemplateImageResolver()
     */
    public TemplateImageResolver getTemplateImageResolver()
    {
        return this.imageResolver;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRegistry#getScriptProcessor()
     */
    public ScriptProcessor getScriptProcessor()
    {
        return this.storage.getScriptProcessor();
    }
    
    /**
     * Web Script Match
     * 
     * @author davidc
     */
    public static class DeclarativeWebScriptMatch implements WebScriptMatch
    {
        private String path;
        private WebScript service;
        private Kind kind;

        /**
         * Construct
         * 
         * @param path
         * @param service
         */
        public DeclarativeWebScriptMatch(String path, WebScript service)
        {
            this.kind = Kind.FULL;
            this.path = path;
            this.service = service;
        }
        
        /**
         * Construct
         * 
         * @param path
         * @param service
         */
        public DeclarativeWebScriptMatch(String path)
        {
            this.kind = Kind.URI;
            this.path = path;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptMatch#getKind()
         */
        public Kind getKind()
        {
            return this.kind;
        }

        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptMatch#getPath()
         */
        public String getPath()
        {
            return path;
        }

        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptMatch#getWebScript()
         */
        public WebScript getWebScript()
        {
            return service;
        }
    }
    
    /**
     * Web Script URL Index Entry
     */
    private static class URLIndex
    {
        private URLIndex(String path, boolean wildcardPath, boolean includeExtension, WebScript script)
        {
            this.path = path;
            this.wildcardPath = wildcardPath;
            this.includeExtension = includeExtension;
            this.script = script;
        }
        
        private String path;
        private boolean wildcardPath;
        private boolean includeExtension;
        private WebScript script;
    }
    
}
