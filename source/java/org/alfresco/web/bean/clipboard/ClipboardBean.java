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
package org.alfresco.web.bean.clipboard;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.repo.component.shelf.UIClipboardShelfItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean backing the Clipboard shelf functionality.
 * <p>
 * The clipboard bean is responsible for processing Cut/Copy requests to the clipboard
 * and for executing the various Paste calls available to the user. 
 * 
 * @author Kevin Roast
 */
public class ClipboardBean implements Serializable
{
   private static final long serialVersionUID = -6299320341615099651L;

   private static Log logger = LogFactory.getLog(ClipboardBean.class);
   
   /** I18N messages */
   private static final String MSG_ERROR_PASTE  = "error_paste";
   private static final String MSG_VIEW_FOR_PASTE_WARN = "not_suitable_view_for_paste_warn";
   
   /** Current state of the clipboard items */
   private List<ClipboardItem> items = new ArrayList<ClipboardItem>(4);
   
   transient private NodeService nodeService;
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @return Returns a list representing the items on the user clipboard.
    */
   public List<ClipboardItem> getItems()
   {
      return this.items;
   }
   
   /**
    * @param items   List representing the items on the user clipboard.
    */
   public void setItems(List<ClipboardItem> items)
   {
      this.items = items;
   }
   
   public void setNodeService(NodeService nodeService)
   {

       this.nodeService = nodeService;
   }
   
   private NodeService getNodeService()
   {
       if (nodeService == null)
       {
           nodeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
       }
       return nodeService;
   }

   // ------------------------------------------------------------------------------
   // Navigation action event handlers 
   
   /**
    * Action handler called to add a node to the clipboard for a Copy operation
    */
   public void copyNode(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String ref = params.get("ref");
      if (ref != null && ref.length() != 0)
      {
         addClipboardNode(new NodeRef(ref), null, ClipboardStatus.COPY);
      }
   }
   
   /**
    * Action handler called to add a node to the clipboard for a Cut operation
    */
   public void cutNode(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String ref = params.get("ref");
      String parent = params.get("parent");
      if (ref != null && ref.length() != 0)
      {
          NodeRef parentNodeRef = null;
          if (parent != null)
          {
              parentNodeRef = new NodeRef(Repository.getStoreRef(), parent);
              // ALF-8885 fix, if copied node is the same as parent node we should use null.
              // Primary parent will be used later in FileFolderService#moveOrCopy method
              if (parentNodeRef.toString().equals(ref))
              {
                  parentNodeRef = null;
              }
          }
          addClipboardNode(new NodeRef(ref), parentNodeRef, ClipboardStatus.CUT);
      }
   }
   
   /**
    * Action handler call from the browse screen to Paste All clipboard items into the current Space
    */
   public void pasteAll(ActionEvent event)
   {
      performPasteItems(-1, UIClipboardShelfItem.ACTION_PASTE_ALL);
   }
   
   /**
    * Action handler called to paste one or all items from the clipboard
    */
   public void pasteItem(ActionEvent event)
   {
      UIClipboardShelfItem.ClipboardEvent clipEvent = (UIClipboardShelfItem.ClipboardEvent)event;
      
      int index = clipEvent.Index;
      if (index >= this.items.size())
      {
         throw new IllegalStateException("Clipboard attempting paste a non existent item index: " + index);
      }
      
      performPasteItems(index, clipEvent.Action);
   }
   
