/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
