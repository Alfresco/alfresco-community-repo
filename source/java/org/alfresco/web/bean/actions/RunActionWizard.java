package org.alfresco.web.bean.actions;

import java.io.Serializable;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.web.app.Application;

/**
 * Bean implementation for the "Run Action" wizard.
 * 
 * @author gavinc
 */
public class RunActionWizard extends BaseActionWizard
{
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // execute each action added in the wizard
      for (Map<String, Serializable> actionParams : this.allActionsProperties)
      {
         // use the base class version of buildActionParams(), but for this we need 
         // to setup the currentActionProperties and action variables
         String actionName = (String)actionParams.get(PROP_ACTION_NAME);
         this.action = actionName;
         this.currentActionProperties = actionParams;
         Map<String, Serializable> repoActionParams = buildActionParams();
         
         // add the action to the rule
         Action action = this.actionService.createAction(actionName);
         action.setParameterValues(repoActionParams);
         
         // execute the action on the current document node
         this.actionService.executeAction(action, this.browseBean.getDocument().getNodeRef());
      }

      return outcome;
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // reset the current document properties/aspects in case we have changed them
      // during the execution of the custom action
      this.browseBean.getDocument().reset();
      
      return outcome;
   }
   
   @Override
   protected String getErrorMessageId()
   {
      return "error_actions";
   }

   /**
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      // create the summary using all the actions
      StringBuilder actionsSummary = new StringBuilder();
      for (Map<String, Serializable> props : this.allActionsProperties)
      {
         actionsSummary.append(props.get(PROP_ACTION_SUMMARY));
         actionsSummary.append("<br/>");
      }
      
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());

      return buildSummary(
            new String[] {bundle.getString("actions")},
            new String[] {actionsSummary.toString()});
   }
   
   @Override
   public boolean getNextButtonDisabled()
   {
      return (this.allActionsDataModel.getRowCount() == 0);
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return (this.allActionsDataModel.getRowCount() == 0);
   }
}
