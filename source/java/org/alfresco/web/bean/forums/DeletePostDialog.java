package org.alfresco.web.bean.forums;

import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.content.DeleteContentDialog;

/**
 * Bean implementation for the "Delete Post" dialog.
 * 
 * @author gavinc
 */
public class DeletePostDialog extends DeleteContentDialog
{
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      super.doPostCommitProcessing(context, outcome);
      
      return this.getDefaultFinishOutcome();
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
      String postConfirmMsg = Application.getMessage(FacesContext.getCurrentInstance(), 
               "delete_post_confirm");
      
      return MessageFormat.format(postConfirmMsg, 
            new Object[] {this.browseBean.getDocument().getProperties().get("creator")});
   }
}
