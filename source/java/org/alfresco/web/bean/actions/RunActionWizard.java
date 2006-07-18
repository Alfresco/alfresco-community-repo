package org.alfresco.web.bean.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;

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
         
         // get the action handler to prepare for the save
         Map<String, Serializable> repoActionParams = new HashMap<String, Serializable>();
         IHandler handler = this.actionHandlers.get(this.action);
         if (handler != null)
         {
            handler.prepareForSave(actionParams, repoActionParams);
         }
         
         // add the action to the rule
         Action action = this.actionService.createAction(actionName);
         action.setParameterValues(repoActionParams);
         
         // execute the action on the current document node
         NodeRef nodeRef = new NodeRef(Repository.getStoreRef(), this.parameters.get("id"));
         this.actionService.executeAction(action, nodeRef);
      }

      return outcome;
   }
   
   @Override
   public List<SelectItem> getActions()
   {
      if (this.actions == null)
      {
         NodeRef nodeRef = new NodeRef(Repository.getStoreRef(), this.parameters.get("id"));
         List<ActionDefinition> ruleActions = this.actionService.getActionDefinitions(nodeRef);
         this.actions = new ArrayList<SelectItem>();
         for (ActionDefinition ruleActionDef : ruleActions)
         {
            this.actions.add(new SelectItem(ruleActionDef.getName(), ruleActionDef.getTitle()));
         }
         
         // make sure the list is sorted by the label
         QuickSort sorter = new QuickSort(this.actions, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
         
         // add the select an action item at the start of the list
         this.actions.add(0, new SelectItem("null", 
               Application.getMessage(FacesContext.getCurrentInstance(), "select_an_action")));
      }
      
      return this.actions;
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // reset the current document properties/aspects in case we have changed them
      // during the execution of the custom action
      Node document = this.browseBean.getDocument();
      if (document != null)
      {
         document.reset();
      }
      
      // reset the current space properties/aspects as well in case we have 
      // changed them during the execution of the custom action
      Node space = this.browseBean.getActionSpace();
      if (space != null)
      {
         space.reset();
      }
      
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
