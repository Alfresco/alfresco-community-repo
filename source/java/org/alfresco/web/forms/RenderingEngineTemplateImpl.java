/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.web.forms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.remote.AVMRemote;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.XMLUtil;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.surf.util.URLEncoder;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.forms.RenderingEngine.TemplateNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import freemarker.ext.dom.NodeModel;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleHash;


/**
 * Implementation of a rendering engine template
 */
public class RenderingEngineTemplateImpl
   implements RenderingEngineTemplate
{
   private static final long serialVersionUID = -1656812676972437532L;
   
   private static final Log logger = LogFactory.getLog(RenderingEngineTemplateImpl.class);

   private static final DynamicNamespacePrefixResolver namespacePrefixResolver = 
      new DynamicNamespacePrefixResolver();
   static
   {
      RenderingEngineTemplateImpl.namespacePrefixResolver.registerNamespace(NamespaceService.ALFRESCO_PREFIX,
                                                                            NamespaceService.ALFRESCO_URI);
   }

   static final QName PROP_RESOURCE_RESOLVER = QName.createQName(NamespaceService.ALFRESCO_PREFIX,
                                                                 "resource_resolver",
                                                                 namespacePrefixResolver);
   private static final String WEBSCRIPT_PREFIX = "webscript://";

   private final NodeRef nodeRef;
   private final NodeRef renditionPropertiesNodeRef;
   private transient FormsService formsService;

   protected RenderingEngineTemplateImpl(final NodeRef nodeRef,
                                         final NodeRef renditionPropertiesNodeRef,
                                         final FormsService formsService)
   {
      if (nodeRef == null)
      {
         throw new NullPointerException();
      }
      if (renditionPropertiesNodeRef == null)
      {
         throw new NullPointerException();
      }
      if (formsService == null)
      {
         throw new NullPointerException();
      }
      this.nodeRef = nodeRef;
      this.renditionPropertiesNodeRef = renditionPropertiesNodeRef;
      this.formsService = formsService;
   }

   private FormsService getFormsService()
   {
      if (formsService == null)
      {
         formsService = (FormsService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "FormsService");
      }
      return formsService;
   }
   
   public String getName()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.nodeRef, ContentModel.PROP_NAME);
   }

   public String getTitle()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.nodeRef, ContentModel.PROP_TITLE);
   }

   public String getDescription()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.nodeRef, 
                                             ContentModel.PROP_DESCRIPTION);
   }
   
   public String getOutputPathPattern()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.renditionPropertiesNodeRef, 
                                             WCMAppModel.PROP_OUTPUT_PATH_PATTERN);
   }

   public NodeRef getNodeRef()
   {
      return this.nodeRef;
   }

   public NodeRef getRenditionPropertiesNodeRef()
   {
      return this.renditionPropertiesNodeRef;
   }
   
   public InputStream getInputStream()
      throws IOException
   {
      final ContentService contentService = this.getServiceRegistry().getContentService();
      final ContentReader contentReader = 
         contentService.getReader(this.nodeRef, ContentModel.TYPE_CONTENT);
      return contentReader.getContentInputStream();
   }

   /**
    * Provides the rendering engine to use when processing this template.
    *
    * @return the rendering engine to use when processing this template.
    */
   public RenderingEngine getRenderingEngine()
   {
      NodeService nodeService = this.getServiceRegistry().getNodeService();
      String renderingEngineName = null;
      try
      {
         renderingEngineName = (String)nodeService.getProperty(this.nodeRef, WCMAppModel.PROP_PARENT_RENDERING_ENGINE_NAME);
      }
      catch (InvalidNodeRefException e)
      {
          logger.warn("RenderingEngineTemplate not found: "+e);
          throw new TemplateNotFoundException("RenderingEngineTemplate not found", e);
      }
      return this.getFormsService().getRenderingEngine(renderingEngineName);
   }

   /**
    * Generates an output path for the rendition by compiling the output path pattern
    * as a freemarker template.
    *
    * @param formInstanceData the form instance data to use for the rendition path.
    * @param currentAVMPath the current path in which the form is being created.
    * @param name the name which is used in a pattern
    *
    * @return the output path to use for renditions.
    */
   public String getOutputPathForRendition(final FormInstanceData formInstanceData, final String currentAVMPath, final String name)
   {
      final ServiceRegistry sr = this.getServiceRegistry();
      final AVMService avmService = sr.getAVMLockingAwareService();

      final String formInstanceDataAVMPath = formInstanceData.getPath();

      final Map<String, Object> root = new HashMap<String, Object>();
      
      final String webappName =
         (avmService.hasAspect(-1,
                               AVMUtil.getWebappPath(formInstanceDataAVMPath),
                               WCMAppModel.ASPECT_WEBAPP)
          ? AVMUtil.getWebapp(formInstanceDataAVMPath)
          : null);
      root.put("webapp", webappName);

      root.put("name", name);
      root.put("extension", 
               sr.getMimetypeService().getExtension(this.getMimetypeForRendition()));
      Document formInstanceDataDocument = null;
      try
      {
         formInstanceDataDocument = formInstanceData.getDocument();
      }
      catch (Exception e)
      {
         logger.error(e);
         throw new AlfrescoRuntimeException(e.getMessage(), e);
      }
      final String parentAVMPath = AVMNodeConverter.SplitBase(formInstanceDataAVMPath)[0];

      root.put("xml", NodeModel.wrap(formInstanceDataDocument));
      root.put("node", new TemplateNode(((FormInstanceDataImpl)formInstanceData).getNodeRef(), sr, null));
      root.put("date", new SimpleDate(new Date(), SimpleDate.DATETIME));
      root.put("cwd", AVMUtil.getWebappRelativePath(currentAVMPath));
      final TemplateService templateService = sr.getTemplateService();
      final String outputPathPattern = (FreeMarkerUtil.buildNamespaceDeclaration(formInstanceDataDocument) +
                                        this.getOutputPathPattern());
      String result = null;
      try
      {
         result = templateService.processTemplateString("freemarker", 
                                                        outputPathPattern,
                                                        new SimpleHash(root));
      }
      catch (final TemplateException te)
      {
         logger.error(te.getMessage(), te);
         throw new AlfrescoRuntimeException("Error processing output path pattern " + outputPathPattern + 
                                            " for " + name + 
                                            " in webapp " + webappName +
                                            ":\n" + te.getMessage(), 
                                            te);
      }

      result = AVMUtil.buildPath(parentAVMPath, 
                                      result,
                                      AVMUtil.PathRelation.SANDBOX_RELATIVE);
      
      if (logger.isDebugEnabled())
      {
         logger.debug("processed pattern " + outputPathPattern + " as " + result);
      }
      
      return result;
   }

   public String getMimetypeForRendition()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.renditionPropertiesNodeRef, 
                                             WCMAppModel.PROP_MIMETYPE_FOR_RENDITION);
   }

   public Rendition render(final FormInstanceData formInstanceData, 
                           final String renditionAvmPath)
      throws IOException,
      SAXException,
      RenderingEngine.RenderingException
   {
      final AVMService avmService = this.getServiceRegistry().getAVMLockingAwareService();
      
      boolean isRegenerate = true;
      boolean exists = avmService.lookup(-1, renditionAvmPath) != null;
      
      if (! exists)
      {
         final String parentAVMPath = AVMNodeConverter.SplitBase(renditionAvmPath)[0];
         AVMUtil.makeAllDirectories(parentAVMPath);
         avmService.createFile(parentAVMPath,
                               AVMNodeConverter.SplitBase(renditionAvmPath)[1]).close();
         
         if (logger.isDebugEnabled())
         {
            logger.debug("Created file node for file: " + renditionAvmPath);
         }
         
         avmService.addAspect(renditionAvmPath, ContentModel.ASPECT_TITLED);
         avmService.addAspect(renditionAvmPath, WCMAppModel.ASPECT_RENDITION);
         
         isRegenerate = false;
      }
      else
      {
          // ETHREEOH-2110
          if (! avmService.hasAspect(-1, renditionAvmPath, WCMAppModel.ASPECT_RENDITION))
          {
              avmService.addAspect(renditionAvmPath, WCMAppModel.ASPECT_RENDITION);
              isRegenerate = false;
          }
      }
      
      final Rendition result = new RenditionImpl(-1, 
                                                 renditionAvmPath,
                                                 this.getFormsService());
      this.render(formInstanceData, result);
      
      if (!isRegenerate)
      {
         final PropertyValue pv = 
            avmService.getNodeProperty(-1, formInstanceData.getPath(), WCMAppModel.PROP_RENDITIONS);
         final Collection<Serializable> renditions = (pv == null 
                                                      ? new HashSet<Serializable>() 
                                                      : pv.getCollection(DataTypeDefinition.TEXT));
         renditions.add(AVMUtil.getStoreRelativePath(renditionAvmPath));
         avmService.setNodeProperty(formInstanceData.getPath(), 
                                    WCMAppModel.PROP_RENDITIONS,
                                    new PropertyValue(DataTypeDefinition.TEXT,
                                                      (Serializable)renditions));
      }
      return result;
   }

   public void render(final FormInstanceData formInstanceData,
                      final Rendition rendition)
      throws IOException,
      SAXException,
      RenderingEngine.RenderingException
   {
      RenderingEngine re = this.getRenderingEngine();
      if (re == null)
      {
          return;
      }
      
      final OutputStream out = rendition.getOutputStream();
      try
      {
         re.render(this.buildModel(formInstanceData, rendition), 
                                          this, 
                                          out);
      }
      finally
      {
         out.close();
      }

      final Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(5, 1.0f);
      props.put(WCMAppModel.PROP_PARENT_FORM_NAME, 
                new PropertyValue(DataTypeDefinition.TEXT, 
                                  formInstanceData.getForm().getName()));
      props.put(ContentModel.PROP_TITLE,
                new PropertyValue(DataTypeDefinition.TEXT,
                                  AVMNodeConverter.SplitBase(rendition.getPath())[1]));
      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      props.put(ContentModel.PROP_DESCRIPTION,
                new PropertyValue(DataTypeDefinition.TEXT,
                                  MessageFormat.format(bundle.getString("default_rendition_description"), 
                                                       this.getTitle(),
                                                       AVMUtil.getSandboxRelativePath(rendition.getPath()))));
      props.put(WCMAppModel.PROP_PARENT_RENDERING_ENGINE_TEMPLATE,
                new PropertyValue(DataTypeDefinition.NODE_REF,
                                  this.nodeRef));
      props.put(WCMAppModel.PROP_PARENT_RENDITION_PROPERTIES,
                new PropertyValue(DataTypeDefinition.NODE_REF,
                                  this.renditionPropertiesNodeRef));
      // extract a store relative path for the primary form instance data
      props.put(WCMAppModel.PROP_PRIMARY_FORM_INSTANCE_DATA, 
                new PropertyValue(DataTypeDefinition.TEXT,
                                  AVMUtil.getStoreRelativePath(formInstanceData.getPath())));

      final AVMService avmService = this.getServiceRegistry().getAVMLockingAwareService();
      avmService.setNodeProperties(rendition.getPath(), props);
   }

  /**
    * Builds the model to pass to the rendering engine.
    */
   protected Map<QName, Object> buildModel(final FormInstanceData formInstanceData,
                                           final Rendition rendition)
      throws IOException,
      SAXException
   {
      final String formInstanceDataAvmPath = formInstanceData.getPath();
      final String renditionAvmPath = rendition.getPath();
      final String parentPath = AVMNodeConverter.SplitBase(formInstanceDataAvmPath)[0];
      final String sandboxUrl = AVMUtil.getPreviewURI(AVMUtil.getStoreName(formInstanceDataAvmPath));
      final String webappUrl = AVMUtil.buildWebappUrl(formInstanceDataAvmPath);
      final HashMap<QName, Object> model = new HashMap<QName, Object>();
      // add simple scalar parameters
      model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX,
                                  "date",
                                  namespacePrefixResolver),
                new Date());
      model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX,
                                  "avm_sandbox_url",
                                  namespacePrefixResolver), 
                sandboxUrl);
      model.put(RenderingEngineTemplateImpl.PROP_RESOURCE_RESOLVER,
                new RenderingEngine.TemplateResourceResolver()
                {
                   public InputStream resolve(final String name)
                   {
                      final NodeService nodeService = 
                         RenderingEngineTemplateImpl.this.getServiceRegistry().getNodeService();
                      final NodeRef parentNodeRef = 
                         nodeService.getPrimaryParent(RenderingEngineTemplateImpl.this.getNodeRef()).getParentRef();
                      
                      if (logger.isDebugEnabled())
                      {
                         logger.debug("request to resolve resource " + name +
                                      " webapp url is " + webappUrl +
                                      " and data dictionary workspace is " + parentNodeRef);
                      }
                      
                      final NodeRef result = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, name);
                      if (result != null)
                      {
                         final ContentService contentService = 
                            RenderingEngineTemplateImpl.this.getServiceRegistry().getContentService();
                         try
                         {
                            if (logger.isDebugEnabled())
                            {
                               logger.debug("found " + name + " in data dictonary: " + result);
                            }
                            
                            return contentService.getReader(result, ContentModel.PROP_CONTENT).getContentInputStream();
                         }
                         catch (Exception e)
                         {
                            logger.warn(e);
                         }
                      }
                      
                      if (name.startsWith(WEBSCRIPT_PREFIX))
                      {
                          try
                          {
                              final FacesContext facesContext = FacesContext.getCurrentInstance();
                              final ExternalContext externalContext = facesContext.getExternalContext();
                              final HttpServletRequest request = (HttpServletRequest)externalContext.getRequest();
                          
                              String decodedName = URLDecoder.decode(name.substring(WEBSCRIPT_PREFIX.length()));
                              String rewrittenName = decodedName;
                              
                              if (decodedName.contains("${storeid}"))
                              {
                                 rewrittenName = rewrittenName.replace("${storeid}", AVMUtil.getStoreName(formInstanceDataAvmPath));
                              }
                              else
                              {
                                 if (decodedName.contains("{storeid}"))
                                 {
                                     rewrittenName = rewrittenName.replace("{storeid}", AVMUtil.getStoreName(formInstanceDataAvmPath));
                                 }
                              }
                              
                              if (decodedName.contains("${ticket}"))
                              {
                                 AuthenticationService authenticationService = Repository.getServiceRegistry(facesContext).getAuthenticationService();
                                 final String ticket = authenticationService.getCurrentTicket();
                                 rewrittenName  = rewrittenName.replace("${ticket}", ticket);
                              }
                              else
                              {
                                 if (decodedName.contains("{ticket}"))
                                 {
                                     AuthenticationService authenticationService = Repository.getServiceRegistry(facesContext).getAuthenticationService();
                                     final String ticket = authenticationService.getCurrentTicket();
                                     rewrittenName = rewrittenName.replace("{ticket}", ticket);
                                 }
                              }
                              
                              final String webscriptURI = (request.getScheme() + "://" +
                                                           request.getServerName() + ':' + 
                                                           request.getServerPort() + 
                                                           request.getContextPath() + "/wcservice/" +
                                                           rewrittenName);
                              
                              if (logger.isDebugEnabled())
                              {
                                  logger.debug("loading webscript: " + webscriptURI);
                              }
                              
                              final URI uri = new URI(webscriptURI);
                              return uri.toURL().openStream();
                          }
                          catch (Exception e)
                          {
                             logger.warn(e);
                          }
                      }
                      
                      try
                      {
                         final String[] path = (name.startsWith("/") ? name.substring(1) : name).split("/");
                         for (int i = 0; i < path.length; i++)
                         {
                            path[i] = URLEncoder.encode(path[i]);
                         }
                         
                         final URI uri = new URI(webappUrl + '/' + StringUtils.join(path, '/'));
                         
                         if (logger.isDebugEnabled())
                         {
                            logger.debug("loading " + uri);
                         }
                         
                         return uri.toURL().openStream();
                      }
                      catch (Exception e)
                      {
                         logger.warn(e);
                         return null;
                      }
                   }
                });
      model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX,
                                  "form_instance_data_file_name",
                                  namespacePrefixResolver),
                AVMNodeConverter.SplitBase(formInstanceDataAvmPath)[1]);
      model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX,
                                  "rendition_file_name",
                                  namespacePrefixResolver),
                AVMNodeConverter.SplitBase(renditionAvmPath)[1]);
      model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX,
                                  "parent_path",
                                  namespacePrefixResolver),
                parentPath);
      final FacesContext fc = FacesContext.getCurrentInstance();
      model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX,
                                  "request_context_path",
                                  namespacePrefixResolver),
                fc.getExternalContext().getRequestContextPath());

      // add methods
      final FormDataFunctions fdf = this.getFormDataFunctions();
      
      model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX,
                                  "encodeQuotes",
                                  namespacePrefixResolver),
                  new RenderingEngine.TemplateProcessorMethod()
                  {
                      public Object exec(final Object[] arguments)
                         throws IOException,
                         SAXException
                      {
                         if (arguments.length != 1)
                         {
                            throw new IllegalArgumentException("expected 1 argument to encodeQuotes.  got " +
                                                               arguments.length);

                         }
                         if (! (arguments[0] instanceof String))
                         {
                            throw new ClassCastException("expected arguments[0] to be a " + String.class.getName() +
                                                         ".  got a " + arguments[0].getClass().getName() + ".");
                         }
                         String text = (String)arguments[0];
                         
                         if (logger.isDebugEnabled())
                         {
                            logger.debug("tpm_encodeQuotes('" + text + "'), parentPath = " + parentPath);
                         }
                         
                         final String result = fdf.encodeQuotes(text);
                         return result;
                      }
                   });

      
      model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX,
                                  "parseXMLDocument",
                                  namespacePrefixResolver),
                new RenderingEngine.TemplateProcessorMethod()
                {
                   public Object exec(final Object[] arguments)
                      throws IOException,
                      SAXException
                   {
                      if (arguments.length != 1)
                      {
                         throw new IllegalArgumentException("expected 1 argument to parseXMLDocument.  got " +
                                                            arguments.length);

                      }
                      if (! (arguments[0] instanceof String))
                      {
                         throw new ClassCastException("expected arguments[0] to be a " + String.class.getName() +
                                                      ".  got a " + arguments[0].getClass().getName() + ".");
                      }
                      String path = (String)arguments[0];
                      path = AVMUtil.buildPath(parentPath,
                                                    path,
                                                    AVMUtil.PathRelation.WEBAPP_RELATIVE);
                      
                      if (logger.isDebugEnabled())
                      {
                         logger.debug("tpm_parseXMLDocument('" + path + "'), parentPath = " + parentPath);
                      }
                      
                      final Document d = fdf.parseXMLDocument(path);
                      return d != null ? d.getDocumentElement() : null;
                   }
                });
      model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX,
                                  "parseXMLDocuments",
                                  namespacePrefixResolver),
                new RenderingEngine.TemplateProcessorMethod()
                {
                   public Object exec(final Object[] arguments)
                      throws IOException,
                      SAXException
                   {
                      if (arguments.length > 2)
                      {
                         throw new IllegalArgumentException("expected exactly one or two arguments to " +
                                                            "parseXMLDocuments.  got " + arguments.length);
                      }
                      if (! (arguments[0] instanceof String))
                      {
                         throw new ClassCastException("expected arguments[0] to be a " + String.class.getName() +
                                                      ".  got a " + arguments[0].getClass().getName() + ".");
                      }

                      if (arguments.length == 2 && ! (arguments[1] instanceof String))
                      {
                         throw new ClassCastException("expected arguments[1] to be a " + String.class.getName() +
                                                      ".  got a " + arguments[1].getClass().getName() + ".");
                      }
                      
                      String path = arguments.length == 2 ? (String)arguments[1] : "";
                      path = AVMUtil.buildPath(parentPath,
                                                    path,
                                                    AVMUtil.PathRelation.WEBAPP_RELATIVE);
                      final String formName = (String)arguments[0];
                      
                      if (logger.isDebugEnabled())
                      {
                         logger.debug("tpm_parseXMLDocuments('" + formName + "','" + path + 
                                      "'), parentPath = " + parentPath);
                      }
                      
                      final Map<String, Document> resultMap = fdf.parseXMLDocuments(formName, path);
                      
                      if (logger.isDebugEnabled())
                      {
                         logger.debug("received " + resultMap.size() + 
                                      " documents in " + path +
                                      " with form name " + formName);
                      }
                      
                      // create a root document for rooting all the results.  we do this
                      // so that each document root element has a common parent node
                      // and so that xpath axes work properly
                      final Document rootNodeDocument = XMLUtil.newDocument();
                      final Element rootNodeDocumentEl = 
                         rootNodeDocument.createElementNS(NamespaceService.ALFRESCO_URI,
                                                          NamespaceService.ALFRESCO_PREFIX + ":file_list");
                      rootNodeDocumentEl.setAttribute("xmlns:" + NamespaceService.ALFRESCO_PREFIX, 
                                                      NamespaceService.ALFRESCO_URI); 
                      rootNodeDocument.appendChild(rootNodeDocumentEl);
               
                      final List<Node> result = new ArrayList<Node>(resultMap.size());
                      for (Map.Entry<String, Document> e : resultMap.entrySet())
                      {
                         final Element documentEl = e.getValue().getDocumentElement();
                         documentEl.setAttribute("xmlns:" + NamespaceService.ALFRESCO_PREFIX, 
                                                 NamespaceService.ALFRESCO_URI); 
                         documentEl.setAttributeNS(NamespaceService.ALFRESCO_URI, 
                                                   NamespaceService.ALFRESCO_PREFIX + ":file_name", 
                                                   e.getKey());
                         final Node n = rootNodeDocument.importNode(documentEl, true);
                         rootNodeDocumentEl.appendChild(n);
                         result.add(n);
                      }
                      return result.toArray(new Node[result.size()]);
                   }
                });
      model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX,
                                  "_getAVMPath",
                                  namespacePrefixResolver),
                new RenderingEngine.TemplateProcessorMethod()
                {
                   public Object exec(final Object[] arguments)
                   {
                      if (arguments.length != 1)
                      {
                         throw new IllegalArgumentException("expected one argument to _getAVMPath.  got " + 
                                                            arguments.length);
                      }
                      if (! (arguments[0] instanceof String))
                      {
                         throw new ClassCastException("expected arguments[0] to be a " + String.class.getName() +
                                                      ".  got a " + arguments[0].getClass().getName() + ".");
                      }

                      final String path = (String)arguments[0];
                      
                      if (logger.isDebugEnabled())
                      {
                         logger.debug("tpm_getAVMPAth('" + path + "'), parentPath = " + parentPath);
                      }
                      
                      return AVMUtil.buildPath(parentPath,
                                                    path,
                                                    AVMUtil.PathRelation.WEBAPP_RELATIVE);
                   }
                });

      // add the xml document
      model.put(RenderingEngine.ROOT_NAMESPACE, formInstanceData.getDocument());
      return model;
   }

   protected static FormDataFunctions getFormDataFunctions()
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      final WebApplicationContext wac = 
         FacesContextUtils.getRequiredWebApplicationContext(fc);
      return new FormDataFunctions((AVMRemote)wac.getBean("avmRemote"));
   }

   private ServiceRegistry getServiceRegistry()
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      return Repository.getServiceRegistry(fc);
   }

   public int hashCode()
   {
      return this.getName().hashCode();
   }

   public String toString()
   {
      return this.getClass().getName() + "{name : " + this.getName() + "}";
   }
   
   public boolean isExists()
   {
       final NodeService nodeService = this.getServiceRegistry().getNodeService();
       return nodeService.exists(this.nodeRef);
   }

}

