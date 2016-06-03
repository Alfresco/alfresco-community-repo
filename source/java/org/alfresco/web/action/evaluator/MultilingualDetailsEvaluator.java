package org.alfresco.web.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.web.bean.repository.Node;

/**
 * Evaluates whether the Manage Multinlingual Details action should be visible. 
 * 
 * The action is available only if the node is a translation.  
 * 
 * @author Yannick Pignot
 */
public class MultilingualDetailsEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 1154993208046462796L;

   public boolean evaluate(Node node)
   {
      return (node.hasAspect(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT) == true);
   }
}
