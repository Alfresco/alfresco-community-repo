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
import org.alfresco.web.bean.UserPreferencesBean;
import org.alfresco.web.bean.ml.MultilingualUtils;
import org.alfresco.web.bean.repository.Node;

/**
 * Evaluates whether the Add Translation (with or without content) action should be visible.
 *
 * If the node is not already Multilingual, locked, or if a translation exists for each available
 * filter language, don't allow the action.
 *
 * The current user can add a translation to a translation set only if he has enough right to add
 * a content to the space where the pivot translation is located in.
 *
 * @author Yannick Pignot
 */
public class AddTranslationEvaluator implements ActionEvaluator
{
   public boolean evaluate(Node node)
   {
      boolean isNodeMultililingal = node.hasAspect(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
      boolean isMLContainer = node.getType().equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER);

      // the node must be multiligual (translation or ml container)
      if(isNodeMultililingal || isMLContainer)
      {
          FacesContext fc = FacesContext.getCurrentInstance();

          // the current user must have enough right to add a content to the space
          // where the pivot translation is located in
          if(MultilingualUtils.canAddChildrenToPivotSpace(node, fc))
          {
              MultilingualContentService mlservice =
                  (MultilingualContentService) FacesHelper.getManagedBean(fc, "MultilingualContentService");

               UserPreferencesBean userprefs =
                  (UserPreferencesBean) FacesHelper.getManagedBean(fc, "UserPreferencesBean");

               // the number of translation of this document
               int availableTranslationCount    = mlservice.getTranslations(node.getNodeRef()).size();
               // the total number of available languages for the translation
               int contentFilterLanguagesCount  = userprefs.getContentFilterLanguages(false).length;

               // the number of translation must be < to the total number of available language for the content filter
               return (availableTranslationCount < contentFilterLanguagesCount);
          }
          else
          {
              return false;
          }
      }
      else
      {
         return false;
      }
   }
}