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
   private static final long serialVersionUID = -4246549059188399460L;
   
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
      NodeRef parent = null;
      ChildAssociationRef assoc = this.getNodeService().getPrimaryParent(node.getNodeRef());
      if (assoc != null)
      {
         // get the parent node
         parent = assoc.getParentRef();
         
         // if the parent type is a forum space then we need the dialog to go
         // back to the forums view otherwise it will use the default of 'browse',
         // this happens when a forum being used to discuss a node is deleted.
         QName parentType = this.getNodeService().getType(parent);
         if (parentType.equals(ForumModel.TYPE_FORUMS))
         {
            this.reDisplayForums = true;
         }
      }

      // delete the forum itself
      outcome = super.finishImpl(context, outcome);
      
      // remove the discussable aspect if appropriate
      if (assoc != null && parent != null)
      {
         // get the association type
         QName type = assoc.getTypeQName();
         if (type.equals(ForumModel.ASSOC_DISCUSSION))
         {
            // if the association type is the 'discussion' association we
            // need to remove the discussable aspect from the parent node
            this.getNodeService().removeAspect(parent, ForumModel.ASPECT_DISCUSSABLE);
         }
      }
      
      return outcome;
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
