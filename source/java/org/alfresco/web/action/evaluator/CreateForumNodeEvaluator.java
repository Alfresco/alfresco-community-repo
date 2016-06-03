package org.alfresco.web.action.evaluator;

import org.alfresco.model.ForumModel;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Create a forum around a node.
 * 
 * @author Kevin Roast
 */
public class CreateForumNodeEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -5132048668011887505L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      return (node.hasAspect(ForumModel.ASPECT_DISCUSSABLE) == false &&
              node.isLocked() == false);
   }
}
