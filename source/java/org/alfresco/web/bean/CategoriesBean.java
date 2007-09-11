/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.service.cmr.search.CategoryService.Mode;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIBreadcrumb;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.alfresco.web.ui.repo.component.IRepoBreadcrumbHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing Bean for the Category Management pages.
 * 
 * @author Kevin Roast
 */
public class CategoriesBean implements IContextListener
{
   private static final String DEFAULT_OUTCOME = "finish";

   private static final String MSG_CATEGORIES = "categories";

   private static Log    logger = LogFactory.getLog(CategoriesBean.class);
   
   /** The NodeService to be used by the bean */
   protected NodeService nodeService;
   
   protected CategoryService categoryService;
   
   /** Component references */
   protected UIRichList categoriesRichList;
   
   /** Currently visible category Node*/
   private Node category = null;
   
   /** Current category ref */
   private NodeRef categoryRef = null;
   
   /** Action category node */
   private Node actionCategory = null;
      
   /** Members of the linked items of a category */
   private Collection<ChildAssociationRef> members = null;
   
   /** Dialog properties */
   private String name = null;
   private String description = null;
   
   /** RichList view mode */
   private String viewMode = "icons"; 
   
   /** Category path breadcrumb location */
   private List<IBreadcrumbHandler> location = null;
   
   
   // ------------------------------------------------------------------------------
   // Construction 
   
   /**
    * Default Constructor
    */
   public CategoriesBean()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param nodeService      The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   /**
    * @param categoryService  The CategoryService to set.
    */
   public void setCategoryService(CategoryService categoryService)
   {
      this.categoryService = categoryService;
   }
   
   /**
    * @param list    The categories RichList to set.
    */
   public void setCategoriesRichList(UIRichList list)
   {
      this.categoriesRichList = list;
   }
   
