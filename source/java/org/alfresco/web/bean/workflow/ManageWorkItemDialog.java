package org.alfresco.web.bean.workflow;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.config.DialogsConfigElement.DialogButtonConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Manage WorkItem" dialog.
 * 
 * @author gavinc
 */
public class ManageWorkItemDialog extends BaseDialogBean
{
   private static final Log logger = LogFactory.getLog(ManageWorkItemDialog.class);
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      return null;
   }
   
   @Override
   public List<DialogButtonConfig> getAdditionalButtons()
   {
      List<DialogButtonConfig> buttons = new ArrayList<DialogButtonConfig>(1);
      buttons.add(new DialogButtonConfig("reassign-button",
            "Reassign", null, "#{ManageWorkItemDialog.reassign}", "false", null));
      
      return buttons; 
   }
   
   // ------------------------------------------------------------------------------
   // Event handlers
   
   public void approve()
   {
      logger.info("approve button was pressed");
   }
   
   public void reject()
   {
      logger.info("reject button was pressed");
   }
   
   public void reassign()
   {
      logger.info("reassign button was pressed");
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   public boolean getApproveDisabled()
   {
      return true;
   }
}
