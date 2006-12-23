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
import javax.faces.context.FacesContext;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.alfresco.web.forms.xforms.XFormsProcessor;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

class FormImpl 
    implements Form
{
   private static final Log LOGGER = LogFactory.getLog(FormImpl.class);
   
   private final NodeRef folderNodeRef;

   private final static LinkedList<FormProcessor> PROCESSORS = 
      new LinkedList<FormProcessor>();
   static 
   {
      FormImpl.PROCESSORS.add(new XFormsProcessor());
   }
   
   public FormImpl(final NodeRef folderNodeRef)
   {
      this.folderNodeRef = folderNodeRef;
   }

   public String getName()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.folderNodeRef, 
                                             ContentModel.PROP_NAME);
   }

   public String getTitle()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.folderNodeRef, 
                                             ContentModel.PROP_TITLE);
   }

   public String getDescription()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.folderNodeRef, 
                                             ContentModel.PROP_DESCRIPTION);
   }
   
   public String getOutputPathPattern()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(this.folderNodeRef,
                                             WCMAppModel.PROP_OUTPUT_PATH_PATTERN_FORM_INSTANCE_DATA);
   }

   public WorkflowDefinition getDefaultWorkflow()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final String defaultWorkflowName = (String)nodeService.getProperty(this.folderNodeRef,
                                                                       WCMAppModel.PROP_DEFAULT_WORKFLOW_NAME);
      final WorkflowService workflowService = this.getServiceRegistry().getWorkflowService();
      return (defaultWorkflowName != null
              ? workflowService.getDefinitionByName(defaultWorkflowName)
              : null);
   }

   public String getOutputPathForFormInstanceData(final Document formInstanceData,
                                                  final String formInstanceDataName,
                                                  final String parentAVMPath,
                                                  final String webappName)
   {
      final String outputPathPattern = this.getOutputPathPattern();

      final Map<String, Object> root = new HashMap<String, Object>();
      root.put("webapp", webappName);
      root.put("xml", NodeModel.wrap(formInstanceData));
      root.put("name", formInstanceDataName);
      root.put("date", new SimpleDate(new Date(), SimpleDate.DATETIME));

      final TemplateService templateService = this.getServiceRegistry().getTemplateService();

      String result = templateService.processTemplateString(null, 
                                                            outputPathPattern, 
                                                            new SimpleHash(root));
      result = AVMConstants.buildPath(parentAVMPath, 
                                      result,
                                      AVMConstants.PathRelation.SANDBOX_RELATIVE);
      LOGGER.debug("processed pattern " + outputPathPattern + " as " + result);
      return result;
   }

   public String getSchemaRootElementName()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)nodeService.getProperty(folderNodeRef, 
                                             WCMAppModel.PROP_XML_SCHEMA_ROOT_ELEMENT_NAME);
   }

   public Document getSchema()
      throws IOException, 
      SAXException
   {
      final FormsService ts = FormsService.getInstance();
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final NodeRef schemaNodeRef = (NodeRef)nodeService.getProperty(folderNodeRef,
                                                                     WCMAppModel.PROP_XML_SCHEMA);
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
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final List<AssociationRef> refs = nodeService.getTargetAssocs(this.folderNodeRef, 
                                                                    WCMAppModel.ASSOC_RENDERING_ENGINE_TEMPLATES);
      final List<RenderingEngineTemplate> result = new ArrayList<RenderingEngineTemplate>(refs.size());
      for (AssociationRef assoc : refs)
      {
         final NodeRef retNodeRef = assoc.getTargetRef();
         for (ChildAssociationRef assoc2 : nodeService.getChildAssocs(retNodeRef,
                                                                      WCMAppModel.ASSOC_RENDITION_PROPERTIES,
                                                                      RegexQNamePattern.MATCH_ALL))
         {
            final NodeRef renditionPropertiesNodeRef = assoc2.getChildRef();
            
            final RenderingEngineTemplate ret = 
               new RenderingEngineTemplateImpl(retNodeRef, renditionPropertiesNodeRef);
            LOGGER.debug("loaded rendering engine template " + ret);
            result.add(ret);
         }
      }
      return result;
   }

   public void registerFormInstanceData(final NodeRef formInstanceDataNodeRef)
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
      props.put(WCMAppModel.PROP_PARENT_FORM, this.folderNodeRef);
      props.put(WCMAppModel.PROP_PARENT_FORM_NAME, this.getName());
      nodeService.addAspect(formInstanceDataNodeRef, WCMAppModel.ASPECT_FORM_INSTANCE_DATA, props);
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

   private ServiceRegistry getServiceRegistry()
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      return Repository.getServiceRegistry(fc);
   }
}