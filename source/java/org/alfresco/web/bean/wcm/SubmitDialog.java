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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
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

/**
 * @author Kevin Roast
 */
public class SubmitDialog extends BaseDialogBean
{
   private static final String SPACE_ICON = "/images/icons/" + BrowseBean.SPACE_SMALL_DEFAULT + ".gif";
   private static final String MSG_DELETED_ITEM = "avm_node_deleted";
   
   private String comment;
   private String label;
   private String[] workflowSelectedValue;
   
   private List<ItemWrapper> submitItems;
   private List<ItemWrapper> warningItems;
   private HashSet<FormWorkflowWrapper> workflows;
   private Map<String, FormWorkflowWrapper> formWorkflowMap;
   private List<UIListItem> workflowItems;
   
   protected AVMService avmService;
   protected AVMBrowseBean avmBrowseBean;
   protected WorkflowService workflowService;
   protected AVMSyncService avmSyncService;
   protected NameMatcher nameMatcher;
   
   /**
    * @param avmService       The AVM Service to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
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
               webFormRef, WCMAppModel.TYPE_WORKFLOWDEFAULTS, RegexQNamePattern.MATCH_ALL);
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
      // get the defaults from the workflow configuration attached to the selected workflow
      Map<QName, Serializable> params = null;
      String workflowName = this.workflowSelectedValue[0];
      for (FormWorkflowWrapper wrapper : this.workflows)
      {
         if (wrapper.Name.equals(workflowName))
         {
            params = wrapper.Params;
         }
      }
      
      if (params != null)
      {
         // create container for our avm workflow package
         NodeRef workflowPackage = createWorkflowPackage();
         params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
         
         // add submission parameters
         params.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, getComment());
         params.put(AVMWorkflowUtil.PROP_LABEL, getLabel());
         params.put(AVMWorkflowUtil.PROP_FROM_PATH, AVMConstants.buildAVMStoreRootPath(this.avmBrowseBean.getSandbox()));
         
         // start the workflow to get access to the start task
         WorkflowDefinition wfDef = workflowService.getDefinitionByName(workflowName);
         WorkflowPath path = this.workflowService.startWorkflow(wfDef.id, params);
         if (path != null)
         {
            // extract the start task
            List<WorkflowTask> tasks = this.workflowService.getTasksForWorkflowPath(path.id);
            if (tasks.size() == 1)
            {
               WorkflowTask startTask = tasks.get(0);
               
               if (startTask.state == WorkflowTaskState.IN_PROGRESS)
               {
                  // end the start task to trigger the first 'proper' task in the workflow
                  this.workflowService.endTask(startTask.id, null);
               }
            }
         }
      }
      else
      {
         // TODO: jump to dialog and allow user to finish wf properties config!
         throw new AlfrescoRuntimeException("Workflow has not been configured correctly, cannot submit items.");
      }
      
      return outcome;
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonDisabled()
    */
   @Override
   public boolean getFinishButtonDisabled()
   {
      return (getWorkflowSelectedValue() == null || getSubmitItemsSize() == 0);
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
            WorkflowDefinition workflowDef = this.workflowService.getDefinitionByName(wrapper.Name);
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
      if (this.avmBrowseBean.getSubmitAll())
      {
         String userStore = this.avmBrowseBean.getSandbox() + ":/";
         String stagingStore = this.avmBrowseBean.getStagingStore() + ":/";
         List<AVMDifference> diffs = avmSyncService.compare(-1, userStore, -1, stagingStore, nameMatcher);
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
            if (hasAssociatedWorkflow(AVMNodeConverter.ToNodeRef(-1, node.getPath())) == false)
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
                           // NOTE: this ref will be in the 'preview' store convert back to user store first
                           String strFormInstance = (String)this.nodeService.getProperty(
                                       ref, WCMAppModel.PROP_PRIMARY_FORM_INSTANCE_DATA);
                           strFormInstance = strFormInstance.replaceFirst(AVMConstants.STORE_PREVIEW, 
                                                                          AVMConstants.STORE_MAIN);
                           formInstanceDataRef = new NodeRef(strFormInstance);
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
                        if (wrapper != null && wrapper.Params != null)
                        {
                           // found a workflow with params attached to the form
                           this.workflows.add(wrapper);
                        }
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
   
   private boolean hasAssociatedWorkflow(NodeRef ref)
   {
      // TODO: does not appear to work for AVM - need a specific impl instead
      return (this.workflowService.getWorkflowsForContent(ref, true).size() != 0);
   }
   
   /**
    * Construct a workflow package as a layered directory over the staging sandbox. The items for
    * submission are pushed into the layer and the package constructed around it.
    * 
    * @return Reference to the package
    */
   private NodeRef createWorkflowPackage()
   {
      List<ItemWrapper> items = getSubmitItems();
      
      // create package paths (layered to staging area as target)
      String stagingPath = AVMConstants.buildAVMStoreRootPath(this.avmBrowseBean.getStagingStore());
      String packagesPath = AVMWorkflowUtil.createAVMLayeredPackage(this.avmService, stagingPath);
      
      // construct diffs for selected items for submission
      String sandboxPath = AVMConstants.buildAVMStoreRootPath(this.avmBrowseBean.getSandbox());
      List<AVMDifference> diffs = new ArrayList<AVMDifference>(this.submitItems.size());
      for (ItemWrapper wrapper : this.submitItems)
      {
         String srcPath = sandboxPath + wrapper.getPath();
         String destPath = packagesPath + wrapper.getPath();
         AVMDifference diff = new AVMDifference(-1, srcPath, -1, destPath, AVMDifference.NEWER);
         diffs.add(diff);
      }
      
      // write changes to layer so files are marked as modified
      this.avmSyncService.update(diffs, null, true, true, false, false, null, null);
      
      // convert package to workflow package
      AVMNodeDescriptor packageDesc = this.avmService.lookup(-1, packagesPath);
      NodeRef packageNodeRef = this.workflowService.createPackage(
            AVMNodeConverter.ToNodeRef(-1, packageDesc.getPath()));
      this.nodeService.setProperty(packageNodeRef, WorkflowModel.PROP_IS_SYSTEM_PACKAGE, true);
      
      return packageNodeRef;
   }
   
   
   /**
    * Simple structure class to wrap form workflow name and default parameter values
    */
   private static class FormWorkflowWrapper
   {
      public String Name;
      public Map<QName, Serializable> Params;
      private Pattern filenamePattern;
      
      FormWorkflowWrapper(String name, Map<QName, Serializable> params)
      {
         this.Name = name;
         this.Params = params;
      }
      
      FormWorkflowWrapper(String name, Map<QName, Serializable> params, String filenamePattern)
      {
         this.Name = name;
         this.Params = params;
         if (filenamePattern != null)
         {
            this.filenamePattern = Pattern.compile(filenamePattern);
         }
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
         return this.Name.hashCode();
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof FormWorkflowWrapper)
         {
            return this.Name.equals( ((FormWorkflowWrapper)obj).Name );
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
      private static final String rootPath = '/' + AVMConstants.DIR_WEBAPPS;
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
         if (descriptor.isDeleted() == false)
         {
            return descriptor.getName();
         }
         else
         {
            return descriptor.getName() + " [" +
               Application.getMessage(FacesContext.getCurrentInstance(), MSG_DELETED_ITEM) + "]";
         }
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
      
      public String getIcon()
      {
         // TODO: handle deleted file types here once implemented in the AVMNodeType enum
         if (descriptor.isFile())
         {
            return Utils.getFileTypeImage(descriptor.getName(), true);
         }
         else
         {
            return SPACE_ICON;
         }
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
