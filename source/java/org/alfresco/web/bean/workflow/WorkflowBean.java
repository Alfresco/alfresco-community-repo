package org.alfresco.web.bean.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
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
 * Managed bean used for handling workflow related features
 * 
 * @author gavinc
 */
public class WorkflowBean
{
   protected NodeService nodeService;
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
         this.workItems = new ArrayList<Node>(tasks.size());
         for (WorkflowTask task : tasks)
         {
            Node node = createWorkItem(task);
            this.workItems.add(node);
            
            if (logger.isDebugEnabled())
               logger.debug("Added to do work item: " + node);
         }
         
         // commit the changes
         tx.commit();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage("Failed to get to do work items: " + e.toString(), e);
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
         this.completedWorkItems = new ArrayList<Node>(tasks.size());
         for (WorkflowTask task : tasks)
         {
            Node node = createWorkItem(task);
            this.completedWorkItems.add(node);
            
            if (logger.isDebugEnabled())
               logger.debug("Added completed work item: " + node);
         }
         
         // commit the changes
         tx.commit();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage("Failed to get completed work items: " + e.toString(), e);
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
      
      // add the name of the source space (if there is one)
      // TODO: remove this workaroud where JBPM may return a String and not the NodeRef
      Serializable obj = task.properties.get(WorkflowModel.PROP_CONTEXT);
      NodeRef context = null;
      if (obj instanceof NodeRef)
      {
         context = (NodeRef)obj;
      }
      else if (obj instanceof String)
      {
         context = new NodeRef((String)obj);
      }
      
      if (context != null)
      {
         String name = Repository.getNameForNode(this.nodeService, context);
         node.getProperties().put("sourceSpaceName", name);
         node.getProperties().put("sourceSpaceId", context.getId());
      }
      
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
         node.getProperties().put("workflowInstanceName", task.path.instance.definition.title);
      }
      
      return node;
   }
}
