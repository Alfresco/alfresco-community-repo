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
package org.alfresco.web.api.framework;

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

import org.alfresco.web.api.framework.APIDescription.RequiredAuthentication;
import org.alfresco.web.api.framework.APIDescription.RequiredTransaction;
import org.alfresco.web.api.framework.APIDescription.URI;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.RegexpMethodPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * API Service Registry of declarative (scripted/template driven) api services 
 * 
 * @author davidc
 */
public class DeclarativeAPIRegistry implements APIRegistry, ApplicationContextAware, APIContextAware
{
    // Logger
    private static final Log logger = LogFactory.getLog(DeclarativeAPIRegistry.class);

    private ApplicationContext applicationContext;
    private ServletContext servletContext;
    private String defaultServiceImpl;
    private MethodInterceptor authenticator;
    private MethodInterceptor serviceLogger;
    private MethodInterceptor serviceTransaction;
    private FormatRegistry formatRegistry;
    private APIStores stores;
    
    // map of services by service id
    // NOTE: The map is sorted by id (ascending order)
    private Map<String, APIService> servicesById = new TreeMap<String, APIService>();
    
    // map of services by url
    // NOTE: The map is sorted by url (descending order)
    private Map<String, APIService> servicesByURL = new TreeMap<String, APIService>(Collections.reverseOrder());
    

    //
    // Initialisation
    // 
    
    /**
     * Sets the available API Stores
     * 
     * @param stores
     */
    public void setStores(APIStores stores)
    {
        this.stores = stores;
    }

    /**
     * Sets the service authenticator
     * 
     * @param authenticator
     */
    public void setAuthenticator(MethodInterceptor authenticator)
    {
        this.authenticator = authenticator;
    }

    /**
     * Sets the service logger
     * 
     * @param serviceLogger
     */
    public void setServiceLogger(MethodInterceptor serviceLogger)
    {
        this.serviceLogger = serviceLogger;
    }
    
    /**
     * Sets the service transaction
     * 
     * @param serviceTransaction
     */
    public void setServiceTransaction(MethodInterceptor serviceTransaction)
    {
        this.serviceTransaction = serviceTransaction;
    }
    
