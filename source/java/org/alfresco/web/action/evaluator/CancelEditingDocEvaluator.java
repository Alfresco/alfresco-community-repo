package org.alfresco.web.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Cancel editing document.
 */
public class CancelEditingDocEvaluator extends BaseActionEvaluator
{
   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      return (node.hasAspect(ContentModel.ASPECT_WORKING_COPY) &&
              node.getProperties().get(ContentModel.PROP_WORKING_COPY_MODE) != null &&
              node.hasPermission(PermissionService.CANCEL_CHECK_OUT));
   }
}
