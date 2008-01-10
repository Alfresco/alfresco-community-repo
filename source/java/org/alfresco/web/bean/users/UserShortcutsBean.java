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
package org.alfresco.web.bean.users;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.PreferencesService;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.repo.component.shelf.UIShortcutsShelfItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This bean manages the user defined list of Recent Spaces in the Shelf component.
 * 
 * @author Kevin Roast
 */
public class UserShortcutsBean
{
   private static Log    logger = LogFactory.getLog(UserShortcutsBean.class);
   
   /** The NodeService to be used by the bean */
   protected NodeService nodeService;
   
   /** The BrowseBean reference */
   protected BrowseBean browseBean;
   
   /** The PermissionService reference */
   protected PermissionService permissionService;
   
   /** List of shortcut nodes */
   private List<Node> shortcuts = null;
   
   private String PREF_SHORTCUTS = "shortcuts";
   
   
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
    * @param permissionService The PermissionService to set.
    */
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
   
   /**
    * @return the List of shortcut Nodes
    */
   public List<Node> getShortcuts()
   {
      if (this.shortcuts == null)
      {
         List<String> shortcuts = null;
         NodeRef prefRef = null;
         UserTransaction tx = null;
         boolean rollback = false;
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            // get the shortcuts from the preferences for this user
            shortcuts = (List<String>)PreferencesService.getPreferences(context).getValue(PREF_SHORTCUTS);
            if (shortcuts != null)
            {
               // each shortcut node ID is persisted as a list item in a well known property
               this.shortcuts = new ArrayList<Node>(shortcuts.size());
               for (int i=0; i<shortcuts.size(); i++)
               {
                  NodeRef ref = new NodeRef(Repository.getStoreRef(), shortcuts.get(i));
                  try
                  {
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
                  catch (AccessDeniedException accessErr)
                  {
                     // ignore this shortcut node - no longer exists in the system!
                     // we write the node list back again afterwards to correct this
                     if (logger.isDebugEnabled())
                        logger.debug("Found invalid shortcut node Id: " + ref.getId());
                     rollback = true;
                  }
               }
            }
            else
            {
               this.shortcuts = new ArrayList<Node>(5);
            }
            
            if (rollback == false)
            {
               tx.commit();
            }
            else
            {
               tx.rollback();
            }
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         }
         
         // if the count of accessable shortcuts is different to our original list then
         // write the valid shortcut IDs back to correct invalid node refs
         if (shortcuts != null && shortcuts.size() != this.shortcuts.size())
         {
            try
            {
               shortcuts = new ArrayList<String>(this.shortcuts.size());
               for (int i=0; i<this.shortcuts.size(); i++)
               {
                  shortcuts.add(this.shortcuts.get(i).getId());
               }
               PreferencesService.getPreferences().setValue(PREF_SHORTCUTS, (Serializable)shortcuts);
            }
            catch (Exception err)
            {
               Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                     FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
            }
         }
      }
      
      return this.shortcuts;
   }
   
   /**
    * @param nodes     List of shortcuts Nodes
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
                  
                  List<String> shortcuts = (List<String>)PreferencesService.getPreferences(context).getValue(PREF_SHORTCUTS);
                  if (shortcuts == null)
                  {
                     shortcuts = new ArrayList<String>(1);
                  }
                  shortcuts.add(node.getNodeRef().getId());
                  PreferencesService.getPreferences(context).setValue(PREF_SHORTCUTS, (Serializable)shortcuts);
                  
                  // commit the transaction
                  tx.commit();
                  
                  // add our new shortcut Node to the in-memory list
                  getShortcuts().add(node);
                  
                  if (logger.isDebugEnabled())
                     logger.debug("Added node: " + node.getName() + " to the user shortcuts list.");
               }
               catch (Throwable err)
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
         
         List<String> shortcuts = (List<String>)PreferencesService.getPreferences(context).getValue(PREF_SHORTCUTS);
         if (shortcuts != null && shortcuts.size() > shortcutEvent.Index)
         {
            // remove the shortcut from the saved list and persist back
            shortcuts.remove(shortcutEvent.Index);
            PreferencesService.getPreferences(context).setValue(PREF_SHORTCUTS, (Serializable)shortcuts);
            
            // commit the transaction
            tx.commit();
            
            // remove shortcut Node from the in-memory list
            Node node = getShortcuts().remove(shortcutEvent.Index);
            
            if (logger.isDebugEnabled())
               logger.debug("Removed node: " + node.getName() + " from the user shortcuts list.");
         }
      }
      catch (Throwable err)
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
         if (permissionService.hasPermission(selectedNode.getNodeRef(), PermissionService.READ) == AccessStatus.ALLOWED)
         {
            if (nodeService.exists(selectedNode.getNodeRef()) == false)
            {
               throw new InvalidNodeRefException(selectedNode.getNodeRef());
            }
            
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
               fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:showDocDetails");
            }
         }
         else
         {
            Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(), "error_shortcut_permissions"));
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
            
            List<String> shortcuts = (List<String>)PreferencesService.getPreferences(context).getValue(PREF_SHORTCUTS);
            if (shortcuts != null && shortcuts.size() > shortcutEvent.Index)
            {
               // remove the shortcut from the saved list and persist back
               shortcuts.remove(shortcutEvent.Index);
               PreferencesService.getPreferences(context).setValue(PREF_SHORTCUTS, (Serializable)shortcuts);
               
               // commit the transaction
               tx.commit();
               
               // remove shortcut Node from the in-memory list
               Node node = getShortcuts().remove(shortcutEvent.Index);
               
               if (logger.isDebugEnabled())
                  logger.debug("Removed deleted node: " + node.getName() + " from the user shortcuts list.");
            }
         }
         catch (Throwable err)
         {
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         }
      }
   }
}
