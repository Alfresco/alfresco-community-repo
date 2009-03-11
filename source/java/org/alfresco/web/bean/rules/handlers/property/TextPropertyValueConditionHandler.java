/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.rules.handlers.property;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.rules.handlers.PropertyValueHandler;
import org.alfresco.web.bean.wizard.IWizardBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Condition handler for the "compare-text-property" condition.
 * 
 * @author Jean Barmash
 */
public class TextPropertyValueConditionHandler extends PropertyValueHandler
{
   private static final Log logger = LogFactory.getLog(TextPropertyValueConditionHandler.class);
   public static final String NAME = "compare-text-property";

   public static final String UI_PARAM_OPERATION = "operation";
   public static final String UI_PARAM_QNAME = "qname";

   public String getJSPPath()
   {
      return getJSPPath(getConditionName());
   }

   protected String getConditionName()
   {
      return TextPropertyValueConditionHandler.NAME;
   }

   public void prepareForSave(Map<String, Serializable> conditionParams, Map<String, Serializable> repoProps)
   {
      if (logger.isDebugEnabled())
         logger.debug("Preparing to Save Text Condition Parameters");

      super.prepareForSave(conditionParams, repoProps);
      String propertyString = (String) conditionParams.get(UI_PARAM_QNAME);

      FacesContext fc = FacesContext.getCurrentInstance();
      ServiceRegistry serviceRegistry = Repository.getServiceRegistry(fc);
      QName qname = null;
      
      if ((propertyString.indexOf(':')) == -1)  // TODO: there might be a better way to resolve namespaces 
         qname = QName.createQName(propertyString);
      else
         qname = QName.createQName(propertyString, serviceRegistry.getNamespaceService());
      
      if (logger.isDebugEnabled())
         logger.warn("Storing Property QName  " + qname);
      
      repoProps.put(ComparePropertyValueEvaluator.PARAM_PROPERTY, qname);

      String operation = (String) conditionParams.get(UI_PARAM_OPERATION);
      repoProps.put(ComparePropertyValueEvaluator.PARAM_OPERATION, operation);
   }

   public void prepareForEdit(Map<String, Serializable> conditionProps, Map<String, Serializable> repoProps)
   {
      if (logger.isDebugEnabled())
         logger.debug("Retrieving Text Condition Parameters for editing");
      
      super.prepareForEdit(conditionProps, repoProps);
      conditionProps.put(UI_PARAM_QNAME, ((QName) repoProps.get(ComparePropertyValueEvaluator.PARAM_PROPERTY))
            .toPrefixString());
      conditionProps.put(UI_PARAM_OPERATION, repoProps.get(ComparePropertyValueEvaluator.PARAM_OPERATION).toString());
   }

   public String generateSummary(FacesContext context, IWizardBean wizard, Map<String, Serializable> conditionProps)
   {
      Boolean not = (Boolean) conditionProps.get(PROP_CONDITION_NOT);
      String msgId = getSummaryStringTemplate(not);
      String text = conditionProps.get(PROP_CONTAINS_TEXT).toString();
      String operation = (String) conditionProps.get(UI_PARAM_OPERATION);
      String qname = (String) conditionProps.get(UI_PARAM_QNAME);

      return MessageFormat.format(Application.getMessage(context, msgId), new Object[]
       { qname, Application.getMessage(context, displayOperation(operation)), text });
   }
   
   protected String displayOperation(String operation) 
   {
      ComparePropertyValueOperation op = ComparePropertyValueOperation.valueOf(operation);
      switch (op) 
      {
         case EQUALS: 
            return "property_date_condition_equals";
         case CONTAINS: 
            return "property_condition_contains";
         case BEGINS: 
            return "property_condition_beginswith";
         case ENDS:
            return "property_condition_endswith";
         default: return "property_condition_invalid";
      }
   }
   
   protected String getSummaryStringTemplate(Boolean not)
   {
      String msgId = not.booleanValue() ? "condition_compare_text_property_value_not"
            : "condition_compare_text_property_value";
      return msgId;
   }

}
