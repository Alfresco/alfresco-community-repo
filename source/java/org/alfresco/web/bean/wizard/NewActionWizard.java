/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.bean.wizard;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handler class used by the New Action Wizard 
 * 
 * @author Kevin Roast
 */
public class NewActionWizard extends BaseActionWizard
{
   private static Log logger = LogFactory.getLog(NewActionWizard.class);
   
   private static final String ERROR = "error_action";
   
   // TODO: retrieve these from the config service
   private static final String WIZARD_TITLE_ID = "create_action_title";
   private static final String WIZARD_DESC_ID = "create_action_desc";
   private static final String STEP1_TITLE_ID = "create_action_step1_title";
   private static final String STEP2_TITLE_ID = "create_action_step2_title";
   private static final String FINISH_INSTRUCTION_ID = "create_action_finish_instruction";
   
   /**
    * Deals with the finish button being pressed
    * 
    * @return outcome
    */
   public String finish()
   {
      String outcome = FINISH_OUTCOME;
      
      UserTransaction tx = null;
      
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         // build the action params map based on the selected action instance
         Map<String, Serializable> actionParams = buildActionParams();
         
         // build the action to execute
         Action action = this.actionService.createAction(getAction());
         action.setParameterValues(actionParams);
         
         // execute the action on the current document node
         this.actionService.executeAction(action, this.browseBean.getDocument().getNodeRef());
         
         if (logger.isDebugEnabled())
         {
            logger.debug("Executed action '" + this.action + 
                         "' with action params of " + 
                         this.currentActionProperties);
         }
         
         // reset the current document properties/aspects in case we have changed them
         // during the execution of the custom action
         this.browseBean.getDocument().reset();
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), ERROR), e.getMessage()), e);
         outcome = null;
      }
      
      return outcome;
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getWizardDescription()
    */
   public String getWizardDescription()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), WIZARD_DESC_ID);
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getWizardTitle()
    */
   public String getWizardTitle()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), WIZARD_TITLE_ID);
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepDescription()
    */
   public String getStepDescription()
   {
      return "";
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepTitle()
    */
   public String getStepTitle()
   {
      String stepTitle = null;
      
      switch (this.currentStep)
      {
         case 1:
         {
            stepTitle = Application.getMessage(FacesContext.getCurrentInstance(), STEP1_TITLE_ID);
            break;
         }
         case 2:
         {
            stepTitle = Application.getMessage(FacesContext.getCurrentInstance(), STEP2_TITLE_ID);
            break;
         }
         case 3:
         {
            stepTitle = Application.getMessage(FacesContext.getCurrentInstance(), SUMMARY_TITLE_ID);
            break;
         }
         default:
         {
            stepTitle = "";
         }
      }
      
      return stepTitle;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepInstructions()
    */
   public String getStepInstructions()
   {
      String stepInstruction = null;
      
      switch (this.currentStep)
      {
         case 3:
         {
            stepInstruction = Application.getMessage(FacesContext.getCurrentInstance(), FINISH_INSTRUCTION_ID);
            break;
         }
         default:
         {
            stepInstruction = Application.getMessage(FacesContext.getCurrentInstance(), DEFAULT_INSTRUCTION_ID);
         }
      }
      
      return stepInstruction;
   }
   
   /**
    * Initialises the wizard
    */
   public void init()
   {
      super.init();
   }

   /**
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      String summaryAction = this.actionService.getActionDefinition(
            this.action).getTitle();
      
      return buildSummary(
            new String[] {"Action"},
            new String[] {summaryAction});
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#determineOutcomeForStep(int)
    */
   protected String determineOutcomeForStep(int step)
   {
      String outcome = null;
      
      switch(step)
      {
         case 1:
         {
            outcome = "action";
            break;
         }
         case 2:
         {
            outcome = this.action;
            break;
         }
         case 3:
         {
            outcome = "summary";
            break;
         }
         default:
         {
            outcome = CANCEL_OUTCOME;
         }
      }
      
      return outcome;
   }   
}
