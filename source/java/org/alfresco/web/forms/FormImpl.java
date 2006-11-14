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
import java.io.*;
import java.net.URI;
import java.io.Serializable;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.alfresco.web.forms.xforms.XFormsProcessor;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class FormImpl 
    implements Form
{
   private static final Log LOGGER = LogFactory.getLog(FormImpl.class);
   
   private final NodeRef folderNodeRef;
   private final NodeService nodeService;
   private final ContentService contentService;
   private final TemplateService templateService;

   private final static LinkedList<FormProcessor> PROCESSORS = 
      new LinkedList<FormProcessor>();
   static 
   {
      FormImpl.PROCESSORS.add(new XFormsProcessor());
   }
   
   public FormImpl(final NodeRef folderNodeRef,
                   final NodeService nodeService,
                   final ContentService contentService,
                   final TemplateService templateService)
   {
      this.folderNodeRef = folderNodeRef;
      this.nodeService = nodeService;
      this.contentService = contentService;
      this.templateService = templateService;
   }

   public String getName()
   {
      return (String)
         this.nodeService.getProperty(this.folderNodeRef, 
                                      ContentModel.PROP_TITLE);
   }

   public String getDescription()
   {
      return (String)
         this.nodeService.getProperty(this.folderNodeRef, 
                                      ContentModel.PROP_DESCRIPTION);
   }
   
   public String getOutputPathPattern()
   {
      return (String)
         this.nodeService.getProperty(this.folderNodeRef,
                                      WCMModel.PROP_OUTPUT_PATH_PATTERN_FOR_FORM_INSTANCE_DATA);
   }

   public String getOutputPathForFormInstanceData(final String parentAVMPath,
                                                  final String formInstanceDataFileName,
                                                  final Document formInstanceData)
   {
      final String outputPathPattern = (String)
         this.nodeService.getProperty(this.folderNodeRef, 
                                      WCMModel.PROP_OUTPUT_PATH_PATTERN_FOR_FORM_INSTANCE_DATA);

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
            else if ("name".equals(key))
            {
               return new SimpleScalar(formInstanceDataFileName);
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
      result = AVMConstants.buildAbsoluteAVMPath(parentAVMPath, result);
      LOGGER.debug("processed pattern " + outputPathPattern + " as " + result);
      return result;
   }

   public String getSchemaRootElementName()
   {
      return (String)
         this.nodeService.getProperty(folderNodeRef, 
                                      WCMModel.PROP_XML_SCHEMA_ROOT_ELEMENT_NAME);
   }

   public Document getSchema()
      throws IOException, 
      SAXException
   {
      final FormsService ts = FormsService.getInstance();
      final NodeRef schemaNodeRef = (NodeRef)
         this.nodeService.getProperty(folderNodeRef,
                                      WCMModel.PROP_XML_SCHEMA);
      return ts.parseXML(schemaNodeRef);
   }

   public List<FormProcessor> getFormProcessors()
   {
      return PROCESSORS;
   }

   public void addRenderingEngineTemplate(final RenderingEngineTemplate ret)
   {
//      this.renderingEngineTemplates.add(ret);
      throw new UnsupportedOperationException();
   }

   public List<RenderingEngineTemplate> getRenderingEngineTemplates()
   {
      final List<AssociationRef> refs = this.nodeService.getTargetAssocs(this.folderNodeRef, 
                                                                   WCMModel.ASSOC_RENDERING_ENGINE_TEMPLATES);
      final List<RenderingEngineTemplate> result = new ArrayList<RenderingEngineTemplate>(refs.size());
      for (AssociationRef assoc : refs)
      {
         final NodeRef retNodeRef = assoc.getTargetRef();
         for (ChildAssociationRef assoc2 : this.nodeService.getChildAssocs(retNodeRef,
                                                                           WCMModel.ASSOC_RENDITION_PROPERTIES,
                                                                           RegexQNamePattern.MATCH_ALL))
         {
            final NodeRef renditionPropertiesNodeRef = assoc2.getChildRef();
            
            final RenderingEngineTemplate ret = 
               new RenderingEngineTemplateImpl(retNodeRef,
                                               renditionPropertiesNodeRef,
                                               this.nodeService,
                                               this.contentService,
                                               this.templateService);
            LOGGER.debug("loaded rendering engine template " + ret);
            result.add(ret);
         }
      }
      return result;
   }

   public void registerFormInstanceData(final NodeRef formInstanceDataNodeRef)
   {
      final Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
      props.put(WCMModel.PROP_PARENT_FORM, this.folderNodeRef);
      props.put(WCMModel.PROP_PARENT_FORM_NAME, this.getName());
      this.nodeService.addAspect(formInstanceDataNodeRef, WCMModel.ASPECT_FORM_INSTANCE_DATA, props);
   }

   public int hashCode() 
   {
      return this.getName().hashCode();
   }

   public String toString()
   {
      return (this.getClass().getName() + "{" +
              "name: " + this.getName() + "," +
              "schemaRootElementName: " + this.getSchemaRootElementName() + "," +
              "renderingEngineTemplates: " + this.getRenderingEngineTemplates() +
              "}");
   }
}