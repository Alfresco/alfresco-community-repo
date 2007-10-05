/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
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
