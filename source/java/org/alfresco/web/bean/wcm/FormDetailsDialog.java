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

import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.ui.common.component.UIListItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Kevin Roast
 */
public class FormDetailsDialog extends BaseDialogBean
{
   private static final Log logger = LogFactory.getLog(FormDetailsDialog.class);
   
   protected AVMService avmService;
   protected CreateWebsiteWizard websiteWizard;
   
   private String title;
   private String description;
   private String preScript;
   private String postScript;
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
    * @return Returns the description.
    */
   public String getDescription()
   {
      if (this.description == null)
      {
         this.description = this.websiteWizard.getActionForm().getDescription();
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
         this.title = this.websiteWizard.getActionForm().getTitle();
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
    * @return Returns the post-save Script.
    */
   public String getPostScript()
   {
      return this.postScript;
   }

   /**
    * @param postScript The post-save Script to set.
    */
   public void setPostScript(String postScript)
   {
      this.postScript = postScript;
   }

   /**
    * @return Returns the pre-save Script.
    */
   public String getPreScript()
   {
      return this.preScript;
   }

   /**
    * @param preScript The pre-save Script to set.
    */
   public void setPreScript(String preScript)
   {
      this.preScript = preScript;
   }
   
   /**
    * @return Returns the workflow Selected Value.
    */
   public String[] getWorkflowSelectedValue()
   {
      return this.workflowSelectedValue;
   }

   /**
    * @param workflowSelectedValue The workflow Selected Value to set.
    */
   public void setWorkflowSelectedValue(String[] workflowSelectedValue)
   {
      this.workflowSelectedValue = workflowSelectedValue;
   }
   
   public List<UIListItem> getWorkflowList()
   {
      List<UIListItem> items = new ArrayList<UIListItem>();
      
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
      // TODO: push values from title/description etc. back into action FormWrapper!
      return outcome;
   }
}
