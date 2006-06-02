package org.alfresco.web.bean.rules.handlers;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.rules.CreateRuleWizard;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Condition handler to the "is-subtype" condition.
 * 
 * @author gavinc
 */
public class IsSubTypeHandler extends BaseConditionHandler
{
   protected static final String PROP_MODEL_TYPE = "modeltype";
   
   public String getJSPPath()
   {
      return getJSPPath(IsSubTypeEvaluator.NAME);
   }

   public void prepareForSave(Map<String, Serializable> conditionProps,
         Map<String, Serializable> repoProps)
   {
      QName type = QName.createQName((String)conditionProps.get(PROP_MODEL_TYPE));
      repoProps.put(IsSubTypeEvaluator.PARAM_TYPE, type);
   }

   public void prepareForEdit(Map<String, Serializable> conditionProps,
         Map<String, Serializable> repoProps)
   {
      QName type = (QName)repoProps.get(IsSubTypeEvaluator.PARAM_TYPE);
      conditionProps.put(PROP_MODEL_TYPE, type.toString());
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> conditionProps)
   {
      Boolean not = (Boolean)conditionProps.get(PROP_CONDITION_NOT);
      String msgId = not.booleanValue() ? "condition_is_subtype_not" : "condition_is_subtype";
         
      String label = null;
      String typeName = (String)conditionProps.get(PROP_MODEL_TYPE);
      for (SelectItem item : ((CreateRuleWizard)wizard).getModelTypes())
      {
         if (item.getValue().equals(typeName))
         {
            label = item.getLabel();
            break;
         }
      }
      
      return MessageFormat.format(Application.getMessage(context, msgId),
            new Object[] {label});
   }
}
