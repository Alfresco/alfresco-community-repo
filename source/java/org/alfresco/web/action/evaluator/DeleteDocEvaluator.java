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
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.web.action.ActionEvaluator;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.ml.MultilingualUtils;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Delete document.
 *
 * @author Kevin Roast
 */
public class DeleteDocEvaluator implements ActionEvaluator
{
   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      FacesContext fc = FacesContext.getCurrentInstance();

      // the node to delete is a ml container, test if the user has enought right on each translation
      if(node.getType().equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER))
      {
          return MultilingualUtils.canDeleteEachTranslation(node, fc);
      }

      boolean isPivot = false;

      // special case for multilingual documents
      if (node.getAspects().contains(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
      {
         MultilingualContentService mlservice =
            (MultilingualContentService) FacesHelper.getManagedBean(fc, "MultilingualContentService");

         // if the translation is the last translation, user can delete it
         if (mlservice.getTranslations(node.getNodeRef()).size() == 1)
         {
            isPivot = false;
         }
         // Else if the node is the pivot language, user can't delete it
         else if (mlservice.getPivotTranslation(node.getNodeRef()).getId()
                  .equalsIgnoreCase(node.getNodeRef().getId()))
         {
            isPivot = true;
         }
         // finally, the node is not the pivot translation, user can delete it
      }

      return (node.isLocked() == false &&
              node.hasAspect(ContentModel.ASPECT_WORKING_COPY) == false &&
              isPivot == false);
   }
}