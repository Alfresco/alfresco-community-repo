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