   /**
    * @return Returns the categories RichList to set.
    */
   public UIRichList getCategoriesRichList()
   {
      return this.categoriesRichList;
   }
   
   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      return this.description;
   }

   /**
    * @param description The description to set.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }
   
   /**
    * @return Returns the viewMode.
    */
   public String getViewMode()
   {
      return this.viewMode;
   }

   /**
    * @param viewMode The viewMode to set.
    */
   public void setViewMode(String viewMode)
   {
      this.viewMode = viewMode;
   }

   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * @param name The name to set.
    */
   public void setName(String name)
   {
      this.name = name;
   }
   
   /**
    * @return Returns the members count for current action category.
    */
   public int getMembers()
   {
      return (this.members != null ? this.members.size() : 0);
   }
   
   /**
    * @return Returns the Node being used for the current action screen.
    */
   public Node getActionCategory()
   {
      return this.actionCategory;
   }
   
   /**
    * @param node     Set the Node to be used for the current category screen action.
    */
   public void setActionCategory(Node node)
   {
      this.actionCategory = node;
      
      if (node != null)
      {
         // setup form properties
         this.name = node.getName();
         this.description = (String)node.getProperties().get(ContentModel.PROP_DESCRIPTION);
         this.members = this.categoryService.getChildren(node.getNodeRef(), Mode.MEMBERS, Depth.ANY);
      }
      else
      {
         this.name = null;
         this.description = null;
         this.members = Collections.emptyList();
      }
   }
   
   /**
    * @return The currently displayed category as a Node or null if at the root.
    */
   public Node getCurrentCategory()
   {
      if (this.category == null)
      {
         if (this.categoryRef != null)
         {
            this.category = new Node(this.categoryRef);
         }
      }
      
      return this.category;
   }
   
   /**
    * @return The ID of the currently displayed category or null if at the root.
    */
   public String getCurrentCategoryId()
   {
      if (this.categoryRef != null)
      {
         return categoryRef.getId();
      }
      else
      {
         return null;
      }
   }
   
   /**
    * Set the current category node.
    * <p>
    * Setting this value causes the UI to update and display the specified node as current.
    * 
    * @param ref     The current category node.
    */
   public void setCurrentCategory(NodeRef ref)
   {
      if (logger.isDebugEnabled())
         logger.debug("Setting current category: " + ref);
      
      // set the current NodeRef for our UI context operations
      this.categoryRef = ref;
      
      // clear current node context
      this.category = null;
      
      // inform that the UI needs updating after this change 
      contextUpdated();
   }
   
   /**
    * @return Breadcrumb location list
    */
   public List<IBreadcrumbHandler> getLocation()
   {
      if (this.location == null)
      {
         List<IBreadcrumbHandler> loc = new ArrayList<IBreadcrumbHandler>(8);
         loc.add(new CategoryBreadcrumbHandler(null,
               Application.getMessage(FacesContext.getCurrentInstance(), MSG_CATEGORIES)));
         
         this.location = loc;
      }
      return this.location;
   }
   
   /**
    * @param location Breadcrumb location list
    */
   public void setLocation(List<IBreadcrumbHandler> location)
   {
      this.location = location;
   }
   
   /**
    * @return The list of categories Nodes to display. Returns the list root categories or the
    *         list of sub-categories for the current category if set.
    */
   public List<Node> getCategories()
   {
      List<Node> categories;
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         Collection<ChildAssociationRef> refs;
         if (this.categoryRef == null)
         {
            // root categories
            refs = this.categoryService.getCategories(Repository.getStoreRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, Depth.IMMEDIATE);
         }
         else
         {
            // sub-categories of an existing category
            refs = this.categoryService.getChildren(this.categoryRef, Mode.SUB_CATEGORIES, Depth.IMMEDIATE);
         }
         categories = new ArrayList<Node>(refs.size());
         for (ChildAssociationRef child : refs)
         {
            Node categoryNode = new Node(child.getChildRef());
            // force early props init within transaction
            categoryNode.getProperties();
            categories.add(categoryNode);
         }
         
         // commit the transaction
         tx.commit();
      }
      catch (InvalidNodeRefException refErr)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {refErr.getNodeRef()}) );
         categories = Collections.<Node>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         categories = Collections.<Node>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      
      return categories;
   }
   
   /**
    * Set the Category to be used for next action dialog
    */
   public void setupCategoryAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         if (logger.isDebugEnabled())
            logger.debug("Setup for action, setting current Category to: " + id);
         
         try
         {
            // create the node ref, then our node representation
            NodeRef ref = new NodeRef(Repository.getStoreRef(), id);
            Node node = new Node(ref);
            
            // prepare a node for the action context
            setActionCategory(node);
            
            // clear datalist cache ready from return from action dialog
            contextUpdated();
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {id}) );
         }
      }
   }
   
   /**
    * Clear the category action context - e.g. ready for a Create operation
    */
   public void clearCategoryAction(ActionEvent event)
   {
      setActionCategory(null);
      
      // clear datalist cache ready from return from action dialog
      contextUpdated();
   }
   
   /**
    * Action called when a category folder is clicked.
    * Navigate into the category.
    */
   public void clickCategory(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         try
         {
            NodeRef ref = new NodeRef(Repository.getStoreRef(), id);
            
            // refresh UI based on node selection
            updateUILocation(ref);
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {id}) );
         }
      }
   }
   
   /**
    * Action handler called on Create Category finish button click.
    */
   public String finishCreate()
   {
      String outcome = DEFAULT_OUTCOME;
      
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
         RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
         {
            public Object execute() throws Throwable
            {
               // create category using categoryservice
               NodeRef ref;
               if (categoryRef == null)
               {
                  ref = categoryService.createRootCategory(Repository.getStoreRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, name.trim());
               }
               else
               {
                  ref = categoryService.createCategory(categoryRef, name.trim());
               }
               
               // apply the titled aspect - for description
               Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(1, 1.0f);
               titledProps.put(ContentModel.PROP_DESCRIPTION, description);
               nodeService.addAspect(ref, ContentModel.ASPECT_TITLED, titledProps);
               return null;
            }
         };
         txnHelper.doInTransaction(callback);
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         outcome = null;
      }
      
      return outcome;
   }
   
   /**
    * Action handler called on Edit Category finish button click.
    */
   public String finishEdit()
   {
      String outcome = DEFAULT_OUTCOME;
      
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
         RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>()
         {
            public NodeRef execute() throws Throwable
            {
               // update the category node
               NodeRef nodeRef = getActionCategory().getNodeRef();
               nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, name);
               
               // apply the titled aspect - for description
               if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TITLED) == false)
               {
                  Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(1, 1.0f);
                  titledProps.put(ContentModel.PROP_DESCRIPTION, description);
                  nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, titledProps);
               }
               else
               {
                  nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, description);
               }
               return nodeRef;
            }
         };
         NodeRef nodeRef = txnHelper.doInTransaction(callback);
         
         // edit the node in the breadcrumb if required
         List<IBreadcrumbHandler> location = getLocation();
         IBreadcrumbHandler handler = location.get(location.size() - 1);
         
         // see if the current breadcrumb location is our node 
         if ( nodeRef.equals(((IRepoBreadcrumbHandler)handler).getNodeRef()) )
         {
            // and update with the modified node details
            IBreadcrumbHandler newHandler = new CategoryBreadcrumbHandler(
                  nodeRef, Repository.getNameForNode(nodeService, nodeRef));
            location.set(location.size() - 1, newHandler);
         }
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         outcome = null;
      }
      
      return outcome;
   }
   
   /**
    * Action handler called on Delete Category finish button click.
    */
   public String finishDelete()
   {
      String outcome = DEFAULT_OUTCOME;
      
      if (getActionCategory() != null)
      {
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
            RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>()
            {
               public NodeRef execute() throws Throwable
               {
                  // delete the category node using the nodeservice
                  NodeRef categoryNodeRef = getActionCategory().getNodeRef();
                  categoryService.deleteCategory(categoryNodeRef);
                  
                  // if there are other items in the repository using this category
                  // all the associations to the category should be removed too
                  if (members != null && members.size() > 0)
                  {
                     for (ChildAssociationRef childRef : members)
                     {
                        List<NodeRef> list = new ArrayList<NodeRef>(members.size());
                        
                        NodeRef member = childRef.getChildRef();
                        Collection<NodeRef> categories = (Collection<NodeRef>)nodeService.
                              getProperty(member, ContentModel.PROP_CATEGORIES);

                        for (NodeRef category : categories)
                        {
                           if (category.equals(categoryNodeRef) == false)
                           {
                              list.add(category);
                           }
                        }
                        
                        // persist the list back to the repository
                        nodeService.setProperty(member, ContentModel.PROP_CATEGORIES, (Serializable)list);
                     }
                  }
                  return categoryNodeRef;
               }
            };
            NodeRef categoryNodeRef = txnHelper.doInTransaction(callback);
            
            // remove this node from the breadcrumb if required
            List<IBreadcrumbHandler> location = getLocation();
            IBreadcrumbHandler handler = location.get(location.size() - 1);
            
            // see if the current breadcrumb location is our node 
            if ( categoryNodeRef.equals(((IRepoBreadcrumbHandler)handler).getNodeRef()) )
            {
               location.remove(location.size() - 1);
               
               // now work out which node to set the list to refresh against
               if (location.size() != 0)
               {
                  handler = location.get(location.size() - 1);
                  this.setCurrentCategory(((IRepoBreadcrumbHandler)handler).getNodeRef());
               }
            }
            
            // clear action context
            setActionCategory(null);
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
            outcome = null;
         }
      }
      
      return outcome;
   }
   
   /**
    * Change the current view mode based on user selection
    * 
    * @param event      ActionEvent
    */
   public void viewModeChanged(ActionEvent event)
   {
      UIModeList viewList = (UIModeList)event.getComponent();
      
      // get the view mode ID
      setViewMode(viewList.getValue().toString());
   }
   
   /**
    * Update the breadcrumb with the clicked category location
    */
   private void updateUILocation(NodeRef ref)
   {
      String name = Repository.getNameForNode(this.nodeService, ref);
      this.location.add(new CategoryBreadcrumbHandler(ref, name));
      this.setCurrentCategory(ref);
   }
   
   
   // ------------------------------------------------------------------------------
   // IContextListener implementation 
   
   /**
    * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
    */
   public void contextUpdated()
   {
      if (logger.isDebugEnabled())
         logger.debug("Invalidating Category Management Components...");
      
      // force a requery of the current category ref properties
      this.category = null;
      
      // force a requery of the richlist dataset
      this.categoriesRichList.setValue(null);
   }
   
   /**
    * @see org.alfresco.web.app.context.IContextListener#areaChanged()
    */
   public void areaChanged()
   {
      // nothing to do
   }

   /**
    * @see org.alfresco.web.app.context.IContextListener#spaceChanged()
    */
   public void spaceChanged()
   {
      // nothing to do
   }
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class to handle breadcrumb interaction for Categories pages
    */
   private class CategoryBreadcrumbHandler implements IRepoBreadcrumbHandler
   {
      private static final long serialVersionUID = 3831234653171036630L;
      
      /**
       * Constructor
       * 
       * @param NodeRef    The NodeRef for this browse navigation element
       * @param label      Element label
       */
      public CategoryBreadcrumbHandler(NodeRef nodeRef, String label)
      {
         this.label = label;
         this.nodeRef = nodeRef;
      }
      
      /**
       * @see java.lang.Object#toString()
       */
      public String toString()
      {
         return this.label;
      }

      /**
       * @see org.alfresco.web.ui.common.component.IBreadcrumbHandler#navigationOutcome(org.alfresco.web.ui.common.component.UIBreadcrumb)
       */
      @SuppressWarnings("unchecked")
      public String navigationOutcome(UIBreadcrumb breadcrumb)
      {
         // All category breadcrumb elements relate to a Categiry Node Id
         // when selected we set the current category Id and return
         setCurrentCategory(this.nodeRef);
         setLocation( (List)breadcrumb.getValue() );
         
         return null;
      }
      
      public NodeRef getNodeRef()
      {
         return this.nodeRef;
      }
      
      private NodeRef nodeRef;
      private String label;
   }
}
