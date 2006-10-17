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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.GuestTemplateContentServlet;
import org.alfresco.web.app.servlet.TemplateContentServlet;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;

/**
 * Backing bean provided access to the details of a Space
 * 
 * @author Kevin Roast
 */
public class SpaceDetailsBean extends BaseDetailsBean
{
   private static final String MSG_HAS_FOLLOWING_CATEGORIES = "has_following_categories_space";
   private static final String MSG_NO_CATEGORIES_APPLIED = "no_categories_applied_space";
   private static final String MSG_ERROR_UPDATE_CATEGORY = "error_update_category";
   private static final String MSG_ERROR_ASPECT_CLASSIFY = "error_aspect_classify_space";

   /** PermissionService bean reference */
   protected PermissionService permissionService;
   
   /** Category details */
   private NodeRef addedCategory;
   private List categories;
   
   /** RSS Template ID */
   private String rssTemplate;
   
   
   // ------------------------------------------------------------------------------
   // Construction
   
   /**
    * Default constructor
    */
   public SpaceDetailsBean()
   {
      super();
      
      // initial state of some panels that don't use the default
      panels.put("rules-panel", false);
      panels.put("dashboard-panel", false);
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param permissionService      The PermissionService to set.
    */
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
   
   /**
    * Returns the Node this bean is currently representing
    * 
    * @return The Node
    */
   public Node getNode()
   {
      return this.browseBean.getActionSpace();
   }
   
   /**
    * Returns the Space this bean is currently representing
    * 
    * @return The Space Node
    */
   public Node getSpace()
   {
      return getNode();
   }
   
   /**
    * Resolve the actual document Node from any Link object that may be proxying it
    * 
    * @return current document Node or document Node resolved from any Link object
    */
   protected Node getLinkResolvedNode()
   {
      Node space = getSpace();
      if (ContentModel.TYPE_FOLDERLINK.equals(space.getType()))
      {
         NodeRef destRef = (NodeRef)space.getProperties().get(ContentModel.PROP_LINK_DESTINATION);
         if (nodeService.exists(destRef))
         {
            space = new Node(destRef);
         }
      }
      return space;
   }
   
   /**
    * Returns a model for use by a template on the Space Details page.
    * 
    * @return model containing current current space info.
    */
   @SuppressWarnings("unchecked")
   public Map getTemplateModel()
   {
      HashMap model = new HashMap(1, 1.0f);
      
      FacesContext fc = FacesContext.getCurrentInstance();
      TemplateNode spaceNode = new TemplateNode(getSpace().getNodeRef(),
              Repository.getServiceRegistry(fc), imageResolver);
      model.put("space", spaceNode);
      
      return model;
   }
   
   /**
    * @see org.alfresco.web.bean.BaseDetailsBean#getPropertiesPanelId()
    */
   protected String getPropertiesPanelId()
   {
      return "space-props";
   }
   
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Navigates to next item in the list of Spaces
    */
   public void nextItem(ActionEvent event)
   {
      boolean foundNextItem = false;
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         List<Node> nodes = this.browseBean.getNodes();
         if (nodes.size() > 1)
         {
            // perform a linear search - this is slow but stateless
            // otherwise we would have to manage state of last selected node
            // this gets very tricky as this bean is instantiated once and never
            // reset - it does not know when the document has changed etc.
            for (int i=0; i<nodes.size(); i++)
            {
               if (id.equals(nodes.get(i).getId()) == true)
               {
                  Node next;
                  // found our item - navigate to next
                  if (i != nodes.size() - 1)
                  {
                     next = nodes.get(i + 1);
                  }
                  else
                  {
                     // handle wrapping case
                     next = nodes.get(0);
                  }
                  
                  // prepare for showing details for this node
                  this.browseBean.setupSpaceAction(next.getId(), false);
                  
                  // we found a next item
                  foundNextItem = true;
               }
            }
         }
         
         // if we did not find a next item make sure the current node is 
         // in the dispatch context otherwise the details screen will go back 
         // to the default one.
         if (foundNextItem == false)
         {
            NodeRef currNodeRef = new NodeRef(Repository.getStoreRef(), id);
            Node currNode = new Node(currNodeRef);
            this.navigator.setupDispatchContext(currNode);
         }
      }
   }
   
