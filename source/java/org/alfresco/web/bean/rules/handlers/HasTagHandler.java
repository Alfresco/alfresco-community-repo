/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.bean.rules.handlers;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.repo.action.evaluator.HasTagEvaluator;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Condition handler for the "has-tag" condition.
 * 
 * @author arsenyko
 */
public class HasTagHandler extends BaseConditionHandler
{
   private static final long serialVersionUID = 1L;
   
   public String getJSPPath()
   {
      return getJSPPath(HasTagEvaluator.NAME);
   }

   public void prepareForSave(Map<String, Serializable> conditionProps,
         Map<String, Serializable> repoProps)
   {
      String tag = (String)conditionProps.get(HasTagEvaluator.PARAM_TAG);
      repoProps.put(HasTagEvaluator.PARAM_TAG, tag);
   }

   public void prepareForEdit(Map<String, Serializable> conditionProps,
         Map<String, Serializable> repoProps)
   {
      String tag = (String)repoProps.get(HasTagEvaluator.PARAM_TAG);
      conditionProps.put(HasTagEvaluator.PARAM_TAG, tag);
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> conditionProps)
   {
      Boolean not = (Boolean)conditionProps.get(PROP_CONDITION_NOT);
      String msgId = not.booleanValue() ? "condition_has_tag_not" : "condition_has_tag";
         
      String label = (String) conditionProps.get(HasTagEvaluator.PARAM_TAG);
      
      return MessageFormat.format(Application.getMessage(context, msgId),
            new Object[] {label});
   }
}
