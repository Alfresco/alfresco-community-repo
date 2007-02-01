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

import org.alfresco.config.JNDIConstants;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.wf.AVMSubmittedAspect;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
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
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.forms.FormInstanceData;
import org.alfresco.web.forms.FormInstanceDataImpl;
import org.alfresco.web.forms.Rendition;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.wcm.WebResources;
import org.alfresco.util.VirtServerUtils;



/**
 * Submit items for WCM workflow dialog.
 * 
 * @author Kevin Roast
 */
public class SubmitDialog extends BaseDialogBean
{
   private static final String SPACE_ICON = "/images/icons/" + BrowseBean.SPACE_SMALL_DEFAULT + ".gif";
   private static final String MSG_DELETED_ITEM = "avm_node_deleted";
   private static final String MSG_ERR_WORKFLOW_CONFIG = "submit_workflow_config_error";
   
   private String comment;
   private String label;
   private String[] workflowSelectedValue;
   
   private List<ItemWrapper> submitItems;
   private List<ItemWrapper> warningItems;
   private HashSet<FormWorkflowWrapper> workflows;
   private Map<String, FormWorkflowWrapper> formWorkflowMap;
   private List<UIListItem> workflowItems;


   // The virtualization server might need to be notified 
   // because one or more of the files submitted could alter 
   // the behavior the virtual webapp in the target of the submit.
   // For example, the user might be submitting a new jar or web.xml file. 
   //
   // This must take place after the transaction has been completed;
   // therefore, a variable is needed to store the path to the 
   // updated webapp so it can happen in doPostCommitProcessing.
   //
   private String virtUpdatePath;     
   
   protected AVMService avmService;
   protected AVMSubmittedAspect avmSubmittedAspect;
   protected AVMBrowseBean avmBrowseBean;
   protected WorkflowService workflowService;
   protected AVMSyncService avmSyncService;
   protected NameMatcher nameMatcher;
   
   /** Current workflow for dialog context */
   protected WorkflowConfiguration actionWorkflow = null;
   
   /**
    * @param avmService       The AVM Service to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   /**
    * @param avmSubmittedAspect  The AVM Submitted Aspect to set.
    */
   public void setAvmSubmittedAspect(AVMSubmittedAspect avmSubmittedAspect)
   {
      this.avmSubmittedAspect = avmSubmittedAspect;
   }

