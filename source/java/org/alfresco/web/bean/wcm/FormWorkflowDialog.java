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

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.TransientNode;
import org.alfresco.web.bean.wcm.CreateWebsiteWizard.WorkflowWrapper;
import org.alfresco.web.bean.workflow.WorkflowUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing bean for the Website Project Configure Workflow dialog.
 * Launched from the Configure Workflow button on the Define Web Content Forms page.
 * 
 * @author Kevin Roast
 */
public class FormWorkflowDialog extends BaseDialogBean
{
   private static final Log logger = LogFactory.getLog(FormWorkflowDialog.class);
   
   protected WorkflowService workflowService;
   protected CreateWebsiteWizard websiteWizard;
   protected TransientNode workflowNode;
   
   
   /**
    * @param workflowService  The WorkflowService to set.
    */
   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }

   /**
    * @param wizard           The Create Website Wizard to set.
    */
   public void setCreateWebsiteWizard(CreateWebsiteWizard wizard)
   {
      this.websiteWizard = wizard;
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.workflowNode = null;
      WorkflowWrapper workflow = getActionWorkflow();
      if (workflow != null && workflow.getParams() != null)
      {
         // get params from action Workflow and populate the TransientNode properties and assocs
         this.workflowNode = new TransientNode(workflow.getType(),
                  "task_" + System.currentTimeMillis(), workflow.getParams());
      }
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // push serialized params back into workflow object
      if (this.workflowNode != null)
      {
         getActionWorkflow().setParams( WorkflowUtil.prepareTaskParams(this.workflowNode) );
         getActionWorkflow().setType(this.workflowNode.getType());
      }
      return outcome;
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonDisabled()
    */
   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }
   
   /**
    * @return an object representing the workflow for the current action
    */
   public WorkflowWrapper getActionWorkflow()
   {
      return this.websiteWizard.getActionWorkflow();
   }
   
   /**
    * Returns the Node representing the start task metadata required
    * 
    * @return The Node for the start task
    */
   public Node getWorkflowMetadataNode()
   {
      if (this.workflowNode == null)
      {
         // TODO: remove the 'jbpm$' prefix once bug fix to WorkflowService has been merged across!
         WorkflowDefinition flowDef = this.workflowService.getDefinitionByName("jbpm$" + getActionWorkflow().getName());
         
         if (logger.isDebugEnabled())
            logger.debug("Selected workflow: "+ flowDef);
         
         if (flowDef != null)
         {
            WorkflowTaskDefinition taskDef = flowDef.startTaskDefinition;
            if (taskDef != null)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Start task definition: " + taskDef);
               
               // create an instance of a task from the data dictionary
               this.workflowNode = new TransientNode(taskDef.metadata.getName(),
                     "task_" + System.currentTimeMillis(), null);
            }
         }
      }
      return this.workflowNode;
   }
}
