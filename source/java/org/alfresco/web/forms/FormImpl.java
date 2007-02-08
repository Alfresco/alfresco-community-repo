/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
import org.alfresco.web.bean.wcm.AVMWorkflowUtil;
import org.alfresco.web.forms.xforms.XFormsProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class FormImpl 
    implements Form
{
   private static final Log LOGGER = LogFactory.getLog(FormImpl.class);
   
   private final NodeRef folderNodeRef;
   private transient Map<String, RenderingEngineTemplate> renderingEngineTemplates;

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
                                             WCMAppModel.PROP_OUTPUT_PATH_PATTERN);
   }

   public WorkflowDefinition getDefaultWorkflow()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final WorkflowService workflowService = this.getServiceRegistry().getWorkflowService();

      final NodeRef workflowRef = this.getDefaultWorkflowNodeRef();
      final String workflowName = 
         (workflowRef != null 
          ? (String)nodeService.getProperty(workflowRef, WCMAppModel.PROP_WORKFLOW_NAME)
          : null);
               
      return workflowName != null ? workflowService.getDefinitionByName(workflowName) : null;
   }

   public Map<QName, Serializable> getDefaultWorkflowParameters()
   {
      final NodeRef workflowRef = this.getDefaultWorkflowNodeRef();
      return (Map<QName, Serializable>)AVMWorkflowUtil.deserializeWorkflowParams(workflowRef);
   }

   protected NodeRef getDefaultWorkflowNodeRef()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final List<ChildAssociationRef> workflowRefs = 
         nodeService.getChildAssocs(this.folderNodeRef,
                                    WCMAppModel.ASSOC_FORM_WORKFLOW_DEFAULTS,
                                    RegexQNamePattern.MATCH_ALL);
      if (workflowRefs.size() == 0)
      {
         return null;
      }

      assert workflowRefs.size() == 1 : "found more than one workflow parameters node for " + this.getName();

      return workflowRefs.get(0).getChildRef();
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
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final NodeRef schemaNodeRef = (NodeRef)
         nodeService.getProperty(folderNodeRef, WCMAppModel.PROP_XML_SCHEMA);
      return XMLUtil.parse(schemaNodeRef,
                           this.getServiceRegistry().getContentService());
   }

   public List<FormProcessor> getFormProcessors()
   {
      return PROCESSORS;
   }

   public void addRenderingEngineTemplate(final RenderingEngineTemplate ret)
   {
      throw new UnsupportedOperationException();
   }

   public List<RenderingEngineTemplate> getRenderingEngineTemplates()
   {
      if (this.renderingEngineTemplates == null)
      {
         this.renderingEngineTemplates = this.loadRenderingEngineTemplates();
      }
      return Collections.unmodifiableList(new ArrayList(this.renderingEngineTemplates.values()));
   }

   public RenderingEngineTemplate getRenderingEngineTemplate(final String name)
   {
      if (this.renderingEngineTemplates == null)
      {
         this.renderingEngineTemplates = this.loadRenderingEngineTemplates();
      }
      return this.renderingEngineTemplates.get(name);
   }

   public NodeRef getNodeRef()
   {
      return this.folderNodeRef;
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

   public boolean equals(final Object other)
   {
      if (other == null || !(other instanceof FormImpl))
      {
         return false;
      }
      return this.getNodeRef().equals(((FormImpl)other).getNodeRef());
   }

   protected ServiceRegistry getServiceRegistry()
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      return Repository.getServiceRegistry(fc);
   }

   protected Map<String, RenderingEngineTemplate> loadRenderingEngineTemplates()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final List<AssociationRef> refs = nodeService.getTargetAssocs(this.folderNodeRef, 
                                                                    WCMAppModel.ASSOC_RENDERING_ENGINE_TEMPLATES);
      final Map<String, RenderingEngineTemplate> result = new HashMap<String, RenderingEngineTemplate>(refs.size(), 1.0f);
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
            result.put(ret.getName(), ret);
         }
      }
      return result;
   }
}