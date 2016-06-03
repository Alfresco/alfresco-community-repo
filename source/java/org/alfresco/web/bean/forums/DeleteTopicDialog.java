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
 * Bean implementation for the "Delete Topic" dialog
 * 
 * @author gavinc
 */
public class DeleteTopicDialog extends DeleteSpaceDialog
{
   private static final long serialVersionUID = 548182341698381545L;
   
   protected boolean reDisplayTopics;

   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // reset the reDisplayTopics flag
      this.reDisplayTopics = false;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // find out what the parent type of the node being deleted 
      Node node = this.browseBean.getActionSpace();
      ChildAssociationRef assoc = this.getNodeService().getPrimaryParent(node.getNodeRef());
      if (assoc != null)
      {
         NodeRef parent = assoc.getParentRef();
         QName parentType = this.getNodeService().getType(parent);
         if (parentType.equals(ForumModel.TYPE_FORUM))
         {
            this.reDisplayTopics = true;
         }
      }

      return super.finishImpl(context, outcome);
   }

   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      outcome = super.doPostCommitProcessing(context, outcome);
      
      if (this.reDisplayTopics)
      {
         return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME +
                AlfrescoNavigationHandler.OUTCOME_SEPARATOR + "topicDeleted";
      }
      else
      {
         return outcome;
      }
   }
   
   /**
    * Returns the message bundle id of the confirmation message to display to 
    * the user before deleting the topic.
    * 
    * @return The message bundle id
    */
   @Override
   protected String getConfirmMessageId()
   {
      return "delete_topic_confirm";
   }
}
