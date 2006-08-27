package org.alfresco.web.bean.workflow;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.NodePropertyResolver;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.TransientNode;
import org.alfresco.web.config.DialogsConfigElement.DialogButtonConfig;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Manage WorkItem" dialog.
 * 
 * @author gavinc
 */
public class ManageWorkItemDialog extends BaseDialogBean
{
   protected WorkflowService workflowService;
   protected Node workItemNode;
   protected WorkflowTask workItem;
   protected WorkflowTransition[] transitions;
   protected List<Node> resources;
   protected WorkItemCompleteResolver completeResolver = new WorkItemCompleteResolver();
   protected UIRichList packageItemsRichList;

   protected static final String ID_PREFIX = "transition_";
   protected static final String CLIENT_ID_PREFIX = "dialog:" + ID_PREFIX;
   
   private static final Log logger = LogFactory.getLog(ManageWorkItemDialog.class);

   // ------------------------------------------------------------------------------
   // Dialog implementation

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      String taskId = this.parameters.get("id");
      this.workItem = this.workflowService.getTaskById(taskId);
      
      if (this.workItem != null)
      {
         // setup a transient node to represent the work item we're managing
         WorkflowTaskDefinition taskDef = this.workItem.definition;
         this.workItemNode = new TransientNode(taskDef.metadata.getName(),
                  "task_" + System.currentTimeMillis(), this.workItem.properties);
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("Saving work item: " + this.workItemNode.getId());
      
      // prepare the edited parameters for saving
      Map<QName, Serializable> params = WorkflowBean.prepareWorkItemParams(this.workItemNode);
      
      // update the task with the updated parameters
      this.workflowService.updateTask(this.workItem.id, params, null, null);
      
      return outcome;
   }