    /**
     * Sets the default service implementation bean
     * 
     * @param defaultServiceImpl
     */
    public void setDefaultServiceImpl(String defaultServiceImpl)
    {
        this.defaultServiceImpl = defaultServiceImpl;
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
     * @see org.alfresco.web.api.APIContextAware#setAPIContext(javax.servlet.ServletContext)
     */
    public void setAPIContext(ServletContext context)
    {
        this.servletContext = context;
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    
    /**
     * Initialise API Services
     *
     * Note: Each invocation of this method resets the list of the services
     */
    public void initServices()
    {
        if (logger.isDebugEnabled())
            logger.debug("Initialising Web API services");
        
        // clear currently registered services
        servicesById.clear();
        servicesByURL.clear();
        
        // register services
        for (APIStore apiStore : stores.getAPIStores())
        {
            if (logger.isDebugEnabled())
                logger.debug("Locating services within " + apiStore.getBasePath());
            
            String basePath = apiStore.getBasePath();
            String[] serviceDescPaths = apiStore.getDescriptionDocumentPaths();
            for (String serviceDescPath : serviceDescPaths)
            {
                // build service description
                APIDescription serviceDesc = null;
                InputStream serviceDescIS = null;
                try
                {
                    serviceDescIS = apiStore.getDescriptionDocument(serviceDescPath);
                    serviceDesc = createServiceDescription(basePath, serviceDescPath, serviceDescIS);
                }
                catch(IOException e)
                {
                    throw new APIException("Failed to read service description document " + apiStore.getBasePath() + serviceDescPath, e);
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
                if (servicesById.containsKey(id))
                {
                    // move to next service
                    if (logger.isDebugEnabled())
                    {
                        APIService existingService = servicesById.get(id);
                        logger.debug("Service description document " + serviceDesc.getSourceLocation() + " overridden by " + existingService.getDescription().getSourceLocation());
                    }
                    continue;
                }
                
                // construct service implementation
                String serviceImplName = (applicationContext.containsBean("web.api." + id)) ? "web.api." + id : defaultServiceImpl;
                AbstractAPIService serviceImpl = (AbstractAPIService)applicationContext.getBean(serviceImplName);
                serviceImpl.setDescription(serviceDesc);
                serviceImpl.init(this);
                
                // wrap service implementation in appropriate interceptors (e.g. authentication)
                APIService serviceImplIF = (APIService)serviceImpl;
                if (serviceLogger != null && serviceTransaction != null && authenticator != null)
                {
                    ProxyFactory authFactory = new ProxyFactory();
                    authFactory.addInterface(APIService.class);
                    authFactory.setTarget(serviceImplIF);

                    // logging interceptor
                    if (serviceLogger != null)
                    {
                        RegexpMethodPointcutAdvisor advisor = new RegexpMethodPointcutAdvisor(".*execute", serviceLogger);
                        authFactory.addAdvisor(advisor);
                    }
                
                    // transaction interceptor
                    if (serviceDesc.getRequiredTransaction() != RequiredTransaction.none)
                    {
                        if (serviceTransaction == null)
                        {
                            throw new APIException("Web API Transaction not specified");
                        }
                        RegexpMethodPointcutAdvisor advisor = new RegexpMethodPointcutAdvisor(".*execute", serviceTransaction);
                        authFactory.addAdvisor(advisor);
                    }
                    
                    // authentication interceptor
                    if (serviceDesc.getRequiredAuthentication() != RequiredAuthentication.none)
                    {
                        if (authenticator == null)
                        {
                            throw new APIException("Web API Authenticator not specified");
                        }
                        RegexpMethodPointcutAdvisor advisor = new RegexpMethodPointcutAdvisor(".*execute", authenticator);
                        authFactory.addAdvisor(advisor);
                    }

                    serviceImplIF = (APIService)authFactory.getProxy();
                }
                
                if (logger.isDebugEnabled())
                    logger.debug("Found service " + serviceDescPath + " (id: " + id + ", impl: " + serviceImplName + ", auth: " + serviceDesc.getRequiredAuthentication() + ", trx: " + serviceDesc.getRequiredTransaction() + ")");
                
                // register service and its urls
                servicesById.put(id, serviceImplIF);
                for (URI uri : serviceDesc.getURIs())
                {
                    // establish static part of url template
                    String uriTemplate = uri.getURI();
                    int queryArgIdx = uriTemplate.indexOf('?');
                    if (queryArgIdx != -1)
                    {
                        uriTemplate = uriTemplate.substring(0, queryArgIdx);
                    }
                    int tokenIdx = uriTemplate.indexOf('{');
                    if (tokenIdx != -1)
                    {
                        uriTemplate = uriTemplate.substring(0, tokenIdx);
                    }
                    
                    // index service by static part of url (ensuring no other service has already claimed the url)
                    String uriIdx = serviceDesc.getMethod().toString() + ":" + uriTemplate;
                    if (servicesByURL.containsKey(uriIdx))
                    {
                        APIService existingService = servicesByURL.get(uriIdx);
                        if (!existingService.getDescription().getId().equals(serviceDesc.getId()))
                        {
                            throw new APIException("Service document " + existingService.getDescription().getSourceLocation() + " already defines the url '" + uriIdx);
                        }
                    }
                    else
                    {
                        servicesByURL.put(uriIdx, serviceImplIF);
                        
                        if (logger.isDebugEnabled())
                            logger.debug("Registered Web API URL '" + uriIdx + "'");
                    }
                }
            }
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Registered " + servicesById.size() + " services; " + servicesByURL.size() + " URLs");
    }

    /**
     * Create an API Service Description
     * 
     * @param basePath
     * @param serviceDescPath
     * @param serviceDoc
     * 
     * @return  api service description
     */
    private APIDescription createServiceDescription(String basePath, String serviceDescPath, InputStream serviceDoc)
    {
        SAXReader reader = new SAXReader();
        try
        {
            Document document = reader.read(serviceDoc);
            Element rootElement = document.getRootElement();
            if (!rootElement.getName().equals("servicedescription"))
            {
                throw new APIException("Expected <servicedescription> root element - found <" + rootElement.getName() + ">");
            }

            // retrieve id
            String id = serviceDescPath.substring(0, serviceDescPath.lastIndexOf("_desc.xml")).replace('/', '.');
            
            // retrieve http method
            int methodIdx = id.lastIndexOf('_');
            if (methodIdx == id.length() - 1)
            {
                throw new APIException("Unable to establish HTTP Method from service description: naming convention must be <name>_<method>_desc.xml");
            }
            String method = id.substring(id.lastIndexOf('_') + 1).toUpperCase();
            
            // retrieve short name
            Element shortNameElement = rootElement.element("shortname");
            if (shortNameElement == null || shortNameElement.getTextTrim() == null || shortNameElement.getTextTrim().length() == 0)
            {
                throw new APIException("Expected <shortname> value");
            }
            String shortName = shortNameElement.getTextTrim();

            // retrieve description
            Element descriptionElement = rootElement.element("description");
            String description = descriptionElement.getTextTrim();
            
            // retrieve urls
            List urlElements = rootElement.elements("url");
            if (urlElements == null || urlElements.size() == 0)
            {
                throw new APIException("Expected at one <url> element");
            }
            List<APIDescription.URI> uris = new ArrayList<APIDescription.URI>();
            Iterator iterElements = urlElements.iterator();
            while(iterElements.hasNext())
            {
                // retrieve url element
                Element urlElement = (Element)iterElements.next();
                
                // retrieve url mimetype
                String format = urlElement.attributeValue("format");
                if (format == null)
                {
                    // default to unspecified format
                    format = "";
                }
                
                // retrieve url template
                String template = urlElement.attributeValue("template");
                if (template == null || template.length() == 0)
                {
                    throw new APIException("Expected template attribute on <url> element");
                }
                
                APIDescriptionImpl.URIImpl uriImpl = new APIDescriptionImpl.URIImpl();
                uriImpl.setFormat(format);
                uriImpl.setUri(template);
                uris.add(uriImpl);
            }
            
            // retrieve authentication
            RequiredAuthentication reqAuth = RequiredAuthentication.none;
            Element authElement = rootElement.element("authentication");
            if (authElement != null)
            {
                String reqAuthStr = authElement.getTextTrim();
                if (reqAuthStr == null || reqAuthStr.length() == 0)
                {
                    throw new APIException("Expected <authentication> value");
                }
                reqAuth = RequiredAuthentication.valueOf(reqAuthStr);
                if (reqAuth == null)
                {
                    throw new APIException("Authentication '" + reqAuthStr + "' is not a valid value");
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
                    throw new APIException("Expected <transaction> value");
                }
                reqTrx = RequiredTransaction.valueOf(reqTrxStr);
                if (reqTrx == null)
                {
                    throw new APIException("Transaction '" + reqTrxStr + "' is not a valid value");
                }
            }
            
            // construct service description
            APIDescriptionImpl serviceDesc = new APIDescriptionImpl();
            serviceDesc.setSourceLocation(basePath + "/" + serviceDescPath);
            serviceDesc.setId(id);
            serviceDesc.setShortName(shortName);
            serviceDesc.setDescription(description);
            serviceDesc.setRequiredAuthentication(reqAuth);
            serviceDesc.setRequiredTransaction(reqTrx);
            serviceDesc.setMethod(method);
            serviceDesc.setUris(uris.toArray(new APIDescription.URI[uris.size()]));
            serviceDesc.setDefaultFormat(uris.get(0).getFormat());
            return serviceDesc;
        }
        catch(DocumentException e)
        {
            throw new APIException("Failed to parse service description document " + serviceDescPath, e);
        }
        catch(APIException e)
        {
            throw new APIException("Failed to parise service description document " + serviceDescPath, e);
        }
    }


    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIRegistry#getServices()
     */
    public Collection<APIService> getServices()
    {
        return servicesById.values();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIRegistry#getService(java.lang.String)
     */
    public APIService getService(String id)
    {
        return servicesById.get(id);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIRegistry#findService(java.lang.String, java.lang.String)
     */
    public APIServiceMatch findService(String method, String uri)
    {
        // TODO: Replace with more efficient approach
        DeclarativeAPIServiceMatch apiServiceMatch = null;
        String match = method.toString().toUpperCase() + ":" + uri;
        for (Map.Entry<String, APIService> service : servicesByURL.entrySet())
        {
            String indexedPath = service.getKey();
            if (match.startsWith(indexedPath))
            {
                String matchPath = indexedPath.substring(indexedPath.indexOf(':') +1);
                apiServiceMatch = new DeclarativeAPIServiceMatch(matchPath, service.getValue()); 
                break;
            }
        }
        return apiServiceMatch;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIRegistry#getContext()
     */
    public ServletContext getContext()
    {
        return servletContext;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIRegistry#getFormatRegistry()
     */
    public FormatRegistry getFormatRegistry()
    {
        return this.formatRegistry;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIRegistry#getTemplateProcessor()
     */
    public APITemplateProcessor getTemplateProcessor()
    {
        return this.stores.getTemplateProcessor();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIRegistry#getScriptProcessor()
     */
    public APIScriptProcessor getScriptProcessor()
    {
        return this.stores.getScriptProcessor();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.api.framework.APIRegistry#reset()
     */
    public void reset()
    {
        getTemplateProcessor().resetCache();
        getScriptProcessor().resetCache();
        initServices();
    }
    

    /**
     * API Service Match
     * 
     * @author davidc
     */
    public static class DeclarativeAPIServiceMatch implements APIServiceMatch
    {
        private String path;
        private APIService service;

        /**
         * Construct
         * 
         * @param path
         * @param service
         */
        public DeclarativeAPIServiceMatch(String path, APIService service)
        {
            this.path = path;
            this.service = service;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.web.api.APIServiceMatch#getPath()
         */
        public String getPath()
        {
            return path;
        }

        /* (non-Javadoc)
         * @see org.alfresco.web.api.APIServiceMatch#getService()
         */
        public APIService getService()
        {
            return service;
        }
    }

}
