package org.alfresco.web.bean.actions.handlers;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.repo.action.executer.SpecialiseTypeActionExecuter;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.actions.BaseActionWizard;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Action handler for the "specialise-type" action.
 * 
 * @author gavinc
 */
public class SpecialiseTypeHandler extends BaseActionHandler
{
   public static final String PROP_OBJECT_TYPE = "objecttype";
   
   public String getJSPPath()
   {
      return getJSPPath(SpecialiseTypeActionExecuter.NAME);
   }

   public void prepareForSave(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      String objectType = (String)actionProps.get(PROP_OBJECT_TYPE);
      repoProps.put(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, 
            QName.createQName(objectType));
   }

   public void prepareForEdit(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      QName specialiseType = (QName)repoProps.get(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME);
      actionProps.put(PROP_OBJECT_TYPE, specialiseType.toString());
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> actionProps)
   {
      String label = null;
      String objectType = (String)actionProps.get(PROP_OBJECT_TYPE);
      for (SelectItem item  : ((BaseActionWizard)wizard).getObjectTypes())
      {
         if (item.getValue().equals(objectType) == true)
         {
            label = item.getLabel();
            break;
         }
      }
         
      return MessageFormat.format(Application.getMessage(context, "action_specialise_type"),
            new Object[] {label});
   }

}
