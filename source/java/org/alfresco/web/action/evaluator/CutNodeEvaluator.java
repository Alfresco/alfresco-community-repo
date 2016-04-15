package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.web.bean.ml.MultilingualUtils;
import org.alfresco.web.bean.repository.Node;

/**
 * Evaluates whether the Cut Node action should be visible.
 *
 * Among all available operations over non-multilingual documents (i.e. copy,
 * delete, start discussion, etc), there is a missing one: <b>Move</b>.
 * Translations cannot be moved due to the exiting link it has with the logical
 * document. Despite it is technically achievable, it could be functionally
 * troublesome. Spreading translations of the same semantic message among several
 * spaces could lead to confusion and problems.
 *
 * If the node to move is a mlContainer, the user must have enough right to delete each translation
 *
 * @author Yannick Pignot
 */
public class CutNodeEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 5162681242056158214L;

   public boolean evaluate(Node node)
   {

      FacesContext fc = FacesContext.getCurrentInstance();

      // the node to delete is a ml container, test if the user has enought right on each translation
      if(node.getType().equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER))
      {
          return MultilingualUtils.canMoveEachTranslation(node, fc);
      }


      boolean eval = true;

      // impossible to cut/copy a translation without content.
      if (node.getAspects().contains(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
      {
         eval = false;
      }
      else
      {
         eval = !node.getAspects().contains(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
      }

      return eval;
   }
}
