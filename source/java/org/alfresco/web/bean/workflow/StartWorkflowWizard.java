package org.alfresco.web.bean.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
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
         logger.debug("Starting workflow with params: " + this.startTaskNode.getProperties());
      
      // start the workflow to get access to the start task
      WorkflowPath path = this.workflowService.startWorkflow(this.selectedWorkflow, prepareTaskParams());
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
            logger.debug("Starting workflow: "+ flowDef);

         WorkflowTaskDefinition taskDef = flowDef.startTaskDefinition;
         if (taskDef != null)
         {
            if (logger.isDebugEnabled())
               logger.debug("Start task definition: " + taskDef);
            
            // create an instance of a task from the data dictionary
            this.startTaskNode = new TransientNode(taskDef.metadata.getName(),
                  "task_" + System.currentTimeMillis(), null);
               
            if (logger.isDebugEnabled())
               logger.debug("Created node for task: " + this.startTaskNode);
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
            this.availableWorkflows.add(new SelectItem(workflowDef.id, workflowDef.name));
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
   
   protected Map<QName, Serializable> prepareTaskParams()
   {
      Map<QName, Serializable> params = new HashMap<QName, Serializable>();
      
      // marshal the properties and associations captured by the property sheet
      // back into a Map to pass to the workflow service

      // go through all the properties in the transient node and add them to
      // params map
      Map<String, Object> props = this.startTaskNode.getProperties();
      for (String propName : props.keySet())
      {
         QName propQName = Repository.resolveToQName(propName);
         params.put(propQName, (Serializable)props.get(propName));
      }
      
      // go through any associations that have been added to the start task
      // and build a list of NodeRefs representing the targets
      Map<String, Map<String, AssociationRef>> assocs = this.startTaskNode.getAddedAssociations();
      for (String assocName : assocs.keySet())
      {
         QName assocQName = Repository.resolveToQName(assocName);
         
         // get the associations added and create list of targets
         Map<String, AssociationRef> addedAssocs = assocs.get(assocName);
         List<NodeRef> targets = new ArrayList<NodeRef>(addedAssocs.size());
         for (AssociationRef assoc : addedAssocs.values())
         {
            targets.add(assoc.getTargetRef());
         }
         
         // add the targets for this particular association
         params.put(assocQName, (Serializable)targets);
      }
      
      //      String reviewer = (String)this.startTaskNode.getProperties().get(
//            ContentModel.PROP_NAME);
//      Map<QName, Serializable> params = new HashMap<QName, Serializable>(1);
//      params.put(QName.createQName(NamespaceService.DEFAULT_URI, "reviewer"), reviewer);

      
      return params;
   }
}
