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
package org.alfresco.web.bean.forums;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.spaces.DeleteSpaceDialog;

/**
 * Bean implementation for the "Delete Forum" dialog
 * 
 * @author gavinc
 */
public class DeleteForumDialog extends DeleteSpaceDialog
{
   protected boolean reDisplayForums;

   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // reset the reDisplayForums flag
      this.reDisplayForums = false;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // find out what the parent type of the node being deleted 
      Node node = this.browseBean.getActionSpace();
      ChildAssociationRef assoc = this.nodeService.getPrimaryParent(node.getNodeRef());
      if (assoc != null)
      {
         // get the parent node
         NodeRef parent = assoc.getParentRef();
         
         // get the association type
         QName type = assoc.getTypeQName();
         if (type.equals(ForumModel.ASSOC_DISCUSSION))
         {
            // if the association type is the 'discussion' association we
            // need to remove the discussable aspect from the parent node
            this.nodeService.removeAspect(parent, ForumModel.ASPECT_DISCUSSABLE);
         }
         
         // if the parent type is a forum space then we need the dialog to go
         // back to the forums view otherwise it will use the default of 'browse',
         // this happens when a forum being used to discuss a node is deleted.
         QName parentType = this.nodeService.getType(parent);
         if (parentType.equals(ForumModel.TYPE_FORUMS))
         {
            this.reDisplayForums = true;
         }
      }

      return super.finishImpl(context, outcome);
   }

   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      outcome = super.doPostCommitProcessing(context, outcome);
      
      if (this.reDisplayForums)
      {
         return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME +
                AlfrescoNavigationHandler.OUTCOME_SEPARATOR + "forumDeleted";
      }
      else
      {
         return outcome;
      }
   }
   
   /**
    * Returns the message bundle id of the confirmation message to display to 
    * the user before deleting the forum.
    * 
    * @return The message bundle id
    */
   @Override
   protected String getConfirmMessageId()
   {
      return "delete_forum_confirm";
   }
}
