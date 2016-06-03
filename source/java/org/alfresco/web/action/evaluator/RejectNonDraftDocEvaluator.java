package org.alfresco.web.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - 'Reject' workflow step for document.
 * 
 * @author Kevin Roast
 */
public class RejectNonDraftDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 6296671033469500696L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      return (node.getProperties().get("app:rejectStep") != null &&
              node.isLocked() == false &&
              node.hasAspect(ContentModel.ASPECT_WORKING_COPY) == false);
   }
}
