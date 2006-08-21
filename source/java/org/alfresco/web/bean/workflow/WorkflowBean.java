package org.alfresco.web.bean.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.TransientMapNode;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Managed bean used for handling workflow related features
 * 
 * @author gavinc
 */
public class WorkflowBean
{
   protected WorkflowService workflowService;
   protected List<Node> workItems;
   protected List<Node> completedWorkItems;
   
   private static final Log logger = LogFactory.getLog(WorkflowBean.class);
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   /**
    * Returns a list of nodes representing the to do work items the 
    * current user has.
    * 
    * @return List of to do work items
    */
   public List<Node> getWorkItemsToDo()
   {
      // get the current username
      FacesContext fc = FacesContext.getCurrentInstance();
      User user = Application.getCurrentUser(fc);
      String userName = ISO9075.encode(user.getUserName());
      
      // get the current in progress tasks for the current user
      List<WorkflowTask> tasks = this.workflowService.getAssignedTasks(
            userName, WorkflowTaskState.IN_PROGRESS);
      
      // create a list of transient nodes to represent
      this.workItems = new ArrayList<Node>(tasks.size());
      for (WorkflowTask task : tasks)
      {
         this.workItems.add(createWorkItem(task));
      }
      
      return this.workItems;
   }
   
   /**
    * Returns a list of nodes representing the completed work items the 
    * current user has.
    * 
    * @return List of completed work items
    */
   public List<Node> getWorkItemsCompleted()
   {
      // get the current username
      FacesContext fc = FacesContext.getCurrentInstance();
      User user = Application.getCurrentUser(fc);
      String userName = ISO9075.encode(user.getUserName());
      
      // get the current in progress tasks for the current user
      List<WorkflowTask> tasks = this.workflowService.getAssignedTasks(
            userName, WorkflowTaskState.COMPLETED);
      
      // create a list of transient nodes to represent
      this.completedWorkItems = new ArrayList<Node>(tasks.size());
      for (WorkflowTask task : tasks)
      {
         this.completedWorkItems.add(createWorkItem(task));
      }
      
      return this.completedWorkItems;
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
   
   // ------------------------------------------------------------------------------
   // Helper methods

   public static Map<QName, Serializable> prepareWorkItemParams(Node node)
   {
      Map<QName, Serializable> params = new HashMap<QName, Serializable>();
      
      // marshal the properties and associations captured by the property sheet
      // back into a Map to pass to the workflow service

      // go through all the properties in the transient node and add them to
      // params map
      Map<String, Object> props = node.getProperties();
      for (String propName : props.keySet())
      {
         QName propQName = Repository.resolveToQName(propName);
         params.put(propQName, (Serializable)props.get(propName));
      }
      
      // go through any associations that have been added to the start task
      // and build a list of NodeRefs representing the targets
      Map<String, Map<String, AssociationRef>> assocs = node.getAddedAssociations();
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
      
      if (logger.isDebugEnabled())
         logger.debug("Prepared parameters: " + params);
      
      return params;
   }
   
   /**
    * Creates and populates a TransientNode to represent the given
    * workflow task from the repository workflow engine
    * 
    * @param task The task to create a representation of
    */
   protected TransientMapNode createWorkItem(WorkflowTask task)
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
      
      return node;
   }
}
