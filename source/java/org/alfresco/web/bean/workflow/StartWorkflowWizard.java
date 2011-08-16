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
package org.alfresco.web.bean.workflow;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.springframework.extensions.config.ConfigElement;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.publishing.PublishingEventHelper;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.TransientNode;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the Start Workflow Wizard.
 * 
 * @author gavinc
 */
public class StartWorkflowWizard extends BaseWizardBean
{
   private static final long serialVersionUID = -4370844066621902880L;
   
   protected String selectedWorkflow;
   protected String previouslySelectedWorkflow;
   
   transient protected List<SelectItem> availableWorkflows;
   transient private Map<String, WorkflowDefinition> workflows;
   
   protected List<String> wcmWorkflows;
   protected List<String> invitationWorkflows;
   protected List<String> publishingWorkflows;
   
   transient private WorkflowService workflowService;
   transient private InvitationService invitationService;
   
   protected Node startTaskNode;
   protected List<Node> resources;
   protected List<String> packageItemsToAdd;
   protected UIRichList packageItemsRichList;
   protected String[] itemsToAdd;
   protected boolean isItemBeingAdded = false;
   protected boolean nextButtonDisabled = false;
   
   transient private NodeService unprotectedNodeService;
   
   private static final Log logger = LogFactory.getLog(StartWorkflowWizard.class);
   
   public void setUnprotectedNodeService(NodeService unprotectedNodeService)
   {
       this.unprotectedNodeService = unprotectedNodeService;
   }
   
   protected NodeService getUnprotectedNodeService()
   {
      if (this.unprotectedNodeService == null)
      {
         this.unprotectedNodeService = (NodeService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "nodeService");
      }
      return this.unprotectedNodeService;
   }
   
   protected Map<String, WorkflowDefinition> getWorkflows()
   {
      if (this.workflows == null)
      {
         initializeWorkflows();
      }
      return this.workflows;
   }
   
   // ------------------------------------------------------------------------------
   // Wizard implementation

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // reset the selected workflow
      if (this.availableWorkflows != null && this.availableWorkflows.size() > 0)
      {
         this.selectedWorkflow = (String)this.availableWorkflows.get(0).getValue();
      }
      else
      {
         this.selectedWorkflow = null;
      }
      
      this.previouslySelectedWorkflow = null;
      this.startTaskNode = null;
      this.resources = null;
      this.itemsToAdd = null;
      this.packageItemsToAdd = new ArrayList<String>();
      this.isItemBeingAdded = false;
      resetRichList();
      