   /**
    * Perform a paste for the specified clipboard item(s)
    * 
    * @param index      of clipboard item to paste or -1 for all
    * @param action     the clipboard action to perform (see UIClipboardShelfItem)
    */
   private void performPasteItems(int index, int action)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      boolean toClear = false;
      try
      {
         if (index == -1)
         {
            // paste all
            List<ClipboardItem> toRemove = new ArrayList<ClipboardItem>();
            for (ClipboardItem item : this.items)
            {
               if (!getNodeService().exists(item.getNodeRef()))
               {
                  toRemove.add(item);
                  toClear = true;
                  continue;
               }

               if (performClipboardOperation(item, action) == true)
               {
                  // if cut operation then remove item from the clipboard
                  if (item.getMode() == ClipboardStatus.CUT)
                  {
                     // remember which items to remove.
                     toRemove.add(item);
                  }
                  toClear = true;
               }
            }
            
            // clear the clipboard after a paste all
            if (toClear)
            {
                if (Application.getClientConfig(context).isPasteAllAndClearEnabled())
                {
                   this.items.clear();
                }
                else if (toRemove.size() > 0)
                {
                   // remove the items that were cut above
                   for (ClipboardItem item : toRemove)
                   {
                      this.items.remove(item);
                   }
                }
             }
         }
         else
         {
            // single paste operation
            ClipboardItem item = this.items.get(index);
            if (performClipboardOperation(item, action) == true)
            {
               // if cut operation then remove item from the clipboard
               if (item.getMode() == ClipboardStatus.CUT)
               {
                  this.items.remove(index);
               }
            }
         }
         
         // refresh UI on success
         UIContextService.getInstance(context).notifyBeans();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(Application.getMessage(context, MSG_ERROR_PASTE) + err.getMessage(), err);
      }
   }

   /**
    * Perform the operation for the specified clipboard item
    * 
    * @param item       the ClipboardItem
    * @param action     the clipboard action to perform (see UIClipboardShelfItem)
    * 
    * @return true on successful operation
    */
   private boolean performClipboardOperation(ClipboardItem item, int action)
      throws Throwable
   {
      boolean success = false;
      
      FacesContext fc = FacesContext.getCurrentInstance();
      
      // test the current JSF view to see if the clipboard item can paste to it
      if (logger.isDebugEnabled())
         logger.debug("Clipboard destination View Id: " + fc.getViewRoot().getViewId());
      if (item.getMode() == ClipboardStatus.CUT)
      {
         if (item.canMoveToViewId(fc.getViewRoot().getViewId()) == true)
         {
            success = item.paste(fc, fc.getViewRoot().getViewId(), action);
         }
         else
         {
            if (Application.getClientConfig(fc).isClipboardStatusVisible())
            {
               String pattern = Application.getMessage(fc, MSG_VIEW_FOR_PASTE_WARN);
               String msg = MessageFormat.format(pattern, item.getName());
               FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, msg, msg);
               fc.addMessage(null, facesMsg);
            }
            // we cannot support this view as a Move paste location
            if (logger.isDebugEnabled())
               logger.debug("Clipboard Item: " + item.getNodeRef() + " not suitable for Move paste to current View Id."); 
         }
      }
      else if (item.getMode() == ClipboardStatus.COPY)
      {
         if (item.canCopyToViewId(fc.getViewRoot().getViewId()) == true)
         {
            success = item.paste(fc, fc.getViewRoot().getViewId(), action);
         }
         else
         {
            if (Application.getClientConfig(fc).isClipboardStatusVisible())
            {
               String pattern = Application.getMessage(fc, MSG_VIEW_FOR_PASTE_WARN);
               String msg = MessageFormat.format(pattern, item.getName());
               FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, msg, msg);
               fc.addMessage(null, facesMsg);
            }
            // we cannot support this view as a Copy paste location
            if (logger.isDebugEnabled())
               logger.debug("Clipboard Item: " + item.getNodeRef() + " not suitable for Copy paste to current View Id."); 
         }
      }
      
      return success;
   }
   
   /**
    * Add a clipboard node to the clipboard ready for a cut/copy operation
    * 
    * @param ref     NodeRef of the item for the operation
    * @param parent  Parent of the item for the operation
    * @param mode    ClipboardStatus for the operation
    */
   private void addClipboardNode(NodeRef ref, NodeRef parent, ClipboardStatus mode)
   {
      // construct item based on store protocol
      ClipboardItem item = null;
      if (StoreRef.PROTOCOL_WORKSPACE.equals(ref.getStoreRef().getProtocol()))
      {
         item = new WorkspaceClipboardItem(ref, parent, mode);
      }
      else if (StoreRef.PROTOCOL_AVM.equals(ref.getStoreRef().getProtocol()))
      {
         item = new AVMClipboardItem(ref, mode);
      }
      else
      {
         logger.warn("Unable to add item to clipboard - unknown store protocol: " + ref.getStoreRef().getProtocol());
      }
      
      if (item != null)
      {
         // check for duplicates first
         boolean foundDuplicate = false;
         for (int i=0; i<items.size(); i++)
         {
            if (items.get(i).equals(item))
            {
               // found a duplicate replace with new instance as copy mode may have changed
               items.set(i, item);
               foundDuplicate = true;
               break;
            }
         }
         // if duplicate not found, then append to list
         if (foundDuplicate == false)
         {
            items.add(item);
         }
         
         // add a message to inform the user of the clipboard state now if configured
         FacesContext context = FacesContext.getCurrentInstance();
         if (Application.getClientConfig(context).isClipboardStatusVisible())
         {
            String pattern = Application.getMessage(context, "node_added_clipboard");
            String msg = MessageFormat.format(pattern, items.size());
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
            context.addMessage(null, facesMsg);
         }
      }
   }
}
