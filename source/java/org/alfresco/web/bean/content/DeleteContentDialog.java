package org.alfresco.web.bean.content;

import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Delete Content" dialog
 * 
 * @author gavinc
 */
public class DeleteContentDialog extends BaseDialogBean
{
   private static final Log logger = LogFactory.getLog(DeleteContentDialog.class);
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // get the content to delete
      Node node = this.browseBean.getDocument();
      if (node != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Trying to delete content node: " + node.getId());
         
         // delete the node
         this.nodeService.deleteNode(node.getNodeRef());         
      }
      else
      {
         logger.warn("WARNING: delete called without a current Document!");
      }
      
      return outcome;
   }
      
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // clear action context
      this.browseBean.setDocument(null);
            
      // setting the outcome will show the browse view again
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME +
             AlfrescoNavigationHandler.OUTCOME_SEPARATOR + "browse";
   }
   
   @Override
   protected String getErrorMessageId()
   {
      return "error_delete_file";
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
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
               "delete_file_confirm");
      
      return MessageFormat.format(fileConfirmMsg, 
            new Object[] {this.browseBean.getDocument().getName()});
   }
}
