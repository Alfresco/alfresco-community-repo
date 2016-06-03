package org.alfresco.web.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - 'Approve' workflow step for document.
 * 
 * @author Kevin Roast
 */
public class ApproveNonDraftDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -277600395385704689L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      return (node.getProperties().get("app:approveStep") != null &&
              node.isLocked() == false &&
              node.hasAspect(ContentModel.ASPECT_WORKING_COPY) == false);
   }
}
