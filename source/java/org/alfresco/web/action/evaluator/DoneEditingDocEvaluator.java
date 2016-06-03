package org.alfresco.web.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Done editing document.
 */
public class DoneEditingDocEvaluator extends BaseActionEvaluator
{
   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      return (node.getProperties().get(ContentModel.PROP_WORKING_COPY_MODE) != null &&
              node.hasAspect(ContentModel.ASPECT_WORKING_COPY) &&
              node.hasPermission(PermissionService.CHECK_IN));
   }
}