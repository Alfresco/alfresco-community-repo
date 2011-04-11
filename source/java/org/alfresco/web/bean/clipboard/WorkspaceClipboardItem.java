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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.CrossRepositoryCopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.wcm.util.WCMUtil;
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
   private static final long serialVersionUID = -1686557602737846009L;
   
   private static final String WORKSPACE_PASTE_VIEW_ID = "/jsp/browse/browse.jsp";
   private static final String AVM_PASTE_VIEW_ID = "/jsp/wcm/browse-sandbox.jsp";
   private static final String FORUMS_PASTE_VIEW_ID = "/jsp/forums/forums.jsp";
   private static final String FORUM_PASTE_VIEW_ID = "/jsp/forums/forum.jsp";

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
    * @param ref
    * @param mode
    */
   public WorkspaceClipboardItem(NodeRef ref, NodeRef parent, ClipboardStatus mode)
   {
      super(ref, parent, mode);
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
      if (AVM_PASTE_VIEW_ID.equals(viewId)) 
      {
         AVMBrowseBean avmBrowseBean = (AVMBrowseBean)FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), AVMBrowseBean.BEAN_NAME);
         String destPath = avmBrowseBean.getCurrentPath();   

         if (WCMUtil.isStagingStore(WCMUtil.getStoreName(destPath))) 
         { 
            return false; 
         }
          
         return true;
      }
      else
      {
         return (WORKSPACE_PASTE_VIEW_ID.equals(viewId) ||
                 FORUMS_PASTE_VIEW_ID.equals(viewId) || 
                 FORUM_PASTE_VIEW_ID.equals(viewId));
      }
   }

   /**
    * @see org.alfresco.web.bean.clipboard.ClipboardItem#canMoveToViewId(java.lang.String)
    */
   public boolean canMoveToViewId(String viewId)
   {
      return (WORKSPACE_PASTE_VIEW_ID.equals(viewId) || FORUMS_PASTE_VIEW_ID.equals(viewId) || 
              FORUM_PASTE_VIEW_ID.equals(viewId));
   }

   /**
    * @see org.alfresco.web.bean.clipboard.ClipboardItem#paste(javax.faces.context.FacesContext, java.lang.String, int)
    */
   public boolean paste(final FacesContext fc, String viewId, final int action)
   {
      final ServiceRegistry serviceRegistry = getServiceRegistry();
      final RetryingTransactionHelper retryingTransactionHelper = serviceRegistry.getRetryingTransactionHelper();
      if (WORKSPACE_PASTE_VIEW_ID.equals(viewId) || FORUMS_PASTE_VIEW_ID.equals(viewId) || 
          FORUM_PASTE_VIEW_ID.equals(viewId))
      {
         NavigationBean navigator = (NavigationBean)FacesHelper.getManagedBean(fc, NavigationBean.BEAN_NAME);
         final NodeRef destRef = new NodeRef(Repository.getStoreRef(), navigator.getCurrentNodeId());

         final DictionaryService dd = serviceRegistry.getDictionaryService();
         final NodeService nodeService = serviceRegistry.getNodeService();
         final FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
         final CopyService copyService = serviceRegistry.getCopyService();
         final MultilingualContentService multilingualContentService = serviceRegistry.getMultilingualContentService();
        
         final boolean isPrimaryParent;

         final ChildAssociationRef assocRef;

         if (getParent() == null)
         {
             assocRef = nodeService.getPrimaryParent(getNodeRef());
             isPrimaryParent = true;
         }
         else
         {
             NodeRef parentNodeRef = getParent();
             List<ChildAssociationRef> assocList = nodeService.getParentAssocs(getNodeRef());
             ChildAssociationRef foundRef = null;
             if (assocList != null)
             {
                for (ChildAssociationRef assocListEntry : assocList)
                {
                   if (parentNodeRef.equals(assocListEntry.getParentRef()))
                   {
                      foundRef = assocListEntry;
                      break;
                   }
                }
             }
             assocRef = foundRef;
             isPrimaryParent = parentNodeRef.equals(nodeService.getPrimaryParent(getNodeRef()).getParentRef());
         }
  
         // initial name to attempt the copy of the item with
         String name = getName();
         String translationPrefix = "";

         if (action == UIClipboardShelfItem.ACTION_PASTE_LINK)
         {
            // copy as link was specifically requested by the user
            String linkTo = Application.getMessage(fc, MSG_LINK_TO);
            name = linkTo + ' ' + name;
         }

         // Loop until we find a target name that doesn't exist
         for(;;)
         {
            try
            {
               final String currentTranslationPrefix = translationPrefix;
               final String currentName = name;
               
               // attempt each copy/paste in its own transaction
               retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
               {
                  public Void execute() throws Throwable
                  {
                     if (getMode() == ClipboardStatus.COPY)
                     {
                        if (action == UIClipboardShelfItem.ACTION_PASTE_LINK)
                        {
                           // LINK operation
                           if (logger.isDebugEnabled())
                              logger.debug("Attempting to link node ID: " + getNodeRef() + " into node: " + destRef.toString());
   
                           // we create a special Link Object node that has a property to reference the original
                           // create the node using the nodeService (can only use FileFolderService for content)
                           if (checkExists(currentName + LINK_NODE_EXTENSION, destRef) == false)
                           {
                              Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
                              String newName = currentName + LINK_NODE_EXTENSION;
                              props.put(ContentModel.PROP_NAME, newName);
                              props.put(ContentModel.PROP_LINK_DESTINATION, getNodeRef());
                              if (dd.isSubClass(getType(), ContentModel.TYPE_CONTENT))
                              {
                                 // create File Link node
                                 ChildAssociationRef childRef = nodeService.createNode(
                                       destRef,
                                       ContentModel.ASSOC_CONTAINS,
                                 QName.createQName(assocRef.getQName().getNamespaceURI(), newName),
                                       ApplicationModel.TYPE_FILELINK,
                                       props);
   
                                 // apply the titled aspect - title and description
                                 Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(2, 1.0f);
                                 titledProps.put(ContentModel.PROP_TITLE, currentName);
                                 titledProps.put(ContentModel.PROP_DESCRIPTION, currentName);
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
                                 uiFacetsProps.put(ContentModel.PROP_TITLE, currentName);
                                 uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, currentName);
                                 nodeService.addAspect(childRef.getChildRef(), ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
                              }
                           }
                        }
                        else
                        {
                           // COPY operation
                           if (logger.isDebugEnabled())
                              logger.debug("Attempting to copy node: " + getNodeRef() + " into node ID: " + destRef.toString());
   
                           // first check that we are not attempting to copy a duplicate into the same parent
                           if (destRef.equals(assocRef.getParentRef()) && currentName.equals(getName()))
                           {
                              // manually change the name if this occurs
                              throw new FileExistsException(destRef, currentName);
                           }
   
                           if (dd.isSubClass(getType(), ContentModel.TYPE_CONTENT) ||
                               dd.isSubClass(getType(), ContentModel.TYPE_FOLDER))
                           {
                              // copy the file/folder
                              fileFolderService.copy(
                                    getNodeRef(),
                                    destRef,
                                    currentName);
                           }
                           else if(dd.isSubClass(getType(), ContentModel.TYPE_MULTILINGUAL_CONTAINER))
                           {
                               // copy the mlContainer and its translations
                               multilingualContentService.copyTranslationContainer(getNodeRef(), destRef, currentTranslationPrefix);
                           }
                           else
                           {
                              // copy the node
                              if (checkExists(currentName, destRef) == false)
                              {
                                 copyService.copyAndRename(
                                       getNodeRef(),
                                       destRef,
                                       ContentModel.ASSOC_CONTAINS,
                                       assocRef.getQName(),
                                       true);
                              }
                           }
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
                           fileFolderService.moveFrom(getNodeRef(), getParent(), destRef, currentName);
                        }
                        else if(dd.isSubClass(getType(), ContentModel.TYPE_MULTILINGUAL_CONTAINER))
                        {
                            // copy the mlContainer and its translations
                            multilingualContentService.moveTranslationContainer(getNodeRef(), destRef);
                        }
                        else
                        {
                           if (isPrimaryParent)
                           {
                              // move the node
                              nodeService.moveNode(getNodeRef(), destRef, ContentModel.ASSOC_CONTAINS, assocRef.getQName());
                           }
                           else
                           {
                              nodeService.removeChild(getParent(), getNodeRef());
                              nodeService.addChild(destRef, getNodeRef(), assocRef.getTypeQName(), assocRef.getQName());
                           }
                        }
                     }
                     return null;
                  }
               });

               // We got here without error, so no need to loop with a new name
               break;
            }
            catch (FileExistsException fileExistsErr)
            {
               // If mode is COPY, have another go around the loop with a new name
               if (getMode() == ClipboardStatus.COPY)
               {
                  String copyOf = Application.getMessage(fc, MSG_COPY_OF);
                  name = copyOf + ' ' + name;
                  translationPrefix = copyOf + ' ' + translationPrefix;                  
               }
               else
               {
                   // we should not rename an item when it is being moved - so exit
                   throw fileExistsErr;
               }
            }
         }
         return true;
      }
      else if (AVM_PASTE_VIEW_ID.equals(viewId))
      {
         AVMBrowseBean avmBrowseBean = (AVMBrowseBean)FacesHelper.getManagedBean(fc, AVMBrowseBean.BEAN_NAME);

         final String destPath = avmBrowseBean.getCurrentPath();
         final NodeRef destRef = AVMNodeConverter.ToNodeRef(-1, destPath);

         final CrossRepositoryCopyService crossRepoCopyService = getServiceRegistry().getCrossRepositoryCopyService();

         // initial name to attempt the copy of the item with
         String name = getName();

         for(;;)
         {
            try
            {
               final String currentName = name;

               // attempt each copy/paste in its own transaction
               retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
               {
                  public Void execute() throws Throwable
                  {
                     if (getMode() == ClipboardStatus.COPY)
                     {
                        // COPY operation
                        if (logger.isDebugEnabled())
                           logger.debug("Attempting to copy node: " + getNodeRef() + " into node ID: " + destRef.toString());
      
                        // inter-store copy operation
                        crossRepoCopyService.copy(getNodeRef(), destRef, currentName);
                  
                        if (destRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM))
                        {
                            // ETHREEOH-2110
                            AVMNodeDescriptor desc = getAvmService().lookup(-1, destPath + "/" + currentName);
                            recursiveFormCheck(desc);
                        }
                     }
                     else
                     {
                        // this should not occur as the canMoveToViewId() will return false
                        throw new Exception("Move operation not supported between stores.");
                     }                  
                     return null;               
                  }
               });

               // We got here without error, so no need to loop with a new name
               break;
            }
            catch (FileExistsException fileExistsErr)
            {
               // If mode is COPY, have another go around the loop with a new name
               if (getMode() == ClipboardStatus.COPY)
               {
                  String copyOf = Application.getMessage(fc, MSG_COPY_OF);
                  name = copyOf + ' ' + name;                  
               }
               else
               {
                   // we should not rename an item when it is being moved - so exit
                   throw fileExistsErr;
               }
            }
         }
         return true;
      }
      else
      {
         return false;
      }
   }
}
