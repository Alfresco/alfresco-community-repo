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
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespaceService;
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
   protected WorkflowInstance workflowInstance;
   protected WorkflowTransition[] transitions;
   protected NodeRef workflowPackage;
   protected List<Node> resources;
   protected WorkItemCompleteResolver completeResolver = new WorkItemCompleteResolver();
   protected UIRichList packageItemsRichList;
   protected List<String> packageItemsToAdd;
   protected List<String> packageItemsToRemove;
   protected String[] itemsToAdd;
   protected boolean isItemBeingAdded = false;

   protected static final String ID_PREFIX = "transition_";
   protected static final String CLIENT_ID_PREFIX = "dialog:" + ID_PREFIX;
   
   private static final Log logger = LogFactory.getLog(ManageWorkItemDialog.class);

   // ------------------------------------------------------------------------------
   // Dialog implementation

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // reset variables
      this.workItem = null;
      this.workItemNode = null;
      this.workflowInstance = null;
      this.transitions = null;
      this.workflowPackage = null;
      this.resources = null;
      this.itemsToAdd = null;
      this.packageItemsToAdd = null;
      this.packageItemsToRemove = null;
      this.isItemBeingAdded = false;
      if (this.packageItemsRichList != null)
      {
         this.packageItemsRichList.setValue(null);
      }
      
      // get the task details
      String taskId = this.parameters.get("id");
      this.workItem = this.workflowService.getTaskById(taskId);
      
      if (this.workItem != null)
      {
         // setup a transient node to represent the work item we're managing
         WorkflowTaskDefinition taskDef = this.workItem.definition;
         this.workItemNode = new TransientNode(taskDef.metadata.getName(),
                  "task_" + System.currentTimeMillis(), this.workItem.properties);
         
         // get access to the workflow instance for the work item
         this.workflowInstance = this.workItem.path.instance;
         
         // setup the workflow package for the task
         Serializable obj = this.workItem.properties.get(WorkflowModel.ASSOC_PACKAGE);
         // TODO: remove this workaroud where JBPM may return a String and not the NodeRef
         if (obj instanceof NodeRef)
         {
            this.workflowPackage = (NodeRef)obj;
         }
         else if (obj instanceof String)
         {
            this.workflowPackage = new NodeRef((String)obj);
         }
         
         if (logger.isDebugEnabled())
            logger.debug("Found workflow package for work item '" + 
                  this.workItem.id + "': " + this.workflowPackage );
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("Saving task: " + this.workItem.id);
      
      // prepare the edited parameters for saving
      Map<QName, Serializable> params = WorkflowBean.prepareWorkItemParams(this.workItemNode);
      
      // remove any items the user selected to remove 
      if (this.workflowPackage != null && this.packageItemsToRemove != null && 
          this.packageItemsToRemove.size() > 0)
      {
         for (String removedItem : this.packageItemsToRemove)
         {
            this.nodeService.removeChild(this.workflowPackage, new NodeRef(removedItem));
         }
      }
      
      // add any items the user selected to add 
      if (this.workflowPackage != null && this.packageItemsToAdd != null && 
          this.packageItemsToAdd.size() > 0)
      {
         for (String addedItem : this.packageItemsToAdd)
         {
            NodeRef addedNodeRef = new NodeRef(addedItem);
            this.nodeService.addChild(this.workflowPackage, addedNodeRef, 
                  ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                  QName.createValidLocalName((String)this.nodeService.getProperty(
                        addedNodeRef, ContentModel.PROP_NAME))));
         }
      }
      
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
    * Adds an item to the workflow package
    * 
    * @param event The event
    */
   public void addPackageItems(ActionEvent event)
   {
      if (this.itemsToAdd != null)
      {
         if (this.packageItemsToAdd == null)
         {
            // create the list of items to add if necessary
            this.packageItemsToAdd = new ArrayList<String>(this.itemsToAdd.length);
         }
         
         for (String item : this.itemsToAdd)
         {
            // if this item is in the remove list it means it was there originally
            // and has now been re-added, as a result we don't need to do anything
            // to the original workflow package, therefore remove from the remove list
            if (this.packageItemsToRemove != null && this.packageItemsToRemove.contains(item))
            {
               this.packageItemsToRemove.remove(item);
               
               if (logger.isDebugEnabled())
                  logger.debug("Removed item from the removed list: " + item);
            }
            else
            {
               this.packageItemsToAdd.add(item);
               
               if (logger.isDebugEnabled())
                  logger.debug("Added item to the added list: " + item);
            }
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
      
      if (this.packageItemsToAdd != null && this.packageItemsToAdd.contains(nodeRef))
      {
         // remove the item from the added list if it was added in this dialog session
         this.packageItemsToAdd.remove(nodeRef);
         
         if (logger.isDebugEnabled())
            logger.debug("Removed item from the added list: " + nodeRef); 
      }
      else
      {
         // add the node to the list of items to remove
         if (this.packageItemsToRemove == null)
         {
            this.packageItemsToRemove = new ArrayList<String>(1);
         }
         
         this.packageItemsToRemove.add(nodeRef);
         
         if (logger.isDebugEnabled())
            logger.debug("Added item to the removed list: " + nodeRef);
      }
      
      // reset the rich list so it re-renders
      this.packageItemsRichList.setValue(null);
   }
   
   /**
    * Toggles the complete flag for a workflow package item
    * 
    * @param event The event containing a reference to the item to toggle the status for
    */
   public void togglePackageItemComplete(ActionEvent event)
   {
      // TODO: implement this!
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
    * Returns the Node representing the work item
    * 
    * @return The node
    */
   public Node getWorkItemNode()
   {
      return this.workItemNode;
   }
   
   /**
    * Returns the WorkflowInstance that the current task belongs to
    * 
    * @return The workflow instance
    */
   public WorkflowInstance getWorkflowInstance()
   {
      return this.workflowInstance;
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
      this.resources = new ArrayList<Node>(4);
      
      if (this.workflowPackage != null)
      {
         UserTransaction tx = null;
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context, true);
            tx.begin();
            
            // get existing workflow package items
            List<ChildAssociationRef> childRefs = this.nodeService.getChildAssocs(
                  this.workflowPackage, ContentModel.ASSOC_CONTAINS, 
                  RegexQNamePattern.MATCH_ALL);   
            
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
                        // if the node is not in the removed list then add create the 
                        // client side representation and add to the list
                        if (this.packageItemsToRemove == null || 
                            this.packageItemsToRemove.contains(nodeRef.toString()) == false)
                        {
                           createAndAddNode(nodeRef);
                        }
                     }
                  }
                  else
                  {
                     if (logger.isWarnEnabled())
                        logger.warn("Found invalid object in database: id = " + nodeRef + ", type = " + type);
                  }
               }
               else
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Ignoring " + nodeRef + " as it has been removed from the repository");
               }
            }
            
            // now iterate through the items to add list and add them to the list of resources
            if (this.packageItemsToAdd != null)
            {
               for (String newItem : this.packageItemsToAdd)
               {
                  NodeRef nodeRef = new NodeRef(newItem);
                  if (this.nodeService.exists(nodeRef))
                  {
                     // we know the type is correct as this was added as a result of a query
                     // for all content items so just add the item to the resources list
                     createAndAddNode(nodeRef);
                  }
                  else
                  {
                     if (logger.isDebugEnabled())
                        logger.debug("Ignoring " + nodeRef + " as it has been removed from the repository");
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
   
   protected void createAndAddNode(NodeRef nodeRef)
   {
      // create our Node representation
      MapNode node = new MapNode(nodeRef, this.nodeService, true);
      this.browseBean.setupCommonBindingProperties(node);
      
      // add property resolvers to show path information
      node.addPropertyResolver("path", this.browseBean.resolverPath);
      node.addPropertyResolver("displayPath", this.browseBean.resolverDisplayPath);
      
      // add a property resolver to indicate whether the item has been completed or not
//                           node.addPropertyResolver("completed", this.completeResolver);
      
      this.resources.add(node);
   }
   
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
