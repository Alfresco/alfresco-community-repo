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
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
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
   
   /**
    * Provides an input stream to the rendering engine template.
    * 
    * @return the input stream to the rendering engine template.
    */
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
    * Returns the output path to use for renditions.
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

   /**
    * Returns the mime type to use for generated assets.
    *
    * @return the mime type to use for generated assets.
    */
   public String getMimetypeForRendition()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.renditionPropertiesNodeRef, 
                                             WCMAppModel.PROP_MIMETYPE_FOR_RENDITION);
   }

   /**
    * Produces a rendition of the provided formInstanceData.
    */
   public Rendition render(final FormInstanceData formInstanceData)
      throws IOException,
      SAXException,
      RenderingEngine.RenderingException
   {
      final AVMService avmService = this.getServiceRegistry().getAVMService();
      final String renditionAvmPath = this.getOutputPathForRendition(formInstanceData);
      if (avmService.lookup(-1, renditionAvmPath) == null)
      {
         final String parentAVMPath = AVMNodeConverter.SplitBase(renditionAvmPath)[0];
         AVMConstants.makeAllDirectories(parentAVMPath);
         avmService.createFile(parentAVMPath,
                               AVMNodeConverter.SplitBase(renditionAvmPath)[1]);
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("Created file node for file: " + renditionAvmPath);
      }

      final Rendition result = new RenditionImpl(AVMNodeConverter.ToNodeRef(-1, renditionAvmPath));
      this.getRenderingEngine().render(formInstanceData, this, result);

      avmService.addAspect(renditionAvmPath, WCMAppModel.ASPECT_FORM_INSTANCE_DATA);
      avmService.addAspect(renditionAvmPath, ContentModel.ASPECT_TITLED);
      avmService.addAspect(renditionAvmPath, WCMAppModel.ASPECT_RENDITION);

      final Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(5, 1.0f);
      props.put(WCMAppModel.PROP_PARENT_FORM_NAME, 
                new PropertyValue(DataTypeDefinition.TEXT, 
                                  formInstanceData.getForm().getName()));
      props.put(ContentModel.PROP_TITLE,
                new PropertyValue(DataTypeDefinition.TEXT,
                                  AVMNodeConverter.SplitBase(renditionAvmPath)[1]));
      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      props.put(ContentModel.PROP_DESCRIPTION,
                new PropertyValue(DataTypeDefinition.TEXT,
                                  MessageFormat.format(bundle.getString("default_rendition_description"), 
                                                       this.getTitle(),
                                                       AVMConstants.getSandboxRelativePath(renditionAvmPath))));
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

      avmService.setNodeProperties(renditionAvmPath, props);
      
      final PropertyValue pv = 
         avmService.getNodeProperty(-1, formInstanceData.getPath(), WCMAppModel.PROP_RENDITIONS);
      Collection<Serializable> renditions = (pv == null 
                                             ? new LinkedList<Serializable>() 
                                             : pv.getCollection(DataTypeDefinition.TEXT));
      renditions.add(AVMConstants.getStoreRelativePath(renditionAvmPath));
      avmService.setNodeProperty(formInstanceData.getPath(), 
                                 WCMAppModel.PROP_RENDITIONS,
                                 new PropertyValue(DataTypeDefinition.TEXT,
                                                   (Serializable)renditions));
      return result;
   }

   private ServiceRegistry getServiceRegistry()
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      return Repository.getServiceRegistry(fc);
   }
}

