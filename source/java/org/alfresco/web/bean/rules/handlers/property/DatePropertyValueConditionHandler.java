package org.alfresco.web.bean.rules.handlers.property;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;

/**
 * Condition handler for the "compare-date-property" condition.
 * 
 * @author Jean Barmash
 */
public class DatePropertyValueConditionHandler extends TextPropertyValueConditionHandler
{     

   public static final String NAME = "compare-date-property";
         
   @Override
   protected String getConditionName()
   {
      return DatePropertyValueConditionHandler.NAME;
   }

   //Responsible for serializing the value of the property, which could have different types
   protected void prepareForSaveWithCorrectType(Map<String, Serializable> conditionProps, Map<String, Serializable> repoProps)
   {
      Date date = (Date) conditionProps.get(PROP_CONTAINS_TEXT);
      repoProps.put(ComparePropertyValueEvaluator.PARAM_VALUE, date);
   }
   
   protected void prepareForEditWithCorrectType(Map<String, Serializable> conditionProps,
         Map<String, Serializable> repoProps)
   {
      Date dateValue = (Date) repoProps.get(ComparePropertyValueEvaluator.PARAM_VALUE);
      conditionProps.put(PROP_CONTAINS_TEXT, dateValue);
   }

   @Override
   protected String getSummaryStringTemplate(Boolean not)
   {
      String msgId = not.booleanValue() ? "condition_compare_date_property_value_not"
            : "condition_compare_date_property_value";
      return msgId;
   }

   @Override
   protected String displayOperation(String operation) 
   {
      ComparePropertyValueOperation op = ComparePropertyValueOperation.valueOf(operation);
      switch (op) 
      {
         case EQUALS: 
            return "property_date_condition_equals";
         case GREATER_THAN: 
            return "property_date_condition_greaterthan";
         case GREATER_THAN_EQUAL: 
            return "property_date_condition_greaterthanequals";
         case LESS_THAN:
            return "property_date_condition_lessthan";
         case LESS_THAN_EQUAL: 
            return "property_date_condition_lessthanequals";
         default: return "property_condition_invalid";
      }
   }
   
   
}
