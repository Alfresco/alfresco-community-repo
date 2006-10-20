package org.alfresco.web.bean.workflow;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.util.ISO9075;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.TransientMapNode;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Managed bean used for providing support for the workflow task dashlets
 * 
 * @author gavinc
 */
public class WorkflowBean
{
   protected NodeService nodeService;
   protected WorkflowService workflowService;
   protected List<Node> tasks;
   protected List<Node> completedTasks;
   
   private static final Log logger = LogFactory.getLog(WorkflowBean.class);
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   /**
    * Returns a list of nodes representing the to do tasks the 
    * current user has.
    * 
    * @return List of to do tasks
    */
   public List<Node> getTasksToDo()
   {
      if (this.tasks == null)
      {
         // get the current username
         FacesContext context = FacesContext.getCurrentInstance();
         User user = Application.getCurrentUser(context);
         String userName = ISO9075.encode(user.getUserName());
         
         UserTransaction tx = null;
         try
         {
            tx = Repository.getUserTransaction(context, true);
            tx.begin();
            
            // get the current in progress tasks for the current user
            List<WorkflowTask> tasks = this.workflowService.getAssignedTasks(
                  userName, WorkflowTaskState.IN_PROGRESS);
            
            // create a list of transient nodes to represent
            this.tasks = new ArrayList<Node>(tasks.size());
            for (WorkflowTask task : tasks)
            {
               Node node = createTask(task);
               this.tasks.add(node);
               
               if (logger.isDebugEnabled())
                  logger.debug("Added to do task: " + node);
            }
            
            // commit the changes
            tx.commit();
         }
         catch (Throwable e)
         {
            // rollback the transaction
            try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
            Utils.addErrorMessage("Failed to get to do tasks: " + e.toString(), e);
         }
      }
      
      return this.tasks;
   }
   
   /**
    * Returns a list of nodes representing the completed tasks the 
    * current user has.
    * 
    * @return List of completed tasks
    */
   public List<Node> getTasksCompleted()
   {
      if (this.completedTasks == null)
      {
         // get the current username
         FacesContext context = FacesContext.getCurrentInstance();
         User user = Application.getCurrentUser(context);
         String userName = ISO9075.encode(user.getUserName());
         
         UserTransaction tx = null;
         try
         {
            tx = Repository.getUserTransaction(context, true);
            tx.begin();
            
            // get the current in progress tasks for the current user
            List<WorkflowTask> tasks = this.workflowService.getAssignedTasks(
                  userName, WorkflowTaskState.COMPLETED);
            
            // create a list of transient nodes to represent
            this.completedTasks = new ArrayList<Node>(tasks.size());
            for (WorkflowTask task : tasks)
            {
               Node node = createTask(task);
               this.completedTasks.add(node);
               
               if (logger.isDebugEnabled())
                  logger.debug("Added completed task: " + node);
            }
            
            // commit the changes
            tx.commit();
         }
         catch (Throwable e)
         {
            // rollback the transaction
            try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
            Utils.addErrorMessage("Failed to get completed tasks: " + e.toString(), e);
         }  
      }
      
      return this.completedTasks;
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
   
   /**
    * Sets the node service to use
    * 
    * @param nodeService NodeService instance
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   /**
    * Creates and populates a TransientNode to represent the given
    * workflow task from the repository workflow engine
    * 
    * @param task The task to create a representation of
    */
   protected TransientMapNode createTask(WorkflowTask task)
   {
      // get the type of the task
      WorkflowTaskDefinition taskDef = task.definition;
      
      // create the basic transient node
      TransientMapNode node = new TransientMapNode(taskDef.metadata.getName(),
            task.title, task.properties);
      
      // add properties for the other useful metadata
      node.getProperties().put(ContentModel.PROP_NAME.toString(), task.title);
      node.getProperties().put("type", taskDef.metadata.getTitle());
      node.getProperties().put("id", task.id);
      
      // add extra properties for completed tasks
      if (task.state.equals(WorkflowTaskState.COMPLETED))
      {
         // add the outcome label for any completed task
         String outcome = null;
         String transition = (String)task.properties.get(WorkflowModel.PROP_OUTCOME);
         if (transition != null)
         {
            WorkflowTransition[] transitions = task.definition.node.transitions;
            for (WorkflowTransition trans : transitions)
            {
               if (trans.id.equals(transition))
               {
                  outcome = trans.title;
                  break;
               }
            }
            
            if (outcome != null)
            {
               node.getProperties().put("outcome", outcome);
            }
         }
         
         // add the workflow instance id and name this taks belongs to
         node.getProperties().put("workflowInstanceId", task.path.instance.id);
         
         // add the task itself as a property
         node.getProperties().put("workflowTask", task);
      }
      
      return node;
   }
}
