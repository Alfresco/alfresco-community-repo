package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.web.bean.ml.MultilingualUtils;
import org.alfresco.web.bean.repository.Node;

/**
 * Evaluates whether the new edition wizard action should be visible.
 *
 * The creation of a new edtion implies the deletion of each translation and the creation
 * of new content in the space
 *
 * @author Yanick Pignot
 */
public class NewEditionEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -7511956951071280506L;

   public boolean evaluate(Node node)
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return MultilingualUtils.canStartNewEditon(node, fc);
   }
}