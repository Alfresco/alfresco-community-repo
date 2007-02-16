/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.bean.clipboard;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.CrossRepositoryCopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMBrowseBean;
import org.alfresco.web.ui.repo.component.shelf.UIClipboardShelfItem;

/**
 * Class representing a 'workspace' store protocol clipboard item
 * 
 * @author Kevin Roast
 */
public class WorkspaceClipboardItem extends AbstractClipboardItem
{
   private static final String WORKSPACE_PASTE_VIEW_ID = "/jsp/browse/browse.jsp";
   private static final String AVM_PASTE_VIEW_ID = "/jsp/wcm/browse-sandbox.jsp";
   
   private static final String MSG_LINK_TO = "link_to";
   
   // File extension to use for link nodes
   private static final String LINK_NODE_EXTENSION = ".url";
   
   
   /**
    * @param ref
    * @param mode
    */
   public WorkspaceClipboardItem(NodeRef ref, ClipboardStatus mode)
   {
      super(ref, mode);
   }
   
   /**
    * @see org.alfresco.web.bean.clipboard.ClipboardItem#supportsLink()
    */
   public boolean supportsLink()
   {
      return true;
   }
   
   /**
    * @see org.alfresco.web.bean.clipboard.ClipboardItem#canCopyToViewId(java.lang.String)
    */
   public boolean canCopyToViewId(String viewId)
   {
      return (WORKSPACE_PASTE_VIEW_ID.equals(viewId) || AVM_PASTE_VIEW_ID.equals(viewId));
   }

   /**
    * @see org.alfresco.web.bean.clipboard.ClipboardItem#canMoveToViewId(java.lang.String)
    */
   public boolean canMoveToViewId(String viewId)
   {
      return (WORKSPACE_PASTE_VIEW_ID.equals(viewId));
   }

