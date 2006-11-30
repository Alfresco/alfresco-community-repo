package org.alfresco.web.bean.workflow;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Reassign Work Item" dialog
 * 
 * @author gavinc
 */
public class ReassignWorkItemDialog extends BaseDialogBean
{
   protected String workItemId;
   
   protected WorkflowService workflowService;
   protected PersonService personService;
   
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
            this.workflowService.updateTask(this.workItemId, params, null, null);
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
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /**
    * Property accessed by the Generic Picker component.
    * 
    * @return the array of filter options to show in the users/groups picker
    */
   public SelectItem[] getFilters()
   {
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      return new SelectItem[] {new SelectItem("0", bundle.getString("users"))};
   }
   
   /**
    * Query callback method executed by the Generic Picker component.
    * This method is part of the contract to the Generic Picker, it is up to the backing bean
    * to execute whatever query is appropriate and return the results.
    * 
    * @param filterIndex        Index of the filter drop-down selection
    * @param contains           Text from the contains textbox
    * 
    * @return An array of SelectItem objects containing the results to display in the picker.
    */
   public SelectItem[] pickerCallback(int filterIndex, String contains)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      
      SelectItem[] items;
      
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         // build xpath to match available User/Person objects
         NodeRef peopleRef = personService.getPeopleContainer();
         // NOTE: see SearcherComponentTest
         String xpath = "*[like(@" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + "firstName, '%" + contains + "%', false)" +
                 " or " + "like(@" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + "lastName, '%" + contains + "%', false)]";
         
         List<NodeRef> nodes = searchService.selectNodes(
               peopleRef,
               xpath,
               null,
               this.namespaceService,
               false);
         
         items = new SelectItem[nodes.size()];
         for (int index=0; index<nodes.size(); index++)
         {
            NodeRef personRef = nodes.get(index);
            String firstName = (String)this.nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
            String lastName = (String)this.nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
            String username = (String)this.nodeService.getProperty(personRef, ContentModel.PROP_USERNAME);
            SelectItem item = new SortableSelectItem(username, firstName + " " + lastName, lastName);
            items[index] = item;
         }
         
         Arrays.sort(items);
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         
         items = new SelectItem[0];
      }
      
      return items;
   }

   /**
    * Sets the workflow service to use
    * 
    * @param workflowService The WorkflowService instance
    */
   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }
   
   /**
    * @param permissionService   The PermissionService to set.
    */
   public void setPersonService(PersonService personService)
   {
      this.personService = personService;
   }
}
