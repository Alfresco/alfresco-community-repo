package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.web.bean.coci.EditOfflineDialog;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * UI Action Evaluator - Update document content.
 * 
 * @author Kevin Roast
 */
public class UpdateDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 6030963610213633893L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      DictionaryService dd = Repository.getServiceRegistry(
            FacesContext.getCurrentInstance()).getDictionaryService();

      boolean isOfflineEditing =
          (EditOfflineDialog.OFFLINE_EDITING.equals(node.getProperties().get(ContentModel.PROP_WORKING_COPY_MODE)));
      
      return dd.isSubClass(node.getType(), ContentModel.TYPE_CONTENT) && 
             ((node.isWorkingCopyOwner() && !isOfflineEditing) ||
              (!node.isLocked() && !node.hasAspect(ContentModel.ASPECT_WORKING_COPY)));
   }
}