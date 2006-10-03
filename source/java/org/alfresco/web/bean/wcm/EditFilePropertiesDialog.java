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
package org.alfresco.web.bean.wcm;

import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.content.EditContentPropertiesDialog;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Backing bean for the Edit File Properties dialog.
 * 
 * @author Kevin Roast
 */
public class EditFilePropertiesDialog extends EditContentPropertiesDialog
{
   protected AVMBrowseBean avmBrowseBean;
   protected AVMService avmService;
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param avmBrowseBean    The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * @param avmService       The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Dialog implementation

   /**
    * @see org.alfresco.web.bean.content.EditContentPropertiesDialog#initEditableNode()
    */
   @Override
   protected Node initEditableNode()
   {
      return new Node(this.avmBrowseBean.getAvmNode().getNodeRef());
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // a rename may have occured - we need to reset the NodeRef of the modified AVM Node
      // as an AVM NodeRef contains the name as part of ref - which can therefore change! 
      String name = this.editableNode.getName();
      String oldPath = AVMNodeConverter.ToAVMVersionPath(this.editableNode.getNodeRef()).getSecond();
      String newPath = oldPath.substring(0, oldPath.lastIndexOf('/') + 1) + name;
      this.avmBrowseBean.setAvmNode(new AVMNode(this.avmService.lookup(-1, newPath)));
      
      return outcome;
   }
   
   /**
    * Formats the error message to display if an error occurs during finish processing
    * 
    * @param The exception
    * @return The formatted message
    */
   @Override
   protected String formatErrorMessage(Throwable exception)
   {
      if (exception instanceof FileExistsException)
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_EXISTS), 
               ((FileExistsException)exception).getName());
      }
      else if (exception instanceof InvalidNodeRefException)
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), 
               new Object[] {this.avmBrowseBean.getAvmNode().getPath()});
      }
      else
      {
         return super.formatErrorMessage(exception);
      }
   }
}
