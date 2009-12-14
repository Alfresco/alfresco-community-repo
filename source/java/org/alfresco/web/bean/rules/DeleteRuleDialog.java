/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.rules;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DeleteRuleDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 2009345695752548885L;
   
   private static final String MSG_ERROR_DELETE_RULE = "error_delete_rule";
   private static final String MSG_DELETE_RULE = "delete_rule";
   private static final String MSG_YES = "yes";
   private static final String MSG_NO = "no";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";

   transient private Rule currentRule;
   transient private RuleService ruleService;
   private static Log logger = LogFactory.getLog(DeleteRuleDialog.class);

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      String nodeRef = parameters.get("nodeRef");

      // make sure nodeRef was supplied
      ParameterCheck.mandatoryString("nodeRef", nodeRef);

      if (logger.isDebugEnabled())
         logger.debug("Rule clicked, it's nodeRef is: " + nodeRef);

      this.currentRule = getRuleService().getRule(new NodeRef(nodeRef));

   }

   /**
    * @param ruleService Sets the rule service to use
    */
   public void setRuleService(RuleService ruleService)
   {
      this.ruleService = ruleService;
   }
   
   /**
    * @return ruleService
    */
   protected RuleService getRuleService()
   {
    //check for null for cluster environment
      if (ruleService == null)
      {
         ruleService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getRuleService();
      }
      return ruleService;
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
    * @return The space to work against
    */
   public Node getSpace()
   {
      return this.browseBean.getActionSpace();
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      if (this.currentRule != null)
      {
         try
         {
            String ruleTitle = this.currentRule.getTitle();

            getRuleService().removeRule(getSpace().getNodeRef(), this.currentRule);

            // clear the current rule
            this.currentRule = null;

            if (logger.isDebugEnabled())
               logger.debug("Deleted rule '" + ruleTitle + "'");
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(), MSG_ERROR_DELETE_RULE) + err.getMessage(), err);
            ReportedException.throwIfNecessary(err);
         }
      }
      else
      {
         logger.warn("WARNING: deleteOK called without a current Rule!");
      }

      return outcome;
   }

   @Override
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_DELETE_RULE) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE)
               + currentRule.getTitle() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }

   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_NO);
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_YES);
   }

}
