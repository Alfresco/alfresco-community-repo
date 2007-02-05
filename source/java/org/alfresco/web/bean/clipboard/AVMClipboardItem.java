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

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.CrossRepositoryCopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMBrowseBean;

/**
 * Class representing an 'avm' store protocol clipboard item
 * 
 * @author Kevin Roast
 */
public class AVMClipboardItem extends AbstractClipboardItem
{
   private static final String AVM_PASTE_VIEW_ID = "/jsp/wcm/browse-sandbox.jsp";
   private static final String WORKSPACE_PASTE_VIEW_ID = "/jsp/browse/browse.jsp";
   
   /**
    * @param ref
    * @param mode
    */
   public AVMClipboardItem(NodeRef ref, ClipboardStatus mode)
   {
      super(ref, mode);
   }

   /**
    * @see org.alfresco.web.bean.clipboard.ClipboardItem#supportsLink()
    */
   public boolean supportsLink()
   {
      return false;
   }

   /**
    * @see org.alfresco.web.bean.clipboard.ClipboardItem#canCopyToViewId(java.lang.String)
    */
   public boolean canCopyToViewId(String viewId)
   {
      return (AVM_PASTE_VIEW_ID.equals(viewId) || WORKSPACE_PASTE_VIEW_ID.equals(viewId));
   }

   /**
    * @see org.alfresco.web.bean.clipboard.ClipboardItem#canMoveToViewId(java.lang.String)
    */
   public boolean canMoveToViewId(String viewId)
   {
      return (AVM_PASTE_VIEW_ID.equals(viewId));
   }

   /**
    * @see org.alfresco.web.bean.clipboard.ClipboardItem#paste(javax.faces.context.FacesContext, java.lang.String, int)
    */
   public boolean paste(FacesContext fc, String viewId, int action) throws Throwable
   {
      if (AVM_PASTE_VIEW_ID.equals(viewId))
      {
         AVMBrowseBean avmBrowseBean = (AVMBrowseBean)FacesHelper.getManagedBean(fc, AVMBrowseBean.BEAN_NAME);
         
         String destPath = avmBrowseBean.getCurrentPath();
         NodeRef destRef = AVMNodeConverter.ToNodeRef(-1, destPath);
         String sourcePath = AVMNodeConverter.ToAVMVersionPath(getNodeRef()).getSecond();
         
         FileFolderService fileFolderService = getServiceRegistry().getFileFolderService();
         AVMService avmService = getServiceRegistry().getAVMService();
         
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
                     logger.debug("Attempting to copy node path: " + sourcePath + " into path: " + destPath);
                  
                  // copy the avm path
                  // first check that we are not attempting to copy a duplicate into the same parent
                  if (AVMNodeConverter.ExtendAVMPath(destPath, name).equals(sourcePath))
                  {
                     // manually change the name if this occurs
                     String copyOf = Application.getMessage(fc, MSG_COPY_OF);
                     name = copyOf + ' ' + name;
                  }
                  /*fileFolderService.copy(
                        getNodeRef(),
                        destRef,
                        name);*/
                  avmService.copy(-1, sourcePath, destPath, name);
                  
                  // if we get here without an exception, the clipboard copy operation was successful
                  operationComplete = true;
               }
               else
               {
                  // MOVE operation
                  if (logger.isDebugEnabled())
                     logger.debug("Attempting to move node path: " + sourcePath + " into path: " + destRef);
                  
                  // move the avm path
                  /*fileFolderService.move(
                        getNodeRef(),
                        destRef,
                        name);*/
                  avmService.rename(AVMNodeConverter.SplitBase(sourcePath)[0], getName(),
                        destPath, name);
                  
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
            catch (AVMExistsException avmExistsErr)
            {
               if (getMode() != ClipboardStatus.COPY)
               {    
                   // we should not rename an item when it is being moved - so exit
                   throw avmExistsErr;
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
      else if (WORKSPACE_PASTE_VIEW_ID.equals(viewId))
      {
         NavigationBean navigator = (NavigationBean)FacesHelper.getManagedBean(fc, NavigationBean.BEAN_NAME);
         NodeRef destRef = new NodeRef(Repository.getStoreRef(), navigator.getCurrentNodeId());
         
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
