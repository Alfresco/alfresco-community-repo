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
 * http://www.alfresco.com/legal/licensing */
package org.alfresco.web.forms;

import freemarker.ext.dom.NodeModel;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;
import javax.faces.context.FacesContext;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.namespace.*;
import org.alfresco.service.cmr.remote.AVMRemote;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;


/**
 * Implementation of a rendering engine template
 */
public class RenderingEngineTemplateImpl
   implements RenderingEngineTemplate
{
   private static final Log LOGGER = LogFactory.getLog(RenderingEngineTemplateImpl.class);

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

   private final NodeRef nodeRef;
   private final NodeRef renditionPropertiesNodeRef;

   protected RenderingEngineTemplateImpl(final NodeRef nodeRef,
                                         final NodeRef renditionPropertiesNodeRef)
   {
      if (nodeRef == null)
      {
         throw new NullPointerException();
      }
      if (renditionPropertiesNodeRef == null)
      {
         throw new NullPointerException();
      }
      this.nodeRef = nodeRef;
      this.renditionPropertiesNodeRef = renditionPropertiesNodeRef;
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
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final String renderingEngineName = (String)
         nodeService.getProperty(this.nodeRef,
                                 WCMAppModel.PROP_PARENT_RENDERING_ENGINE_NAME);
      final FormsService fs = FormsService.getInstance();
      return fs.getRenderingEngine(renderingEngineName);
   }

   /**
    * Generates an output path for the rendition by compiling the output path pattern
    * as a freemarker template.
    *
    * @param formInstanceData the form instance data to use for the rendition path.
    * @param currentAVMPath the current path in which the form is being created.
    *
    * @return the output path to use for renditions.
    */
   public String getOutputPathForRendition(final FormInstanceData formInstanceData, final String currentAVMPath)
   {
      final ServiceRegistry sr = this.getServiceRegistry();
      final NodeService nodeService = sr.getNodeService();
      final AVMService avmService = sr.getAVMService();

      final String formInstanceDataAVMPath = formInstanceData.getPath();

      final Map<String, Object> root = new HashMap<String, Object>();
      
      final String webappName =
         (avmService.hasAspect(-1,
                               AVMUtil.getWebappPath(formInstanceDataAVMPath),
                               WCMAppModel.ASPECT_WEBAPP)
          ? AVMUtil.getWebapp(formInstanceDataAVMPath)
          : null);
      root.put("webapp", webappName);

      final String formInstanceDataName = formInstanceData.getName();
      root.put("name", formInstanceDataName.replaceAll("(.+)\\..*", "$1"));
      root.put("extension", 
               sr.getMimetypeService().getExtension(this.getMimetypeForRendition()));
      Document formInstanceDataDocument = null;
      try
      {
         formInstanceDataDocument = formInstanceData.getDocument();
      }
      catch (Exception e)
      {
         LOGGER.error(e);
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
         LOGGER.error(te.getMessage(), te);
         throw new AlfrescoRuntimeException("Error processing output path pattern " + outputPathPattern + 
                                            " for " + formInstanceDataName + 
                                            " in webapp " + webappName +
                                            ":\n" + te.getMessage(), 
                                            te);
      }

      result = AVMUtil.buildPath(parentAVMPath, 
                                      result,
                                      AVMUtil.PathRelation.SANDBOX_RELATIVE);
      LOGGER.debug("processed pattern " + outputPathPattern + " as " + result);
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
      final AVMService avmService = this.getServiceRegistry().getAVMService();
      final boolean isRegenerate = avmService.lookup(-1, renditionAvmPath) != null;
      if (!isRegenerate)
      {
         final String parentAVMPath = AVMNodeConverter.SplitBase(renditionAvmPath)[0];
         AVMUtil.makeAllDirectories(parentAVMPath);
         avmService.createFile(parentAVMPath,
                               AVMNodeConverter.SplitBase(renditionAvmPath)[1]);
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("Created file node for file: " + renditionAvmPath);
      }

      final Rendition result = new RenditionImpl(AVMNodeConverter.ToNodeRef(-1, renditionAvmPath));
      this.render(formInstanceData, result);

      if (!isRegenerate)
      {
         avmService.addAspect(renditionAvmPath, WCMAppModel.ASPECT_FORM_INSTANCE_DATA);
         avmService.addAspect(renditionAvmPath, ContentModel.ASPECT_TITLED);
         avmService.addAspect(renditionAvmPath, WCMAppModel.ASPECT_RENDITION);

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
      final OutputStream out = rendition.getOutputStream();
      try
      {
         this.getRenderingEngine().render(this.buildModel(formInstanceData, rendition), 
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

      final AVMService avmService = this.getServiceRegistry().getAVMService();
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
      final String sandboxUrl = AVMUtil.buildStoreUrl(formInstanceDataAvmPath);
      final String webappUrl = AVMUtil.buildWebappUrl(formInstanceDataAvmPath);
      final HashMap<QName, Object> model = new HashMap<QName, Object>();
      // add simple scalar parameters
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
                      LOGGER.debug("request to resolve resource " + name +
                                   " webapp url is " + webappUrl +
                                   " and data dictionary workspace is " + parentNodeRef);
                      final NodeRef result = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, name);
                      if (result != null)
                      {
                         final ContentService contentService = 
                            RenderingEngineTemplateImpl.this.getServiceRegistry().getContentService();
                         try
                         {
                            LOGGER.debug("found " + name + " in data dictonary: " + result);
                            return contentService.getReader(result, ContentModel.PROP_CONTENT).getContentInputStream();
                         }
                         catch (Exception e)
                         {
                            LOGGER.debug(e);
                         }
                      }

                      try
                      {
                         final String[] path = (name.startsWith("/") ? name.substring(1) : name).split("/");
                         for (int i = 0; i < path.length; i++)
                         {
                            path[i] = URLEncoder.encode(path[i], "utf-8").replace("+", "%20");
                         }
                         
                         final URI uri = new URI(webappUrl + '/' + StringUtils.join(path, '/'));
                         if (LOGGER.isDebugEnabled())
                            LOGGER.debug("loading " + uri);
                         return uri.toURL().openStream();
                      }
                      catch (Exception e)
                      {
                         LOGGER.debug(e);
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
                      LOGGER.debug("tpm_parseXMLDocument('" + path + 
                                   "'), parentPath = " + parentPath);
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
                      LOGGER.debug("tpm_parseXMLDocuments('" + formName + "','" + path + 
                                   "'), parentPath = " + parentPath);
                      final Map<String, Document> resultMap = fdf.parseXMLDocuments(formName, path);
                      LOGGER.debug("received " + resultMap.size() + 
                                   " documents in " + path +
                                   " with form name " + formName);

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
                      LOGGER.debug("tpm_getAVMPAth('" + path + "'), parentPath = " + parentPath);
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
}

