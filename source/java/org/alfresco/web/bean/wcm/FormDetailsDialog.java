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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.wcm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.wcm.CreateWebsiteWizard.FormWrapper;
import org.alfresco.web.bean.wcm.CreateWebsiteWizard.WorkflowWrapper;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.wcm.WebResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing bean for the Website Project Form Details dialog.
 * Launched from the Form Details button on the Define Web Content Forms page.
 * 
 * @author Kevin Roast
 */
public class FormDetailsDialog extends BaseDialogBean
{
   private static final Log logger = LogFactory.getLog(FormDetailsDialog.class);
   
   protected AVMService avmService;
   protected CreateWebsiteWizard websiteWizard;
   protected WorkflowService workflowService;
   
   private String title;
   private String description;
   private String outputPathPattern;
   private String[] workflowSelectedValue;
   

   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      this.title = null;
      this.description = null;
      this.outputPathPattern = null;
      this.workflowSelectedValue = null;
   }

   @Override
   public String getContainerDescription()
   {
      return MessageFormat.format(Application.getBundle(FacesContext.getCurrentInstance()).getString("form_template_details_desc"), 
                                  this.getActionForm().getName(),
                                  this.websiteWizard.getName());
   }
   
   /**
    * @param avmService    The avmService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   /**
    * @param wizard        The Create Website Wizard to set.
    */
   public void setCreateWebsiteWizard(CreateWebsiteWizard wizard)
   {
      this.websiteWizard = wizard;
   }
   
   /**
    * @param workflowService  The WorkflowService to set.
    */
   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }
   
   /**
    * @return an object representing the form for the current action
    */
   public FormWrapper getActionForm()
   {
      return this.websiteWizard.getActionForm();
   }

   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      if (this.description == null)
      {
         this.description = getActionForm().getDescription();
      }
      return this.description;
   }

   /**
    * @param description The description to set.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * @return Returns the title.
    */
   public String getTitle()
   {
      if (this.title == null)
      {
         this.title = getActionForm().getTitle();
      }
      return this.title;
   }

   /**
    * @param title The title to set.
    */
   public void setTitle(String title)
   {
      this.title = title;
   }
   
   /**
    * @return Returns the filename pattern
    */
   public String getOutputPathPattern()
   {
      if (this.outputPathPattern == null)
      {
         this.outputPathPattern = getActionForm().getOutputPathPattern();
      }
      return this.outputPathPattern;
   }

   /**
    * @param pattern The filename pattern to set.
    */
   public void setOutputPathPattern(String pattern)
   {
      this.outputPathPattern = pattern;
   }
   
   /**
    * @return Returns the workflow Selected Value.
    */
   public String[] getWorkflowSelectedValue()
   {
      if (this.workflowSelectedValue == null)
      {
         WorkflowWrapper workflow = getActionForm().getWorkflow();
         if (workflow != null)
         {
            this.workflowSelectedValue = new String[] {workflow.getName()};
         }
      }
      return this.workflowSelectedValue;
   }

   /**
    * @param workflowSelectedValue The workflow Selected Value to set.
    */
   public void setWorkflowSelectedValue(String[] workflowSelectedValue)
   {
      this.workflowSelectedValue = workflowSelectedValue;
   }
   
   /**
    * @return List of UIListItem object representing the available workflows for the template
    */
   public List<UIListItem> getWorkflowList()
   {
      // get list of workflows from config definitions
      List<WorkflowDefinition> workflowDefs = AVMWorkflowUtil.getConfiguredWorkflows();
      List<UIListItem> items = new ArrayList<UIListItem>(workflowDefs.size());
      for (WorkflowDefinition workflowDef : workflowDefs)
      {
         UIListItem item = new UIListItem();
         item.setValue(workflowDef.getName());
         item.setLabel(workflowDef.getTitle());
         item.setDescription(workflowDef.getDescription());
         item.setImage(WebResources.IMAGE_WORKFLOW_32);
         items.add(item);
      }
      
      return items;
   }
   
   
   // ------------------------------------------------------------------------------
   // Dialog implementation

   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // push values from title/description etc. back into action FormWrapper
      FormWrapper form = getActionForm();
      if (this.title != null)
      {
         form.setTitle(this.title);
      }
      if (this.description != null)
      {
         form.setDescription(this.description);
      }
      if (this.outputPathPattern != null)
      {
         form.setOutputPathPattern(this.outputPathPattern);
      }
      if (this.workflowSelectedValue != null && this.workflowSelectedValue.length != 0)
      {
         // test to make sure we don't lose the existing workflow configuration
         String wfSelected = this.workflowSelectedValue[0];
         if (form.getWorkflow() == null || form.getWorkflow().getName().equals(wfSelected) == false)
         {
            WorkflowDefinition def = this.workflowService.getDefinitionByName(wfSelected);
            form.setWorkflow(new CreateWebsiteWizard.WorkflowWrapper(def.getName(), def.getTitle(), def.getDescription()));
         }
      }
      return outcome;
   }
}
