package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * UI Action Evaluator - Checkout document.
 * 
 * @author Kevin Roast
 */
public class CheckoutDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 5510366635124591353L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      DictionaryService dd = Repository.getServiceRegistry(
            FacesContext.getCurrentInstance()).getDictionaryService();
      
      return dd.isSubClass(node.getType(), ContentModel.TYPE_CONTENT) && 
             ((node.hasPermission(PermissionService.CHECK_OUT) &&
              (node.isLocked() == false &&
               node.hasAspect(ContentModel.ASPECT_WORKING_COPY) == false) &&
               node.hasAspect(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION) == false));
   }
}
