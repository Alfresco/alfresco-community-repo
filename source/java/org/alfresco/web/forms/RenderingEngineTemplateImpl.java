/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.forms;

import freemarker.ext.dom.NodeModel;
import freemarker.template.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import javax.faces.context.FacesContext;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.*;
import org.alfresco.service.cmr.remote.AVMRemote;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMConstants;
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
    * @return the output path to use for renditions.
    */
   public String getOutputPathForRendition(final FormInstanceData formInstanceData /*,final String parentAVMPath */)
   {
      final ServiceRegistry sr = this.getServiceRegistry();
      final NodeService nodeService = sr.getNodeService();
      final AVMService avmService = sr.getAVMService();

      final String formInstanceDataAVMPath = formInstanceData.getPath();

      final Map<String, Object> root = new HashMap<String, Object>();
      
      final String webappName =
         (avmService.hasAspect(-1,
                               AVMConstants.getWebappPath(formInstanceDataAVMPath),
                               WCMAppModel.ASPECT_WEBAPP)
          ? AVMConstants.getWebapp(formInstanceDataAVMPath)
          : null);
      root.put("webapp", webappName);

      final String formInstanceDataName = formInstanceData.getName();
      root.put("name", formInstanceDataName.replaceAll("(.+)\\..*", "$1"));
      root.put("extension", 
               sr.getMimetypeService().getExtension(this.getMimetypeForRendition()));

      try
      {
         root.put("xml", NodeModel.wrap(formInstanceData.getDocument()));
      }
      catch (Exception e)
      {
         LOGGER.error(e);
      }

      root.put("node", new TemplateNode(((FormInstanceDataImpl)formInstanceData).getNodeRef(), sr, null));
      root.put("date", new SimpleDate(new Date(), SimpleDate.DATETIME));

      final TemplateService templateService = sr.getTemplateService();
      final String outputPathPattern = this.getOutputPathPattern();
      String result = templateService.processTemplateString(null, 
                                                            outputPathPattern,
                                                            new SimpleHash(root));
      final String parentAVMPath = AVMNodeConverter.SplitBase(formInstanceDataAVMPath)[0];
      result = AVMConstants.buildPath(parentAVMPath, 
                                      result,
                                      AVMConstants.PathRelation.SANDBOX_RELATIVE);
      LOGGER.debug("processed pattern " + outputPathPattern + " as " + result);
      return result;
   }

   public String getMimetypeForRendition()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.renditionPropertiesNodeRef, 
                                             WCMAppModel.PROP_MIMETYPE_FOR_RENDITION);
   }

   public Rendition render(final FormInstanceData formInstanceData)
      throws IOException,
      SAXException,
      RenderingEngine.RenderingException
   {
      final AVMService avmService = this.getServiceRegistry().getAVMService();
      final String renditionAvmPath = this.getOutputPathForRendition(formInstanceData);
      final boolean isRegenerate = avmService.lookup(-1, renditionAvmPath) != null;
      if (!isRegenerate)
      {
         final String parentAVMPath = AVMNodeConverter.SplitBase(renditionAvmPath)[0];
         AVMConstants.makeAllDirectories(parentAVMPath);
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
         renditions.add(AVMConstants.getStoreRelativePath(renditionAvmPath));
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
                                                       AVMConstants.getSandboxRelativePath(rendition.getPath()))));
      props.put(WCMAppModel.PROP_PARENT_RENDERING_ENGINE_TEMPLATE,
                new PropertyValue(DataTypeDefinition.NODE_REF,
                                  this.nodeRef));
      props.put(WCMAppModel.PROP_PARENT_RENDITION_PROPERTIES,
                new PropertyValue(DataTypeDefinition.NODE_REF,
                                  this.renditionPropertiesNodeRef));
      // extract a store relative path for the primary form instance data
      props.put(WCMAppModel.PROP_PRIMARY_FORM_INSTANCE_DATA, 
                new PropertyValue(DataTypeDefinition.TEXT,
                                  AVMConstants.getStoreRelativePath(formInstanceData.getPath())));

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
      final DynamicNamespacePrefixResolver namespacePrefixResolver = 
         new DynamicNamespacePrefixResolver();
      namespacePrefixResolver.registerNamespace(NamespaceService.ALFRESCO_PREFIX,
                                                NamespaceService.ALFRESCO_URI);

      final String formInstanceDataAvmPath = formInstanceData.getPath();
      final String renditionAvmPath = rendition.getPath();
      final String parentPath = AVMNodeConverter.SplitBase(formInstanceDataAvmPath)[0];
      final String sandboxUrl = AVMConstants.buildStoreUrl(formInstanceDataAvmPath);
      final HashMap<QName, Object> model = new HashMap<QName, Object>();
      // add simple scalar parameters
      model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX,
                                  "avm_sandbox_url",
                                  namespacePrefixResolver), 
                sandboxUrl);
      model.put(XSLTRenderingEngine.PROP_URI_RESOLVER_BASE_URI,
                sandboxUrl);
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
                      path = AVMConstants.buildPath(parentPath,
                                                    path,
                                                    AVMConstants.PathRelation.WEBAPP_RELATIVE);
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
                      if (arguments.length > 1)
                      {
                         throw new IllegalArgumentException("expected zero or one arguments to parseXMLDocuments.  got " +
                                                            arguments.length);
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
                      path = AVMConstants.buildPath(parentPath,
                                                    path,
                                                    AVMConstants.PathRelation.WEBAPP_RELATIVE);
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
                      return AVMConstants.buildPath(parentPath,
                                                    path,
                                                    AVMConstants.PathRelation.WEBAPP_RELATIVE);
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
}

