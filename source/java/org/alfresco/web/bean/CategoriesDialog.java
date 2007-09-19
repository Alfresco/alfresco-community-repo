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
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.service.cmr.search.CategoryService.Mode;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIBreadcrumb;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.repo.component.IRepoBreadcrumbHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing Bean for the Category Management pages.
 * 
 * @author Kevin Roast
 */
public class CategoriesDialog extends BaseDialogBean implements IContextListener
{
   protected CategoriesProperties properties;

   private static final String MSG_CATEGORIES = "categories";

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
   
   public void setProperties(CategoriesProperties properties)
   {
      this.properties = properties;
   }
   
   /**
    * @return Returns the members count for current action category.
    */
   public int getMembers()
   {
      return (properties.getMembers() != null ? properties.getMembers().size() : 0);
   }
   
   /**
    * @param node    Set the Node to be used for the current category screen action.
    */
   @SuppressWarnings("unchecked")
   public void setActionCategory(Node node)
   {
      properties.setActionCategory(node);
      
      if (node != null)
      {
         // setup form properties
         properties.setName(node.getName());
         properties.setDescription((String)node.getProperties().get(ContentModel.PROP_DESCRIPTION));
         properties.setMembers(properties.getCategoryService().getChildren(node.getNodeRef(), Mode.MEMBERS, Depth.ANY));
      }
      else
      {
         properties.setName(null);
         properties.setDescription(null);
         Object emptyCollection = Collections.emptyList();
         properties.setMembers((Collection<ChildAssociationRef>) emptyCollection);
      }
   }
   
   /**
    * @return The currently displayed category as a Node or null if at the root.
    */
   public Node getCurrentCategory()
   {
      if (properties.getCategory() == null)
      {
         if (properties.getCategoryRef() != null)
         {
            properties.setCategory(new Node(properties.getCategoryRef()));
         }
      }
      
      return properties.getCategory();
   }
   
   /**
    * @return The ID of the currently displayed category or null if at the root.
    */
   public String getCurrentCategoryId()
   {
      if (properties.getCategoryRef() != null)
      {
         return properties.getCategoryRef().getId();
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
      properties.setCategoryRef(ref);
      
      // clear current node context
      properties.setCategory(null);
      
      // inform that the UI needs updating after this change 
      contextUpdated();
   }
   
   /**
    * @return Breadcrumb location list
    */
   public List<IBreadcrumbHandler> getLocation()
   {
      if (properties.getLocation() == null)
      {
         List<IBreadcrumbHandler> loc = new ArrayList<IBreadcrumbHandler>(8);
         loc.add(new CategoryBreadcrumbHandler(null,
               Application.getMessage(FacesContext.getCurrentInstance(), MSG_CATEGORIES)));
         
         properties.setLocation(loc);
      }
      return properties.getLocation();
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
         if (properties.getCategoryRef() == null)
         {
            // root categories
            refs = properties.getCategoryService().getCategories(Repository.getStoreRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, Depth.IMMEDIATE);
         }
         else
         {
            // sub-categories of an existing category
            refs = properties.getCategoryService().getChildren(properties.getCategoryRef(), Mode.SUB_CATEGORIES, Depth.IMMEDIATE);
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
    * Change the current view mode based on user selection
    * 
    * @param event ActionEvent
    */
   public void viewModeChanged(ActionEvent event)
   {
      UIModeList viewList = (UIModeList)event.getComponent();
      
      // get the view mode ID
      properties.setViewMode(viewList.getValue().toString());
   }
   
   /**
    * Update the breadcrumb with the clicked category location
    */
   private void updateUILocation(NodeRef ref)
   {
      String name = Repository.getNameForNode(this.nodeService, ref);
      properties.getLocation().add(new CategoryBreadcrumbHandler(ref, name));
      this.setCurrentCategory(ref);
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
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
      properties.setCategory(null);
      
      // force a requery of the richlist dataset
      properties.getCategoriesRichList().setValue(null);
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
   protected class CategoryBreadcrumbHandler implements IRepoBreadcrumbHandler
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
         properties.setLocation((List)breadcrumb.getValue());

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
