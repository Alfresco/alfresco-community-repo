package org.alfresco.web.action.evaluator;

import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Take ownership of a document.
 * 
 * @author Kevin Roast
 */
public class TakeOwnershipDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 3966463533922521230L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      return (node.isLocked() == false);
   }
}
