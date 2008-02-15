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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.config.JNDIConstants;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.model.WCMWorkflowModel;
import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.NameMatcher;
import org.alfresco.util.VirtServerUtils;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.config.ClientConfigElement;
import org.alfresco.web.forms.FormInstanceData;
import org.alfresco.web.forms.FormsService;
import org.alfresco.web.forms.Rendition;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Submit items for WCM workflow dialog.
 *
 * @author Kevin Roast
 */
public class SubmitDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -2445905376358150000L;
   
   public static final String PARAM_LOAD_SELECTED_NODES_FROM_BROWSE_BEAN = "loadSelectedNodesFromBrowseBean";
   private static final String SPACE_ICON = "/images/icons/" + BrowseBean.SPACE_SMALL_DEFAULT + ".gif";
   private static final String MSG_DELETED_ITEM = "avm_node_deleted";
   private static final String MSG_ERR_WORKFLOW_CONFIG = "submit_workflow_config_error";

   private String comment;
   private String label;
   private String[] workflowSelectedValue;
   private boolean enteringExpireDate = false;
   private boolean loadSelectedNodesFromBrowseBean = false;
   private boolean validateLinks = true;
   private Date defaultExpireDate;
   private Date launchDate;

   private List<ItemWrapper> submitItems;
   private List<ItemWrapper> warningItems;
   private HashSet<FormWorkflowWrapper> workflows;
   private Map<String, Date> expirationDates;
   private List<UIListItem> workflowItems;
   private Map<QName, Serializable> workflowParams;
   private SandboxInfo sandboxInfo;
   private List<String> srcPaths;

   // The virtualization server might need to be notified
   // because one or more of the files submitted could alter
   // the behavior the virtual webapp in the target of the submit.
   // For example, the user might be submitting a new jar or web.xml file.
   //
   // This must take place after the transaction has been completed;
   // therefore, a variable is needed to store the path to the
   // updated webapp so it can happen in doPostCommitProcessing.
   private String virtUpdatePath;

   
   protected AVMBrowseBean avmBrowseBean;
   
   transient private AVMService avmService;
   transient private WorkflowService workflowService;
   transient private AVMSyncService avmSyncService;
   transient private AVMLockingService avmLockingService;
   transient private FormsService formsService;
   
   protected NameMatcher nameMatcher;
  

   /** Current workflow for dialog context */
   protected WorkflowConfiguration actionWorkflow = null;

   private static final Log logger = LogFactory.getLog(SubmitDialog.class);

   /**
    * @param avmService       The AVM Service to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }

   protected AVMService getAvmService()
   {
      if (avmService == null)
      {
         avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMService();
      }
      return avmService;
   }
   
   /**
    * @param avmSyncService   The AVMSyncService to set.
    */
   public void setAvmSyncService(AVMSyncService avmSyncService)
   {
      this.avmSyncService = avmSyncService;
   }

   protected AVMSyncService getAvmSyncService()
   {
      if (this.avmSyncService == null)
      {
         this.avmSyncService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMSyncService();
      }
      return this.avmSyncService;
   }

   /**
    * @param avmLockingService The AVMLockingService to set
    */
   public void setAvmLockingService(AVMLockingService avmLockingService)
   {
      this.avmLockingService = avmLockingService;
   }

   protected AVMLockingService getAvmLockingService()
   {
      if (this.avmLockingService == null)
      {
         this.avmLockingService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMLockingService();
      }
      return this.avmLockingService;
   }

   /**
    * @param avmBrowseBean    The AVM BrowseBean to set
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }

   /**
    * @param workflowService  The WorkflowService to set.
    */
   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }

   protected WorkflowService getWorkflowService()
   {
      if (workflowService == null)
      {
         workflowService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getWorkflowService();
      }
      return workflowService;
   }
   
   /**
    * @param nameMatcher The nameMatcher to set.
    */
   public void setNameMatcher(NameMatcher nameMatcher)
   {
      this.nameMatcher = nameMatcher;
   }

   /**
    * @param formsService    The FormsService to set.
    */
   public void setFormsService(final FormsService formsService)
   {
      this.formsService = formsService;
   }
   
   protected FormsService getFormsService()
   {
      if (formsService == null)
      {
         formsService = (FormsService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "FormsService");
      }
      return formsService;
   }


   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      this.comment = null;
      this.label = null;
      this.submitItems = null;
      this.warningItems = null;
      this.workflowItems = null;
      this.workflows = new HashSet<FormWorkflowWrapper>(4);
      this.expirationDates = new HashMap<String, Date>(8);
      this.defaultExpireDate = new Date();
      this.workflowSelectedValue = null;
      this.launchDate = null;
      this.validateLinks = true;
      this.workflowParams = null;
      this.sandboxInfo = null;
      this.virtUpdatePath = null;

      // determine if the dialog has been started from a workflow
      this.loadSelectedNodesFromBrowseBean = Boolean.valueOf(this.parameters.get(PARAM_LOAD_SELECTED_NODES_FROM_BROWSE_BEAN));
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // NOTE: This does not get called in this dialog as we have overridden finish()

      return null;
   }

   @Override
   public String finish()
   {
      // NOTE: We need to handle the transaction ourselves in this dialog as the store needs
      //       to be committed and the virtualisation server informed of the workflow
      //       sandbox BEFORE the workflow gets started. This is so that the link validation
      //       service can use the virtualisation server to produce the link report.
      //       The best solution would be for the link check node of the workflow to be
      //       asynchronous and to wait until the initiating transaction had committed
      //       before running but there's no support for this.
      //       We therefore need to use 2 transactions, one to create the workflow store
      //       (if necessary) and one to start the workflow

      if (getSubmitItemsSize() == 0)
      {
         return null;
      }

      FacesContext context = FacesContext.getCurrentInstance();
      String outcome = null;

      // check the isFinished flag to stop the finish button
      // being pressed multiple times
      if (this.isFinished == false)
      {
         this.isFinished = true;

         try
         {
            if (this.workflowSelectedValue != null)
            {
               // if there is a workflow set submit via that
               outcome = submitViaWorkflow(context);
            }
            else
            {
               // if there's no workflow submit changes directly to staging
               outcome = submitDirectToStaging(context);

               // force an update of the virt server if necessary
               if (this.virtUpdatePath != null)
               {
                  AVMUtil.updateVServerWebapp(this.virtUpdatePath, true);
               }
            }
         }
         finally
         {
            // reset the flag so we can re-attempt the operation
            isFinished = false;
         }
      }

      return outcome;
   }

   /**
    * Submits the selected items straight to the staging area i.e. when
    * there is no workflow configured for the web project.
    *
    * @param context Faces context
    * @return The outcome to use
    */
   protected String submitDirectToStaging(final FacesContext context)
   {
      String outcome = null;

      RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
      RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>()
      {
         public String execute() throws Throwable
         {
            // call the actual implementation
            return submitDirectToStagingImpl();
         }
      };

      try
      {
         // Execute
         outcome = txnHelper.doInTransaction(callback);
      }
      catch (Throwable e)
      {
         Utils.addErrorMessage(formatErrorMessage(e), e);
         outcome = getErrorOutcome(e);
      }

      return outcome;
   }

   /**
    * Submits the selected items via the configured workflow.
    * <p>
    * This method uses 2 separate transactions to perform the submit.
    * The first one creates the workflow sandbox. The virtualisation
    * server is then informed of the new stores. The second
    * transaction then starts the appropriate workflow. This approach
    * is needed to allow link validation to be performed on the
    * workflow sandbox.
    * </p>
    *
    * @param context Faces context
    * @return The outcome to use
    */
   protected String submitViaWorkflow(final FacesContext context)
   {
      String outcome = null;

      RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
      RetryingTransactionCallback<String> sandboxCallback = new RetryingTransactionCallback<String>()
      {
         public String execute() throws Throwable
         {
            // call the actual implementation
            createWorkflowSandbox(context);
            return null;
         }
      };

      RetryingTransactionCallback<String> workflowCallback = new RetryingTransactionCallback<String>()
      {
         public String execute() throws Throwable
         {
            // call the actual implementation
            startWorkflow();
            return null;
         }
      };

      try
      {
         // create the workflow sandbox firstly
         txnHelper.doInTransaction(sandboxCallback, false, true);

         if (this.sandboxInfo != null)
         {
            // inform the virtualisation server if the workflow sandbox was created
            if (this.virtUpdatePath != null)
            {
               AVMUtil.updateVServerWebapp(this.virtUpdatePath, true);
            }

            try
            {
               // start the workflow
               txnHelper.doInTransaction(workflowCallback, false, true);
            }
            catch (Throwable err)
            {
               cleanupWorkflowSandbox(context);
               throw err;
            }

            // if we get this far return the default outcome
            outcome = this.getDefaultFinishOutcome();
         }
      }
      catch (Throwable e)
      {
         Utils.addErrorMessage(formatErrorMessage(e), e);
         outcome = getErrorOutcome(e);
      }

      return outcome;
   }

   /**
    * Performs the actual submisson to staging
    *
    * @return The outcome to use
    */
   protected String submitDirectToStagingImpl()
   {
      // direct submit to the staging area without workflow
      List<ItemWrapper> items = getSubmitItems();

      // construct diffs for selected items for submission
      String sandboxPath = AVMUtil.buildSandboxRootPath(this.avmBrowseBean.getSandbox());
      String stagingPath = AVMUtil.buildSandboxRootPath(this.avmBrowseBean.getStagingStore());
      List<AVMDifference> diffs = new ArrayList<AVMDifference>(items.size());
      String storeId = this.avmBrowseBean.getWebProject().getStoreId();
      for (ItemWrapper wrapper : items)
      {
         String srcPath = sandboxPath + wrapper.getPath();
         String destPath = stagingPath + wrapper.getPath();
         AVMDifference diff = new AVMDifference(-1, srcPath, -1, destPath, AVMDifference.NEWER);
         diffs.add(diff);

         // process the expiration date (if any)
         processExpirationDate(srcPath);

         // recursively remove locks from this item
         recursivelyRemoveLocks(storeId, -1, getAvmService().lookup(-1, srcPath, true), srcPath);

         // If nothing has required notifying the virtualization server
         // so far, check to see if destPath forces a notification
         // (e.g.:  it might be a path to a jar file within WEB-INF/lib).
         if ( (this.virtUpdatePath == null) &&
               VirtServerUtils.requiresUpdateNotification( destPath ) )
         {
            this.virtUpdatePath = destPath;
         }
      }
      // write changes to layer so files are marked as modified
      this.getAvmSyncService().update(diffs, null, true, true, false, false, this.label, this.comment);
      AVMDAOs.Instance().fAVMNodeDAO.flush();
      getAvmSyncService().flatten(sandboxPath, stagingPath);
      // if we get this far return the default outcome
      return this.getDefaultFinishOutcome();
   }

   /**
    * Creates a workflow sandbox for all the submitted items
    *
    * @param context Faces context
    */
   protected void createWorkflowSandbox(FacesContext context)
   {
      // get the defaults from the workflow configuration attached to the selected workflow
      String workflowName = this.workflowSelectedValue[0];
      for (FormWorkflowWrapper wrapper : this.workflows)
      {
         if (wrapper.name.equals(workflowName))
         {
            this.workflowParams = wrapper.params;
         }
      }

      if (this.workflowParams != null)
      {
         // Create workflow sandbox for workflow package
         this.sandboxInfo = SandboxFactory.createWorkflowSandbox(
                  this.avmBrowseBean.getStagingStore());

         // create container for our avm workflow package
         final List<ItemWrapper> items = this.getSubmitItems();
         this.srcPaths = new ArrayList<String>(items.size());

         for (ItemWrapper wrapper : items)
         {
            // Example srcPath:
            //     mysite--alice:/www/avm_webapps/ROOT/foo.txt
            String srcPath = wrapper.getDescriptor().getPath();

            // We *always* want to update virtualization server
            // when a workflow sandbox is given data in the
            // context of a submit workflow.  Without this,
            // it would be impossible to see workflow data
            // in context.  The raw operation to create a
            // workflow sandbox does not notify the virtualization
            // server that it exists because it's useful to
            // defer this operation until everything is already
            // in place; this allows pointlessly fine-grained
            // notifications to be suppressed (they're expensive).
            //
            // Therefore, just derive the name of the webapp
            // in the workflow sandbox from the 1st item in
            // the submit list (even if it's not in WEB-INF),
            // and force the virt server notification after the
            // transaction has completed via doPostCommitProcessing.
            if (this.virtUpdatePath  == null)
            {
               // Example workflow main store name:
               //     mysite--workflow-9161f640-b020-11db-8015-130bf9b5b652
               String workflowMainStoreName = sandboxInfo.getMainStoreName();

               // The virtUpdatePath looks just like the srcPath
               // except that it belongs to a the main store of
               // the workflow sandbox instead of the sandbox
               // that originated the submit.
               this.virtUpdatePath =
                  workflowMainStoreName +
                  srcPath.substring(srcPath.indexOf(':'),srcPath.length());
            }

            // process the expiration date (if any)
            processExpirationDate(srcPath);

            this.srcPaths.add(srcPath);
         }

         String workflowMainStoreName = sandboxInfo.getMainStoreName();
         List<AVMDifference> diffs = new ArrayList<AVMDifference>(srcPaths.size());
         for (final String srcPath : srcPaths)
         {
            diffs.add(new AVMDifference(-1, srcPath, -1,
                     AVMUtil.getCorrespondingPath(srcPath, workflowMainStoreName),
                     AVMDifference.NEWER));
         }

         // write changes to layer so files are marked as modified
         getAvmSyncService().update(diffs, null, true, true, false, false, null, null);
      }
      else
      {
         // create error msg for display in dialog - the user must configure the workflow params
         Utils.addErrorMessage(Application.getMessage(context, MSG_ERR_WORKFLOW_CONFIG));
      }
   }

   /**
    * Starts the configured workflow to allow the submitted items to be link
    * checked and reviewed.
    */
   protected void startWorkflow()
   {
      if (this.workflowParams != null)
      {
         // start the workflow to get access to the start task
         String workflowName = this.workflowSelectedValue[0];
         WorkflowDefinition wfDef = getWorkflowService().getDefinitionByName(workflowName);
         WorkflowPath path = getWorkflowService().startWorkflow(wfDef.id, null);
         if (path != null)
         {
            // extract the start task
            List<WorkflowTask> tasks = getWorkflowService().getTasksForWorkflowPath(path.id);
            if (tasks.size() == 1)
            {
               WorkflowTask startTask = tasks.get(0);

               if (startTask.state == WorkflowTaskState.IN_PROGRESS)
               {
                  final NodeRef workflowPackage =
                     AVMWorkflowUtil.createWorkflowPackage(this.srcPaths, sandboxInfo, path);

                  this.workflowParams.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);

                  // add submission parameters
                  this.workflowParams.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, getComment());
                  this.workflowParams.put(WCMWorkflowModel.PROP_LABEL, getLabel());
                  this.workflowParams.put(WCMWorkflowModel.PROP_FROM_PATH,
                           AVMUtil.buildStoreRootPath(this.avmBrowseBean.getSandbox()));
                  this.workflowParams.put(WCMWorkflowModel.PROP_LAUNCH_DATE, this.launchDate);
                  this.workflowParams.put(WCMWorkflowModel.PROP_VALIDATE_LINKS,
                           new Boolean(this.validateLinks));
                  this.workflowParams.put(WCMWorkflowModel.PROP_WEBAPP,
                           this.avmBrowseBean.getWebapp());
                  this.workflowParams.put(WCMWorkflowModel.ASSOC_WEBPROJECT,
                           this.avmBrowseBean.getWebsite().getNodeRef());

                  // update start task with submit parameters
                  getWorkflowService().updateTask(startTask.id, this.workflowParams, null, null);

                  // end the start task to trigger the first 'proper' task in the workflow
                  getWorkflowService().endTask(startTask.id, null);
               }
            }
         }
      }
   }

   /**
    * Cleans up the workflow sandbox created by the first transaction. This
    * action is itself preformed in a separate transaction.
    *
    * @param context Faces context
    */
   protected void cleanupWorkflowSandbox(FacesContext context)
   {
      RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
      RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>()
      {
         public String execute() throws Throwable
         {
            // call the actual implementation
            cleanupWorkflowSandboxImpl();
            return null;
         }
      };

      try
      {
         // Execute the cleanup handler
         txnHelper.doInTransaction(callback);
      }
      catch (Throwable e)
      {
         // not much we can do now, just log the error to inform admins
         logger.error("Failed to cleanup workflow sandbox after workflow failure", e);
      }
   }

   /**
    * Performs the actual deletion of stores in the workflow sandbox.
    */
   protected void cleanupWorkflowSandboxImpl()
   {
      if (this.sandboxInfo != null)
      {
         String mainWorkflowStore = this.sandboxInfo.getMainStoreName();
         Map<QName, PropertyValue> matches = getAvmService().queryStorePropertyKey(mainWorkflowStore, 
                  QName.createQName(null, ".sandbox-id%"));
         QName sandboxID = matches.keySet().iterator().next();
         // Get all the stores in the sandbox.
         Map<String, Map<QName, PropertyValue>> stores = getAvmService().queryStoresPropertyKeys(sandboxID);
         for (String storeName : stores.keySet())
         {
            getAvmService().purgeStore(storeName);
         }
      }
   }

   /**
    * Recursively remove locks from a path. Walking child folders looking for files
    * to remove locks from.
    */
   private void recursivelyRemoveLocks(String webProject, int version, AVMNodeDescriptor desc, String path)
   {
      if (desc.isFile() || desc.isDeletedFile())
      {
         this.getAvmLockingService().removeLock(webProject, path.substring(path.indexOf(":") + 1));
      }
      else
      {
         if (desc.isDeletedDirectory())
         {
            // lookup the previous child and get its contents
            final List<AVMNodeDescriptor> history = getAvmService().getHistory(desc, 2);
            if (history.size() == 1)
            {
               return;
            }
            desc = history.get(1);
         }

         Map<String, AVMNodeDescriptor> list = getAvmService().getDirectoryListingDirect(desc, true);
         for (Map.Entry<String, AVMNodeDescriptor> child : list.entrySet())
         {
            String name = child.getKey();
            AVMNodeDescriptor childDesc = child.getValue();
            recursivelyRemoveLocks(webProject, version, childDesc, path + "/" + name);
         }
      }
   }

   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonDisabled()
    */
   @Override
   public boolean getFinishButtonDisabled()
   {
      return (getSubmitItemsSize() == 0);
   }

   /**
    * @return Returns the workflow comment.
    */
   public String getComment()
   {
      return this.comment;
   }

   /**
    * @param comment    The workflow comment to set.
    */
   public void setComment(String comment)
   {
      this.comment = comment;
   }

   /**
    * @return Returns the snapshot label.
    */
   public String getLabel()
   {
      return this.label;
   }

   /**
    * @param label      The snapshot label to set.
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   /**
    * @return The default expiration date
    */
   public Date getDefaultExpireDate()
   {
      return this.defaultExpireDate;
   }

   /**
    * @param defaultExpireDate The default expiration date
    */
   public void setDefaultExpireDate(Date defaultExpireDate)
   {
      this.defaultExpireDate = defaultExpireDate;
   }

   /**
    * @return true if a default expiration date is being entered
    */
   public boolean isEnteringExpireDate()
   {
      return this.enteringExpireDate;
   }

   /**
    * @return Map of expiration dates for the modified items
    */
   public Map<String, Date> getExpiredDates()
   {
      return this.expirationDates;
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

   /**
    * @return Returns the content launch date
    */
   public Date getLaunchDate()
   {
      return this.launchDate;
   }

   /**
    * @param launchDate The content launch date
    */
   public void setLaunchDate(Date launchDate)
   {
      this.launchDate = launchDate;
   }

   /**
    * @return Flag to indicate whether links should be validated
    */
   public boolean isValidateLinks()
   {
      return this.validateLinks;
   }

   /**
    * @param validateLinks Flag to indicate whether links should be validated
    */
   public void setValidateLinks(boolean validateLinks)
   {
      this.validateLinks = validateLinks;
   }

   /**
    * @return List of UIListItem object representing the available workflows for the website
    */
   public List<UIListItem> getWorkflowList()
   {
      if (this.workflowItems == null)
      {
         // ensure all workflows have been collected from any form generated assets
         calcluateListItemsAndWorkflows();

         // add the list of workflows for the website itself to the set
         NodeRef websiteRef = this.avmBrowseBean.getWebsite().getNodeRef();
         List<ChildAssociationRef> webWorkflowRefs = getNodeService().getChildAssocs(
               websiteRef, WCMAppModel.ASSOC_WEBWORKFLOWDEFAULTS, RegexQNamePattern.MATCH_ALL);
         List<FormWorkflowWrapper> workflowMatchers = new ArrayList<FormWorkflowWrapper>(webWorkflowRefs.size());
         for (ChildAssociationRef ref : webWorkflowRefs)
         {
            NodeRef wfDefaultsRef = ref.getChildRef();
            String wfName = (String)getNodeService().getProperty(wfDefaultsRef, WCMAppModel.PROP_WORKFLOW_NAME);
            Map<QName, Serializable> params = (Map<QName, Serializable>)AVMWorkflowUtil.deserializeWorkflowParams(
                  wfDefaultsRef);
            String matchPattern = (String)getNodeService().getProperty(
                  wfDefaultsRef, WCMAppModel.PROP_FILENAMEPATTERN);
            if (matchPattern != null)
            {
               // add to temp list with the file name pattern
               workflowMatchers.add(new FormWorkflowWrapper(wfName, params, matchPattern));
            }
         }

         // perform match on each submitted file against available workflows
         for (ItemWrapper wrapper : this.submitItems)
         {
            String path = wrapper.getPath();
            // shallow copy the list of matchers so we can remove items while looping
            List<FormWorkflowWrapper> matchers = new ArrayList<FormWorkflowWrapper>(workflowMatchers);
            for (int i=0; i<matchers.size(); i++)
            {
               FormWorkflowWrapper matcher = matchers.get(i);
               // see if the file path matches this workflow path pattern
               if (matcher.matchesPath(path) == true)
               {
                  // found a match - remove the workflow from the list of ones to check
                  this.workflows.add(matcher);
                  workflowMatchers.remove(matcher);
               }
            }
            // if all workflows are matched, there is no need to continue looping
            if (workflowMatchers.size() == 0) break;
         }

         // build a UI item for each available workflow
         List<UIListItem> items = new ArrayList<UIListItem>(this.workflows.size());
         for (FormWorkflowWrapper wrapper : this.workflows)
         {
            WorkflowDefinition workflowDef = getWorkflowService().getDefinitionByName(wrapper.name);
            UIListItem item = new UIListItem();
            item.setValue(workflowDef.getName());
            String label = workflowDef.getTitle();
            String desc = workflowDef.getDescription();
            if (desc != null && desc.length() > 0)
            {
               label = label + "(" + desc + ")";
            }
            item.setLabel(label);
            items.add(item);

            // add first workflow as default selection
            if (workflowSelectedValue == null)
            {
               workflowSelectedValue = new String[]{ workflowDef.getName() };
            }
         }
         this.workflowItems = items;
      }

      return this.workflowItems;
   }

   /**
    * @return size of the workflow selection list
    */
   public int getWorkflowListSize()
   {
      return getWorkflowList().size();
   }

   /**
    * @return the List of bean items to show in the Submit list
    */
   public List<ItemWrapper> getSubmitItems()
   {
      if (this.submitItems == null)
      {
         // this method builds all submit and warning item data structures
         calcluateListItemsAndWorkflows();
      }
      return this.submitItems;
   }

   /**
    * @return size of the submit list
    */
   public int getSubmitItemsSize()
   {
      return getSubmitItems().size();
   }

   /**
    * @return the List of bean items to show in the Warning list
    */
   public List<ItemWrapper> getWarningItems()
   {
      if (this.warningItems == null)
      {
         // this method builds all submit and warning item data structures
         calcluateListItemsAndWorkflows();
      }
      return this.warningItems;
   }

   /**
    * @return size of the warning list
    */
   public int getWarningItemsSize()
   {
      return this.getWarningItems().size();
   }

   /**
    * Calculate the lists of Submittable Items, Warning items and the list of available workflows.
    */
   private void calcluateListItemsAndWorkflows()
   {
      UserTransaction tx = null;

      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, true);
         tx.begin();

         List<AVMNodeDescriptor> selected;
         if (this.loadSelectedNodesFromBrowseBean)
         {
            // if the dialog was started from a workflow the AVM browse bean should
            // have the list of nodes that need submitting
            selected = this.avmBrowseBean.getNodesForSubmit();
            this.avmBrowseBean.setNodesForSubmit(null);
         }
         // if the dialog was started from the UI determine what nodes the user selected to submit
         else if (this.avmBrowseBean.getAllItemsAction())
         {
            String webapp = this.avmBrowseBean.getWebapp();
            String userStore = AVMUtil.buildStoreWebappPath(this.avmBrowseBean.getSandbox(), webapp);
            String stagingStore = AVMUtil.buildStoreWebappPath(this.avmBrowseBean.getStagingStore(), webapp);
            List<AVMDifference> diffs = this.getAvmSyncService().compare(-1, userStore, -1, stagingStore, nameMatcher);
            selected = new ArrayList<AVMNodeDescriptor>(diffs.size());
            for (AVMDifference diff : diffs)
            {
               selected.add(getAvmService().lookup(-1, diff.getSourcePath(), true));
            }
         }
         else if (this.avmBrowseBean.getAvmActionNode() == null)
         {
            // multiple items selected
            selected = this.avmBrowseBean.getSelectedSandboxItems();
         }
         else
         {
            // single item selected
            selected = new ArrayList<AVMNodeDescriptor>(1);
            selected.add(getAvmService().lookup(-1, this.avmBrowseBean.getAvmActionNode().getPath(), true));
         }

         if (selected == null)
         {
            this.submitItems = Collections.<ItemWrapper>emptyList();
            this.warningItems = Collections.<ItemWrapper>emptyList();
         }
         else
         {
            Set<String> submittedPaths = new HashSet<String>(selected.size());
            this.submitItems = new ArrayList<ItemWrapper>(selected.size());
            this.warningItems = new ArrayList<ItemWrapper>(selected.size() >> 1);
            List<WorkflowTask> tasks = null;
            for (AVMNodeDescriptor node : selected)
            {
               if (tasks == null)
               {
                  tasks = AVMWorkflowUtil.getAssociatedTasksForSandbox(AVMUtil.getStoreName(node.getPath()));
               }
               if (AVMWorkflowUtil.getAssociatedTasksForNode(node, tasks).size() != 0)
               {
                  this.warningItems.add(new ItemWrapper(node));
                  continue;
               }
               NodeRef ref = AVMNodeConverter.ToNodeRef(-1, node.getPath());
               if (submittedPaths.contains(node.getPath()))
               {
                  continue;
               }
               if (node.isDeleted())
               {
                  // found a deleted node for submit
                  this.submitItems.add(new ItemWrapper(node));
                  submittedPaths.add(node.getPath());
               }
               // lookup if this item was created via a form - then lookup the workflow defaults
               // for that form and store into the list of available workflows
               else if (!getNodeService().hasAspect(ref, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
               {
                  this.submitItems.add(new ItemWrapper(node));
                  submittedPaths.add(node.getPath());
               }
               else
               {
                  FormInstanceData fid = null;
                  // check if this is a rendition - as they also have the forminstancedata aspect
                  if (getNodeService().hasAspect(ref, WCMAppModel.ASPECT_RENDITION))
                  {
                     // found a generated rendition asset - locate the parent form instance data file
                     // and use this to find all generated assets that are appropriate
                     // NOTE: this path value is store relative
                     fid = getFormsService().getRendition(ref).getPrimaryFormInstanceData();
                  }
                  else
                  {
                     fid = getFormsService().getFormInstanceData(ref);
                  }

                  // add the form instance data file to the list for submission
                  if (!submittedPaths.contains(fid.getPath()))
                  {
                     this.submitItems.add(new ItemWrapper(getAvmService().lookup(-1, fid.getPath())));
                     submittedPaths.add(fid.getPath());
                  }

                  // locate renditions for this form instance data file and add to list for submission
                  for (final Rendition rendition : fid.getRenditions())
                  {
                     final String renditionPath = rendition.getPath();
                     if (!submittedPaths.contains(renditionPath))
                     {
                        this.submitItems.add(new ItemWrapper(getAvmService().lookup(-1, renditionPath)));
                        submittedPaths.add(renditionPath);
                     }
                  }
                  WorkflowDefinition defaultWfDef = fid.getForm().getDefaultWorkflow();
                  if (defaultWfDef != null)
                  {
                     this.workflows.add(new FormWorkflowWrapper(defaultWfDef.getName(),
                              fid.getForm().getDefaultWorkflowParameters()));
                  }
               }
            }
         }

         tx.commit();
      }
      catch (Throwable e)
      {
         // rollback the transaction on error
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}

         // rethrow the exception to highlight the problem
         throw (RuntimeException)e;
      }
   }

   /**
    * Sets up the expiration date for the given source path
    *
    * @param srcPath The path to set the expiration date for
    */
   private void processExpirationDate(String srcPath)
   {
      // if an expiration date has been set for this item we need to
      // add the expires aspect and the date supplied
      Date expirationDate = this.expirationDates.get(srcPath);
      if (expirationDate == null)
      {
         return;
      }
      // make sure the aspect is present
      if (getAvmService().hasAspect(-1, srcPath, WCMAppModel.ASPECT_EXPIRES) == false)
      {
         getAvmService().addAspect(srcPath, WCMAppModel.ASPECT_EXPIRES);
      }

      // set the expiration date
      getAvmService().setNodeProperty(srcPath, WCMAppModel.PROP_EXPIRATIONDATE, 
                                      new PropertyValue(DataTypeDefinition.DATETIME, expirationDate));

      if (logger.isDebugEnabled())
         logger.debug("Set expiration date of " + expirationDate +
                      " for " + srcPath);
   }

   /**
    * Action method to setup a workflow for dialog context for the current row
    */
   public void setupConfigureWorkflow(ActionEvent event)
   {
      if (this.workflowSelectedValue != null)
      {
         String workflowName = this.workflowSelectedValue[0];
         for (WorkflowConfiguration wrapper : this.workflows)
         {
            if (wrapper.getName().equals(workflowName))
            {
               setActionWorkflow(wrapper);
            }
         }
      }
   }

   /**
    * @return Returns the action Workflow for dialog context
    */
   public WorkflowConfiguration getActionWorkflow()
   {
      return this.actionWorkflow;
   }

   /**
    * @param actionWorkflow   The action Workflow to set for dialog context
    */
   public void setActionWorkflow(WorkflowConfiguration actionWorkflow)
   {
      this.actionWorkflow = actionWorkflow;
   }

   /**
    * Applies the entered default date to all modified items
    *
    * @param event The event
    */
   public void applyDefaultExpireDateToAll(ActionEvent event)
   {
      if (logger.isDebugEnabled())
         logger.debug("applying default expiration date of " + this.defaultExpireDate + " to all modified items");

      List<ItemWrapper> items = this.getSubmitItems();
      for (ItemWrapper item : items)
      {
         if (item.descriptor.getType() == 0)
         {
            this.expirationDates.put(item.descriptor.getPath(), this.defaultExpireDate);
         }
      }

      this.enteringExpireDate = false;
   }

   /**
    * Toggles the enteringExpireDate flag
    *
    * @param event The event
    */
   public void enterExpireDate(ActionEvent event)
   {
      this.enteringExpireDate = true;
   }

   /**
    * Simple structure class to wrap form workflow name and default parameter values
    */
   private static class FormWorkflowWrapper implements WorkflowConfiguration
   {
      private static final long serialVersionUID = -6264439015998987731L;
      
      private String name;
      private Map<QName, Serializable> params;
      private QName type;
      private String strFilenamePattern;
      private Pattern filenamePattern;

      FormWorkflowWrapper(String name, Map<QName, Serializable> params)
      {
         this.name = name;
         this.params = params;
      }

      FormWorkflowWrapper(String name, Map<QName, Serializable> params, String filenamePattern)
      {
         this.name = name;
         this.params = params;
         setFilenamePattern(filenamePattern);
      }

      public String getName()
      {
         return this.name;
      }

      public String getFilenamePattern()
      {
         return this.strFilenamePattern;
      }

      public void setFilenamePattern(String pattern)
      {
         if (pattern != null)
         {
            this.strFilenamePattern = pattern;
            this.filenamePattern = Pattern.compile(pattern);
         }
      }

      public Map<QName, Serializable> getParams()
      {
         return this.params;
      }

      public void setParams(Map<QName, Serializable> params)
      {
         this.params = params;
      }

      public QName getType()
      {
         return this.type;
      }

      public void setType(QName type)
      {
         this.type = type;
      }

      boolean matchesPath(String path)
      {
         return (filenamePattern != null &&
                 filenamePattern.matcher(path).matches());
      }

      @Override
      public int hashCode()
      {
         return this.name.hashCode();
      }

      @Override
      public boolean equals(Object obj)
      {
         return (obj instanceof FormWorkflowWrapper &&
                 this.name.equals(((FormWorkflowWrapper)obj).name));
      }
   }

   /**
    * Wrapper class to provide UI RichList component getters for an AVM node descriptor
    */
   public class ItemWrapper implements Serializable
   {
      private static final long serialVersionUID = 6079164681664703986L;
      
      private static final String rootPath = '/' + JNDIConstants.DIR_DEFAULT_APPBASE;
      private AVMNodeDescriptor descriptor;

      public ItemWrapper(AVMNodeDescriptor descriptor)
      {
         this.descriptor = descriptor;
      }

      public boolean getExpirable()
      {
         return this.descriptor.isFile() && (this.descriptor.isDeleted() == false);
      }

      public boolean getDeleted()
      {
         return descriptor.isDeleted();
      }

      public String getName()
      {
         String result = descriptor.getName();
         if (descriptor.isDeleted())
         {
            result +=  " [" + Application.getMessage(FacesContext.getCurrentInstance(), MSG_DELETED_ITEM) + "]";
         }
         return result;
      }

      public String getModifiedDate()
      {
         return ISO8601DateFormat.format(new Date(descriptor.getModDate()));
      }

      public String getExpirationDate()
      {
         String expireDate = null;

         Date date = expirationDates.get(this.descriptor.getPath());
         if (date != null)
         {
            expireDate = ISO8601DateFormat.format(date);
         }

         return expireDate;
      }

      public String getDescription()
      {
         if (descriptor.isDeleted() == false)
         {
            return (String)getNodeService().getProperty(
                  AVMNodeConverter.ToNodeRef(-1, descriptor.getPath()), ContentModel.PROP_DESCRIPTION);
         }
         else
         {
            return "";
         }
      }

      public String getPath()
      {
         return descriptor.getPath().substring(descriptor.getPath().indexOf(rootPath) + rootPath.length());
      }

      public String getFullPath()
      {
         return descriptor.getPath();
      }

      public String getUrl()
      {
         return DownloadContentServlet.generateBrowserURL(
               AVMNodeConverter.ToNodeRef(-1, descriptor.getPath()), descriptor.getName());
      }

      public String getPreviewUrl()
      {
         ClientConfigElement config =  Application.getClientConfig(FacesContext.getCurrentInstance());
         String dns = AVMUtil.lookupStoreDNS(AVMUtil.getStoreName(descriptor.getPath()));
         return AVMUtil.buildAssetUrl(AVMUtil.getSandboxRelativePath(descriptor.getPath()),
                                           config.getWCMDomain(),
                                           config.getWCMPort(),
                                           dns);
      }

      public AVMNodeDescriptor getDescriptor()
      {
         return this.descriptor;
      }

      public String getIcon()
      {
         return (descriptor.isFile() || descriptor.isDeletedFile()
                 ? Utils.getFileTypeImage(descriptor.getName(), true)
                 : SPACE_ICON);
      }

      @Override
      public boolean equals(Object obj)
      {
         return (obj instanceof ItemWrapper &&
                 ((ItemWrapper)obj).descriptor.getPath().equals(descriptor.getPath()));
      }

      @Override
      public int hashCode()
      {
         return descriptor.getPath().hashCode();
      }
   }
}
