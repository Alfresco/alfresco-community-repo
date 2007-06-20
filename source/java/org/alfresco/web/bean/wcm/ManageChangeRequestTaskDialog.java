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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.wf.AVMSubmittedAspect;
import org.alfresco.sandbox.SandboxConstants;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.workflow.ManageTaskDialog;
import org.alfresco.web.bean.workflow.WorkflowUtil;
import org.alfresco.web.config.DialogsConfigElement.DialogButtonConfig;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Manage Task" dialog when dealing
 * with the "Change Request" task specifically.
 * 
 * @author gavinc
 */
public class ManageChangeRequestTaskDialog extends ManageTaskDialog
{
   protected boolean doResubmitNow = false;
   protected AVMBrowseBean avmBrowseBean;
   protected AVMSubmittedAspect avmSubmittedAspect;
   
   private final static Log logger = LogFactory.getLog(ManageChangeRequestTaskDialog.class);
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // reset the doResubmit flag
      this.doResubmitNow = false;
   }
   
   @Override
   public List<DialogButtonConfig> getAdditionalButtons()
   {
      List<DialogButtonConfig> buttons = super.getAdditionalButtons();
      
      buttons.add(new DialogButtonConfig(ID_PREFIX + "resubmit", null, "task_done_resubmit_all",
                         "#{DialogManager.bean.transitionAndResubmit}", "false", null));
      
      return buttons;
   }

   // ------------------------------------------------------------------------------
   // Event handlers
   
   @Override
   public String transition()
   {
      String outcome = getDefaultFinishOutcome();
      
      if (logger.isDebugEnabled())
         logger.debug("Transitioning change request task: " + this.task.id);
      
      FacesContext context = FacesContext.getCurrentInstance();
      UserTransaction tx = null;
  
      try
      {
         tx = Repository.getUserTransaction(context);
         tx.begin();
        
         // prepare the edited parameters for saving
         Map<QName, Serializable> params = WorkflowUtil.prepareTaskParams(this.taskNode);
         
         // update the task with the updated parameters and resources
         this.workflowService.updateTask(this.task.id, params, null, null);
         
         // get the list of nodes that have expired (comparing workflow store to
         // the users main store)
         List<String> submitPaths = new ArrayList<String>();
         List<AVMNodeDescriptor> submitNodes = new ArrayList<AVMNodeDescriptor>();
         Pair<Integer, String> pkgPath = AVMNodeConverter.ToAVMVersionPath(this.workflowPackage);
         AVMNodeDescriptor pkgDesc = this.avmService.lookup(pkgPath.getFirst(), pkgPath.getSecond());
         String targetPath = pkgDesc.getIndirection();
         List<AVMDifference> diffs = this.avmSyncService.compare(pkgPath.getFirst(), 
                  pkgPath.getSecond(), -1, targetPath, null);
         for (AVMDifference diff : diffs)
         {
            AVMNodeDescriptor node = this.avmService.lookup(diff.getDestinationVersion(), 
                     diff.getDestinationPath());
            if (node != null)
            {
               submitNodes.add(node);
               submitPaths.add(diff.getDestinationPath());
            }
         }
         
         // update the users main store with the changes from the workflow store
         this.avmSyncService.update(diffs, null, false, false, true, true, null, null);
         
         // re-submit all the items now if requested
         if (this.doResubmitNow)
         {
            setupSubmitDialog(context, submitPaths, submitNodes);
         }
         
         // signal the default transition to the workflow task
         this.workflowService.endTask(this.task.id, null);
         
         // commit the changes
         tx.commit();
         
         // if we get this far close the task dialog
         if (this.doResubmitNow)
         {
            // open the submit dialog if re-submitting
            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + 
                      AlfrescoNavigationHandler.OUTCOME_SEPARATOR + 
                      AlfrescoNavigationHandler.DIALOG_PREFIX + "submitSandboxItems";
         }
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(formatErrorMessage(e), e);
         outcome = this.getErrorOutcome(e);
      }
      
      return outcome;
   }
   
   /**
    * Event handler for the 'Task Done & Re-Submit All' button
    */
   public String transitionAndResubmit()
   {
      this.doResubmitNow = true;
      return this.transition();
   }
   
   // ------------------------------------------------------------------------------
   // Setters
   
   /**
    * @param avmBrowseBean AVMBrowseBean instance
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }

   /**
    * Sets the avm submitted aspect service to use
    * 
    * @param avmSubmittedAspect AVMSubmittedAspect instance
    */
   public void setAvmSubmittedAspect(AVMSubmittedAspect avmSubmittedAspect)
   {
      this.avmSubmittedAspect = avmSubmittedAspect;
   }
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   /**
    * Submits all the expired items immediately after the task is completed
    * by launching the submit dialog with the expired items set as the modified
    * items
    * 
    * @param context Faces context
    * @param submitPaths The list of paths being submitted
    * @param submitNodes Node desriptor for each node being submitted
    */
   protected void setupSubmitDialog(FacesContext context, List<String> submitPaths, 
            List<AVMNodeDescriptor> submitNodes)
   {
      // start the submission dialog with the list of paths to submit
      if (logger.isDebugEnabled())
         logger.debug("starting submit dialog with expired paths: " + submitPaths);

      // get hold of the node ref that represents the web project the expired items
      // belong to and get the name of the users main store
      NodeRef userStoreNodeRef = (NodeRef)this.nodeService.getProperty(
               this.workflowPackage, WCMModel.PROP_AVM_DIR_INDIRECTION);
      String userStoreAvmPath = AVMNodeConverter.ToAVMVersionPath(userStoreNodeRef).getSecond();
      String userStoreName = AVMUtil.getStoreName(userStoreAvmPath);
      String stagingStoreName = this.avmService.getStoreProperty(userStoreName, 
               SandboxConstants.PROP_WEBSITE_NAME).getStringValue();
      NodeRef webProjectRef = AVMUtil.getWebProjectNodeFromStore(stagingStoreName);
      
      // update the UI context to the web project
      this.browseBean.clickSpace(webProjectRef);
      this.avmBrowseBean.setupSandboxAction(userStoreName, 
               Application.getCurrentUser(context).getUserName());
      
      // setup the context for the submit dialog and initialise it
      this.avmBrowseBean.setExpiredNodes(submitNodes);
      Map<String, String> dialogParams = new HashMap<String, String>(1);
      dialogParams.put(SubmitDialog.PARAM_STARTED_FROM_WORKFLOW, Boolean.TRUE.toString());
      Application.getDialogManager().setupParameters(dialogParams);
   }
}
