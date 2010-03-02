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

import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Condition handler for the "compare-property-value" condition.
 * 
 * @author gavinc
 */
public class PropertyValueHandler extends BaseConditionHandler
{
   private static final long serialVersionUID = 6718858865738420012L;
   
   public static final String PROP_CONTAINS_TEXT = "containstext";
   
   public String getJSPPath()
   {
      return getJSPPath(ComparePropertyValueEvaluator.NAME);
   }

   public void prepareForSave(Map<String, Serializable> conditionProps,
         Map<String, Serializable> repoProps)
   {
      prepareForSaveWithCorrectType(conditionProps, repoProps);
   }

   //Responsible for serializing the value of the property, which could have different types
   protected void prepareForSaveWithCorrectType(Map<String, Serializable> conditionProps, Map<String, Serializable> repoProps)
   {
      String text = (String)conditionProps.get(PROP_CONTAINS_TEXT);
      repoProps.put(ComparePropertyValueEvaluator.PARAM_VALUE, text);
   }

   public void prepareForEdit(Map<String, Serializable> conditionProps,
         Map<String, Serializable> repoProps)
   {
      prepareForEditWithCorrectType(conditionProps, repoProps);
   }

   protected void prepareForEditWithCorrectType(Map<String, Serializable> conditionProps,
         Map<String, Serializable> repoProps)
   {
      String propValue = (String)repoProps.get(ComparePropertyValueEvaluator.PARAM_VALUE);
      conditionProps.put(PROP_CONTAINS_TEXT, propValue);
   }

   
   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> conditionProps)
   {
      Boolean not = (Boolean)conditionProps.get(PROP_CONDITION_NOT);
      String msgId = not.booleanValue() ? 
            "condition_compare_property_value_not" : "condition_compare_property_value";
      
      String text = (String)conditionProps.get(PROP_CONTAINS_TEXT);
      
      return MessageFormat.format(Application.getMessage(context, msgId),
            new Object[] {text});
   }
}
