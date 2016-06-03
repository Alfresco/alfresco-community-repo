package org.alfresco.web.bean.rules.handlers.property;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;

/**
 * Condition handler for the "compare-integer-property" condition.
 * 
 * @author Jean Barmash
 */
public class IntegerPropertyValueConditionHandler extends TextPropertyValueConditionHandler
{

   public static final String NAME = "compare-integer-property";

   @Override
   protected String getConditionName()
   {
      return IntegerPropertyValueConditionHandler.NAME;
   }
   
   //Responsible for serializing the value of the property, which could have different types
   protected void prepareForSaveWithCorrectType(Map<String, Serializable> conditionProps, Map<String, Serializable> repoProps)
   {
      Long number = Long.parseLong((String)conditionProps.get(PROP_CONTAINS_TEXT));
      repoProps.put(ComparePropertyValueEvaluator.PARAM_VALUE, number );
   }
   
   protected void prepareForEditWithCorrectType(Map<String, Serializable> conditionProps,
         Map<String, Serializable> repoProps)
   {
      Long propValue = (Long)repoProps.get(ComparePropertyValueEvaluator.PARAM_VALUE);
      conditionProps.put(PROP_CONTAINS_TEXT, propValue.toString());
   }
   
   @Override
   protected String getSummaryStringTemplate(Boolean not)
   {
      String msgId = not.booleanValue() ? "condition_compare_integer_property_value_not"
            : "condition_compare_integer_property_value";
      return msgId;
   }

   @Override
   protected String displayOperation(String operation) 
   {
      ComparePropertyValueOperation op = ComparePropertyValueOperation.valueOf(operation);
      switch (op) 
      {
         case EQUALS: 
            return "property_condition_equals";
         case GREATER_THAN: 
            return "property_condition_greaterthan";
         case GREATER_THAN_EQUAL: 
            return "property_condition_greaterthanequals";
         case LESS_THAN:
            return "property_condition_lessthan";
         case LESS_THAN_EQUAL: 
            return "property_condition_lessthanequals";
         default: return "property_condition_invalid";
      }
   }

   
}
