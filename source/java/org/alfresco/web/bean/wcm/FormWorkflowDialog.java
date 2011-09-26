/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.bean.wcm;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.TransientNode;
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
   private static final long serialVersionUID = -7858470945912453738L;

   private static final String MSG_ERROR_FILENAME_PATTERN = "error_filename_pattern";

   private static final Log logger = LogFactory.getLog(FormWorkflowDialog.class);
   
   private String filenamePattern;
   
   transient private WorkflowService workflowService;
   
   protected CreateWebsiteWizard websiteWizard;
   protected TransientNode workflowNode;
   
   
   /**
    * @param workflowService  The WorkflowService to set.
    */
   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }
   
   protected WorkflowService getWorkflowService()
   {
      if (this.workflowService == null)
      {
         this.workflowService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getWorkflowService();
      }
      return this.workflowService;
   }

   /**
    * @param wizard           The Create Website Wizard to set.
    */
   public void setCreateWebsiteWizard(CreateWebsiteWizard wizard)
   {
      this.websiteWizard = wizard;
   }
   
   /**
    * @return Returns the filename pattern.
    */
   public String getFilenamePattern()
   {
      if (this.filenamePattern == null)
      {
         this.filenamePattern = getActionWorkflow().getFilenamePattern();
      }
      return this.filenamePattern;
   }
   
   /**
    * @param filenamePattern The filename pattern to set.
    */
   public void setFilenamePattern(String filenamePattern)
   {
      if (this.filenamePattern != null && this.filenamePattern.length() != 0)
      {
         this.filenamePattern = filenamePattern;
      }
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.filenamePattern = null;
      this.workflowNode = null;
      
      WorkflowConfiguration workflow = getActionWorkflow();
      if (workflow == null)
      {
         throw new IllegalArgumentException("Workflow action context is mandatory.");
      }
      
      // populate the workflow if exists and already has a task type assigned
      if (workflow.getType() != null)
      {
         // bind against current params from action Workflow
         this.workflowNode = new TransientNode(workflow.getType(),
                  "task_" + System.currentTimeMillis(), workflow.getParams());
      }
      else
      {
         // no type found - init workflow node type based on workflow definition
         WorkflowDefinition flowDef = this.getWorkflowService().getDefinitionByName(workflow.getName());
         if (flowDef != null)
         {
            WorkflowTaskDefinition taskDef = flowDef.getStartTaskDefinition();
            if (taskDef != null)
            {
               // create an instance of a task from the data dictionary
               this.workflowNode = new TransientNode(taskDef.metadata.getName(),
                     "task_" + System.currentTimeMillis(), workflow.getParams());
            }
         }
      }
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      if (this.workflowNode != null)
      {
         // push serialized params back into workflow object
         WorkflowConfiguration wf = getActionWorkflow();
         Map<QName, Serializable> taskParams = WorkflowUtil.prepareTaskParams(this.workflowNode);
         if (wf.getParams() == null)
         {
            wf.setParams(taskParams);
         }
         else
         {
            // merge existing with params - as only new items are returned from the editor
            Map<QName, Serializable> params = wf.getParams();
            for (QName qname : taskParams.keySet())
            {
               Serializable value = taskParams.get(qname);
               if (params.get(qname) == null || (value instanceof List == false))
               {
                  params.put(qname, value);
               }
               else
               {
                  List current = (List)params.get(qname);
                  if (qname.equals(WorkflowModel.ASSOC_ASSIGNEES)) 
                  { 
                     current.clear(); 
                  }
                  current.addAll((List)value);
               }
            }
            // remove any deleted associations
            Map<String, Map<String, AssociationRef>> assocs = this.workflowNode.getRemovedAssociations();
            for (String assocName : assocs.keySet())
            {
               QName assocQName = Repository.resolveToQName(assocName);
               
               // get the associations removed and create list of targets
               if (params.containsKey(assocQName))
               {
                  List current = (List)params.get(assocQName);
                  if (current != null)
                  {
                     Map<String, AssociationRef> removedAssocs = assocs.get(assocName);
                     for (AssociationRef assoc : removedAssocs.values())
                     {
                        current.remove(assoc.getTargetRef());
                     }
                  }
               }
            }
            wf.setParams(params);
         }
         wf.setType(this.workflowNode.getType());
         
         if (this.filenamePattern != null && this.filenamePattern.length() != 0)
         {
            // check the filename pattern compiles and display an error if a problem occurs
            try
            {
               Pattern.compile(this.filenamePattern);
            }
            catch (PatternSyntaxException pax)
            {
               throw new AlfrescoRuntimeException(
                  MessageFormat.format(
                        Application.getMessage(FacesContext.getCurrentInstance(), MSG_ERROR_FILENAME_PATTERN),
                        pax.getMessage()), pax);
            }
            wf.setFilenamePattern(this.filenamePattern);
         }
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
   public WorkflowConfiguration getActionWorkflow()
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
      return this.workflowNode;
   }
}