   @Override
   public List<DialogButtonConfig> getAdditionalButtons()
   {
      List<DialogButtonConfig> buttons = null;

      if (this.workItem != null)
      {
         // get the transitions available from this work item and 
         // show them in the dialog as additional buttons
         this.transitions = this.workItem.path.node.transitions;

         if (this.transitions != null)
         {
            buttons = new ArrayList<DialogButtonConfig>(this.transitions.length);
            
            for (WorkflowTransition trans : this.transitions)
            {
               buttons.add(new DialogButtonConfig(ID_PREFIX + trans.title, trans.title, null,
                     "#{DialogManager.bean.transition}", "false", null));
            }
         }
      }
      
      return buttons;
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "save");
   }
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   // ------------------------------------------------------------------------------
   // Event handlers

   @SuppressWarnings("unused")
   public String transition()
   {
      String outcome = getDefaultFinishOutcome();
      
      if (logger.isDebugEnabled())
         logger.debug("Transitioning work item: " + this.workItemNode.getId());
      
      // to find out which transition button was pressed we need
      // to look for the button's id in the request parameters,
      // the first non-null result is the button that was pressed.
      FacesContext context = FacesContext.getCurrentInstance();
      Map reqParams = context.getExternalContext().getRequestParameterMap();
      
      String selectedTransition = null;
      for (WorkflowTransition trans : this.transitions)
      {
         Object result = reqParams.get(CLIENT_ID_PREFIX + trans.title);
         if (result != null)
         {
            // this was the button that was pressed
            selectedTransition = trans.id;
            break;
         }
      }
      
      if (selectedTransition != null)
      {
         UserTransaction tx = null;
      
         try
         {
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            // prepare the edited parameters for saving
            Map<QName, Serializable> params = WorkflowBean.prepareWorkItemParams(this.workItemNode);
      
            // update the task with the updated parameters
            this.workflowService.updateTask(this.workItem.id, params, null, null);
         
            // signal the selected transition to the workflow task
            this.workflowService.endTask(this.workItem.id, selectedTransition);
            
            // commit the changes
            tx.commit();
            
            if (logger.isDebugEnabled())
               logger.debug("Ended work item with transition: " + selectedTransition);
         }
         catch (Throwable e)
         {
            // rollback the transaction
            try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
            Utils.addErrorMessage(formatErrorMessage(e), e);
            outcome = this.getErrorOutcome(e);
         }
      }
      
      return outcome;
   }

   /**
    * Removes an item from the workflow package
    * 
    * @param event The event containing a reference to the item to remove
    */
   public void removePackageItem(ActionEvent event)
   {
      logger.info("remove package item: " + event);
   }
   
   /**
    * Toggles the complete flag for a workflow package item
    * 
    * @param event The event containing a reference to the item to toggle the status for
    */
   public void togglePackageItemComplete(ActionEvent event)
   {
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         UIActionLink link = (UIActionLink)event.getComponent();
         Map<String, String> params = link.getParameterMap();
         
         // create the node ref for the item we are toggling
         NodeRef nodeRef = new NodeRef(Repository.getStoreRef(),
               (String)params.get("id"));
   
         // get the existing list of completed items
         List<NodeRef> completedItems = (List<NodeRef>)this.workItem.properties.get(
               WorkflowModel.PROP_COMPLETED_ITEMS);
         
         if (completedItems == null)
         {
            // if it doesn't exist yet create the list and add the noderef
            completedItems = new ArrayList<NodeRef>(1);
            completedItems.add(nodeRef);
            this.workItem.properties.put(WorkflowModel.PROP_COMPLETED_ITEMS, 
                  (Serializable)completedItems);
         }
         else
         {
            if (completedItems.contains(nodeRef))
            {
               // the item is already in the list remove it
               completedItems.remove(nodeRef);
               
               // NOTE: There is a bug somwehere which causes the list to be
               //       returned as a byte array instead of a list if an empty
               //       list is persisted, therefore if the list is now empty
               //       set the completed items back to null
               if (completedItems.size() == 0)
               {
                  this.workItem.properties.put(WorkflowModel.PROP_COMPLETED_ITEMS, null);
               }
            }
            else
            {
               // the noderef is not in the list yet so just add it
               completedItems.add(nodeRef);
            }
         }
      
         // update the task with the updated parameters
         this.workflowService.updateTask(this.workItem.id, this.workItem.properties, 
               null, null);
         
         // commit the transaction
         tx.commit();
         
         // reset the rich list if the change was successful
         this.packageItemsRichList.setValue(null);
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         this.resources = Collections.<Node>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

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
    * Returns the Node representing the work item
    * 
    * @return The node
    */
   public Node getWorkItemNode()
   {
      return this.workItemNode;
   }
   
   /**
    * Returns the action group the current task uses for the workflow package
    * 
    * @return action group id
    */
   public String getPackageActionGroup()
   {
      return (String)this.workItem.properties.get(
            WorkflowModel.PROP_PACKAGE_ACTION_GROUP);
   }
   
   /**
    * Returns the action group the current task uses for each workflow package item
    * 
    * @return action group id
    */
   public String getPackageItemActionGroup()
   {
      return (String)this.workItem.properties.get(
            WorkflowModel.PROP_PACKAGE_ITEM_ACTION_GROUP);
   }
   
   /**
    * Returns a list of resources associated with this work item
    * i.e. the children of the workflow package
    * 
    * @return The list of nodes
    */
   public List<Node> getResources()
   {
      NodeRef workflowPackage = null;
      Serializable obj = this.workItem.properties.get(WorkflowModel.ASSOC_PACKAGE);
      // TODO: remove this workaroud where JBPM may return a String and not the NodeRef
      if (obj instanceof NodeRef)
      {
         workflowPackage = (NodeRef)obj;
      }
      else if (obj instanceof String)
      {
         workflowPackage = new NodeRef((String)obj);
      }
      
      this.resources = new ArrayList<Node>(4);
      
      if (workflowPackage != null)
      {
         UserTransaction tx = null;
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context, true);
            tx.begin();
            
            if (logger.isDebugEnabled())
               logger.debug("Found workflow package for work item '" + 
                     this.workItem.id + "': " + workflowPackage );
            
            List<ChildAssociationRef> childRefs = this.nodeService.getChildAssocs(workflowPackage, 
                     ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);   
            
            for (ChildAssociationRef ref: childRefs)
            {
               // create our Node representation from the NodeRef
               NodeRef nodeRef = ref.getChildRef();
               
               if (this.nodeService.exists(nodeRef))
               {
                  // find it's type so we can see if it's a node we are interested in
                  QName type = this.nodeService.getType(nodeRef);
                  
                  // make sure the type is defined in the data dictionary
                  TypeDefinition typeDef = this.dictionaryService.getType(type);
                  
                  if (typeDef != null)
                  {
                     // look for content nodes or links to content
                     // NOTE: folders within workflow packages are ignored for now
                     if (this.dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT) || 
                         ContentModel.TYPE_FILELINK.equals(type))
                     {
                        // create our Node representation
                        MapNode node = new MapNode(nodeRef, this.nodeService, true);
                        this.browseBean.setupCommonBindingProperties(node);
                        
                        // add property resolvers to show path information
                        node.addPropertyResolver("path", this.browseBean.resolverPath);
                        node.addPropertyResolver("displayPath", this.browseBean.resolverDisplayPath);
                        
                        // add a property resolver to indicate whether the item has been completed or not
                        node.addPropertyResolver("completed", this.completeResolver);
                        
                        this.resources.add(node);
                     }
                  }
                  else
                  {
                     if (logger.isWarnEnabled())
                        logger.warn("Found invalid object in database: id = " + nodeRef + ", type = " + type);
                  }
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
      }
      else if (logger.isDebugEnabled())
      {
         logger.debug("Failed to find workflow package for work item: " + this.workItem.id);
      }
      
      return this.resources;
   }
   
   /**
    * Sets the workflow service to use
    * 
    * @param workflowService
    *           WorkflowService instance
    */
   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Property resolver to determine if the given node has been flagged as complete
    */
   protected class WorkItemCompleteResolver implements NodePropertyResolver
   {
      public Object get(Node node)
      {
         String result = Application.getMessage(FacesContext.getCurrentInstance(), "no");
         
         List<NodeRef> completedItems = (List<NodeRef>)workItem.properties.get(
               WorkflowModel.PROP_COMPLETED_ITEMS);
         
         if (completedItems != null && completedItems.size() > 0 && 
             completedItems.contains(node.getNodeRef()))
         {
            result = Application.getMessage(FacesContext.getCurrentInstance(), "yes");
         }
         
         return result;
      }
   }
}
