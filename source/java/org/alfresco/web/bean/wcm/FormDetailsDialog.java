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
package org.alfresco.web.bean.wcm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
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
         WorkflowDefinition def = this.workflowService.getDefinitionByName(this.workflowSelectedValue[0]);
         form.setWorkflow(new CreateWebsiteWizard.WorkflowWrapper(def.getName(), def.getTitle(), def.getDescription()));
      }
      return outcome;
   }
}
