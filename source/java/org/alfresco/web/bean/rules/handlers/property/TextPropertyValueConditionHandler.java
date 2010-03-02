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
package org.alfresco.web.bean.rules.handlers.property;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
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
   private static final String DEFAULT_NAMESPACE = NamespaceService.CONTENT_MODEL_PREFIX;

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

      QName qname = null;
      
      // TODO: there might be a better way to resolve namespaces instead of propertyString.indexOf(':')?
      
      // The part of ADB-131 fix. We use default namespase prefix of content model 'cm'.
      // It is necessary to enable an ability to set the property as its localName (e.g. description).
      // It keeps also an ability to enter a user content model text properties such as 'my:description'
      propertyString = propertyString.indexOf(':') == -1 ?
                       DEFAULT_NAMESPACE + QName.NAMESPACE_PREFIX + propertyString :
                       propertyString;
      qname = QName.createQName(propertyString, getNamespaceService());
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
      // The part of ADB-131 fix. The NamespaceService is used to get a valid prefix string.
      // When the user attempt to edit a rule a 'Summary' string contain an invalid property name
      // and then the property will be saved with default prefix 'cm'. But the property may be as 'my:description' 
      conditionProps.put(UI_PARAM_QNAME, ((QName) repoProps.get(ComparePropertyValueEvaluator.PARAM_PROPERTY)).toPrefixString(getNamespaceService()));
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
