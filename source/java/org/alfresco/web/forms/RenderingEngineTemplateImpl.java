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
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.namespace.QName;
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
   private final NodeService nodeService;
   private final ContentService contentService;
   private final TemplateService templateService;

   protected RenderingEngineTemplateImpl(final NodeRef nodeRef,
                                         final NodeRef renditionPropertiesNodeRef,
                                         final NodeService nodeService,
                                         final ContentService contentService,
                                         final TemplateService templateService)
   {
      this.nodeRef = nodeRef;
      this.renditionPropertiesNodeRef = renditionPropertiesNodeRef;
      this.nodeService = nodeService;
      this.contentService = contentService;
      this.templateService = templateService;
   }

   public String getName()
   {
      return (String)
         this.nodeService.getProperty(this.nodeRef, 
                                      ContentModel.PROP_NAME);
   }

   public String getDescription()
   {
      return (String)
         this.nodeService.getProperty(this.nodeRef, 
                                      ContentModel.PROP_DESCRIPTION);
   }
   
   public String getOutputPathPattern()
   {
      return (String)
         this.nodeService.getProperty(this.renditionPropertiesNodeRef, 
                                      WCMModel.PROP_OUTPUT_PATH_PATTERN_FOR_RENDITION);
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
      final ContentReader contentReader = 
         this.contentService.getReader(this.nodeRef, ContentModel.TYPE_CONTENT);
      return contentReader.getContentInputStream();
   }

   /**
    * Provides the rendering engine to use when processing this template.
    *
    * @return the rendering engine to use when processing this template.
    */
   public RenderingEngine getRenderingEngine()
   {
      final String renderingEngineName = (String)
         this.nodeService.getProperty(this.nodeRef,
                                      WCMModel.PROP_PARENT_RENDERING_ENGINE_NAME);
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
      final String outputPathPattern = (String)
         this.nodeService.getProperty(this.renditionPropertiesNodeRef, 
                                      WCMModel.PROP_OUTPUT_PATH_PATTERN_FOR_RENDITION);
      final String formInstanceDataAVMPath = 
         AVMNodeConverter.ToAVMVersionPath(formInstanceDataNodeRef).getSecond();

      final TemplateHashModel formInstanceDataModel = new TemplateHashModel()
      {
         private TemplateModel formInstanceDataModel; 

         public TemplateModel get(final String key)
         {
            LOGGER.debug("looking up property " + key);
            if ("xml".equals(key))
            {
               try
               {
                  if (formInstanceDataModel == null)
                  {
                     final FormsService fs = FormsService.getInstance();
                     final Document formInstanceData = fs.parseXML(formInstanceDataNodeRef);
                     formInstanceDataModel = NodeModel.wrap(formInstanceData);
                  }
                  return formInstanceDataModel;
               }
               catch (Exception e)
               {
                  LOGGER.error(e);
                  return null;
               }
            }
            else
            {
               final Map<QName, Serializable> properties = 
                  nodeService.getProperties(formInstanceDataNodeRef);
               for (QName qname : properties.keySet())
               {
                  if (qname.getLocalName().equals(key))
                  {
                     return new SimpleScalar((String)properties.get(qname));
                  }
               }
            }
            return null;
         }

         public boolean isEmpty()
         {
            return false;
         }
      };

      final Map<String, TemplateModel> root = new HashMap<String, TemplateModel>();
      root.put("formInstanceData", formInstanceDataModel);
      root.put("date", new SimpleDate(new Date(), SimpleDate.DATETIME));

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
      return (String)
         this.nodeService.getProperty(this.renditionPropertiesNodeRef, 
                                      WCMModel.PROP_MIMETYPE_FOR_RENDITION);
   }

   public void registerRendition(final NodeRef renditionNodeRef,
                                 final NodeRef primaryFormInstanceDataNodeRef)
   {
      final Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
      props.put(WCMModel.PROP_PARENT_RENDERING_ENGINE_TEMPLATE, this.nodeRef);
      props.put(WCMModel.PROP_PRIMARY_FORM_INSTANCE_DATA, primaryFormInstanceDataNodeRef);
      this.nodeService.addAspect(renditionNodeRef, WCMModel.ASPECT_RENDITION, props);
   }
}