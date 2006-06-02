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
   public static final String PROP_CONTAINS_TEXT = "containstext";
   
   public String getJSPPath()
   {
      return getJSPPath(ComparePropertyValueEvaluator.NAME);
   }

   public void prepareForSave(Map<String, Serializable> conditionProps,
         Map<String, Serializable> repoProps)
   {
      String text = (String)conditionProps.get(PROP_CONTAINS_TEXT);
      repoProps.put(ComparePropertyValueEvaluator.PARAM_VALUE, text);
   }

   public void prepareForEdit(Map<String, Serializable> conditionProps,
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
