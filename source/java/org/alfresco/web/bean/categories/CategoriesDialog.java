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
package org.alfresco.web.bean.categories;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.service.cmr.search.CategoryService.Mode;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.dialog.ChangeViewSupport;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIBreadcrumb;
import org.alfresco.web.ui.common.component.UIListItem;
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
public class CategoriesDialog extends BaseDialogBean implements IContextListener, ChangeViewSupport
{
   private static final long serialVersionUID = -1254971127977205987L;

   public static final String KEY_CATEGORY = "category";
	
   public static final String PARAM_CATEGORY_REF = "categoryRef";
   
   private static final String VIEW_ICONS = "icons";
   private static final String VIEW_DETAILS = "details";
   
   private static final String LABEL_VIEW_ICONS = "category_icons";
   private static final String LABEL_VIEW_DETAILS = "category_details";
   private final static String MSG_CLOSE = "close";
   
   transient private CategoryService categoryService;
   
   /** Members of the linked items of a category */
   private Collection<ChildAssociationRef> members = null;

   /** Currently visible category Node */
   private Node category = null;
   
   /** Current category ref */
   private NodeRef categoryRef = null;
   
   /** Action category node */
   private Node actionCategory = null;
   
   /** RichList view mode */
   private String viewMode = "icons";
   
   /** Component references */
   protected UIRichList categoriesRichList;
   
   /** Category path breadcrumb location */
   private List<IBreadcrumbHandler> location = null;

   private static final String MSG_CATEGORIES = "categories";
   
   /** Dialog properties */
   private String name = null;
   private String description = null;

   private static Log    logger = LogFactory.getLog(CategoriesDialog.class);
   
   // ------------------------------------------------------------------------------
   // Construction
   
