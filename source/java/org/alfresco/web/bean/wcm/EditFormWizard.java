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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.web.bean.wcm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.forms.xforms.FormBuilderException;
import org.alfresco.web.forms.xforms.SchemaUtil;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormsService;
import org.alfresco.web.forms.RenderingEngineTemplate;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing bean for the Edit Form wizard.
 * 
 * @author Ariel Backenroth
 */
public class EditFormWizard 
   extends CreateFormWizard
{
   private final static Log LOGGER = LogFactory.getLog(EditFormWizard.class);

   private List<RenderingEngineTemplateData> removedRenderingEngineTemplates;

   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   /**
    * Initialises the wizard
    */
   @Override
   public void init(final Map<String, String> parameters)
   {
      super.init(parameters);
      
      // the editMode flag is used to disabled some wizard fields
      //      this.editMode = true;
      
      final NodeRef formNodeRef = this.browseBean.getActionSpace().getNodeRef();
      if (formNodeRef == null)
      {
         throw new IllegalArgumentException("Edit Form wizard requires action node context.");
      }

      final Form form = FormsService.getInstance().getForm(formNodeRef);
      // simple properties
      this.setFormName(form.getName());
      this.setFormTitle(form.getTitle());
      this.setFormDescription(form.getDescription());
      this.setSchemaRootElementName(form.getSchemaRootElementName());
      final NodeRef schemaNodeRef = (NodeRef)
         this.nodeService.getProperty(formNodeRef, WCMAppModel.PROP_XML_SCHEMA);
      this.setSchemaFileName((String)this.nodeService.getProperty(schemaNodeRef, 
                                                                  ContentModel.PROP_NAME));
      try
      {
         this.schema = SchemaUtil.parseSchema(form.getSchema(), false);
      }
      catch (Throwable t)
      {
         final String msg = "unable to parse " + form.getName();
         Utils.addErrorMessage(msg, t);
      }
      final WorkflowDefinition wf = form.getDefaultWorkflow();
      if (wf != null)
      {
         this.defaultWorkflowName = wf.getName();
      }
      else
      {
         this.applyDefaultWorkflow = false;
      }
      this.setOutputPathPatternForFormInstanceData(form.getOutputPathPattern());

      for (RenderingEngineTemplate ret : form.getRenderingEngineTemplates())
      {
         final RenderingEngineTemplateData data =
            this.new RenderingEngineTemplateData(ret);
         this.renderingEngineTemplates.add(data);
      }
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) 
      throws Exception
   {
      final NodeRef formNodeRef = this.browseBean.getActionSpace().getNodeRef();
      
      // apply the name, title and description props
      this.nodeService.setProperty(formNodeRef, ContentModel.PROP_NAME, this.getFormName());
      this.nodeService.setProperty(formNodeRef, ContentModel.PROP_TITLE, this.getFormTitle());
      this.nodeService.setProperty(formNodeRef, ContentModel.PROP_DESCRIPTION, this.getFormDescription());
      this.nodeService.setProperty(formNodeRef, 
                                   WCMAppModel.PROP_OUTPUT_PATH_PATTERN, 
                                   this.getOutputPathPatternForFormInstanceData());
      this.nodeService.setProperty(formNodeRef,
                                   WCMAppModel.PROP_XML_SCHEMA_ROOT_ELEMENT_NAME,
                                   this.getSchemaRootElementName());
      final WorkflowDefinition wd = this.getDefaultWorkflowDefinition();
      final List<ChildAssociationRef> workflowRefs = 
         this.nodeService.getChildAssocs(formNodeRef,
                                         WCMAppModel.ASSOC_FORM_WORKFLOW_DEFAULTS,
                                         RegexQNamePattern.MATCH_ALL);

      if (wd != null && workflowRefs.size() == 0)
      {
         LOGGER.debug("adding workflow definition " + wd.getName() + 
                      " to form " + this.getFormName());
         final Map<QName, Serializable> props = new HashMap<QName, Serializable>(1, 1.0f);
         props.put(WCMAppModel.PROP_WORKFLOW_NAME, wd.getName());
         this.nodeService.createNode(formNodeRef,
                                     WCMAppModel.ASSOC_FORM_WORKFLOW_DEFAULTS,
                                     WCMAppModel.ASSOC_FORM_WORKFLOW_DEFAULTS,
                                     WCMAppModel.TYPE_WORKFLOW_DEFAULTS,
                                     props);
      }
      else if (wd != null && workflowRefs.size() == 1)
      {
         LOGGER.debug("setting workflow definition " + wd.getName() + 
                      " to form " + this.getFormName());
         this.nodeService.setProperty(workflowRefs.get(0).getChildRef(),
                                      WCMAppModel.PROP_WORKFLOW_NAME,
                                      wd.getName());
      }              
      else if (wd == null && workflowRefs.size() == 1)
      {
         LOGGER.debug("removing workflow definitions from form " + this.getFormName());
         this.nodeService.removeChild(formNodeRef, workflowRefs.get(0).getChildRef());
      }         

      if (this.getSchemaFile() != null)
      {
         FileInfo fileInfo = 
            this.fileFolderService.create(formNodeRef,
                                          this.getSchemaFileName(),
                                          ContentModel.TYPE_CONTENT);
         // get a writer for the content and put the file
         final ContentWriter writer = this.contentService.getWriter(fileInfo.getNodeRef(),
                                                                    ContentModel.PROP_CONTENT,
                                                                    true);
         // set the mimetype and encoding
         writer.setMimetype(MimetypeMap.MIMETYPE_XML);
         writer.setEncoding("UTF-8");
         writer.putContent(this.getSchemaFile());
         this.nodeService.setProperty(formNodeRef,
                                      WCMAppModel.PROP_XML_SCHEMA, 
                                      fileInfo.getNodeRef());
      }

      if (this.removedRenderingEngineTemplates != null)
      {
         for (RenderingEngineTemplateData retd : this.removedRenderingEngineTemplates)
         {
            this.nodeService.removeChild(formNodeRef, retd.getNodeRef());
         }
      }

      for (RenderingEngineTemplateData retd : this.renderingEngineTemplates)
      {
         if (retd.getFile() != null)
         {
            this.saveRenderingEngineTemplate(retd, formNodeRef);
         }
      }
      return AlfrescoNavigationHandler.CLOSE_WIZARD_OUTCOME;
   }

   /**
    * Action handler called when the Remove button is pressed to remove a 
    * rendering engine
    */
   @Override
   public void removeSelectedRenderingEngineTemplate(final ActionEvent event)
   {
      final RenderingEngineTemplateData wrapper = (RenderingEngineTemplateData)
         this.renderingEngineTemplatesDataModel.getRowData();
      if (wrapper != null)
      {
         if (this.removedRenderingEngineTemplates == null)
         {
            this.removedRenderingEngineTemplates = new LinkedList<RenderingEngineTemplateData>();
         }
         this.removedRenderingEngineTemplates.add(wrapper);
      }
      super.removeSelectedRenderingEngineTemplate(event);
   }


   /** Indicates whether or not the wizard is currently in edit mode */
   public boolean getEditMode()
   {
      return true;
   }
}