   /**
    * @see org.alfresco.web.bean.clipboard.ClipboardItem#paste(javax.faces.context.FacesContext, java.lang.String, int)
    */
   public boolean paste(FacesContext fc, String viewId, int action)
      throws Throwable
   {
      if (WORKSPACE_PASTE_VIEW_ID.equals(viewId))
      {
         NavigationBean navigator = (NavigationBean)FacesHelper.getManagedBean(fc, NavigationBean.BEAN_NAME);
         NodeRef destRef = new NodeRef(Repository.getStoreRef(), navigator.getCurrentNodeId());
         
         DictionaryService dd = getServiceRegistry().getDictionaryService();
         NodeService nodeService = getServiceRegistry().getNodeService();
         FileFolderService fileFolderService = getServiceRegistry().getFileFolderService();
         CopyService copyService = getServiceRegistry().getCopyService();
         
         // TODO: Should we be using primary parent here?
         //       We are assuming that the item exists in only a single parent and that the source for
         //       the clipboard operation (e.g. the source folder) is specifically that parent node.
         //       So does not allow for more than one possible parent node - or for linked objects!
         //       This code should be refactored to use a parent ID when appropriate. 
         ChildAssociationRef assocRef = nodeService.getPrimaryParent(getNodeRef());
         
         // initial name to attempt the copy of the item with
         String name = getName();
         if (action == UIClipboardShelfItem.ACTION_PASTE_LINK)
         {
            // copy as link was specifically requested by the user
            String linkTo = Application.getMessage(fc, MSG_LINK_TO);                
            name = linkTo + ' ' + name;
         }
         
         boolean operationComplete = false;
         while (operationComplete == false)
         {
            UserTransaction tx = null;
            try
            {
               // attempt each copy/paste in its own transaction
               tx = Repository.getUserTransaction(fc);
               tx.begin();
               if (getMode() == ClipboardStatus.COPY)
               {
                  if (action == UIClipboardShelfItem.ACTION_PASTE_LINK)
                  {
                     // LINK operation
                     if (logger.isDebugEnabled())
                        logger.debug("Attempting to link node ID: " + getNodeRef() + " into node: " + destRef.toString());
                     
                     // we create a special Link Object node that has a property to reference the original
                     // create the node using the nodeService (can only use FileFolderService for content)
                     if (checkExists(name + LINK_NODE_EXTENSION, destRef) == false)
                     {
                        Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
                        props.put(ContentModel.PROP_NAME, name + LINK_NODE_EXTENSION);
                        props.put(ContentModel.PROP_LINK_DESTINATION, getNodeRef());
                        if (dd.isSubClass(getType(), ContentModel.TYPE_CONTENT))
                        {
                           // create File Link node
                           ChildAssociationRef childRef = nodeService.createNode(
                                 destRef,
                                 ContentModel.ASSOC_CONTAINS,
                                 assocRef.getQName(),
                                 ApplicationModel.TYPE_FILELINK,
                                 props);
                           
                           // apply the titled aspect - title and description
                           Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(2, 1.0f);
                           titledProps.put(ContentModel.PROP_TITLE, name);
                           titledProps.put(ContentModel.PROP_DESCRIPTION, name);
                           nodeService.addAspect(childRef.getChildRef(), ContentModel.ASPECT_TITLED, titledProps);
                        }
                        else
                        {
                           // create Folder link node
                           ChildAssociationRef childRef = nodeService.createNode(
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
                           nodeService.addAspect(childRef.getChildRef(), ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
                        }
                        
                        // if we get here without an exception, the clipboard link operation was successful
                        operationComplete = true;
                     }
                  }
                  else
                  {
                     // COPY operation
                     if (logger.isDebugEnabled())
                        logger.debug("Attempting to copy node: " + getNodeRef() + " into node ID: " + destRef.toString());
                     
                     // first check that we are not attempting to copy a duplicate into the same parent
                     if (destRef.equals(assocRef.getParentRef()) && name.equals(getName()))
                     {
                        // manually change the name if this occurs
                        String copyOf = Application.getMessage(fc, MSG_COPY_OF);
                        name = copyOf + ' ' + name;
                     }
                     
                     if (dd.isSubClass(getType(), ContentModel.TYPE_CONTENT) ||
                         dd.isSubClass(getType(), ContentModel.TYPE_FOLDER))
                     {
                        // copy the file/folder
                        fileFolderService.copy(
                              getNodeRef(),
                              destRef,
                              name);
                     }
                     else
                     {
                        // copy the node
                        if (checkExists(name, destRef) == false)
                        {
                           copyService.copyAndRename(
                                 getNodeRef(),
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
                     logger.debug("Attempting to move node: " + getNodeRef() + " into node ID: " + destRef.toString());
                  
                  if (dd.isSubClass(getType(), ContentModel.TYPE_CONTENT) ||
                      dd.isSubClass(getType(), ContentModel.TYPE_FOLDER))
                  {
                     // move the file/folder
                     fileFolderService.move(
                           getNodeRef(),
                           destRef,
                           name);
                  }
                  else
                  {
                     // move the node
                     nodeService.moveNode(
                           getNodeRef(),
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
               if (getMode() != ClipboardStatus.COPY)
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
                  String copyOf = Application.getMessage(fc, MSG_COPY_OF);
                  name = copyOf + ' ' + name;
               }
               else
               {
                  // commit the transaction
                  tx.commit();
               }
            }
         }
         return operationComplete;
      }
      else if (AVM_PASTE_VIEW_ID.equals(viewId))
      {
         AVMBrowseBean avmBrowseBean = (AVMBrowseBean)FacesHelper.getManagedBean(fc, AVMBrowseBean.BEAN_NAME);
         
         String destPath = avmBrowseBean.getCurrentPath();
         NodeRef destRef = AVMNodeConverter.ToNodeRef(-1, destPath);
         
         CrossRepositoryCopyService crossRepoCopyService = getServiceRegistry().getCrossRepositoryCopyService();
         
         // initial name to attempt the copy of the item with
         String name = getName();
         
         boolean operationComplete = false;
         while (operationComplete == false)
         {
            UserTransaction tx = null;
            try
            {
               // attempt each copy/paste in its own transaction
               tx = Repository.getUserTransaction(fc);
               tx.begin();
               if (getMode() == ClipboardStatus.COPY)
               {
                  // COPY operation
                  if (logger.isDebugEnabled())
                     logger.debug("Attempting to copy node: " + getNodeRef() + " into node ID: " + destRef.toString());
                  
                  // inter-store copy operation
                  crossRepoCopyService.copy(getNodeRef(), destRef, name);
                  
                  // if we get here without an exception, the clipboard copy operation was successful
                  operationComplete = true;
               }
               else
               {
                  // this should not occur as the canMoveToViewId() will return false
                  throw new Exception("Move operation not supported between stores.");
               }
            }
            catch (FileExistsException fileExistsErr)
            {
               if (getMode() != ClipboardStatus.COPY)
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
                  String copyOf = Application.getMessage(fc, MSG_COPY_OF);
                  name = copyOf + ' ' + name;
               }
               else
               {
                  // commit the transaction
                  tx.commit();
               }
            }
         }
         return operationComplete;
      }
      else
      {
         return false;
      }
   }
}
