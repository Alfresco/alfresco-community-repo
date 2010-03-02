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
import javax.faces.model.SelectItem;

import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.rules.CreateRuleWizard;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Condition handler for the "compare-mime-type" condition
 * 
 * @author gavinc
 */
public class CompareMimeTypeHandler extends BaseConditionHandler
{
   private static final long serialVersionUID = 6421611697032505073L;
   
   protected static final String PROP_MIMETYPE = "mimetype";
   
   public String getJSPPath()
   {
      return getJSPPath(CompareMimeTypeEvaluator.NAME);
   }

   public void prepareForSave(Map<String, Serializable> conditionProps,
         Map<String, Serializable> repoProps)
   {
      String mimeType = (String)conditionProps.get(PROP_MIMETYPE);
      repoProps.put(CompareMimeTypeEvaluator.PARAM_VALUE, mimeType);
   }

   public void prepareForEdit(Map<String, Serializable> conditionProps,
         Map<String, Serializable> repoProps)
   {
      String mimeType = (String)repoProps.get(CompareMimeTypeEvaluator.PARAM_VALUE);
      conditionProps.put(PROP_MIMETYPE, mimeType);
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> conditionProps)
   {
      Boolean not = (Boolean)conditionProps.get(PROP_CONDITION_NOT);
      String msgId = not.booleanValue() ? "condition_compare_mime_type_not" : "condition_compare_mime_type";
         
      String label = null;
      String mimetype = (String)conditionProps.get(PROP_MIMETYPE);
      for (SelectItem item : ((CreateRuleWizard)wizard).getMimeTypes())
      {
         if (item.getValue().equals(mimetype))
         {
            label = item.getLabel();
            break;
         }
      }
      
      return MessageFormat.format(Application.getMessage(context, msgId),
            new Object[] {label});
   }
}
