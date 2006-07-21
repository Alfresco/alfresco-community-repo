package org.alfresco.web.bean.forums;

import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
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
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   /**
    * Returns the confirmation to display to the user before deleting the content.
    * 
    * @return The formatted message to display
    */
   public String getConfirmMessage()
   {
      String fileConfirmMsg = Application.getMessage(FacesContext.getCurrentInstance(), 
               "delete_forum_confirm");
      
      return MessageFormat.format(fileConfirmMsg, 
            new Object[] {this.browseBean.getActionSpace().getName()});
   }
}
