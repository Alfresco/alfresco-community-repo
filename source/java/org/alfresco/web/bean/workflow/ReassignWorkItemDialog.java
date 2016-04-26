package org.alfresco.web.bean.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Reassign Work Item" dialog
 * 
 * @author gavinc
 */
public class ReassignWorkItemDialog extends BaseReassignDialog
{
   private static final long serialVersionUID = 6501900045849920148L;

   protected String workItemId;
   
   private static final Log logger = LogFactory.getLog(ReassignWorkItemDialog.class);   

   
   // ------------------------------------------------------------------------------
   // Dialog implementation
 
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.workItemId = this.parameters.get("workitem-id");
      if (this.workItemId == null || this.workItemId.length() == 0)
      {
         throw new IllegalArgumentException("Reassign workitem dialog called without task id");
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("Reassigning work item with id: " + this.workItemId);
      
      UIComponent picker = context.getViewRoot().findComponent("dialog:dialog-body:user-picker");
      
      if (picker != null && picker instanceof UIGenericPicker)
      {
         UIGenericPicker userPicker = (UIGenericPicker)picker;
         String[] user = userPicker.getSelectedResults();
         if (user != null && user.length > 0)
         {
            // create a map to hold the new owner property then update the task
            String userName = user[0];
            Map<QName, Serializable> params = new HashMap<QName, Serializable>(1);
            params.put(ContentModel.PROP_OWNER, userName);
            this.getWorkflowService().updateTask(this.workItemId, params, null, null);
         }
         else
         {
            if (logger.isWarnEnabled())
               logger.warn("Failed to find selected user, reassign was unsuccessful");
         }
      }
      else
      {
         if (logger.isWarnEnabled())
            logger.warn("Failed to find user-picker component, reassign was unsuccessful");
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Reassigning work item with id: " + this.workItemId);
      
      return outcome;
   }
   
   @Override
   protected String getErrorMessageId()
   {
      return "error_reassign_workitem";
   }
}
