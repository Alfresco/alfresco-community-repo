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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.TransientMapNode;
import org.alfresco.web.bean.repository.TransientNode;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Managed bean used for providing support for the workflow task dashlets
 * 
 * @author gavinc
 */
public class WorkflowBean implements Serializable
{
   private static final long serialVersionUID = 2950475254440425384L;

   protected NavigationBean navigationBean;
   
   transient private NodeService nodeService;
   transient private WorkflowService workflowService;
   
   protected List<Node> tasks;
   protected List<Node> activeTasks;
   protected List<Node> pooledTasks;
   protected List<Node> completedTasks;
   
   private static final Log logger = LogFactory.getLog(WorkflowBean.class);
   
   public static final String BEAN_NAME = "WorkflowBean";
   
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   /**
    * Returns a list of nodes representing the "all" active tasks.
    * 
    * @return List of all active tasks
    */
   public List<Node> getAllActiveTasks()
   {
      if (this.activeTasks == null)
      {
         // get the current username
         FacesContext context = FacesContext.getCurrentInstance();
         User user = Application.getCurrentUser(context);
         String userName = user.getUserName();
         
         UserTransaction tx = null;
         try
         {
            tx = Repository.getUserTransaction(context, true);
            tx.begin();
            
            // query for all active tasks
            WorkflowTaskQuery query = new WorkflowTaskQuery();
            List<WorkflowTask> tasks = this.getWorkflowService().queryTasks(query);
            
            // create a list of transient nodes to represent
            this.activeTasks = new ArrayList<Node>(tasks.size());
            for (WorkflowTask task : tasks)
            {
               Node node = createTask(task);
               this.activeTasks.add(node);
               
               if (logger.isDebugEnabled())
                  logger.debug("Added active task: " + node);
            }
            
            // commit the changes
            tx.commit();
         }
         catch (Throwable e)
         {
            // rollback the transaction
            try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
            Utils.addErrorMessage("Failed to get all active tasks: " + e.toString(), e);
         }
      }
      
      return this.activeTasks;
   }
      
   /**
    * Returns a list of nodes representing the "pooled" to do tasks the 
    * current user has.
    * 
    * @return List of to do tasks
    */
   public List<Node> getPooledTasks()
   {
      if (this.pooledTasks == null)
      {
         // get the current username
         FacesContext context = FacesContext.getCurrentInstance();
         User user = Application.getCurrentUser(context);
         String userName = user.getUserName();
         
         UserTransaction tx = null;
         try
         {
            tx = Repository.getUserTransaction(context, true);
            tx.begin();
            
            // get the current pooled tasks for the current user
            List<WorkflowTask> tasks = this.getWorkflowService().getPooledTasks(userName);
            
            // create a list of transient nodes to represent
            this.pooledTasks = new ArrayList<Node>(tasks.size());
            for (WorkflowTask task : tasks)
            {
               Node node = createTask(task);
               this.pooledTasks.add(node);
               
               if (logger.isDebugEnabled())
                  logger.debug("Added pooled task: " + node);
            }
            
            // commit the changes
            tx.commit();
         }
         catch (Throwable e)
         {
            // rollback the transaction
            try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
            Utils.addErrorMessage("Failed to get pooled tasks: " + e.toString(), e);
         }
      }
      
      return this.pooledTasks;
   }

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
         String userName = user.getUserName();
         
         UserTransaction tx = null;
         try
         {
            tx = Repository.getUserTransaction(context, true);
            tx.begin();
            
            // get the current in progress tasks for the current user
            List<WorkflowTask> tasks = this.getWorkflowService().getAssignedTasks(
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
         String userName = user.getUserName();
         
         UserTransaction tx = null;
         try
         {
            tx = Repository.getUserTransaction(context, true);
            tx.begin();
            
            // get the current in progress tasks for the current user
            List<WorkflowTask> tasks = this.getWorkflowService().getAssignedTasks(
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
    * Sets the navigation bean to use
    * 
    * @param navigationBean The NavigationBean to set.
    */
   public void setNavigationBean(NavigationBean navigationBean)
   {
      this.navigationBean = navigationBean;
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

   /**
    * Sets the node service to use
    * 
    * @param nodeService NodeService instance
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   protected NodeService getNodeService()
   {
      if (this.nodeService == null)
      {
         this.nodeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
      }
      return this.nodeService;
   }
   // ------------------------------------------------------------------------------
   // Navigation handlers
   
   public void setupTaskDialog(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      String type = params.get("type");
      
      // setup the dispatch context with the task we're opening a dialog for
      TransientNode node = new TransientNode(QName.createQName(type), id, null);
      this.navigationBean.setupDispatchContext(node);
      
      // pass on parameters for the dialog
      Application.getDialogManager().setupParameters(event);
   }
   
   public void setupTaskDialog(String id, String type)
   {
      ParameterCheck.mandatoryString("Task ID", id);
      ParameterCheck.mandatoryString("Task Type", type);
      
      // setup the dispatch context with the task we're opening a dialog for
      TransientNode node = new TransientNode(QName.createQName(type), id, null);
      this.navigationBean.setupDispatchContext(node);
      
      // pass on parameters for the dialog
      Map<String, String> params = new HashMap<String, String>(2, 1.0f);
      params.put("id", id);
      params.put("type", type);
      Application.getDialogManager().setupParameters(params);
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
      node.getProperties().put("type", node.getType().toString());
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
            
            if(outcome == null)
            {
            	// TODO: is this okay -> no real transitions exist for activiti
            	outcome = transition;
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
