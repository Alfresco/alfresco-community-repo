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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.repo.component.shelf.UIClipboardShelfItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Kevin Roast
 */
public class ClipboardBean
{
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
    * @param fileFolderService   The FileFolderService to set.
    */
   public void setFileFolderService(FileFolderService fileFolderService)
   {
      this.fileFolderService = fileFolderService;
   }
   
   /**
    * @param nodeOperationsService   The NodeOperationsService to set.
    */
   public void setNodeOperationsService(CopyService nodeOperationsService)
   {
      this.nodeOperationsService = nodeOperationsService;
   }
   
   /**
    * @param navigator The NavigationBean to set.
    */
   public void setNavigator(NavigationBean navigator)
   {
      this.navigator = navigator;
   }
   
   /**
    * @return Returns the clipboard items.
    */
   public List<ClipboardItem> getItems()
   {
      return this.items;
   }
   
   /**
    * @param items   The clipboard items to set.
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
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         addClipboardNode(id, ClipboardStatus.COPY);
      }
   }
   
   /**
    * Action handler called to add a node to the clipboard for a Cut operation
    */
   public void cutNode(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         addClipboardNode(id, ClipboardStatus.CUT);
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
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         if (index == -1)
         {
            // paste all
            for (int i=0; i<this.items.size(); i++)
            {
               performClipboardOperation(this.items.get(i), action);
            }
            // remove the cut operation item from the clipboard
            List<ClipboardItem> newItems = new ArrayList<ClipboardItem>(this.items.size());
            for (int i=0; i<this.items.size(); i++)
            {
               ClipboardItem item = this.items.get(i);
               if (item.Mode != ClipboardStatus.CUT)
               {
                  newItems.add(item);
               }
            }
            setItems(newItems);
            // TODO: after a paste all - remove items from the clipboard...? or not. ask linton
         }
         else
         {
            // single paste operation
            ClipboardItem item = this.items.get(index);
            performClipboardOperation(item, action);
            if (item.Mode == ClipboardStatus.CUT)
            {
               this.items.remove(index);
            }
         }
         
         // commit the transaction
         tx.commit();
         
         // refresh UI on success
         UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
      }
      catch (Throwable err)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         Utils.addErrorMessage(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_ERROR_PASTE) + err.getMessage(), err);
      }
   }

   /**
    * Perform the operation for the specified clipboard item
    * 
    * @param item       the ClipboardItem
    * @param action     the clipboard action to perform (see UIClipboardShelfItem)
    */
   private void performClipboardOperation(ClipboardItem item, int action)
      throws FileExistsException, FileNotFoundException
   {
      NodeRef destRef = new NodeRef(Repository.getStoreRef(), this.navigator.getCurrentNodeId());
      
      DictionaryService dd = Repository.getServiceRegistry(
            FacesContext.getCurrentInstance()).getDictionaryService();
      
      // TODO: Should we be using primary parent here?
      //       We are assuming that the item exists in only a single parent and that the source for
      //       the clipboard operation (e.g. the source folder) is specifically that parent node.
      //       So does not allow for more than one possible parent node - or for linked objects!
      //       This code should be refactored to use a parent ID when appropriate. 
      ChildAssociationRef assocRef = this.nodeService.getPrimaryParent(item.Node.getNodeRef());
      
      // initial name to attempt the copy of the item with
      String name = item.Node.getName();
      
      boolean operationComplete = false;
      while (operationComplete == false)
      {
         try
         {
            if (item.Mode == ClipboardStatus.COPY)
            {
               if (action == UIClipboardShelfItem.ACTION_PASTE_LINK)
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Attempting to link node ID: " + item.Node.getId() + " into node ID: " + destRef.getId());
                  
                  // copy as link was specifically requested by the user
                  
                  // we create a special Link Object node that has a property to reference the original
                  // use FileFolderService to check if already exists as using nodeService directly here
                  String linkTo = Application.getMessage(FacesContext.getCurrentInstance(), MSG_LINK_TO);
                  
                  // create the node using the nodeService (can only use FileFolderService for content)
                  Map<QName, Serializable> props = new HashMap<QName, Serializable>(4, 1.0f);
                  String linkName = linkTo + ' ' + name;
                  props.put(ContentModel.PROP_NAME, linkName + ".lnk");
                  props.put(ContentModel.PROP_LINK_DESTINATION, item.Node.getNodeRef());
                  if (dd.isSubClass(item.Node.getType(), ContentModel.TYPE_CONTENT))
                  {
                     // create File Link node
                     ChildAssociationRef childRef = this.nodeService.createNode(
                           destRef,
                           ContentModel.ASSOC_CONTAINS,
                           assocRef.getQName(),
                           ContentModel.TYPE_FILELINK,
                           props);
                     
                     // apply the titled aspect - title and description
                     Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(2, 1.0f);
                     titledProps.put(ContentModel.PROP_TITLE, linkName);
                     titledProps.put(ContentModel.PROP_DESCRIPTION, linkName);
                     this.nodeService.addAspect(childRef.getChildRef(), ContentModel.ASPECT_TITLED, titledProps);
                  }
                  else
                  {
                     // create Folder link node
                     ChildAssociationRef childRef = this.nodeService.createNode(
                           destRef,
                           ContentModel.ASSOC_CONTAINS,
                           assocRef.getQName(),
                           ContentModel.TYPE_FOLDERLINK,
                           props);
                     
                     // apply the uifacets aspect - icon, title and description props
                     Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(3, 1.0f);
                     uiFacetsProps.put(ContentModel.PROP_ICON, "space-icon-link");
                     uiFacetsProps.put(ContentModel.PROP_TITLE, linkName);
                     uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, linkName);
                     this.nodeService.addAspect(childRef.getChildRef(), ContentModel.ASPECT_UIFACETS, uiFacetsProps);
                  }
               }
               else
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Attempting to copy node ID: " + item.Node.getId() + " into node ID: " + destRef.getId());
                  
                  if (dd.isSubClass(item.Node.getType(), ContentModel.TYPE_CONTENT))
                  {
                     // call the node ops service to initiate the copy
                     this.fileFolderService.copy(
                           item.Node.getNodeRef(),
                           destRef,
                           name);
                  }
                  else
                  {
                     this.nodeOperationsService.copy(
                           item.Node.getNodeRef(),
                           destRef,
                           ContentModel.ASSOC_CONTAINS,
                           assocRef.getQName(),
                           true);
                  }
               }
            }
            else
            {
               if (logger.isDebugEnabled())
                  logger.debug("Attempting to move node ID: " + item.Node.getId() + " into node ID: " + destRef.getId());
               
               if (dd.isSubClass(item.Node.getType(), ContentModel.TYPE_CONTENT))
               {
                  // move the node
                  this.fileFolderService.move(
                        item.Node.getNodeRef(),
                        destRef,
                        name);      // TODO: could add "Copy of ..." here if move fails
               }
               else
               {
                  // move the node
                  this.nodeService.moveNode(
                        item.Node.getNodeRef(),
                        destRef,
                        ContentModel.ASSOC_CONTAINS,
                        assocRef.getQName());
               }
            }
            
            // if we get here without an exception, the clipboard operation was successful
            operationComplete = true;
         }
         catch (FileExistsException fileExistsErr)
         {
            String copyOf = Application.getMessage(FacesContext.getCurrentInstance(), MSG_COPY_OF);
            name = copyOf + ' ' + name;
         }
      }
   }
   
   /**
    * Add a clipboard node for an operation to the clipboard
    * 
    * @param id      ID of the node for the operation
    * @param mode    ClipboardStatus for the operation
    */
   private void addClipboardNode(String id, ClipboardStatus mode)
   {
      try
      {
         NodeRef ref = new NodeRef(Repository.getStoreRef(), id);
         
         // check for duplicates first
         ClipboardItem item = new ClipboardItem(new Node(ref), mode);
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
      catch (InvalidNodeRefException refErr)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {id}) );
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Private data
   
   private static Log logger = LogFactory.getLog(ClipboardBean.class);
   
   /** I18N messages */
   private static final String MSG_ERROR_PASTE  = "error_paste";
   private static final String MSG_COPY_OF      = "copy_of";
   private static final String MSG_LINK_TO      = "link_to";
   
   /** The NodeService to be used by the bean */
   protected NodeService nodeService;
   
   /** The FileFolderService to be used by the bean */
   protected FileFolderService fileFolderService;
   
   /** The NodeOperationsService to be used by the bean */
   protected CopyService nodeOperationsService;
   
   /** The NavigationBean reference */
   protected NavigationBean navigator;
   
   /** Current state of the clipboard items */
   private List<ClipboardItem> items = new ArrayList<ClipboardItem>(4);
}