   /**
    * Default Constructor
    */
   public CategoriesDialog()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
   }
   
   // ------------------------------------------------------------------------------
   
   /**
    * @return Returns the members count for current action category.
    */
   public int getMembersCount()
   {
      return (getMembers() != null ? getMembers().size() : 0);
   }
   
   public Collection<ChildAssociationRef> getMembers()
   {
       return members;
   }

   public void setMembers(Collection<ChildAssociationRef> members)
   {
       this.members = members;
   }
   
   public Node getActionCategory()
   {
       return actionCategory;
   }

   public String getName()
   {
       return name;
   }

   public void setName(String name)
   {
       this.name = name;
   }
   
   public String getId()
   {
      return getCurrentCategoryId();
   }

   public String getDescription()
   {
       return description;
   }

   public void setDescription(String description)
   {
       this.description = description;
   }
   
   public CategoryService getCategoryService()
   {
       if (categoryService == null)
       {
           categoryService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getCategoryService();
       }
       
       return categoryService;
   }

   public void setCategoryService(CategoryService categoryService)
   {
       this.categoryService = categoryService;
   }
   
   public Node getCategory()
   {
       return category;
   }

   public void setCategory(Node category)
   {
       this.category = category;
   }
   
   public NodeRef getCategoryRef()
   {
       return categoryRef;
   }

   public void setCategoryRef(NodeRef categoryRef)
   {
       this.categoryRef = categoryRef;
   }

   public UIRichList getCategoriesRichList()
   {
       return categoriesRichList;
   }

   public void setCategoriesRichList(UIRichList categoriesRichList)
   {
       this.categoriesRichList = categoriesRichList;
   }
   
   @Override
   public Object getActionsContext()
   {
      return this;
   }
   
   /**
    * @param node    Set the Node to be used for the current category screen action.
    */
   @SuppressWarnings("unchecked")
   public void setActionCategory(Node node)
   {
      this.actionCategory = node;
      
      if (node != null)
      {
         // setup form properties
         setName(node.getName());
         setDescription((String)node.getProperties().get(ContentModel.PROP_DESCRIPTION));
         setMembers(getCategoryService().getChildren(node.getNodeRef(), Mode.MEMBERS, Depth.ANY));
      }
      else
      {
         setName(null);
         setDescription(null);
         Object emptyCollection = Collections.emptyList();
         setMembers((Collection<ChildAssociationRef>) emptyCollection);
      }
   }
   
   /**
    * @return The currently displayed category as a Node or null if at the root.
    */
   public Node getCurrentCategory()
   {
      if (getCategory() == null)
      {
         if (getCategoryRef() != null)
         {
            setCategory(new Node(getCategoryRef()));
         }
      }
      
      return getCategory();
   }
   
   /**
    * @return The ID of the currently displayed category or null if at the root.
    */
   public String getCurrentCategoryId()
   {
      if (getCategoryRef() != null)
      {
         return getCategoryRef().getId();
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
      setCategoryRef(ref);
      
      // clear current node context
      setCategory(null);
      
      // inform that the UI needs updating after this change 
      contextUpdated();
   }
   
   public void setLocation(List<IBreadcrumbHandler> location)
   {
       this.location = location;
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
         
         setLocation(loc);
      }
      return this.location;
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
         if (getCategoryRef() == null)
         {
            // root categories
            refs = getCategoryService().getCategories(Repository.getStoreRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, Depth.IMMEDIATE);
         }
         else
         {
            // sub-categories of an existing category
            refs = getCategoryService().getChildren(getCategoryRef(), Mode.SUB_CATEGORIES, Depth.IMMEDIATE);
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
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {refErr.getNodeRef()}));
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
                  FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {id}));
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
    * Reset the current category navigation point - e.g. ready for redisplay of the root UI
    */
   public void resetCategoryNavigation(ActionEvent event)
   {
      setCurrentCategory(null);
      this.location = null;
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
                  FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] { id }));
         }
      }
   }
   
   /**
    * Update the breadcrumb with the clicked category location
    */
   private void updateUILocation(NodeRef ref)
   {
      String name = Repository.getNameForNode(getNodeService(), ref);
      getLocation().add(new CategoryBreadcrumbHandler(ref, name));
      this.setCurrentCategory(ref);
   }
   
   /**
    * If category.equals(handler.label) then the breadcrumb reverts one step back
    * (needed for deleting)
    * Else current breadcrumb is updated accordingly to the current category
    * (needed for editing)
    */
   protected void removeFromBreadcrumb(String category)
   {
      // remove this node from the breadcrumb if required
      List<IBreadcrumbHandler> location = getLocation();
      CategoryBreadcrumbHandler handler = (CategoryBreadcrumbHandler) location.get(location.size() - 1);
      
      // see if the current breadcrumb location is our Category
      if (category.equals(handler.label))
      {
         location.remove(location.size() - 1);
         
         // now work out which Category to set the list to refresh against
         if (location.size() != 0)
         {
            handler = (CategoryBreadcrumbHandler) location.get(location.size() - 1);
            this.setCurrentCategory(handler.nodeRef); 
         }
      }
      else 
      {
          handler=new CategoryBreadcrumbHandler (getCategory().getNodeRef(), Repository.getNameForNode(getNodeService(), getCategory().getNodeRef()));
          location.set(location.size() - 1, handler);
          this.setCurrentCategory(handler.nodeRef); 
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
   }
   
   public String getContainerSubTitle()
   {
      if (getCurrentCategoryId() != null)
      {
         return getCurrentCategory().getName();
      }
      
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CATEGORIES);
   }
   
   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
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
      setCategory(null);
      
      // force a requery of the richlist dataset
      if (this.categoriesRichList != null)
      {
         this.categoriesRichList.setValue(null);
      }
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
   public class CategoryBreadcrumbHandler implements IRepoBreadcrumbHandler
   {
      private static final long serialVersionUID = 3831234653171036630L;
      
      /**
       * Constructor
       * 
       * @param NodeRef The NodeRef for this browse navigation element
       * @param label Element label
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
         setLocation((List)breadcrumb.getValue());

         return null;
      }
      
      public NodeRef getNodeRef()
      {
         return this.nodeRef;
      }
      
      private NodeRef nodeRef;
      private String label;
   }

   public List<UIListItem> getViewItems()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      List<UIListItem> items = new ArrayList<UIListItem>(2);
      
      UIListItem item1 = new UIListItem();
      item1.setValue(VIEW_ICONS);
      item1.setLabel(Application.getMessage(context, LABEL_VIEW_ICONS));
      items.add(item1);
      
      UIListItem item2 = new UIListItem();
      item2.setValue(VIEW_DETAILS);
      item2.setLabel(Application.getMessage(context, LABEL_VIEW_DETAILS));
      items.add(item2);
      
      return items;
   }
   
   @Override
   public void restored()
   {
	   Object categoryToRemove = FacesContext.getCurrentInstance().getExternalContext().
       getRequestMap().get(KEY_CATEGORY);
	   if (categoryToRemove != null)
	   	{
		   	if (logger.isDebugEnabled())
		   		logger.debug("Removing group '" + categoryToRemove + "' from breadcrumb");
    
		   	removeFromBreadcrumb((String)categoryToRemove);
	   	}
	   contextUpdated();
   }
   
   public String getViewMode()
   {
       return viewMode;
   }

   public void setViewMode(String viewMode)
   {
       this.viewMode = viewMode;
   }
   
   /**
    * Change the current view mode based on user selection
    * 
    * @param event ActionEvent
    */
   public void viewModeChanged(ActionEvent event)
   {
      UIModeList viewList = (UIModeList)event.getComponent();
      
      // get the view mode ID
      setViewMode(viewList.getValue().toString());
   }
   
   
   
   @Override
   public String getMoreActionsConfigId() 
   {
	if(getCurrentCategoryId() != null)
	{
		return "category_more_actions";
	}
	return null;
   }
   
   public NodeRef getNodeRef()
   {
	   return getCurrentCategory().getNodeRef();
   }
}
