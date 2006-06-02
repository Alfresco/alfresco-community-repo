package org.alfresco.web.bean.actions.handlers;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.actions.BaseActionWizard;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Action handler implementation for the "add-features" action.
 * 
 * @author gavinc
 */
public class AddFeaturesHandler extends BaseActionHandler
{
   protected static final String PROP_ASPECT = "aspect";
   
   public String getJSPPath()
   {
      return getJSPPath(AddFeaturesActionExecuter.NAME);
   }

   public void prepareForSave(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoParams)
   {
      QName aspect = Repository.resolveToQName((String)actionProps.get(PROP_ASPECT));
      repoParams.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, aspect);
   }

   public void prepareForEdit(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoParams)
   {
      QName aspect = (QName)repoParams.get(AddFeaturesActionExecuter.PARAM_ASPECT_NAME);
      actionProps.put(PROP_ASPECT, aspect.toString());
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> actionProps)
   {
      String label = null;
      String aspect = (String)actionProps.get(PROP_ASPECT);
         
      // find the label used by looking through the SelectItem list
      for (SelectItem item : ((BaseActionWizard)wizard).getAspects())
      {
         if (item.getValue().equals(aspect))
         {
            label = item.getLabel();
            break;
         }
      }

      return MessageFormat.format(Application.getMessage(context, "action_add_features"), 
            new Object[] {label});
   }
}
