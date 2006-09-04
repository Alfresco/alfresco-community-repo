package org.alfresco.web.bean.workflow;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.TransientNode;
import org.alfresco.web.bean.wizard.BaseWizardBean;
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
   protected String selectedWorkflow;
   protected List<SelectItem> availableWorkflows;
   protected Map<String, WorkflowDefinition> workflows;
   protected WorkflowService workflowService;
   protected Node startTaskNode;
   protected List<Node> resources;
   protected List<String> packageItemsToAdd;
   protected UIRichList packageItemsRichList;
   protected String[] itemsToAdd;
   protected boolean isItemBeingAdded = false;
   protected boolean nextButtonDisabled = false;
   
   private static final Log logger = LogFactory.getLog(StartWorkflowWizard.class);
   
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
      
      this.startTaskNode = null;
      this.resources = null;
      this.itemsToAdd = null;
      this.packageItemsToAdd = new ArrayList<String>();
      this.isItemBeingAdded = false;
      if (this.packageItemsRichList != null)
      {
         this.packageItemsRichList.setValue(null);
         this.packageItemsRichList = null;
      }
      
      // TODO: Does this need to be in a read-only transaction??
      
      // add the item the workflow wizard was started on to the list of resources
      String itemToWorkflowId = this.parameters.get("item-to-workflow");
      if (itemToWorkflowId != null && itemToWorkflowId.length() > 0)
      {
         // create the node ref for the item and determine its type
         NodeRef itemToWorkflow = new NodeRef(Repository.getStoreRef(), itemToWorkflowId);
         QName type = this.nodeService.getType(itemToWorkflow);

         if (this.dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT) || 
             this.dictionaryService.isSubClass(type, ContentModel.TYPE_FILELINK))
         {
            this.packageItemsToAdd.add(itemToWorkflow.toString());
         }
      }
   }
   
   @Override
   public void restored()
   {
      // reset the workflow package rich list so everything gets re-evaluated
      if (this.packageItemsRichList != null)
      {
         this.packageItemsRichList.setValue(null);
         this.packageItemsRichList = null;
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // TODO: Deal with workflows that don't require any data
      
      if (logger.isDebugEnabled())
         logger.debug("Starting workflow: " + this.selectedWorkflow);
      
      // prepare the parameters from the current state of the property sheet
      Map<QName, Serializable> params = WorkflowBean.prepareTaskParams(this.startTaskNode);
      
      if (logger.isDebugEnabled())
         logger.debug("Starting workflow with parameters: " + params);
      
      // create a workflow package for the attached items and add them
      if (this.packageItemsToAdd.size() > 0)
      {
         NodeRef workflowPackage = this.workflowService.createPackage(null);
         
         for (String addedItem : this.packageItemsToAdd)
         {
            NodeRef addedNodeRef = new NodeRef(addedItem);
            this.nodeService.addChild(workflowPackage, addedNodeRef, 
                  ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                  QName.createValidLocalName((String)this.nodeService.getProperty(
                        addedNodeRef, ContentModel.PROP_NAME))));
         }
         
         // add the workflow package to the parameter map
         params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
      }
      
      // setup the context for the workflow (this is the space the workflow was launched from)
      Node workflowContext = this.navigator.getCurrentNode();
      if (workflowContext != null)
      {
         params.put(WorkflowModel.PROP_CONTEXT, (Serializable)workflowContext.getNodeRef());
      }
      
      // start the workflow to get access to the start task
      WorkflowPath path = this.workflowService.startWorkflow(this.selectedWorkflow, params);
      if (path != null)
      {
         // extract the start task
         List<WorkflowTask> tasks = this.workflowService.getTasksForWorkflowPath(path.id);
         if (tasks.size() == 1)
         {
            WorkflowTask startTask = tasks.get(0);
            
            if (logger.isDebugEnabled())
               logger.debug("Found start task:" + startTask);
            
            if (startTask.state == WorkflowTaskState.IN_PROGRESS)
            {
               // end the start task to trigger the first 'proper'
               // task in the workflow
               this.workflowService.endTask(startTask.id, null);
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
      
      if ("options".equals(stepName) && this.startTaskNode == null)
      {
         // retrieve the start task for the selected workflow, get the task
         // definition and create a transient node to allow the property
         // sheet to collect the required data.
         
         WorkflowDefinition flowDef = this.workflows.get(this.selectedWorkflow);        
         
         if (logger.isDebugEnabled())
            logger.debug("Selected workflow: "+ flowDef);

         WorkflowTaskDefinition taskDef = flowDef.startTaskDefinition;
         if (taskDef != null)
         {
            if (logger.isDebugEnabled())
               logger.debug("Start task definition: " + taskDef);
            
            // create an instance of a task from the data dictionary
            this.startTaskNode = new TransientNode(taskDef.metadata.getName(),
                  "task_" + System.currentTimeMillis(), null);
         }
      }

      return null;
   }
   
   @Override
   public boolean getNextButtonDisabled()
   {
      return this.nextButtonDisabled;
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
      
      WorkflowDefinition flowDef = this.workflows.get(this.selectedWorkflow);
      WorkflowTaskDefinition taskDef = flowDef.startTaskDefinition;
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
      
      WorkflowDefinition flowDef = this.workflows.get(this.selectedWorkflow);
      WorkflowTaskDefinition taskDef = flowDef.startTaskDefinition;
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
    * @return
    */
   public boolean getHasStartableWorkflows()
   {
      // get the list of startable workflow, this will intialise the list if necessary
      List<SelectItem> workflows = getStartableWorkflows();
      return (workflows.size() > 0);
   }
   
   /**
    * Returns a list of workflows that can be started.
    * 
    * @return List of SelectItem objects representing the workflows
    */
   public List<SelectItem> getStartableWorkflows()
   {
      if (this.availableWorkflows == null)
      {
         this.availableWorkflows = new ArrayList<SelectItem>(4);
         this.workflows = new HashMap<String, WorkflowDefinition>(4);
         
         List<WorkflowDefinition> workflowDefs =  this.workflowService.getDefinitions();
         for (WorkflowDefinition workflowDef : workflowDefs)
         {
            String label = workflowDef.title;
            if (workflowDef.description != null && workflowDef.description.length() > 0)
            {
               label = label + " (" + workflowDef.description + ")";
            }
            this.availableWorkflows.add(new SelectItem(workflowDef.id, label));
            this.workflows.put(workflowDef.id, workflowDef);
         }
         
         // set the initial selected workflow to the first in the list, unless there are no
         // workflows, in which disable the next button
         if (this.availableWorkflows.size() > 0)
         {
            this.selectedWorkflow = (String)this.availableWorkflows.get(0).getValue();
         }
         else
         {
            this.nextButtonDisabled = true;
         }
      }
      
      return availableWorkflows;
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
            if (this.nodeService.exists(nodeRef))
            {
               // create our Node representation
               MapNode node = new MapNode(nodeRef, this.nodeService, true);
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
}