   /**
    * @param avmSyncService   The AVMSyncService to set.
    */
   public void setAvmSyncService(AVMSyncService avmSyncService)
   {
      this.avmSyncService = avmSyncService;
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
   
   /**
    * @param nameMatcher The nameMatcher to set.
    */
   public void setNameMatcher(NameMatcher nameMatcher)
   {
      this.nameMatcher = nameMatcher;
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
      this.workflowSelectedValue = null;
      
      // walk all the web forms attached the website, and lookup the workflow defaults for each
      NodeRef websiteRef = this.avmBrowseBean.getWebsite().getNodeRef();
      List<ChildAssociationRef> webFormRefs = this.nodeService.getChildAssocs(
            websiteRef, WCMAppModel.ASSOC_WEBFORM, RegexQNamePattern.MATCH_ALL);
      this.formWorkflowMap = new HashMap<String, FormWorkflowWrapper>(webFormRefs.size(), 1.0f);
      for (ChildAssociationRef ref : webFormRefs)
      {
         NodeRef webFormRef = ref.getChildRef();
         String form = (String)this.nodeService.getProperty(webFormRef, WCMAppModel.PROP_FORMNAME);
         List<ChildAssociationRef> wfRefs = this.nodeService.getChildAssocs(
            webFormRef, WCMAppModel.TYPE_WORKFLOW_DEFAULTS, RegexQNamePattern.MATCH_ALL);
         if (wfRefs.size() == 1)
         {
            NodeRef wfDefaultsRef = wfRefs.get(0).getChildRef();
            String wfName = (String)this.nodeService.getProperty(wfDefaultsRef, WCMAppModel.PROP_WORKFLOW_NAME);
            Map<QName, Serializable> params = (Map<QName, Serializable>)AVMWorkflowUtil.deserializeWorkflowParams(
                  wfDefaultsRef);
            this.formWorkflowMap.put(form, new FormWorkflowWrapper(wfName, params));
         }
      }
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      if (getSubmitItemsSize() == 0)
      {
         return null;
      }
      
      // get the defaults from the workflow configuration attached to the selected workflow
      if (this.workflowSelectedValue != null)
      {
         Map<QName, Serializable> params = null;
         String workflowName = this.workflowSelectedValue[0];
         for (FormWorkflowWrapper wrapper : this.workflows)
         {
            if (wrapper.name.equals(workflowName))
            {
               params = wrapper.params;
            }
         }
         
         if (params != null)
         {
            // start the workflow to get access to the start task
            WorkflowDefinition wfDef = workflowService.getDefinitionByName(workflowName);
            WorkflowPath path = this.workflowService.startWorkflow(wfDef.id, null);
            if (path != null)
            {
               // extract the start task
               List<WorkflowTask> tasks = this.workflowService.getTasksForWorkflowPath(path.id);
               if (tasks.size() == 1)
               {
                  WorkflowTask startTask = tasks.get(0);
                  
                  if (startTask.state == WorkflowTaskState.IN_PROGRESS)
                  {
                     // Create workflow sandbox for workflow package
                     SandboxInfo sandboxInfo = 
                        SandboxFactory.createWorkflowSandbox( this.avmBrowseBean.getStagingStore() );

                     // create container for our avm workflow package
                     final List<ItemWrapper> items = this.getSubmitItems();
                     final List<String> srcPaths = new ArrayList<String>(items.size());

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
                        //  notifications to be suppressed (they're expensive).
                        //
                        // Therefore, just derive the name of the webapp
                        // in the workflow sandbox from the 1st item in 
                        // the submiot list (even if it's not in WEB-INF), 
                        // and force the virt server notification after the
                        // transaction has completed via doPostCommitProcessing.

                        if ( this.virtUpdatePath  == null )
                        {
                            // Example workflow main store name:
                            //     mysite--workflow-9161f640-b020-11db-8015-130bf9b5b652
                            String workflowMainStoreName = sandboxInfo.getMainStoreName();

                            // The virtUpdatePath looks just like the srcPath 
                            // except that it belongs to a the main store of
                            // the workflow sandbox instead of the sandbox
                            // that originated the submit.

                            this.virtUpdatePath = workflowMainStoreName + 
                                                  srcPath.substring(
                                                        srcPath.indexOf(':'),
                                                        srcPath.length()
                                                  );
                        }

                        srcPaths.add(srcPath);
                     }

                     final NodeRef workflowPackage =
                        AVMWorkflowUtil.createWorkflowPackage(srcPaths,
                                                              sandboxInfo,
                                                              path,
                                                              avmSubmittedAspect,
                                                              this.avmSyncService,
                                                              this.avmService,
                                                              this.workflowService,
                                                              this.nodeService);

                     params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);


                     
                     // add submission parameters
                     params.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, getComment());
                     params.put(AVMWorkflowUtil.PROP_LABEL, getLabel());
                     params.put(AVMWorkflowUtil.PROP_FROM_PATH, AVMConstants.buildStoreRootPath(this.avmBrowseBean.getSandbox()));
                      
                     // update start task with submit parameters
                     this.workflowService.updateTask(startTask.id, params, null, null);
                      
                     // end the start task to trigger the first 'proper' task in the workflow
                     this.workflowService.endTask(startTask.id, null);
                  }
               }
            }
         }
         else
         {
            // create error msg for display in dialog - the user must configure the workflow params
            Utils.addErrorMessage(Application.getMessage(context, MSG_ERR_WORKFLOW_CONFIG));
            outcome = null;
            // set the isFinished flag to allow the dialog to be finished again after the error
            isFinished = true;
         }
      }
      else
      {
         // direct submit to the staging area without workflow
         List<ItemWrapper> items = getSubmitItems();
         
         // construct diffs for selected items for submission
         String sandboxPath = AVMConstants.buildSandboxRootPath(this.avmBrowseBean.getSandbox());
         String stagingPath = AVMConstants.buildSandboxRootPath(this.avmBrowseBean.getStagingStore());
         List<AVMDifference> diffs = new ArrayList<AVMDifference>(items.size());

         // flag indicating if virt server update is already implied
         boolean update_vserver = false;

         for (ItemWrapper wrapper : items)
         {
            String srcPath = sandboxPath + wrapper.getPath();
            String destPath = stagingPath + wrapper.getPath();
            AVMDifference diff = new AVMDifference(-1, srcPath, -1, destPath, AVMDifference.NEWER);
            diffs.add(diff);

            // If nothing has required notifying the virtualization server
            // so far, check to see if destPath forces a notification
            // (e.g.:  it might be a path to a jar file within WEB-INF/lib).

            if  ( ! update_vserver )
            {
                // Examples of destPath that require virt server notification:
                //
                //     mysite:/www/avm_webapps/ROOT/WEB-INF/web.xml
                //     mysite:/www/avm_webapps/ROOT/WEB-INF/lib/moo.jar
                
                update_vserver = VirtServerUtils.requiresUpdateNotification( destPath );

                if ( update_vserver ) { this.virtUpdatePath = destPath; }
            }
         }
         
         // write changes to layer so files are marked as modified
         this.avmSyncService.update(diffs, null, true, true, false, false, this.label, this.comment);
         AVMDAOs.Instance().fAVMNodeDAO.flush();
         avmSyncService.flatten(sandboxPath, stagingPath);
      }
      
      return outcome;
   }

   /**
   *   Handle notification to the virtualization server 
   *   (this needs to occur after the sandbox is updated).
   */
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {     
       // Force the update because we've already determined
       // that update_path requires virt server notification.
      
       if ( this.virtUpdatePath != null)
       {
           // Examples of destPath that require virt server notification:
           //
           //     mysite:/www/avm_webapps/ROOT/WEB-INF/web.xml
           //     mysite:/www/avm_webapps/ROOT/WEB-INF/lib/moo.jar

           AVMConstants.updateVServerWebapp( this.virtUpdatePath, true );
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
         List<ChildAssociationRef> webWorkflowRefs = this.nodeService.getChildAssocs(
               websiteRef, WCMAppModel.ASSOC_WEBWORKFLOWDEFAULTS, RegexQNamePattern.MATCH_ALL);
         List<FormWorkflowWrapper> workflowMatchers = new ArrayList<FormWorkflowWrapper>(webWorkflowRefs.size());
         for (ChildAssociationRef ref : webWorkflowRefs)
         {
            NodeRef wfDefaultsRef = ref.getChildRef();
            String wfName = (String)this.nodeService.getProperty(wfDefaultsRef, WCMAppModel.PROP_WORKFLOW_NAME);
            Map<QName, Serializable> params = (Map<QName, Serializable>)AVMWorkflowUtil.deserializeWorkflowParams(
                  wfDefaultsRef);
            String matchPattern = (String)this.nodeService.getProperty(
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
            for (int i=0; i<workflowMatchers.size(); i++)
            {
               // see if the file path matches this workflow path pattern
               if (workflowMatchers.get(i).matchesPath(path) == true)
               {
                  // found a match - remove the workflow from the list of ones to check
                  this.workflows.add(workflowMatchers.get(i));
                  workflowMatchers.remove(i);
               }
            }
            // if all workflows are matched, there is no need to continue looping
            if (workflowMatchers.size() == 0) break;
         }
         
         // build a UI item for each available workflow
         List<UIListItem> items = new ArrayList<UIListItem>(this.workflows.size());
         for (FormWorkflowWrapper wrapper : this.workflows)
         {
            WorkflowDefinition workflowDef = this.workflowService.getDefinitionByName(wrapper.name);
            UIListItem item = new UIListItem();
            item.setValue(workflowDef.getName());
            item.setLabel(workflowDef.getTitle());
            item.setDescription(workflowDef.getDescription());
            item.setImage(WebResources.IMAGE_WORKFLOW_32);
            items.add(item);
            // add first workflow as default selection
            if (workflowSelectedValue == null)
            {
               workflowSelectedValue = new String[]{workflowDef.getName()};
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
      // TODO: start txn here?
      List<AVMNodeDescriptor> selected;
      if (this.avmBrowseBean.getAllItemsAction())
      {
         String webapp = this.avmBrowseBean.getWebapp();
         String userStore = AVMConstants.buildStoreWebappPath(this.avmBrowseBean.getSandbox(), webapp);
         String stagingStore = AVMConstants.buildStoreWebappPath(this.avmBrowseBean.getStagingStore(), webapp);
         List<AVMDifference> diffs = this.avmSyncService.compare(-1, userStore, -1, stagingStore, nameMatcher);
         selected = new ArrayList<AVMNodeDescriptor>(diffs.size());
         for (AVMDifference diff : diffs)
         {
            AVMNodeDescriptor node = this.avmService.lookup(-1, diff.getSourcePath(), true);
            selected.add(node);
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
         AVMNodeDescriptor node =
            this.avmService.lookup(-1, this.avmBrowseBean.getAvmActionNode().getPath(), true);
         selected = new ArrayList<AVMNodeDescriptor>(1);
         selected.add(node);
      }
      if (selected != null)
      {
         Set<String> submittedPaths = new HashSet<String>(selected.size());
         this.submitItems = new ArrayList<ItemWrapper>(selected.size());
         this.warningItems = new ArrayList<ItemWrapper>(selected.size() >> 1);
         for (AVMNodeDescriptor node : selected)
         {
            if (this.avmService.hasAspect(-1, node.getPath(), AVMSubmittedAspect.ASPECT) == false)
            {
               NodeRef ref = AVMNodeConverter.ToNodeRef(-1, node.getPath());
               if (submittedPaths.contains(node.getPath()) == false)
               {
                  if (node.isDeleted() == false)
                  {
                     // lookup if this item was created via a form - then lookup the workflow defaults
                     // for that form and store into the list of available workflows
                     if (this.nodeService.hasAspect(ref, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
                     {
                        NodeRef formInstanceDataRef = ref;
                        
                        // check if this is a rendition - as they also have the forminstancedata aspect
                        if (this.nodeService.hasAspect(ref, WCMAppModel.ASPECT_RENDITION))
                        {
                           // found a generated rendition asset - locate the parent form instance data file
                           // and use this to find all generated assets that are appropriate
                           // NOTE: this path value is store relative
                           String strFormInstance = (String)this.nodeService.getProperty(
                                 ref, WCMAppModel.PROP_PRIMARY_FORM_INSTANCE_DATA);
                           strFormInstance = this.avmBrowseBean.getSandbox() + ':' + strFormInstance;
                           formInstanceDataRef = AVMNodeConverter.ToNodeRef(-1, strFormInstance);
                        }
                        
                        // add the form instance data file to the list for submission
                        AVMNodeDescriptor formInstanceNode = this.avmService.lookup(
                              -1, AVMNodeConverter.ToAVMVersionPath(formInstanceDataRef).getSecond());
                        if (submittedPaths.contains(formInstanceNode.getPath()) == false)
                        {
                           this.submitItems.add(new ItemWrapper(formInstanceNode));
                           submittedPaths.add(formInstanceNode.getPath());
                        }
                        
                        // locate renditions for this form instance data file and add to list for submission
                        FormInstanceData formImpl = new FormInstanceDataImpl(formInstanceDataRef);
                        for (Rendition rendition : formImpl.getRenditions())
                        {
                           String renditionPath = rendition.getPath();
                           if (submittedPaths.contains(renditionPath) == false)
                           {
                              AVMNodeDescriptor renditionNode = this.avmService.lookup(-1, renditionPath);
                              this.submitItems.add(new ItemWrapper(renditionNode));
                              submittedPaths.add(renditionPath);
                           }
                        }
                        
                        // lookup the associated Form workflow from the parent form property
                        String formName = (String)this.nodeService.getProperty(
                              formInstanceDataRef, WCMAppModel.PROP_PARENT_FORM_NAME);
                        FormWorkflowWrapper wrapper = this.formWorkflowMap.get(formName);
                        if (wrapper != null)
                        {
                           // found a workflow attached to the form
                           this.workflows.add(wrapper);
                        }
                     }
                     else
                     {
                        this.submitItems.add(new ItemWrapper(node));
                        submittedPaths.add(node.getPath());
                     }
                  }
                  else
                  {
                     // found a deleted node for submit
                     this.submitItems.add(new ItemWrapper(node));
                     submittedPaths.add(node.getPath());
                  }
               }
            }
            else
            {
               this.warningItems.add(new ItemWrapper(node));
            }
         }
      }
      else
      {
         this.submitItems = Collections.<ItemWrapper>emptyList();
         this.warningItems = Collections.<ItemWrapper>emptyList();
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
    * Simple structure class to wrap form workflow name and default parameter values
    */
   private static class FormWorkflowWrapper implements WorkflowConfiguration
   {
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
         if (filenamePattern != null)
         {
            return filenamePattern.matcher(path).matches();
         }
         else
         {
            return false;
         }
      }

      @Override
      public int hashCode()
      {
         return this.name.hashCode();
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof FormWorkflowWrapper)
         {
            return this.name.equals( ((FormWorkflowWrapper)obj).name );
         }
         else
         {
            return false;
         }
      }
   }
   
   /**
    * Wrapper class to provide UI RichList component getters for an AVM node descriptor 
    */
   public class ItemWrapper
   {
      private static final String rootPath = '/' + JNDIConstants.DIR_DEFAULT_APPBASE;
      private AVMNodeDescriptor descriptor;
      
      public ItemWrapper(AVMNodeDescriptor descriptor)
      {
         this.descriptor = descriptor;
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
      
      public String getDescription()
      {
         if (descriptor.isDeleted() == false)
         {
            return (String)nodeService.getProperty(
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
      
      public String getUrl()
      {
         return DownloadContentServlet.generateBrowserURL(
               AVMNodeConverter.ToNodeRef(-1, descriptor.getPath()), descriptor.getName());
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
         if (obj instanceof ItemWrapper)
         {
            return ((ItemWrapper)obj).descriptor.getPath().equals(descriptor.getPath());
         }
         else
         {
            return false;
         }
      }

      @Override
      public int hashCode()
      {
         return descriptor.getPath().hashCode();
      }
   }
}
