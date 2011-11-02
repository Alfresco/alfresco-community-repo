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
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.alfresco.util.NameMatcher;
import org.alfresco.wcm.sandbox.SandboxFactory;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormInstanceData;
import org.alfresco.web.forms.FormNotFoundException;
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

    public static final String MSG_ERR_INVALID_LAUNCH_DATE = "msg_err_invalid_launch_date_on_submit";
    public static final String MSG_ERR_INVALID_EXPIRATION_DATE = "msg_err_invalid_expiration_date_on_submit";
    public static final String MSG_ERR_PATTERN_INVALID_EXPIRATION_DATE = "msg_err_pattern_invalid_expiration_date_on_submit";
   
   private String comment;
   private String label;
   private String[] workflowSelectedValue;
   private boolean enteringExpireDate = false;
   private boolean loadSelectedNodesFromBrowseBean = false;
   private boolean autoDeploy = false;
   private Date defaultExpireDate;
   private Date launchDate;

   private List<ItemWrapper> submitItems;
   private List<ItemWrapper> warningItems;
   private HashSet<FormWorkflowWrapper> workflows;
   private Map<String, Date> expirationDates;
   private List<UIListItem> workflowItems;
   private Map<QName, Serializable> workflowParams;
   
   protected AVMBrowseBean avmBrowseBean;
   
   transient private AVMService avmService;
   transient private WorkflowService workflowService;
   transient private AVMSyncService avmSyncService;
   transient private FormsService formsService;
   transient private SandboxFactory sandboxFactory;
   transient private SandboxService sandboxService;
   
   transient private NameMatcher nameMatcher;
  

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
    * @return nameMatcher
    */
   protected NameMatcher getNameMatcher()
   {
    //check for null for cluster environment
      if (nameMatcher == null)
      {
         nameMatcher = (NameMatcher) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "globalPathExcluder");
      }
      return nameMatcher;
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
   
   public void setSandboxService(final SandboxService sandboxService)
   {
      this.sandboxService = sandboxService;
   }
   
   protected SandboxService getSandboxService()
   {
      if (sandboxService == null)
      {
          sandboxService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getSandboxService();
      }
      return sandboxService;
   }

   // TODO - refactor ... push down into sandbox service (submit to workflow)
   public void setSandboxFactory(final SandboxFactory sandboxFactory)
   {
      this.sandboxFactory = sandboxFactory;
   }
   
   protected SandboxFactory getSandboxFactory()
   {
      if (sandboxFactory == null)
      {
          sandboxFactory = (SandboxFactory) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "SandboxFactory");
      }
      return sandboxFactory;
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
      this.autoDeploy = false;
      this.workflowParams = null;

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
            // note: always submit via workflow (always defaults to direct submit workflow)
            //outcome = submitViaWorkflow(context);
             
             String workflowName = null;
             if ((this.workflowSelectedValue != null) && (this.workflowSelectedValue.length > 0))
             {
                // get the defaults from the workflow configuration attached to the selected workflow
                workflowName = this.workflowSelectedValue[0];
                for (FormWorkflowWrapper wrapper : this.workflows)
                {
                   if (wrapper.name.equals(workflowName))
                   {
                      this.workflowParams = wrapper.params;
                   }
                }

                if (this.workflowParams == null)
                {
                   // create error msg for display in dialog - the user must configure the workflow params
                   Utils.addErrorMessage(Application.getMessage(context, MSG_ERR_WORKFLOW_CONFIG));
                   return outcome;
                }
             }
             
             final String finalWorkflowName = workflowName; 
             
             List<ItemWrapper> items = getSubmitItems();
             
             final List<String> relativePaths = new ArrayList<String>(items.size());
             
             for (ItemWrapper wrapper : items)
             {
                 relativePaths.add(AVMUtil.getStoreRelativePath(wrapper.getDescriptor().getPath()));
             }

             Date currentDate = new Date();
             if (launchDate !=null)
             {
                if (launchDate.before(currentDate))
                {
                   Utils.addErrorMessage(Application.getMessage(context, MSG_ERR_INVALID_LAUNCH_DATE));
                   return null;
                }
             }

             if ((submitItems != null) && (!submitItems.isEmpty()) && (expirationDates != null) && (!expirationDates.isEmpty()))
             {
                StringBuilder errorMessage = new StringBuilder();
                byte errFlag = 0;
                for (ItemWrapper wrapper : submitItems)
                {
                   String key = wrapper.descriptor.getPath();
                   Date expiritionDate = expirationDates.get(key);
                   if (expiritionDate != null)
                   {
                      if (launchDate != null)
                      {
                         if (expiritionDate.before(launchDate))
                         {
                            errFlag = 1;
                            errorMessage.append(wrapper.descriptor.getName()).append(", ");
                         }
                      }
                      else
                      {
                         if (expiritionDate.before(currentDate))
                         {
                            errorMessage.append(wrapper.descriptor.getName()).append(", ");
                            errFlag = 2;
                         }
                      }
                   }
                }
                if (errorMessage.length()>0)
                {
                   errorMessage.delete(errorMessage.length()-2, errorMessage.length());
                }
                switch (errFlag)
                {
                  case 1:
                    {
                       Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context, MSG_ERR_PATTERN_INVALID_EXPIRATION_DATE), errorMessage.toString()));
                       return null;
                    }
                  case 2:
                    {
                       Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context, MSG_ERR_INVALID_EXPIRATION_DATE), errorMessage.toString()));
                       return null;
                    }
                }
             }

             final String sbStoreId = this.avmBrowseBean.getSandbox();

             String submitLabel = this.label;
             String submitComment = this.comment;
             // crop to maximum length
             if (submitLabel != null && submitLabel.length() > 255)
             {
                 submitLabel = submitLabel.substring(0, 255);
             }
             if (submitComment != null && submitComment.length() > 255)
             {
                 submitComment = submitComment.substring(0, 255);
             }
             
             final String finalSubmitLabel = submitLabel;
             final String finalSubmitComment = submitComment;

             // note: always submit via workflow (if no workflow selected, then defaults to direct submit workflow)
             
             // This nees to run with higher rights to push into the work flow store ....
             
             AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {

                public Object doWork() throws Exception
                {
                    getSandboxService().submitListAssets(sbStoreId, relativePaths,
                            finalWorkflowName, workflowParams, 
                            finalSubmitLabel, finalSubmitComment, 
                            expirationDates, launchDate, autoDeploy);
                    return null;
                }
                 
             }, AuthenticationUtil.getSystemUserName());
            
             
             // if we get this far return the default outcome
             outcome = this.getDefaultFinishOutcome();
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
    * @return Flag to indicate whether the changes should be deployed upon approval
    */
   public boolean isAutoDeploy()
   {
      return this.autoDeploy;
   }

   /**
    * @param autoDeploy Flag to indicate whether the changes should be deployed upon approval
    */
   public void setAutoDeploy(boolean autoDeploy)
   {
      this.autoDeploy = autoDeploy;
   }

   /**
    * @return List of UIListItem object representing the available workflows for the website
    */
   @SuppressWarnings("unchecked")
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
            List<AVMDifference> diffs = this.getAvmSyncService().compare(-1, userStore, -1, stagingStore, getNameMatcher());
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
            
            for (AVMNodeDescriptor node : selected)
            {
               if (AVMWorkflowUtil.isInActiveWorkflow(AVMUtil.getStoreName(node.getPath()), node))
               {
                  this.warningItems.add(new ItemWrapper(node));
                  continue;
               }
               NodeRef ref = AVMNodeConverter.ToNodeRef(-1, node.getPath());
               if (submittedPaths.contains(node.getPath()))
               {
                  continue;
               }
               
               boolean isForm = getNodeService().hasAspect(ref, WCMAppModel.ASPECT_FORM_INSTANCE_DATA);
               boolean isRendition = getNodeService().hasAspect(ref, WCMAppModel.ASPECT_RENDITION);
               
               if (((!isForm) && (!isRendition)) || (node.isDeleted() && (!isForm)))
               {
                  // found single item for submit
                  // note: could be a single deleted rendition - to enable deletion of old renditions (eg. if template no longer applicable)
                  this.submitItems.add(new ItemWrapper(node));
                  submittedPaths.add(node.getPath());
               }
               else
               {
                  // item is a form (note: could be deleted) or a rendition
                  
                  FormInstanceData fid = null;
                  try
                  {
                      if (isRendition)
                      {
                         // found a generated rendition asset - locate the parent form instance data file
                         // and use this to find all generated assets that are appropriate
                         // NOTE: this path value is store relative
                         fid = getFormsService().getRendition(ref).getPrimaryFormInstanceData(true);
                      }
                      else
                      {
                         fid = getFormsService().getFormInstanceData(ref);
                      }
                  }
                  catch (FormNotFoundException fnfe)
                  {
                      logger.warn(fnfe);
                  }
                  
                  if (fid != null)
                  {
                      // add the form instance data file to the list for submission
                      if (!submittedPaths.contains(fid.getPath()))
                      {
                         this.submitItems.add(new ItemWrapper(getAvmService().lookup(-1, fid.getPath(), true)));
                         submittedPaths.add(fid.getPath());
                      }
                      
                      // locate renditions for this form instance data file and add to list for submission
                      for (final Rendition rendition : fid.getRenditions(true))
                      {
                         final String renditionPath = rendition.getPath();
                         if (!submittedPaths.contains(renditionPath))
                         {
                            this.submitItems.add(new ItemWrapper(getAvmService().lookup(-1, renditionPath, true)));
                            submittedPaths.add(renditionPath);
                         }
                      }
                      
                      // lookup the workflow defaults for that form and store into the list of available workflows
                      Form f = null;
                      try
                      {
                          f = fid.getForm();
                          WorkflowDefinition defaultWfDef = f.getDefaultWorkflow();
                          if (defaultWfDef != null)
                          {
                             this.workflows.add(new FormWorkflowWrapper(defaultWfDef.getName(),
                                      fid.getForm().getDefaultWorkflowParameters()));
                          }
                      }
                      catch (FormNotFoundException fnfe)
                      {
                          logger.warn(fnfe);
                      }
                  }
                  
                  // See WCM-1090 ACT-1551
                  // cannot depend on renditions of the form instance to contain the present
                  // node. Add it here if it hasn't been added by the above process.
                  if (!submittedPaths.contains(node.getPath()))
                  {
                     this.submitItems.add(new ItemWrapper(node));
                     submittedPaths.add(node.getPath());
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

      public boolean getFile()
      {
         return this.descriptor.isFile();
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
         return AVMUtil.getPreviewURI(descriptor.getPath());
      }

      public AVMNodeDescriptor getDescriptor()
      {
         return this.descriptor;
      }

      public String getIcon()
      {
         return (descriptor.isFile() || descriptor.isDeletedFile()
                 ? FileTypeImageUtils.getFileTypeImage(descriptor.getName(), true)
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