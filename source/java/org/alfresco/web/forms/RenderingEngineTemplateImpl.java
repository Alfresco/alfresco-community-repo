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
import freemarker.template.SimpleDate;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.faces.context.FacesContext;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.namespace.QName;
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
                                             WCMAppModel.PROP_OUTPUT_PATH_PATTERN_RENDITION);
   }

   public NodeRef getNodeRef()
   {
      return this.nodeRef;
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
   public String getOutputPathForRendition(final NodeRef formInstanceDataNodeRef)
   {
      final ServiceRegistry sr = this.getServiceRegistry();
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final String outputPathPattern = (String)
         nodeService.getProperty(this.renditionPropertiesNodeRef, 
                                 WCMAppModel.PROP_OUTPUT_PATH_PATTERN_RENDITION);
      final String formInstanceDataAVMPath = 
         AVMNodeConverter.ToAVMVersionPath(formInstanceDataNodeRef).getSecond();

      final Map<String, Object> root = new HashMap<String, Object>();
      
      final String formInstanceDataName = (String)
         sr.getNodeService().getProperty(formInstanceDataNodeRef, ContentModel.PROP_NAME);
      root.put("name", 
               formInstanceDataName.replaceAll("(.+)\\..*", "$1"));
      root.put("extension", 
               sr.getMimetypeService().getExtension(this.getMimetypeForRendition()));

      try
      {
         final FormsService fs = FormsService.getInstance();
         root.put("xml", NodeModel.wrap(fs.parseXML(formInstanceDataNodeRef)));
      }
      catch (Exception e)
      {
         LOGGER.error(e);
      }

      root.put("node", new TemplateNode(formInstanceDataNodeRef, sr, null));
      root.put("date", new SimpleDate(new Date(), SimpleDate.DATETIME));

      final TemplateService templateService = sr.getTemplateService();
      String result = templateService.processTemplateString(null, 
                                                            outputPathPattern, 
                                                            new SimpleHash(root));
      final String parentAVMPath = AVMNodeConverter.SplitBase(formInstanceDataAVMPath)[0];
      result = AVMConstants.buildAbsoluteAVMPath(parentAVMPath, result);
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

   public void registerRendition(final NodeRef renditionNodeRef,
                                 final NodeRef primaryFormInstanceDataNodeRef)
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
      props.put(WCMAppModel.PROP_PARENT_RENDERING_ENGINE_TEMPLATE, this.nodeRef);
      props.put(WCMAppModel.PROP_PRIMARY_FORM_INSTANCE_DATA, primaryFormInstanceDataNodeRef);
      nodeService.addAspect(renditionNodeRef, WCMAppModel.ASPECT_RENDITION, props);
   }

   private ServiceRegistry getServiceRegistry()
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      return Repository.getServiceRegistry(fc);
   }
}

