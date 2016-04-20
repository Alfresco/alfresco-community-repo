package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.model.ForumModel;
import org.alfresco.web.bean.ml.MultilingualUtils;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Create a forum around a multilingual content node.
 *
 * @author Yanick Pignot
 */
public class CreateForumMLNodeEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -8621940623410511065L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
       // the current user must have enough right to add a content to the space
       // where the pivot translation is located in
       return MultilingualUtils.canAddChildrenToPivotSpace(node, FacesContext.getCurrentInstance())
                   && node.hasAspect(ForumModel.ASPECT_DISCUSSABLE) == false
                   && node.isLocked() == false;

   }
}
