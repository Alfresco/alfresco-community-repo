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
package org.alfresco.web.bean.rules;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.repo.action.executer.ExecuteAllRulesActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing bean for the manage content rules dialog
 *  
 * @author gavinc
 */
public class RulesBean implements IContextListener
{
   private static final String MSG_ERROR_DELETE_RULE = "error_delete_rule";
   private static final String MSG_REAPPLY_RULES_SUCCESS = "reapply_rules_success";
   private static final String LOCAL = "local";
   private static final String INHERITED = "inherited";
   
   private static Log logger = LogFactory.getLog(RulesBean.class);
   
   private String viewMode = INHERITED;
   protected BrowseBean browseBean;
   protected RuleService ruleService;
   private List<WrappedRule> rules;
   private Rule currentRule;
   private UIRichList richList;
   private ActionService actionService;
   
   
   /**
    * Default constructor
    */
   public RulesBean()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
   }
   
   /**
    * Returns the current view mode the list of rules is in
    * 
    * @return The current view mode
    */
   public String getViewMode()
   {
      return this.viewMode;
   }
   
   /**
    * @return The space to work against
    */
   public Node getSpace()
   {
      return this.browseBean.getActionSpace();
   }
   
   /**
    * Returns the list of rules to display
    * 
    * @return
    */
   public List<WrappedRule> getRules()
   {
      boolean includeInherited = true;
      
      if (this.viewMode.equals(LOCAL))
      {
         includeInherited = false;
      }

      // get the rules from the repository
      List<Rule> repoRules = this.ruleService.getRules(getSpace().getNodeRef(), includeInherited);
      this.rules = new ArrayList<WrappedRule>(repoRules.size());
      
      // wrap them all passing the current space
      for (Rule rule : repoRules)
      {
         WrappedRule wrapped = new WrappedRule(rule, getSpace().getNodeRef());
         this.rules.add(wrapped);
      }
      
      return this.rules;
   }
   
   /**
    * Handles a rule being clicked ready for an action i.e. edit or delete
    * 
    * @param event The event representing the click
    */
   public void setupRuleAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         if (logger.isDebugEnabled())
            logger.debug("Rule clicked, it's id is: " + id);
         
         this.currentRule = this.ruleService.getRule(
               getSpace().getNodeRef(), id);
         
         // refresh list
         contextUpdated();
      }
   }
   
   /**
    * Reapply the currently defines rules to the
    * @param event
    */
   public void reapplyRules(ActionEvent event)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      
      UserTransaction tx = null;
      
      try
      {
         tx = Repository.getUserTransaction(fc);
         tx.begin();
         
         // Create the the apply rules action
         Action action = this.actionService.createAction(ExecuteAllRulesActionExecuter.NAME);
         
         // Set the include inherited parameter to match the current filter value
         boolean executeInherited = true;
         if (LOCAL.equals(this.getViewMode()) == true)
         {
             executeInherited = false;
         }
         action.setParameterValue(ExecuteAllRulesActionExecuter.PARAM_EXECUTE_INHERITED_RULES, executeInherited);
         
         // Execute the action
         this.actionService.executeAction(action, this.getSpace().getNodeRef());
         
         // TODO how do I get the message here ...
         String msg = Application.getMessage(fc, MSG_REAPPLY_RULES_SUCCESS);
         FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
         String formId = Utils.getParentForm(fc, event.getComponent()).getClientId(fc);
         fc.addMessage(formId + ":rulesList", facesMsg);
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               fc, Repository.ERROR_GENERIC), e.getMessage()), e);
      }
   }
   
   /**
    * Returns the current rule 
    * 
    * @return The current rule
    */
   public Rule getCurrentRule()
   {
      return this.currentRule;
   }
   
   /**
    * Handler called upon the completion of the Delete Rule page
    * 
    * @return outcome
    */
   public String deleteOK()
   {
      String outcome = null;
      
      if (this.currentRule != null)
      {
         try
         {
            String ruleTitle = this.currentRule.getTitle();
            
            this.ruleService.removeRule(getSpace().getNodeRef(),
                  this.currentRule);
            
            // clear the current rule
            this.currentRule = null;
            
            // setting the outcome will show the browse view again
            outcome = "manageRules";
            
            if (logger.isDebugEnabled())
               logger.debug("Deleted rule '" + ruleTitle + "'");
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(Application.getMessage(
                  FacesContext.getCurrentInstance(), MSG_ERROR_DELETE_RULE) + err.getMessage(), err);
         }
      }
      else
      {
         logger.warn("WARNING: deleteOK called without a current Rule!");
      }
      
      return outcome;
   }
   
   /**
    * Change the current view mode based on user selection
    * 
    * @param event      ActionEvent
    */
   public void viewModeChanged(ActionEvent event)
   {
      UIModeList viewList = (UIModeList)event.getComponent();
      this.viewMode = viewList.getValue().toString();
      
      // force the list to be re-queried when the page is refreshed
      if (this.richList != null)
      {
         this.richList.setValue(null);
      }
   }

   /**
    * Sets the UIRichList component being used by this backing bean
    * 
    * @param richList UIRichList component
    */
   public void setRichList(UIRichList richList)
   {
      this.richList = richList;
   }
   
   /**
    * Returns the UIRichList component being used by this backing bean
    * 
    * @return UIRichList component
    */
   public UIRichList getRichList()
   {
      return this.richList;
   }
   
   /**
    * @param browseBean The BrowseBean to set.
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }
   
   /**
    * @param ruleService Sets the rule service to use
    */
   public void setRuleService(RuleService ruleService)
   {
      this.ruleService = ruleService;
   }
   
   /**
    * Set the action service to use
    * 
    * @param actionService      the action service
    */
   public void setActionService(ActionService actionService)
   {
      this.actionService = actionService;
   }

   
   // ------------------------------------------------------------------------------
   // IContextListener implementation

   /**
    * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
    */
   public void contextUpdated()
   {
      if (this.richList != null)
      {
         this.richList.setValue(null);
      }
   }
   
   
   /**
    * Inner class to wrap the Rule objects so we can expose a flag to indicate whether
    * the rule is a local or inherited rule
    */
   public static class WrappedRule
   {
      private Rule rule;
      private NodeRef ruleNode;
      
      /**
       * Constructs a RuleWrapper object
       * 
       * @param rule The rule we are wrapping
       * @param ruleNode The node the rules belong to 
       */
      public WrappedRule(Rule rule, NodeRef ruleNode)
      {
         this.rule = rule;
         this.ruleNode = ruleNode;
      }
      
      /**
       * Returns the rule being wrapped
       * 
       * @return The wrapped Rule
       */
      public Rule getRule()
      {
         return this.rule;
      }
      
      /**
       * Determines whether the current rule is a local rule or
       * has been inherited from a parent
       * 
       * @return true if the rule is defined on the current node
       */
      public boolean getLocal()
      {
         return ruleNode.equals(this.rule.getOwningNodeRef());
      }

      /** Methods to support sorting of the rules list in a table  */
      
      /**
       * Returns the rule id
       * 
       * @return The id
       */
      public String getId()
      {
         return this.rule.getId();
      }
      
      /**
       * Returns the rule title
       * 
       * @return The title
       */
      public String getTitle()
      {
         return this.rule.getTitle();
      }
      
      /**
       * Returns the rule description
       * 
       * @return The description
       */
      public String getDescription()
      {
         return this.rule.getDescription();
      }
      
      /**
       * Returns the created date
       * 
       * @return The created date
       */
      public Date getCreatedDate()
      {
         return this.rule.getCreatedDate();
      }
      
      /**
       * Returns the modfified date
       * 
       * @return The modified date
       */
      public Date getModifiedDate()
      {
         return this.rule.getModifiedDate();
      }
   }
}
