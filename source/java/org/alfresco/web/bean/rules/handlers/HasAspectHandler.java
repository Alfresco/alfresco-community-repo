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
      for (SelectItem item : ((CreateRuleWizard)wizard).getAspects())
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
