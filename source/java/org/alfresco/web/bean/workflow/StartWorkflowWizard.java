package org.alfresco.web.bean.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

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
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.TransientNode;
import org.alfresco.web.bean.wizard.BaseWizardBean;
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
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // TODO: Deal with workflows that don't require any data
      
      if (logger.isDebugEnabled())
         logger.debug("Starting workflow: " + this.selectedWorkflow);
      
      // prepare the parameters from the current state of the property sheet
      Map<QName, Serializable> params = WorkflowBean.prepareWorkItemParams(this.startTaskNode);
      
      // create a workflow package for the attached items and add them
      String itemToWorkflowId = this.parameters.get("item-to-workflow");
      if (itemToWorkflowId != null && itemToWorkflowId.length() > 0)
      {
         // create the node ref for the item and determine its type
         NodeRef itemToWorkflow = new NodeRef(Repository.getStoreRef(), itemToWorkflowId);
         QName type = this.nodeService.getType(itemToWorkflow);
         
         NodeRef workflowPackage = null;
         if (this.dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT) || 
             this.dictionaryService.isSubClass(type, ContentModel.TYPE_FILELINK))
         {
            // create a workflow package and add the given item to workflow as a child
            workflowPackage = this.workflowService.createPackage(null);
            this.nodeService.addChild(workflowPackage, itemToWorkflow, 
                  ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                  QName.createValidLocalName((String)this.nodeService.getProperty(
                        itemToWorkflow, ContentModel.PROP_NAME))));
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
   // Bean Getters and Setters

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
    * Sets the workflow service to use
    * 
    * @param workflowService WorkflowService instance
    */
   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }
}