      // add the item the workflow wizard was started on to the list of resources
      String itemToWorkflowId = this.parameters.get("item-to-workflow");
      try
      {
         if (itemToWorkflowId != null && itemToWorkflowId.length() > 0)
         {
            // create the node ref for the item and determine its type
            NodeRef itemToWorkflow = new NodeRef(Repository.getStoreRef(), itemToWorkflowId);
            QName type = this.getNodeService().getType(itemToWorkflow);
            
            if (this.getDictionaryService().isSubClass(type, ContentModel.TYPE_CONTENT) || this.getDictionaryService().isSubClass(type, ApplicationModel.TYPE_FILELINK))
            {
               this.packageItemsToAdd.add(itemToWorkflow.toString());
            }
         }
      }
      catch (InvalidNodeRefException refErr)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] { itemToWorkflowId }));
         throw new AbortProcessingException("Invalid node reference");
      }
   }
   
   @Override
   public void restored()
   {
      // reset the workflow package rich list so everything gets re-evaluated
      resetRichList();
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // TODO: Deal with workflows that don't require any data
      
      if (logger.isDebugEnabled())
         logger.debug("Starting workflow: " + this.selectedWorkflow);
      
      // prepare the parameters from the current state of the property sheet
      Map<QName, Serializable> params = WorkflowUtil.prepareTaskParams(this.startTaskNode);
      
      if (logger.isDebugEnabled())
         logger.debug("Starting workflow with parameters: " + params);
      
      // create a workflow package for the attached items and add them
      NodeRef workflowPackage = this.getWorkflowService().createPackage(null);
      params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
         
      for (String addedItem : this.packageItemsToAdd)
      {
        NodeRef addedNodeRef = new NodeRef(addedItem);
        this.getUnprotectedNodeService().addChild(workflowPackage, addedNodeRef, 
              WorkflowModel.ASSOC_PACKAGE_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
              QName.createValidLocalName((String)this.getNodeService().getProperty(
                    addedNodeRef, ContentModel.PROP_NAME))));
      }
      
      // setup the context for the workflow (this is the space the workflow was launched from)
      Node workflowContext = this.navigator.getCurrentNode();
      if (workflowContext != null)
      {
         params.put(WorkflowModel.PROP_CONTEXT, (Serializable)workflowContext.getNodeRef());
      }
      
      // start the workflow to get access to the start task
      WorkflowPath path = this.getWorkflowService().startWorkflow(this.selectedWorkflow, params);
      if (path != null)
      {
         // extract the start task
         List<WorkflowTask> tasks = this.getWorkflowService().getTasksForWorkflowPath(path.id);
         if (tasks.size() == 1)
         {
            WorkflowTask startTask = tasks.get(0);
            
            if (logger.isDebugEnabled())
               logger.debug("Found start task:" + startTask);
            
            if (startTask.state == WorkflowTaskState.IN_PROGRESS)
            {
               // end the start task to trigger the first 'proper'
               // task in the workflow
               this.getWorkflowService().endTask(startTask.id, null);
            }
         }
      
         if (logger.isDebugEnabled())
            logger.debug("Started workflow: " + this.selectedWorkflow);
      }
      
      return outcome;
   }
   
   @Override
   public String next()
   {
      String stepName = Application.getWizardManager().getCurrentStepName();
      
      if ("options".equals(stepName) && 
          (this.selectedWorkflow.equals(this.previouslySelectedWorkflow) == false))
      {
         // retrieve the start task for the selected workflow, get the task
         // definition and create a transient node to allow the property
         // sheet to collect the required data.
         
         WorkflowDefinition flowDef = this.getWorkflows().get(this.selectedWorkflow);
         
         if (logger.isDebugEnabled())
            logger.debug("Selected workflow: "+ flowDef);

         WorkflowTaskDefinition taskDef = flowDef.getStartTaskDefinition();
         if (taskDef != null)
         {
            if (logger.isDebugEnabled())
               logger.debug("Start task definition: " + taskDef);
            
            // create an instance of a task from the data dictionary
            this.startTaskNode = TransientNode.createNew(getDictionaryService(), taskDef.metadata,
                  "task_" + System.currentTimeMillis(), null);
         }
         
         // we also need to reset the resources list so that the actions get re-evaluated
         resetRichList();
      }

      return null;
   }
   
   @Override
   public String back()
   {
      String stepName = Application.getWizardManager().getCurrentStepName();
      
      // if we have come back to the "choose-workflow" step remember
      // the current workflow selection
      if ("choose-workflow".equals(stepName))
      {
         this.previouslySelectedWorkflow = this.selectedWorkflow;
      }
      
      return null;
   }

   @Override
   public boolean getNextButtonDisabled()
   {
      return this.nextButtonDisabled;
   }
   
   @Override
   public String getContainerTitle()
   {
      String wizTitle = null;
      
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      String stepName = Application.getWizardManager().getCurrentStepName();
      
      if ("choose-workflow".equals(stepName) == false && this.selectedWorkflow != null)
      {
         String titlePattern = bundle.getString("start_named_workflow_wizard");
         WorkflowDefinition workflowDef = this.getWorkflows().get(this.selectedWorkflow);
         wizTitle = MessageFormat.format(titlePattern, new Object[] {workflowDef.title});
      }
      else
      {
         wizTitle = bundle.getString("start_workflow_wizard");
      }
         
      return wizTitle;
   }
   
   // ------------------------------------------------------------------------------
   // Event Handlers

   /**
    * Prepares the dialog to allow the user to add an item to the workflow package
    * 
    * @param event The event
    */
   public void prepareForAdd(ActionEvent event)
   {
      this.isItemBeingAdded = true;
   }
   
   /**
    * Cancels the adding of an item to the workflow package
    * 
    * @param event The event
    */
   public void cancelAddPackageItems(ActionEvent event)
   {
      this.isItemBeingAdded = false;
   }
   
   /**
    * Adds items to the workflow package
    * 
    * @param event The event
    */
   public void addPackageItems(ActionEvent event)
   {
      if (this.itemsToAdd != null)
      {
         for (String item : this.itemsToAdd)
         {
            this.packageItemsToAdd.add(item);
               
            if (logger.isDebugEnabled())
               logger.debug("Added item to the added list: " + item);
         }
         
         // reset the rich list so it re-renders
         this.packageItemsRichList.setValue(null);
      }
      
      this.isItemBeingAdded = false;
      this.itemsToAdd = null;
   }
   
   /**
    * Removes an item from the workflow package
    * 
    * @param event The event containing a reference to the item to remove
    */
   public void removePackageItem(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String nodeRef = new NodeRef(Repository.getStoreRef(), params.get("id")).toString();
      
      if (this.packageItemsToAdd.contains(nodeRef))
      {
         // remove the item from the added list if it was added in this dialog session
         this.packageItemsToAdd.remove(nodeRef);
         
         if (logger.isDebugEnabled())
            logger.debug("Removed item from the added list: " + nodeRef);
      }
      
      // reset the rich list so it re-renders
      this.packageItemsRichList.setValue(null);
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /**
    * Returns a String array of NodeRef's that are being added to the workflow package
    * 
    * @return String array of NodeRef's
    */
   public String[] getItemsToAdd()
   {
      return this.itemsToAdd;
   }
   
   /**
    * Sets the NodeRef's to add as items to the workflow package
    * 
    * @param itemsToAdd NodeRef's to add to the workflow package
    */
   public void setItemsToAdd(String[] itemsToAdd)
   {
      this.itemsToAdd = itemsToAdd;
   }
   
   /**
    * Determines whether an item is currently being added to the workflow package
    * 
    * @return true if an item is being added
    */
   public boolean isItemBeingAdded()
   {
      return this.isItemBeingAdded;
   }
   
   /**
    * Sets the rich list being used for the workflow package items
    * 
    * @param richList The rich list instance
    */
   public void setPackageItemsRichList(UIRichList richList)
   {
      this.packageItemsRichList = richList;
   }
   
   /**
    * Returns the rich list being used for the workflow package items
    * 
    * @return The rich list instance
    */
   public UIRichList getPackageItemsRichList()
   {
      return this.packageItemsRichList;
   }
   
   /**
    * Returns the workflow selected by the user
    * 
    * @return The selected workflow
    */
   public String getSelectedWorkflow()
   {
      return selectedWorkflow;
   }
   
   /**
    * Sets the selected workflow
    * 
    * @param selectedWorkflow The workflow selected
    */
   public void setSelectedWorkflow(String selectedWorkflow)
   {
      this.selectedWorkflow = selectedWorkflow;
   }
   
   /**
    * Returns the Node representing the start task metadata required
    * 
    * @return The Node for the start task
    */
   public Node getTaskMetadataNode()
   {
      return this.startTaskNode;
   }
   
   /**
    * Returns the action group the current task uses for the workflow package
    * 
    * @return action group id
    */
   public String getPackageActionGroup()
   {
      String actionGroup = null;
      
      WorkflowDefinition flowDef = this.getWorkflows().get(this.selectedWorkflow);
      WorkflowTaskDefinition taskDef = flowDef.getStartTaskDefinition();
      if (taskDef != null)
      {
         PropertyDefinition propDef = taskDef.metadata.getProperties().get(
               WorkflowModel.PROP_PACKAGE_ACTION_GROUP);
         if (propDef != null)
         {
            actionGroup = propDef.getDefaultValue();
         }
      }
      
      return actionGroup;
   }
   
   /**
    * Returns the action group the current task uses for each workflow package item
    * 
    * @return action group id
    */
   public String getPackageItemActionGroup()
   {
      String actionGroup = null;
      
      WorkflowDefinition flowDef = this.getWorkflows().get(this.selectedWorkflow);
      WorkflowTaskDefinition taskDef = flowDef.getStartTaskDefinition();
      if (taskDef != null)
      {
         PropertyDefinition propDef = taskDef.metadata.getProperties().get(
               WorkflowModel.PROP_PACKAGE_ITEM_ACTION_GROUP);
         if (propDef != null)
         {
            actionGroup = propDef.getDefaultValue();
         }
      }
      
      return actionGroup;
   }
   
   /**
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      
      String workflowName = null;
      for (SelectItem item : this.availableWorkflows)
      {
         if (item.getValue().equals(this.selectedWorkflow))
         {
            workflowName = item.getLabel();
            break;
         }
      }
      
      return buildSummary(
            new String[] {bundle.getString("start_workflow")},
            new String[] {workflowName});
   }
   
   /**
    * Determines whether there are any workflows to start
    * 
    * @return true if there are startable workflows
    */
   public boolean getHasStartableWorkflows()
   {
      // get the list of startable workflow, this will intialise the list if necessary
      List<SelectItem> workflows = getStartableWorkflows();
      return (workflows.size() > 0);
   }
   
   private void initializeWorkflows()
   {
      // NOTE: we don't cache the list of startable workflows as they could get
      //       updated, in which case we need the latest instance id, they could
      //       theoretically also get removed.
      
      this.availableWorkflows = new ArrayList<SelectItem>(4);
      this.workflows = new HashMap<String, WorkflowDefinition>(4);
      
      // get the list of configured WCM workflows and filter these from
      // the list as these workflows are specific to WCM functionality and AVM stores
      List<String> configuredWcmWorkflows = this.getWCMWorkflowNames();
      List<String> configuredInvitationWorkflows = this.getInvitationServiceWorkflowNames();
      List<String> publishingWorkflows = this.getPublishingWorkflowNames();
      
      List<WorkflowDefinition> workflowDefs =  this.getWorkflowService().getDefinitions();
      for (WorkflowDefinition workflowDef : workflowDefs)
      {
         String name = workflowDef.name;
         
         if (configuredWcmWorkflows.contains(name) == false &&
        	 configuredInvitationWorkflows.contains(name) == false &&
        	 publishingWorkflows.contains(name) == false)
         {
            // add the workflow if it is not a WCM specific workflow
            String label = workflowDef.title;
            if (workflowDef.description != null && workflowDef.description.length() > 0)
            {
               label = label + " (" + workflowDef.description + ")";
            }
            this.availableWorkflows.add(new SelectItem(workflowDef.id, label));
            this.workflows.put(workflowDef.id, workflowDef);
         }
      }
      
      // disable the next button if there are no workflows
      if (this.availableWorkflows.size() == 0)
      {
         this.nextButtonDisabled = true;
      }
   }
   
   /**
    * Returns a list of workflows that can be started.
    * 
    * @return List of SelectItem objects representing the workflows
    */
   public List<SelectItem> getStartableWorkflows()
   {
      if (availableWorkflows == null)
      {
         initializeWorkflows();
      }
      
      // Alphabetical list sorting
      // Fix bug reported in https://issues.alfresco.com/browse/ETWOTWO-302
      
      QuickSort sorter = new QuickSort(availableWorkflows, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
      sorter.sort();
      
      // select the first workflow in the list
      if (this.availableWorkflows.size() > 0 && previouslySelectedWorkflow == null)
      {
         this.selectedWorkflow = (String)this.availableWorkflows.get(0).getValue();
      }
      
      return availableWorkflows;
   }

   /**
    * Returns the URL to the Workflow Definition Image of the current task
    * 
    * @return  the url
    */
   public String getWorkflowDefinitionImageUrl()
   {
       String url = null;
       if (selectedWorkflow != null)
       {
           WorkflowDefinition def = getWorkflows().get(selectedWorkflow);
           url = "/workflowdefinitionimage/" + def.id; 
       }
       return url; 
   }
      
   /**
    * Returns a list of resources associated with this task
    * i.e. the children of the workflow package
    * 
    * @return The list of nodes
    */
   public List<Node> getResources()
   {
      this.resources = new ArrayList<Node>(4);
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         for (String newItem : this.packageItemsToAdd)
         {
            NodeRef nodeRef = new NodeRef(newItem);
            if (this.getNodeService().exists(nodeRef))
            {
               // create our Node representation
               MapNode node = new MapNode(nodeRef, this.getNodeService(), true);
               this.browseBean.setupCommonBindingProperties(node);
               
               // add property resolvers to show path information
               node.addPropertyResolver("path", this.browseBean.resolverPath);
               node.addPropertyResolver("displayPath", this.browseBean.resolverDisplayPath);
               
               this.resources.add(node);
            }
            else
            {
               if (logger.isDebugEnabled())
                  logger.debug("Ignoring " + nodeRef + " as it has been removed from the repository");
            }
         }
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         this.resources = Collections.<Node>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      
      return this.resources;
   }
   
   /**
    * Sets the workflow service to use
    * 
    * @param workflowService WorkflowService instance
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

   // ------------------------------------------------------------------------------
   // Helper methods
  
   /**
    * Resets the rich list
    */
   protected void resetRichList()
   {
      if (this.packageItemsRichList != null)
      {
         this.packageItemsRichList.setValue(null);
         this.packageItemsRichList = null;
      }
   }
   
   /**
    * Get the Names of the WCM workflows.
    * 
    * @return The names of the WCM workflows.
    */
   protected List<String> getWCMWorkflowNames()   
   {
      if ((wcmWorkflows == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         ConfigElement config = Application.getConfigService(fc).getGlobalConfig().getConfigElement("wcm");
         if (config != null)
         {
            // get the main WCM workflows
            ConfigElement workflowConfig = config.getChild("workflows");
            if (workflowConfig != null)
            {
               StringTokenizer t = new StringTokenizer(workflowConfig.getValue().trim(), ", ");
               wcmWorkflows = new ArrayList<String>(t.countTokens());
               while (t.hasMoreTokens())
               {
                  String wfName = "jbpm$" + t.nextToken();
                  wcmWorkflows.add(wfName);
               }
            }
            else
            {
               if (logger.isWarnEnabled())
                  logger.warn("WARNING: Unable to find WCM 'workflows' config element definition.");
            }
            
            // get the admin WCM workflows
            ConfigElement adminWorkflowConfig = config.getChild("admin-workflows");
            if (adminWorkflowConfig != null)
            {
               StringTokenizer t = new StringTokenizer(adminWorkflowConfig.getValue().trim(), ", ");
               while (t.hasMoreTokens())
               {
                  String wfName = "jbpm$" + t.nextToken();
                  wcmWorkflows.add(wfName);
               }
            }
            else
            {
               if (logger.isWarnEnabled())
                  logger.warn("WARNING: Unable to find WCM 'admin-workflows' config element definition.");
            }
         }
         else
         {
            logger.warn("WARNING: Unable to find 'wcm' config element definition.");
         }
      }
      
      return wcmWorkflows;
   }
   /**
    * Get the Names of the Invitation Service Workflows
    * 
    * @return The names of the Invitation Service workflows
    */
   protected List<String> getInvitationServiceWorkflowNames()   
   {
      if( invitationWorkflows == null )
      {
         if (invitationService != null)
         {
            invitationWorkflows = invitationService.getInvitationServiceWorkflowNames();
         }
      }
      return invitationWorkflows;
   }
   
   /**
    * Get the names of the publishing workflows
    * 
    * @return The names of the publishing workflows
    */
   protected List<String> getPublishingWorkflowNames()   
   {
      if (publishingWorkflows == null)
      {
         publishingWorkflows = new ArrayList<String>(2);
         
         publishingWorkflows.add(JBPMEngine.ENGINE_ID + "$" + PublishingEventHelper.WORKFLOW_DEFINITION_NAME);
         publishingWorkflows.add(ActivitiConstants.ENGINE_ID + "$" + PublishingEventHelper.WORKFLOW_DEFINITION_NAME);
      }
      
      return publishingWorkflows;
   }

   public void setInvitationService(InvitationService invitationService) 
   {
      this.invitationService = invitationService;
   }

   public  InvitationService getInvitationService() 
   {
      return invitationService;
   }
}
