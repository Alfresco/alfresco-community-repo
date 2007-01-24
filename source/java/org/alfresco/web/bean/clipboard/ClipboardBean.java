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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.NavigationBean;
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
    * @param copyService   The CopyService to set.
    */
   public void setCopyService(CopyService copyService)
   {
      this.copyService = copyService;
   }
   
   /**
    * @param searchService  The SearchService to set.
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
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
               performClipboardOperation(this.items.get(i), action);
            }
            // remove the cut operation item from the clipboard
            List<ClipboardItem> newItems = new ArrayList<ClipboardItem>(this.items.size());
            for (int i=0; i<this.items.size(); i++)
            {
               ClipboardItem item = this.items.get(i);
               if (item.getMode() != ClipboardStatus.CUT)
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
            if (item.getMode() == ClipboardStatus.CUT)
            {
               this.items.remove(index);
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
    */
   private void performClipboardOperation(ClipboardItem item, int action)
      throws Throwable
   {
      NodeRef destRef = new NodeRef(Repository.getStoreRef(), this.navigator.getCurrentNodeId());
      
      DictionaryService dd = Repository.getServiceRegistry(
            FacesContext.getCurrentInstance()).getDictionaryService();
      
      // TODO: Should we be using primary parent here?
      //       We are assuming that the item exists in only a single parent and that the source for
      //       the clipboard operation (e.g. the source folder) is specifically that parent node.
      //       So does not allow for more than one possible parent node - or for linked objects!
      //       This code should be refactored to use a parent ID when appropriate. 
      ChildAssociationRef assocRef = this.nodeService.getPrimaryParent(item.getNodeRef());
      
      // initial name to attempt the copy of the item with
      String name = item.getName();
      if (action == UIClipboardShelfItem.ACTION_PASTE_LINK)
      {
         // copy as link was specifically requested by the user
         String linkTo = Application.getMessage(FacesContext.getCurrentInstance(), MSG_LINK_TO);                
         name = linkTo + ' ' + name;
      }
      
      boolean operationComplete = false;
      while (operationComplete == false)
      {
         UserTransaction tx = null;
         try
         {
            // attempt each copy/paste in its own transaction
            tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
            tx.begin();
            if (item.getMode() == ClipboardStatus.COPY)
            {
               if (action == UIClipboardShelfItem.ACTION_PASTE_LINK)
               {
                  // LINK operation
                  if (logger.isDebugEnabled())
                     logger.debug("Attempting to link node ID: " + item.getId() + " into node ID: " + destRef.getId());
                  
                  // we create a special Link Object node that has a property to reference the original
                  // create the node using the nodeService (can only use FileFolderService for content)
                  if (checkExists(name + ".lnk", destRef) == false)
                  {
                     Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
                     props.put(ContentModel.PROP_NAME, name + ".lnk");
                     props.put(ContentModel.PROP_LINK_DESTINATION, item.getNodeRef());
                     if (dd.isSubClass(item.getType(), ContentModel.TYPE_CONTENT))
                     {
                        // create File Link node
                        ChildAssociationRef childRef = this.nodeService.createNode(
                              destRef,
                              ContentModel.ASSOC_CONTAINS,
                              assocRef.getQName(),
                              ApplicationModel.TYPE_FILELINK,
                              props);
                        
                        // apply the titled aspect - title and description
                        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(2, 1.0f);
                        titledProps.put(ContentModel.PROP_TITLE, name);
                        titledProps.put(ContentModel.PROP_DESCRIPTION, name);
                        this.nodeService.addAspect(childRef.getChildRef(), ContentModel.ASPECT_TITLED, titledProps);
                     }
                     else
                     {
                        // create Folder link node
                        ChildAssociationRef childRef = this.nodeService.createNode(
                              destRef,
                              ContentModel.ASSOC_CONTAINS,
                              assocRef.getQName(),
                              ApplicationModel.TYPE_FOLDERLINK,
                              props);
                        
                        // apply the uifacets aspect - icon, title and description props
                        Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(4, 1.0f);
                        uiFacetsProps.put(ApplicationModel.PROP_ICON, "space-icon-link");
                        uiFacetsProps.put(ContentModel.PROP_TITLE, name);
                        uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, name);
                        this.nodeService.addAspect(childRef.getChildRef(), ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
                     }
                     
                     // if we get here without an exception, the clipboard link operation was successful
                     operationComplete = true;
                  }
               }
               else
               {
                  // COPY operation
                  if (logger.isDebugEnabled())
                     logger.debug("Attempting to copy node ID: " + item.getId() + " into node ID: " + destRef.getId());
                  
                  if (dd.isSubClass(item.getType(), ContentModel.TYPE_CONTENT) ||
                      dd.isSubClass(item.getType(), ContentModel.TYPE_FOLDER))
                  {
                     // copy the file/folder
                     // first check that we are not attempting to copy a duplicate into the same parent
                     if (destRef.equals(assocRef.getParentRef()) && name.equals(item.getName()))
                     {
                        // manually change the name if this occurs
                        String copyOf = Application.getMessage(FacesContext.getCurrentInstance(), MSG_COPY_OF);
                        name = copyOf + ' ' + name;
                     }
                     this.fileFolderService.copy(
                           item.getNodeRef(),
                           destRef,
                           name);
                  }
                  else
                  {
                     // copy the node
                     if (checkExists(name, destRef) == false)
                     {
                        this.copyService.copy(
                              item.getNodeRef(),
                              destRef,
                              ContentModel.ASSOC_CONTAINS,
                              assocRef.getQName(),
                              true);
                     }
                  }
                  
                  // if we get here without an exception, the clipboard copy operation was successful
                  operationComplete = true;
               }
            }
            else
            {
               // MOVE operation
               if (logger.isDebugEnabled())
                  logger.debug("Attempting to move node ID: " + item.getId() + " into node ID: " + destRef.getId());
               
               if (dd.isSubClass(item.getType(), ContentModel.TYPE_CONTENT) ||
                   dd.isSubClass(item.getType(), ContentModel.TYPE_FOLDER))
               {
                  // move the file/folder
                  this.fileFolderService.move(
                        item.getNodeRef(),
                        destRef,
                        name);
               }
               else
               {
                  // move the node
                  this.nodeService.moveNode(
                        item.getNodeRef(),
                        destRef,
                        ContentModel.ASSOC_CONTAINS,
                        assocRef.getQName());
               }
               
               // if we get here without an exception, the clipboard move operation was successful
               operationComplete = true;
            }
         }
         catch (FileExistsException fileExistsErr)
         {
            if (item.getMode() != ClipboardStatus.COPY)
            {    
                // we should not rename an item when it is being moved - so exit
                throw fileExistsErr;
            }
         }
         catch (Throwable e)
         {
            // some other type of exception occured - rollback and exit
            throw e;
         }
         finally
         {
            // rollback if the operation didn't complete
            if (operationComplete == false)
            {
               try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
               String copyOf = Application.getMessage(FacesContext.getCurrentInstance(), MSG_COPY_OF);
               name = copyOf + ' ' + name;
            }
            else
            {
               // commit the transaction
               tx.commit();
            }
         }
      }
   }
   
   private boolean checkExists(String name, NodeRef parent)
   {
      ServiceRegistry services = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      
      QueryParameterDefinition[] params = new QueryParameterDefinition[1];
      params[0] = new QueryParameterDefImpl(
            ContentModel.PROP_NAME,
            services.getDictionaryService().getDataType(
                  DataTypeDefinition.TEXT),
                  true,
                  name);
      
      // execute the query
      List<NodeRef> nodeRefs = searchService.selectNodes(
            parent,
            XPATH_QUERY_NODE_MATCH,
            params,
            services.getNamespaceService(),
            false);
      
      return (nodeRefs.size() != 0);
   }
   
   /**
    * Add a clipboard node for an operation to the clipboard
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
   
   
   // ------------------------------------------------------------------------------
   // Private data
   
   private static Log logger = LogFactory.getLog(ClipboardBean.class);
   
   /** I18N messages */
   private static final String MSG_ERROR_PASTE  = "error_paste";
   private static final String MSG_COPY_OF      = "copy_of";
   private static final String MSG_LINK_TO      = "link_to";
   
   /** Shallow search for nodes with a name pattern */
   private static final String XPATH_QUERY_NODE_MATCH = "./*[like(@cm:name, $cm:name, false)]";
   
   /** The NodeService to be used by the bean */
   protected NodeService nodeService;
   
   /** The FileFolderService to be used by the bean */
   protected FileFolderService fileFolderService;
   
   /** The CopyService to be used by the bean */
   protected CopyService copyService;
   
   /** The SearchService to be used by the bean */
   protected SearchService searchService;
   
   /** The NavigationBean reference */
   protected NavigationBean navigator;
   
   /** Current state of the clipboard items */
   private List<ClipboardItem> items = new ArrayList<ClipboardItem>(4);
}
