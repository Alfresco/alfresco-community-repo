/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.bean.rules.handlers;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.repo.action.evaluator.HasAspectEvaluator;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.rules.CreateRuleWizard;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Condition handler for the "has-aspect" condition.
 * 
 * @author gavinc
 */
public class HasAspectHandler extends BaseConditionHandler
{
   protected static final String PROP_ASPECT = "aspect";
   
   public String getJSPPath()
   {
      return getJSPPath(HasAspectEvaluator.NAME);
   }

   public void prepareForSave(Map<String, Serializable> conditionProps,
         Map<String, Serializable> repoProps)
   {
      QName aspect = QName.createQName((String)conditionProps.get(PROP_ASPECT));
      repoProps.put(HasAspectEvaluator.PARAM_ASPECT, aspect);
   }

   public void prepareForEdit(Map<String, Serializable> conditionProps,
         Map<String, Serializable> repoProps)
   {
      QName aspect = (QName)repoProps.get(HasAspectEvaluator.PARAM_ASPECT);
      conditionProps.put(PROP_ASPECT, aspect.toString());
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> conditionProps)
   {
      Boolean not = (Boolean)conditionProps.get(PROP_CONDITION_NOT);
      String msgId = not.booleanValue() ? "condition_has_aspect_not" : "condition_has_aspect";
         
      String label = null;
      String aspectName = (String)conditionProps.get(PROP_ASPECT);
      for (SelectItem item : ((CreateRuleWizard)wizard).getTestableAspects())
      {
         if (item.getValue().equals(aspectName))
         {
            label = item.getLabel();
            break;
         }
      }
      
      return MessageFormat.format(Application.getMessage(context, msgId),
            new Object[] {label});
   }
}
