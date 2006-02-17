/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
import org.apache.log4j.Logger;

/**
 * Backing Bean for the Category Management pages.
 * 
 * @author Kevin Roast
 */
public class CategoriesBean implements IContextListener
{
   private static final String DEFAULT_OUTCOME = "finish";

   private static final String MSG_CATEGORIES = "categories";

   private static Logger logger = Logger.getLogger(CategoriesBean.class);
   
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
   
   /** Member Count of the linked items of a category */
   private Integer members = null;
   
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
      return (this.members != null ? this.members.intValue() : 0);
   }
   
   /**
    * @return Returns the Node being used for the current action screen.
    */
   public Node getActionCategory()
   {
      return this.actionCategory;
   }
   
   /**
    * @param actionSpace     Set the Node to be used for the current category screen action.
    */
   public void setActionCategory(Node node)
   {
      this.actionCategory = node;
      
      if (node != null)
      {
         // setup form properties
         this.name = node.getName();
         this.description = (String)node.getProperties().get(ContentModel.PROP_DESCRIPTION);
         this.members = this.categoryService.getChildren(node.getNodeRef(), Mode.MEMBERS, Depth.ANY).size();
      }
      else
      {
         this.name = null;
         this.description = null;
         this.members = 0;
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
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         // create category using categoryservice
         NodeRef ref;
         if (categoryRef == null)
         {
            ref = this.categoryService.createRootCategory(Repository.getStoreRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, this.name);
         }
         else
         {
            ref = this.categoryService.createCategory(categoryRef, this.name);
         }
         
         // apply the titled aspect - for description
         Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(1, 1.0f);
         titledProps.put(ContentModel.PROP_DESCRIPTION, this.description);
         this.nodeService.addAspect(ref, ContentModel.ASPECT_TITLED, titledProps);
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable err)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
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
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         // update the category node
         NodeRef nodeRef = getActionCategory().getNodeRef();
         this.nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, this.name);
         
         // apply the titled aspect - for description
         if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TITLED) == false)
         {
            Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(1, 1.0f);
            titledProps.put(ContentModel.PROP_DESCRIPTION, this.description);
            this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, titledProps);
         }
         else
         {
            this.nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, this.description);
         }
         
         // commit the transaction
         tx.commit();
         
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
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
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
         UserTransaction tx = null;
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            // delete the category node using the nodeservice
            NodeRef nodeRef = getActionCategory().getNodeRef();
            this.categoryService.deleteCategory(nodeRef);
            
            // commit the transaction
            tx.commit();
            
            // remove this node from the breadcrumb if required
            List<IBreadcrumbHandler> location = getLocation();
            IBreadcrumbHandler handler = location.get(location.size() - 1);
            
            // see if the current breadcrumb location is our node 
            if ( nodeRef.equals(((IRepoBreadcrumbHandler)handler).getNodeRef()) )
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
            // rollback the transaction
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
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
