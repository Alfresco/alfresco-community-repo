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
package org.alfresco.web.bean.clipboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
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
public class ClipboardBean
{
   private static Log logger = LogFactory.getLog(ClipboardBean.class);
   
   /** I18N messages */
   private static final String MSG_ERROR_PASTE  = "error_paste";
   
   /** Current state of the clipboard items */
   private List<ClipboardItem> items = new ArrayList<ClipboardItem>(4);
   
   
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
         addClipboardNode(new NodeRef(ref), ClipboardStatus.COPY);
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
      if (ref != null && ref.length() != 0)
      {
         addClipboardNode(new NodeRef(ref), ClipboardStatus.CUT);
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
      try
      {
         if (index == -1)
         {
            // paste all
            for (int i=0; i<this.items.size(); i++)
            {
               ClipboardItem item = this.items.get(i);
               if (performClipboardOperation(item, action) == true)
               {
                  // if cut operation then remove item from the clipboard
                  if (item.getMode() == ClipboardStatus.CUT)
                  {
                     this.items.remove(i);
                  }
               }
            }
            // TODO: after a paste all - remove items from the clipboard...? or not. ask linton
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
         UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_PASTE) + err.getMessage(), err);
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
      FacesContext fc = FacesContext.getCurrentInstance();
      
      // test the current JSF view to see if the clipboard item can paste to it
      if (logger.isDebugEnabled())
         logger.debug("Clipboard destintation View Id: " + fc.getViewRoot().getViewId());
      if (item.canPasteToViewId(fc.getViewRoot().getViewId()) == false)
      {
         // early exit if we cannot support this view as a paste location
         if (logger.isDebugEnabled())
            logger.debug("Clipboard Item: " + item.getNodeRef() + " not suitable for paste to current View Id."); 
         return false;
      }
      
      return item.paste(fc, fc.getViewRoot().getViewId(), action);
   }
   
   /**
    * Add a clipboard node to the clipboard ready for a cut/copy operation
    * 
    * @param ref     NodeRef of the item for the operation
    * @param mode    ClipboardStatus for the operation
    */
   private void addClipboardNode(NodeRef ref, ClipboardStatus mode)
   {
      // construct item based on store protocol
      ClipboardItem item = null;
      if (StoreRef.PROTOCOL_WORKSPACE.equals(ref.getStoreRef().getProtocol()))
      {
         item = new WorkspaceClipboardItem(ref, mode);
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
      }
   }
}
