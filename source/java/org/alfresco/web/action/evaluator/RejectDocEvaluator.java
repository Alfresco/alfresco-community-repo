package org.alfresco.web.action.evaluator;

import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - 'Reject' workflow step for document or space.
 * 
 * @author Kevin Roast
 */
public class RejectDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -7733947744617999298L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      return (node.getProperties().get("app:rejectStep") != null &&
              node.isLocked() == false);
   }
}
