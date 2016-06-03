package org.alfresco.web.action.evaluator;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.web.bean.coci.EditOnlineDialog;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * UI Action Evaluator - Edit document online via http.
 */
public class EditDocOnlineHttpEvaluator extends CheckoutDocEvaluator
{
   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      DictionaryService dd = Repository.getServiceRegistry(fc).getDictionaryService();
      
      boolean result = false;
      
      if (node.hasAspect(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
      {
         // this branch from EditDocHttpEvaluator
         // skip, result = false
      }
      else if (dd.isSubClass(node.getType(), ContentModel.TYPE_CONTENT))
      {
         Map<String, Object> props = node.getProperties();
         if ((node.hasAspect(ApplicationModel.ASPECT_INLINEEDITABLE) &&
              props.get(ApplicationModel.PROP_EDITINLINE) != null &&
              ((Boolean)props.get(ApplicationModel.PROP_EDITINLINE)).booleanValue()))
         {
            if (node.hasAspect(ContentModel.ASPECT_WORKING_COPY))
            {
               result = EditOnlineDialog.ONLINE_EDITING.equals(props.get(ContentModel.PROP_WORKING_COPY_MODE));
            }
            else
            {
               result = super.evaluate(node);
            }
         }
      }
      
      return result;
   }
}