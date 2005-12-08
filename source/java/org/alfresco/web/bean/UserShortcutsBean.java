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
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.repo.component.shelf.UIShortcutsShelfItem;
import org.apache.log4j.Logger;

/**
 * This bean manages the user defined list of Recent Spaces in the Shelf component.
 * 
 * @author Kevin Roast
 */
public class UserShortcutsBean
{
   private static Logger logger = Logger.getLogger(UserShortcutsBean.class);
   
   /** The NodeService to be used by the bean */
   private NodeService nodeService;
   
   /** The BrowseBean reference */
   private BrowseBean browseBean;
   
   /** List of shortcut nodes */
   private List<Node> shortcuts = null;
   
   private QName QNAME_SHORTCUTS = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "shortcuts");
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters
   
   /**
    * @param nodeService The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @param browseBean The BrowseBean to set.
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }
   
   /**
    * @return the List of shortcut Nodes
    */
   public List<Node> getShortcuts()
   {
      if (this.shortcuts == null)
      {
         UserTransaction tx = null;
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            // get the shortcuts from the preferences for this user
            NodeRef prefRef = getShortcutsNodeRef();
            List<String> shortcuts = (List<String>)this.nodeService.getProperty(prefRef, QNAME_SHORTCUTS);
            if (shortcuts != null)
            {
               // each shortcut node ID is persisted as a list item in a well known property
               this.shortcuts = new ArrayList<Node>(shortcuts.size());
               for (int i=0; i<shortcuts.size(); i++)
               {
                  NodeRef ref = new NodeRef(Repository.getStoreRef(), shortcuts.get(i));
                  if (this.nodeService.exists(ref) == true)
                  {
                     Node node = new Node(ref);
                     
                     // quick init properties while in the usertransaction
                     node.getProperties();
                     
                     // save ref to the Node for rendering
                     this.shortcuts.add(node);
                  }
                  else
                  {
                     // ignore this shortcut node - no longer exists in the system!
                     // we write the node list back again afterwards to correct this
                     if (logger.isDebugEnabled())
                        logger.debug("Found invalid shortcut node Id: " + ref.getId());
                  }
               }
               
               // if the count of accessable shortcuts is different to our original list then
               // write the valid shortcut IDs back to correct invalid node refs
               if (this.shortcuts.size() != shortcuts.size())
               {
                  shortcuts = new ArrayList<String>(this.shortcuts.size());
                  for (int i=0; i<this.shortcuts.size(); i++)
                  {
                     shortcuts.add(this.shortcuts.get(i).getId());
                  }
                  this.nodeService.setProperty(prefRef, QNAME_SHORTCUTS, (Serializable)shortcuts);
               }
            }
            else
            {
               this.shortcuts = new ArrayList<Node>(5);
            }
            
            tx.commit();
         }
         catch (Exception err)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         }
      }
      
      return this.shortcuts;
   }
   
   /**
    * @param spaces     List of shortcuts Nodes
    */
   public void setShortcuts(List<Node> nodes)
   {
      this.shortcuts = nodes;
   }
   
   
   // ------------------------------------------------------------------------------
   // Action method handlers
   
   /**
    * Action handler called when a new shortcut is to be added to the list
    */
   public void createShortcut(ActionEvent event)
   {
      // TODO: add this action to the Details screen for Space and Document
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         try
         {
            NodeRef ref = new NodeRef(Repository.getStoreRef(), id);
            Node node = new Node(ref);
            
            boolean foundShortcut = false;
            for (int i=0; i<getShortcuts().size(); i++)
            {
               if (node.getId().equals(getShortcuts().get(i).getId()))
               {
                  // found same node already in the list - so we don't need to add it again
                  foundShortcut = true;
                  break;
               }
            }
            
            if (foundShortcut == false)
            {
               // add to persistent store
               UserTransaction tx = null;
               try
               {
                  FacesContext context = FacesContext.getCurrentInstance();
                  tx = Repository.getUserTransaction(context);
                  tx.begin();
                  
                  NodeRef prefRef = getShortcutsNodeRef();
                  List<String> shortcuts = (List<String>)this.nodeService.getProperty(prefRef, QNAME_SHORTCUTS);
                  if (shortcuts == null)
                  {
                     shortcuts = new ArrayList<String>(1);
                  }
                  shortcuts.add(node.getNodeRef().getId());
                  this.nodeService.setProperty(prefRef, QNAME_SHORTCUTS, (Serializable)shortcuts);
                  
                  // commit the transaction
                  tx.commit();
                  
                  // add our new shortcut Node to the in-memory list
                  getShortcuts().add(node);
                  
                  if (logger.isDebugEnabled())
                     logger.debug("Added node: " + node.getName() + " to the user shortcuts list.");
               }
               catch (Exception err)
               {
                  Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                        FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
                  try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
               }
            }
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {id}) );
         }
      }
   }
   
   /**
    * Get the node we need to store our user preferences
    */
   private NodeRef getShortcutsNodeRef()
   {
      return Application.getCurrentUser(FacesContext.getCurrentInstance()).getUserPreferencesRef();
   }
   
   /**
    * Action handler bound to the user shortcuts Shelf component called when a node is removed
    */
   public void removeShortcut(ActionEvent event)
   {
      UIShortcutsShelfItem.ShortcutEvent shortcutEvent = (UIShortcutsShelfItem.ShortcutEvent)event;
      
      // remove from persistent store
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         NodeRef prefRef = getShortcutsNodeRef();
         List<String> shortcuts = (List<String>)this.nodeService.getProperty(prefRef, QNAME_SHORTCUTS);
         if (shortcuts != null && shortcuts.size() > shortcutEvent.Index)
         {
            // remove the shortcut from the saved list and persist back
            shortcuts.remove(shortcutEvent.Index);
            this.nodeService.setProperty(prefRef, QNAME_SHORTCUTS, (Serializable)shortcuts);
            
            // commit the transaction
            tx.commit();
            
            // remove shortcut Node from the in-memory list
            Node node = getShortcuts().remove(shortcutEvent.Index);
            
            if (logger.isDebugEnabled())
               logger.debug("Removed node: " + node.getName() + " from the user shortcuts list.");
         }
      }
      catch (Exception err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }
   
   /**
    * Action handler bound to the user shortcuts Shelf component called when a node is clicked
    */
   public void click(ActionEvent event)
   {
      // work out which node was clicked from the event data
      UIShortcutsShelfItem.ShortcutEvent shortcutEvent = (UIShortcutsShelfItem.ShortcutEvent)event;
      Node selectedNode = getShortcuts().get(shortcutEvent.Index);
      
      try
      {
         DictionaryService dd = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getDictionaryService();
         if (dd.isSubClass(selectedNode.getType(), ContentModel.TYPE_FOLDER))
         {
            // then navigate to the appropriate node in UI
            // use browse bean functionality for this as it will update the breadcrumb for us
            this.browseBean.updateUILocation(selectedNode.getNodeRef());
         }
         else if (dd.isSubClass(selectedNode.getType(), ContentModel.TYPE_CONTENT))
         {
            // view details for document
            this.browseBean.setupContentAction(selectedNode.getId(), true);
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "showDocDetails");
         }
      }
      catch (InvalidNodeRefException refErr)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {selectedNode.getId()}) );
         
         // remove item from the shortcut list
         UserTransaction tx = null;
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            NodeRef prefRef = getShortcutsNodeRef();
            List<String> shortcuts = (List<String>)this.nodeService.getProperty(prefRef, QNAME_SHORTCUTS);
            if (shortcuts != null && shortcuts.size() > shortcutEvent.Index)
            {
               // remove the shortcut from the saved list and persist back
               shortcuts.remove(shortcutEvent.Index);
               this.nodeService.setProperty(prefRef, QNAME_SHORTCUTS, (Serializable)shortcuts);
               
               // commit the transaction
               tx.commit();
               
               // remove shortcut Node from the in-memory list
               Node node = getShortcuts().remove(shortcutEvent.Index);
               
               if (logger.isDebugEnabled())
                  logger.debug("Removed deleted node: " + node.getName() + " from the user shortcuts list.");
            }
         }
         catch (Exception err)
         {
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         }
      }
   }
}
