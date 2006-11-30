package org.alfresco.web.bean.wcm;

import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the AVM "Delete File" dialog
 * 
 * @author kevinr
 */
public class DeleteFileDialog extends BaseDialogBean
{
   private static final Log logger = LogFactory.getLog(DeleteFileDialog.class);
   
   protected AVMService avmService;
   protected AVMBrowseBean avmBrowseBean;
   
   
   /**
    * @param avmBrowseBean The avmBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }

   /**
    * @param avmService    The avmService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // get the content to delete
      AVMNode node = this.avmBrowseBean.getAvmActionNode();
      if (node != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Trying to delete AVM node: " + node.getPath());
         
         // delete the node
         this.avmService.removeNode(
               node.getPath().substring(0, node.getPath().lastIndexOf('/')),
               node.getPath().substring(node.getPath().lastIndexOf('/') + 1));         
      }
      else
      {
         logger.warn("WARNING: delete called without a current AVM Node!");
      }
      
      return outcome;
   }
      
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
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
               "delete_avm_file_confirm");
      
      return MessageFormat.format(fileConfirmMsg, 
            new Object[] {this.avmBrowseBean.getAvmActionNode().getName()});
   }
}
