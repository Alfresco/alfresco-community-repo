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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.app.servlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigService;
import org.alfresco.config.JNDIConstants;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.template.ClassPathRepoTemplateLoader;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.web.scripts.WebScriptCache;
import org.alfresco.web.scripts.WebScriptMatch;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptResponse;
import org.alfresco.web.scripts.WebScriptRuntime;
import org.alfresco.web.scripts.WebScriptServlet;
import org.alfresco.web.scripts.WebScriptURLRequest;
import org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;

/**
 * Servlet for rendering templated pages based on Webscript components.
 * 
 * NOTE: No web-client helper classes should be used here! This servlet should be movable
 *       to the headerless repo remote client with the minimum of work.
 * 
 * @author Kevin Roast
 */
public class PageRendererServlet extends WebScriptServlet
{
   private static Log logger = LogFactory.getLog(PageRendererServlet.class);
   
   private static final String MIMETYPE_HTML = "text/html;charset=utf-8";

   private PageTemplateProcessor templateProcessor;
   private WebScriptTemplateLoader webscriptTemplateLoader;
   private Map<String, PageDefinition> defaultPageDefCache = null;
   
   @Override
   public void init() throws ServletException
   {
      super.init();
      
      // init beans
      ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
      templateProcessor = (PageTemplateProcessor)context.getBean("pagerenderer.templateprocessor");
      webscriptTemplateLoader = new WebScriptTemplateLoader();
      ClassPathRepoTemplateLoader repoLoader = new ClassPathRepoTemplateLoader(
            serviceRegistry.getNodeService(),
            serviceRegistry.getContentService(),
            templateProcessor.getDefaultEncoding());
      templateProcessor.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{
         webscriptTemplateLoader, repoLoader}));
      templateProcessor.initConfig();
      
      // we use a specific config service instance
      configService = (ConfigService)context.getBean("pagerenderer.config");
      
      // create cache for default config
      defaultPageDefCache = Collections.synchronizedMap(new HashMap<String, PageDefinition>());
   }

   @Override
   protected void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
   {
      String uri = req.getRequestURI();
      
      if (logger.isDebugEnabled())
      {
         String qs = req.getQueryString();
         logger.debug("Processing Page Renderer URL: ("  + req.getMethod() + ") " + uri + 
               ((qs != null && qs.length() != 0) ? ("?" + qs) : ""));
      }
      
      uri = uri.substring(req.getContextPath().length());   // skip server context path
      StringTokenizer t = new StringTokenizer(uri, "/");
      int tokenCount = t.countTokens();
      t.nextToken();    // skip servlet name
      if (t.hasMoreTokens() == false)
      {
         throw new IllegalArgumentException("Invalid URL to PageRendererServlet: " + uri);
      }
      
      // retrieve the page name from the url
      String page = t.nextToken();
      
      // the AVM store to retrieve pages and config from
      // TODO: find a better way to set the store for a website servlet
      String store = req.getParameter("store");
      if (store == null)
      {
         throw new AlfrescoRuntimeException("'store' argument mandatory for PageRendererServlet.");
      }
      
      try
      {
         // lookup template path from page config in website AVM store
         PageDefinition pageDefinition = lookupPageDefinition(store, page);
         
         // set response content type and charset
         res.setContentType(MIMETYPE_HTML);
         
         // TODO: What authentication to use here? Guest? Or all templates and webscripts
         //       must reside in a known AVM repo - i.e. skip permissions. Template API still
         //       uses NodeService for some calls - even for AVMTemplateNode - so will need
         //       some kind of security context...
         authenticate(getServletContext(), req, res);
         
         // set the web app context path for the template loader to use when rebuilding urls
         this.webscriptTemplateLoader.setContextPath(req.getContextPath());
         this.webscriptTemplateLoader.setPageDefinition(pageDefinition);
         
         // Process the template page using our custom loader - the loader will find and buffer
         // individual included webscript output into the main writer for the servlet page.
         // TODO: where does the theme name come from!? same problem as where does store name come from...
         String templatePath = getStoreSitePath(store) + '/' + "themes" + '/' + "default" + '/' +
                               "templates" + '/' + pageDefinition.TemplateName;
         
         if (logger.isDebugEnabled())
            logger.debug("Page template resolved as: " + templatePath);
         
         processTemplatePage(templatePath, req, res);
      }
      catch (Throwable err)
      {
         throw new AlfrescoRuntimeException("Error occurred during page rendering. Page id: " +
               page + " with error: " + err.getMessage(), err);
      }
   }
   
   private void processTemplatePage(String templatePath, HttpServletRequest req, HttpServletResponse res)
      throws IOException
   {
      NodeRef ref = AVMNodeConverter.ToNodeRef(-1, templatePath);
      templateProcessor.process(ref.toString(), getModel(req), res.getWriter());
   }
   
   private Object getModel(HttpServletRequest req)
   {
      // TODO: add the full template model here?
      // just a basic minimum model for now
      Map<String, Object> model = new HashMap<String, Object>();
      model.put("url", new URLHelper(req.getContextPath()));
      return model;
   }
   
   /**
    * Get the page definition config object based on the page ID and AVM store.
    * 
    * @param store   AVM store to retrieve data from
    * @param page    Page ID to lookup in web-pagerenderer-config.xml
    * 
    * @return Path to the template content
    */
   private PageDefinition lookupPageDefinition(String store, String page)
   {
      // Lookup page via standard Alfresco XML config
      /*Config config = getConfig();
      if (config != null)
      {
         ConfigElement pagesConfig = config.getConfigElement("pages");
         for (ConfigElement pageConfig : pagesConfig.getChildren("page"))
         {
            String pageId = pageConfig.getAttribute("id");
            if (page.equals(pageId))
            {
               // found page reference
               if (logger.isDebugEnabled())
                  logger.debug("Looked up page id: " + page + " as " + pageConfig.getValue());
               pageValue = pageConfig.getValue();
               break;
            }
         }
      }*/
      
      // Lookup (and cache) config for default page-definition file in root
      PageDefinition defaultPageDef = defaultPageDefCache.get(store);
      if (defaultPageDef == null)
      {
         // read default config for the site and cache the result
         String defaultConfigPath = getStoreSitePath(store) + '/' + "pages" + '/' + "page-definition.xml";
         defaultPageDef = readPageDefinitionConfig(defaultConfigPath, page, null);
         defaultPageDefCache.put(store, defaultPageDef);
      }
      
      // Lookup page xml config in AVM store using page name as location
      String configPath = getStoreSitePath(store) + '/' + "pages" + '/' + page + '/' + "page-definition.xml";
      
      // read the page definition and return it
      return readPageDefinitionConfig(configPath, page, defaultPageDef);
   }
   
   /**
    * Read the page definition config at the specified location. A default config object can be
    * supplied from which values will be taken if none are supplied in the config that is read.
    * 
    * @return PageDefinition representing the config
    */
   private PageDefinition readPageDefinitionConfig(String configPath, String page, PageDefinition defaultPageDef)
   {
      PageDefinition pageDef = null;
      
      AVMService avm = this.serviceRegistry.getAVMService();
      try
      {
         // parse page definition xml config file
         // TODO: convert to pull parser to optimize (see importer ViewParser)
         SAXReader reader = new SAXReader();
         try
         {
            Document document = reader.read(avm.getFileInputStream(-1, configPath));
            
            Element rootElement = document.getRootElement();
            if (!rootElement.getName().equals("page"))
            {
               throw new AlfrescoRuntimeException(
                     "Expected 'page' root element in page-definition.xml config: " + configPath);
            }
            
            String templateName = null;
            if (defaultPageDef != null && defaultPageDef.TemplateName != null)
            {
               // take template name from default config in case none is specified locally
               templateName = defaultPageDef.TemplateName;
            }
            Element templateElement = rootElement.element("template");
            if (defaultPageDef != null && (templateElement == null && templateName == null))
            {
               throw new AlfrescoRuntimeException(
                     "No 'template' element (and no default set) found in page-definition.xml config: " + configPath);
            }
            templateName = templateElement.attributeValue("name");
            if (templateName == null || templateName.length() == 0)
            {
               throw new AlfrescoRuntimeException(
                     "The 'template' element is missing mandatory 'name' attribute in page-definition.xml config: " +
                     configPath);
            }
            
            // create config object for this page and store template name for this page
            pageDef = new PageDefinition(templateName);
            
            // copy in component mappings from default config definitions first
            if (defaultPageDef != null)
            {
               pageDef.Components.putAll(defaultPageDef.Components);
            }
            
            // read the component defs for this page
            // removing 'disabled' components as configured locally
            Element componentsElements = rootElement.element("components");
            if (componentsElements != null)
            {
               for (Element ce : (List<Element>)componentsElements.elements("component"))
               {
                  // read the mandatory component 'id' attribute
                  String id = ce.attributeValue("id");
                  if (id == null || id.length() == 0)
                  {
                     throw new AlfrescoRuntimeException(
                           "A 'component' element is missing mandatory 'id' attribute in page-definition.xml config: " + 
                           configPath);
                  }
                  
                  // next check for the 'disabled' boolean attribute - used to disable components
                  // that are specified in the default config - we don't need to read further
                  String disabled = ce.attributeValue("disabled");
                  if (disabled != null)
                  {
                     if (Boolean.parseBoolean(disabled) == true)
                     {
                        pageDef.Components.remove(id);
                        continue;
                     }
                  }
                  
                  String url = ce.attributeValue("url");
                  if (url == null || url.length() == 0)
                  {
                     throw new AlfrescoRuntimeException(
                           "A 'component' element is missing mandatory 'url' attribute in page-definition.xml config: " + 
                           configPath);
                  }
                  
                  // create minimum component config definition
                  PageComponent component = new PageComponent(id, url);
                  
                  // store any other component properties
                  for (Element pe : (List<Element>)ce.elements())
                  {
                     component.Properties.put(pe.getName(), pe.getTextTrim());
                  }
                  
                  // store component definition in the page definition
                  pageDef.Components.put(component.Id, component);
               }
            }
         }
         catch (DocumentException docErr)
         {
            throw new AlfrescoRuntimeException("Failed to parse 'page-definition.xml' for page '" + page +
                  "' in config: " + configPath, docErr);
         }
      }
      catch (AVMNotFoundException avmErr)
      {
         throw new AlfrescoRuntimeException("Unable to find 'page-definition.xml' for page '" + page +
               "' in expected location path: " + configPath, avmErr);
      }
      
      return pageDef;
   }
   
   private Config getConfig()
   {
      return this.configService.getConfig("PageRenderer");
   }
   
   private static void authenticate(ServletContext sc, HttpServletRequest req, HttpServletResponse res)
      throws IOException
   {
      WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
      AuthenticationService auth = (AuthenticationService)wc.getBean("AuthenticationService");
      auth.authenticate("admin", "admin".toCharArray());
      //auth.authenticateAsGuest();
   }
   
   private static String getStoreSitePath(String store)
   {
      return store + ":/" + JNDIConstants.DIR_DEFAULT_WWW + '/' + "avm_site";
   }
   
   
   /**
    * WebScript runtime for the PageRenderer servlet.
    */
   private class PageRendererWebScriptRuntime extends WebScriptRuntime
   {
      private String webScript;
      private String scriptUrl;
      private String encoding;
      private ByteArrayOutputStream baOut = null;
      
      PageRendererWebScriptRuntime(String webScript, String scriptUrl, String encoding)
      {
         super(registry, serviceRegistry);
         this.encoding = encoding;
         this.webScript = webScript;
         this.scriptUrl = scriptUrl;
         if (logger.isDebugEnabled())
            logger.debug("Constructing runtime for url: " + scriptUrl);
      }

      @Override
      protected String getScriptUrl()
      {
         return webScript;
      }

      @Override
      protected WebScriptRequest createRequest(WebScriptMatch match)
      {
         return new WebScriptPageRendererRequest(scriptUrl, match);
      }

      @Override
      protected WebScriptResponse createResponse()
      {
         // create a response object that we control to write to a temporary output
         // we later use that as the source for the webscript "template"
         try
         {
            this.baOut = new ByteArrayOutputStream(4096);
            BufferedWriter wrOut = new BufferedWriter(
                  encoding == null ? new OutputStreamWriter(baOut) : new OutputStreamWriter(baOut, encoding));
            return new WebScriptPageRendererResponse(wrOut, baOut);
         }
         catch (UnsupportedEncodingException err)
         {
            throw new AlfrescoRuntimeException("Unsupported encoding.", err);
         }
      }

      @Override
      protected boolean authenticate(RequiredAuthentication required, boolean isGuest)
      {
         // TODO: what authentication here?
         return true;
      }

      @Override
      protected String getScriptMethod()
      {
         return "GET";
      }
      
      public Reader getResponseReader()
      {
         try
         {
            if (baOut == null)
            {
               return null;
            }
            else
            {
               return new BufferedReader(new InputStreamReader(
                     encoding == null ? new ByteArrayInputStream(baOut.toByteArray()) :
                        new ByteArrayInputStream(baOut.toByteArray()), encoding));
            }
         }
         catch (UnsupportedEncodingException err)
         {
            throw new AlfrescoRuntimeException("Unsupported encoding.", err);
         }
      }
   }
   
   /**
    * Simple implementation of a WebScript URL Request for a webscript on the page
    */
   private class WebScriptPageRendererRequest extends WebScriptURLRequest
   {
      WebScriptPageRendererRequest(String scriptUrl, WebScriptMatch match)
      {
         super(scriptUrl, match);
      }

      public String getAgent()
      {
         return null;
      }

      public String getServerPath()
      {
         return null;
      }
   }
   
   /**
    * Implementation of a WebScript Response object for PageRenderer servlet
    */
   private class WebScriptPageRendererResponse implements WebScriptResponse
   {
      private Writer outWriter;
      private OutputStream outStream;
      
      public WebScriptPageRendererResponse(Writer outWriter, OutputStream outStream)
      {
         this.outWriter = outWriter;
         this.outStream = outStream;
      }
      
      public String encodeScriptUrl(String url)
      {
         // TODO: some kind of encoding required here - need to allow webscripts to call themselves
         //       on this page - so need the servlet PageRenderer URL plus args to identify the webscript
         //       and it's new url - similar to the JSF or Portlet runtimes
         return url;
      }

      public String getEncodeScriptUrlFunction(String name)
      {
         // TODO: may be required?
         return null;
      }

      public OutputStream getOutputStream() throws IOException
      {
         return this.outStream;
      }

      public Writer getWriter() throws IOException
      {
         return this.outWriter;
      }

      public void reset()
      {
         // not supported
      }

      public void setCache(WebScriptCache cache)
      {
         // not supported
      }

      public void setContentType(String contentType)
      {
         // not supported
      }

      public void setStatus(int status)
      {
         // not supported
      }
   }
   
   /**
    * Template loader that resolves and executes webscript components by looking up layout keys
    * in the template against the component definition service URLs for the page.
    */
   private class WebScriptTemplateLoader implements TemplateLoader
   {
      private ThreadLocal<PageDefinition> pageDefinition = new ThreadLocal<PageDefinition>();
      private String contextPath;
      private long last = 0L;
      
      public void closeTemplateSource(Object templateSource) throws IOException
      {
         // nothing to do
      }

      public Object findTemplateSource(String name) throws IOException
      {
         // The webscript is looked up based on the key in the #include directive - it must
         // be of the form [somekey] so that it can be recognised by the loader
         
         // most templates included by this loader will be children of other templates
         // unfortunately FreeMarker attempts to build paths for you to child templates - they are not
         // really children - so this information must be discarded
         if (name.startsWith("avm://"))
         {
            name = name.substring(name.indexOf("/[") + 1);
         }
         
         if (name.startsWith("[") && name.endsWith("]"))
         {
            String key = name.substring(1, name.length() - 1);
            
            if (logger.isDebugEnabled())
               logger.debug("Found webscript component key: " + key);
            
            // lookup against component def config
            PageComponent component = this.pageDefinition.get().Components.get(key);
            if (component == null)
            {
               // TODO: if the lookup fails, exception or just ignore render and log...?
               throw new AlfrescoRuntimeException("Failed to find component identified by key '" + key +
                     "' found in template: " + pageDefinition.get().TemplateName);
            }
            
            // TODO: remove the /service prefix from all config files?
            String url = component.Url;
            if (url.lastIndexOf('?') != -1)
            {
               url = url.substring("/service".length(), url.lastIndexOf('?'));
            }
            else
            {
               url = url.substring("/service".length());
            }
            return url;
         }
         else
         {
            return null;
         }
      }

      public long getLastModified(Object templateSource)
      {
         return last--;
      }

      public Reader getReader(Object templateSource, String encoding) throws IOException
      {
         // Execute the webscript and return a Reader to the textual content
         String webscriptUrl = this.contextPath + "/service" + templateSource.toString();
         PageRendererWebScriptRuntime runtime = new PageRendererWebScriptRuntime(
               templateSource.toString(), webscriptUrl, encoding);
         runtime.executeScript();
         
         // Return a reader from the runtime that executed the webscript - this effectively
         // returns the result as a "template" source to freemarker. Generally this will not itself
         // be a template but it can contain additional freemarker syntax if required. The downside
         // to this approach is that the result of the webscript is parsed again by freemarker - this
         // should be checked for performance issues.
         return runtime.getResponseReader();
      }
      
      /**
       * Setter to apply the current page definition for this template execution. A ThreadLocal is used
       * to allow multiple servlet threads to execute using the same TemplateLoader (there can only be one)
       * but with different page definitions for each thread.
       */
      public void setPageDefinition(PageDefinition pageDef)
      {
         this.pageDefinition.set(pageDef);
      }
      
      /**
       * Setter called by the servlet to ensure the loader has the full context path available
       * Does not matter if called multiple times by multiple threads as value is always the same.
       */
      public void setContextPath(String path)
      {
         this.contextPath = path;
      }
   }
   
   /**
    * Helper to return context path for generating urls
    */
   public static class URLHelper
   {
       String context;
       
       public URLHelper(String context)
       {
           this.context = context;
       }
       
       public String getContext()
       {
           return context;
       }
   }
   
   private static class PageDefinition
   {
      public String TemplateName;
      public Map<String, PageComponent> Components = new HashMap<String, PageComponent>();
      
      PageDefinition(String templateId)
      {
         this.TemplateName = templateId;
      }
   }
   
   private static class PageComponent
   {
      public String Id;
      public String Url;
      public Map<String, String> Properties = new HashMap<String, String>(4, 1.0f);
      
      PageComponent(String id, String url)
      {
         this.Id = id;
         this.Url = url;
      }
   }
}
