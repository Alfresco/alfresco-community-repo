package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.ml.MultilingualUtils;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Delete document.
 *
 * @author Kevin Roast
 */
public class DeleteDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 5742287199692844685L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      FacesContext fc = FacesContext.getCurrentInstance();

      // the node to delete is a ml container, test if the user has enought right on each translation
      if(node.getType().equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER))
      {
          return MultilingualUtils.canDeleteEachTranslation(node, fc);
      }

      boolean isPivot = false;

      // special case for multilingual documents
      if (node.getAspects().contains(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
      {
         MultilingualContentService mlservice =
            (MultilingualContentService) FacesHelper.getManagedBean(fc, "MultilingualContentService");

         // if the translation is the last translation, user can delete it
         if (mlservice.getTranslations(node.getNodeRef()).size() == 1)
         {
            isPivot = false;
         }
         // Else if the node is the pivot language, user can't delete it
         else if (mlservice.getPivotTranslation(node.getNodeRef()).getId()
                  .equalsIgnoreCase(node.getNodeRef().getId()))
         {
            isPivot = true;
         }
         // finally, the node is not the pivot translation, user can delete it
      }

      return (node.isLocked() == false &&
              node.hasAspect(ContentModel.ASPECT_WORKING_COPY) == false &&
              isPivot == false);
   }
}