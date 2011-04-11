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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.wcm.util.WCMUtil;
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
   private static final long serialVersionUID = -236829535702107101L;
   
   protected boolean doResubmitNow = false;
   protected AVMBrowseBean avmBrowseBean;
   transient private AVMLockingService avmLockingService;
   
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
         logger.debug("Transitioning change request task: " + this.getWorkflowTask().id);
      
      final FacesContext context = FacesContext.getCurrentInstance();
      
      try
      {
         RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
         RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
         {
            public Object execute() throws Throwable
            {
                // get the current username and place in list
                String username = Application.getCurrentUser(context).getUserName();
                List<String> newLockOwners = new ArrayList<String>(1);
                newLockOwners.add(username);
                
                // prepare the edited parameters for saving
                Map<QName, Serializable> params = WorkflowUtil.prepareTaskParams(taskNode);
                
                // update the task with the updated parameters and resources
                getWorkflowService().updateTask(getWorkflowTask().id, params, null, null);
                
                // get the list of nodes that have expired (comparing workflow store to
                // the users main store)
                List<String> submitPaths = new ArrayList<String>();
                List<AVMNodeDescriptor> submitNodes = new ArrayList<AVMNodeDescriptor>();
                Pair<Integer, String> pkgPath = AVMNodeConverter.ToAVMVersionPath(workflowPackage);
                AVMNodeDescriptor pkgDesc = getAvmService().lookup(pkgPath.getFirst(), pkgPath.getSecond());
                String targetPath = pkgDesc.getIndirection();
                List<AVMDifference> diffs = getAvmSyncService().compare(pkgPath.getFirst(), 
                         pkgPath.getSecond(), -1, targetPath, null);
                
                // update the users main store with the changes from the workflow store
                getAvmSyncService().update(diffs, null, false, false, true, true, null, null);
                
                // move locks
                for (AVMDifference diff : diffs)
                {
                   // move the lock for this path from the user workflow sandbox to the users main store
                   // we need to do this because the workflow endTask will trigger deletion of the (user)
                   // workflow store which will in turn recursively remove all the locks.
                   String diffSourcePath = diff.getSourcePath();
                   String diffSourceAvmStore = WCMUtil.getWebProjectStoreIdFromPath(diffSourcePath);
                   String sourceWebProject = WCMUtil.getWebProjectStoreId(diffSourceAvmStore);
                   String diffTargetPath = diff.getDestinationPath();
                   String diffTargetAvmStore = WCMUtil.getWebProjectStoreIdFromPath(diffTargetPath);
                   String targetWebProject = WCMUtil.getWebProjectStoreId(diffSourceAvmStore);
                   if (!sourceWebProject.equals(targetWebProject))
                   {
                      throw new AlfrescoRuntimeException(
                            "The source web project does not match the target web project: \n" +
                            "   Source: " + diffSourcePath + "\n" +
                            "   Target: " + diffTargetPath);
                   }
                   
                   Map<String, String> lockAttributes = Collections.singletonMap(
                           WCMUtil.LOCK_KEY_STORE_NAME, diffTargetAvmStore);
                   boolean modified = getAvmLockingService().modifyLock(
                           sourceWebProject, AVMUtil.getStoreRelativePath(diffSourcePath), username,
                           sourceWebProject, AVMUtil.getStoreRelativePath(diffTargetPath), lockAttributes);
                   if (modified && logger.isDebugEnabled())
                   {
                      logger.debug(
                            "Moved lock: " + AVMUtil.getStoreId(diffSourcePath) + "-" +
                            AVMUtil.getStoreRelativePath(diffSourcePath) +
                            " to user: " + username);
                   }
                }
                
                // re-submit all the items now if requested
                if (doResubmitNow)
                {
                   for (AVMDifference diff : diffs)
                   {
                      String destPath = diff.getDestinationPath();
                      
                      AVMNodeDescriptor node = getAvmService().lookup(diff.getDestinationVersion(), 
                               destPath, true);
                      if (node != null)
                      {
                         submitNodes.add(node);
                         submitPaths.add(destPath);
                      }
                   }
                   
                   setupSubmitDialog(context, submitPaths, submitNodes);
                }
                
                // signal the default transition to the workflow task
                getWorkflowService().endTask(getWorkflowTask().id, null);
                
                return null;
            }
         };
         txnHelper.doInTransaction(callback);
         
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
    * @param avmLockingService The AVMLockingService instance
    */
   public void setAvmLockingService(AVMLockingService avmLockingService)
   {
      this.avmLockingService = avmLockingService;
   }
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   protected AVMLockingService getAvmLockingService()
   {
      if (this.avmLockingService == null)
      {
         this.avmLockingService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMLockingService();
      }
      return this.avmLockingService;
   }

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
      NodeRef userStoreNodeRef = (NodeRef)this.getNodeService().getProperty(
               this.workflowPackage, WCMModel.PROP_AVM_DIR_INDIRECTION);
      String userStoreAvmPath = AVMNodeConverter.ToAVMVersionPath(userStoreNodeRef).getSecond();
      String userStoreName = AVMUtil.getStoreName(userStoreAvmPath);
      NodeRef webProjectRef = getWebProjectService().getWebProjectNodeFromStore(WCMUtil.getWebProjectStoreId(userStoreName));
      
      // update the UI context to the web project
      this.browseBean.clickSpace(webProjectRef);
      this.avmBrowseBean.setupSandboxAction(userStoreName, 
               Application.getCurrentUser(context).getUserName());
      
      // setup the context for the submit dialog and initialise it
      this.avmBrowseBean.setNodesForSubmit(submitNodes);
      Map<String, String> dialogParams = new HashMap<String, String>(1);
      dialogParams.put(SubmitDialog.PARAM_LOAD_SELECTED_NODES_FROM_BROWSE_BEAN, 
                       Boolean.TRUE.toString());
      Application.getDialogManager().setupParameters(dialogParams);
   }
}
