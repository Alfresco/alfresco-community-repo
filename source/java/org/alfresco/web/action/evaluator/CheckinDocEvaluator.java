package org.alfresco.web.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Checkin document.
 * 
 * @author Kevin Roast
 */
public class CheckinDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 5398249535631219663L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      return (node.hasAspect(ContentModel.ASPECT_WORKING_COPY) &&
              node.getProperties().get(ContentModel.PROP_WORKING_COPY_MODE) == null &&
              node.hasPermission(PermissionService.CHECK_IN));
   }
}