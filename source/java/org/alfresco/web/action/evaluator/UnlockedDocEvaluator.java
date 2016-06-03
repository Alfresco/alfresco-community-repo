package org.alfresco.web.action.evaluator;

import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Proceed if the document is not locked.
 * 
 * @author Kevin Roast
 */
public class UnlockedDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -3216759932698306123L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      return (node.isLocked() == false);
   }
}