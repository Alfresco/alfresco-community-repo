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