   /**
    * Navigates to the previous item in the list Spaces
    */
   public void previousItem(ActionEvent event)
   {
      boolean foundPreviousItem = false;
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         List<Node> nodes = this.browseBean.getNodes();
         if (nodes.size() > 1)
         {
            // see above
            for (int i=0; i<nodes.size(); i++)
            {
               if (id.equals(nodes.get(i).getId()) == true)
               {
                  Node previous;
                  // found our item - navigate to previous
                  if (i != 0)
                  {
                     previous = nodes.get(i - 1);
                  }
                  else
                  {
                     // handle wrapping case
                     previous = nodes.get(nodes.size() - 1);
                  }
                  
                  // show details for this node
                  this.browseBean.setupSpaceAction(previous.getId(), false);
                  
                  // we found a next item
                  foundPreviousItem = true;
               }
            }
         }
         
         // if we did not find a previous item make sure the current node is 
         // in the dispatch context otherwise the details screen will go back 
         // to the default one.
         if (foundPreviousItem == false)
         {
            NodeRef currNodeRef = new NodeRef(Repository.getStoreRef(), id);
            Node currNode = new Node(currNodeRef);
            this.navigator.setupDispatchContext(currNode);
         }
      }
   }
   
   /**
    * Action handler to clear the current Space properties before returning to the browse screen,
    * as the user may have modified the properties! 
    */
   public String closeDialog()
   {
      this.navigator.resetCurrentNodeProperties();
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }
   
   // ------------------------------------------------------------------------------
   // Categorised Details
   
   /**
    * Determines whether the current space has any categories applied
    * 
    * @return true if the document has categories attached
    */
   public boolean isCategorised()
   {
      return getSpace().hasAspect(ContentModel.ASPECT_GEN_CLASSIFIABLE);
   }
   
   /**
    * Returns a list of objects representing the categories applied to the 
    * current space
    *  
    * @return List of categories
    */
   public String getCategoriesOverviewHTML()
   {
      String html = null;
      
      if (isCategorised())
      {
         // we know for now that the general classifiable aspect only will be
         // applied so we can retrive the categories property direclty
         Collection<NodeRef> categories = (Collection<NodeRef>)this.nodeService.getProperty(
                 getSpace().getNodeRef(), ContentModel.PROP_CATEGORIES);
         
         if (categories == null || categories.size() == 0)
         {
            html = Application.getMessage(FacesContext.getCurrentInstance(), MSG_NO_CATEGORIES_APPLIED);
         }
         else
         {
            StringBuilder builder = new StringBuilder(Application.getMessage(FacesContext.getCurrentInstance(), 
                  MSG_HAS_FOLLOWING_CATEGORIES));
            
            builder.append("<ul>");
            for (NodeRef ref : categories)
            {
               if (this.nodeService.exists(ref))
               {
                  builder.append("<li>");
                  builder.append(Repository.getNameForNode(this.nodeService, ref));
                  builder.append("</li>");
               }
            }
            builder.append("</ul>");
            
            html = builder.toString();
         }
      }
      
      return html;
   }

   /**
    * Event handler called to setup the categories for editing
    * 
    * @param event The event
    */
   public void setupCategoriesForEdit(ActionEvent event)
   {
      this.categories = (List)this.nodeService.getProperty(getSpace().getNodeRef(), 
               ContentModel.PROP_CATEGORIES);
   }
   
   /**
    * Returns a Map of the initial categories on the node keyed by the NodeRef
    * 
    * @return Map of initial categories
    */
   public List getCategories()
   {
      return this.categories;
   }
   
   /**
    * Sets the categories Map
    * 
    * @param categories
    */
   public void setCategories(List categories)
   {
      this.categories = categories;
   }
   
   /**
    * Returns the last category added from the multi value editor
    * 
    * @return The last category added
    */
   public NodeRef getAddedCategory()
   {
      return this.addedCategory;
   }

   /**
    * Sets the category added from the multi value editor
    * 
    * @param addedCategory The added category
    */
   public void setAddedCategory(NodeRef addedCategory)
   {
      this.addedCategory = addedCategory;
   }
   
   /**
    * Updates the categories for the current document
    *  
    * @return The outcome
    */
   public String saveCategories()
   {
      String outcome = "cancel";
      
      UserTransaction tx = null;
      
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         // firstly retrieve all the properties for the current node
         Map<QName, Serializable> updateProps = this.nodeService.getProperties(
        		 getSpace().getNodeRef());
         
         // create a node ref representation of the selected id and set the new properties
         updateProps.put(ContentModel.PROP_CATEGORIES, (Serializable)this.categories);
         
         // set the properties on the node
         this.nodeService.setProperties(getSpace().getNodeRef(), updateProps);
         
         // commit the transaction
         tx.commit();
         
         // reset the state of the current document so it reflects the changes just made
         getSpace().reset();
         
         outcome = "finish";
      }
      catch (Throwable e)
      {
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_UPDATE_CATEGORY), e.getMessage()), e);
      }
      
      return outcome;
   }
   
   /**
    * Applies the classifiable aspect to the current document
    */
   public void applyClassifiable()
   {
      UserTransaction tx = null;
      
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         // add the general classifiable aspect to the node
         this.nodeService.addAspect(getSpace().getNodeRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, null);
         
         // commit the transaction
         tx.commit();
         
         // reset the state of the current document
         getSpace().reset();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_ASPECT_CLASSIFY), e.getMessage()), e);
      }
   }
   
   /**
    * Returns whether the current sapce is locked
    * 
    * @return true if the document is checked out
    */
   public boolean isLocked()
   {
      return getSpace().isLocked();
   }
   
   /**
    * @return true if the current space has an RSS feed applied
    */
   public boolean isRSSFeed()
   {
      return hasRSSFeed(getSpace());
   }
   
   /**
    * @return true if the current space has an RSS feed applied
    */
   public static boolean hasRSSFeed(Node space)
   {
      return (space.hasAspect(ContentModel.ASPECT_FEEDSOURCE) &&
              space.getProperties().get(ContentModel.PROP_FEEDTEMPLATE) != null);
   }
   
   /**
    * @return RSS Feed URL for the current space
    */
   public String getRSSFeedURL()
   {
      return buildRSSFeedURL(getSpace());
   }
   
   /**
    * Build URL for an RSS space based on the 'feedsource' aspect property.
    *  
    * @param space  Node to build RSS template URL for
    *  
    * @return URL for the RSS feed for a space
    */
   public static String buildRSSFeedURL(Node space)
   {
      // build RSS feed template URL from selected template and the space NodeRef and
      // add the guest=true URL parameter - this is required for no login access and
      // add the mimetype=text/xml URL parameter - required to return correct stream type
      return GuestTemplateContentServlet.generateURL(space.getNodeRef(),
                (NodeRef)space.getProperties().get(ContentModel.PROP_FEEDTEMPLATE))
                    + "/rss.xml?mimetype=text%2Fxml";
   }

   /**
    * @return Returns the current RSS Template ID.
    */
   public String getRSSTemplate()
   {
      // return current template if it exists
      NodeRef ref = (NodeRef)getNode().getProperties().get(ContentModel.PROP_FEEDTEMPLATE);
      return ref != null ? ref.getId() : this.rssTemplate;
   }

   /**
    * @param rssTemplate The RSS Template Id to set.
    */
   public void setRSSTemplate(String rssTemplate)
   {
      this.rssTemplate = rssTemplate;
   }
   
   /**
    * Action handler to apply the selected RSS Template and FeedSource aspect to the current Space
    */
   public void applyRSSTemplate(ActionEvent event)
   {
      if (this.rssTemplate != null && this.rssTemplate.equals(TemplateSupportBean.NO_SELECTION) == false)
      {
         try
         {
            // apply the feedsource aspect if required 
            if (getNode().hasAspect(ContentModel.ASPECT_FEEDSOURCE) == false)
            {
               this.nodeService.addAspect(getNode().getNodeRef(), ContentModel.ASPECT_FEEDSOURCE, null);
            }
            
            // get the selected template Id from the Template Picker
            NodeRef templateRef = new NodeRef(Repository.getStoreRef(), this.rssTemplate);
            
            // set the template NodeRef into the templatable aspect property
            this.nodeService.setProperty(getNode().getNodeRef(), ContentModel.PROP_FEEDTEMPLATE, templateRef); 
            
            // reset node details for next refresh of details page
            getNode().reset();
         }
         catch (Exception e)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
         }
      }
   }
   
   /**
    * Action handler to remove a RSS template from the current Space
    */
   public void removeRSSTemplate(ActionEvent event)
   {
      try
      {
         // clear template property
         this.nodeService.setProperty(getNode().getNodeRef(), ContentModel.PROP_FEEDTEMPLATE, null);
         this.nodeService.removeAspect(getNode().getNodeRef(), ContentModel.ASPECT_FEEDSOURCE);
         
         // reset node details for next refresh of details page
         getNode().reset();
      }
      catch (Exception e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
      }
   }
}
