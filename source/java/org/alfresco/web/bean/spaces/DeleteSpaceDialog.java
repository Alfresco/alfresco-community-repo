package org.alfresco.web.bean.spaces;

import java.text.MessageFormat;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.content.DeleteContentDialog;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Delete Space" dialog
 * 
 * @author gavinc
 */
public class DeleteSpaceDialog extends BaseDialogBean
{
   private static final Log logger = LogFactory.getLog(DeleteContentDialog.class);

   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // get the space to delete
      Node node = this.browseBean.getActionSpace();
      if (node != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Trying to delete space: " + node.getId());
            
         this.nodeService.deleteNode(node.getNodeRef());
      }
      else
      {
         logger.warn("WARNING: delete called without a current Space!");
      }
      
      return outcome;
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      Node node = this.browseBean.getActionSpace();
      
      // remove this node from the breadcrumb if required
      this.browseBean.removeSpaceFromBreadcrumb(node);
            
      // add a message to inform the user that the delete was OK 
      String statusMsg = MessageFormat.format(
            Application.getMessage(FacesContext.getCurrentInstance(), "status_space_deleted"), 
            new Object[]{node.getName()});
      Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, statusMsg);
      
      // clear action context
      this.browseBean.setActionSpace(null);
      
      // setting the outcome will show the browse view again
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + 
             AlfrescoNavigationHandler.OUTCOME_SEPARATOR + "browse";
   }
   
   @Override
   protected String getErrorMessageId()
   {
      return "error_delete_space";
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
               "delete_space_confirm");
      
      return MessageFormat.format(fileConfirmMsg, 
            new Object[] {this.browseBean.getActionSpace().getName()});
   }
}
