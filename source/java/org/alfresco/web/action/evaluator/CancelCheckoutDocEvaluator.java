package org.alfresco.web.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Cancel checkout document.
 * 
 * @author Kevin Roast
 */
public class CancelCheckoutDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -9015403093449070254L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      return (node.hasAspect(ContentModel.ASPECT_WORKING_COPY) &&
              node.getProperties().get(ContentModel.PROP_WORKING_COPY_MODE) == null &&
              node.hasPermission(PermissionService.CANCEL_CHECK_OUT));
   }
}