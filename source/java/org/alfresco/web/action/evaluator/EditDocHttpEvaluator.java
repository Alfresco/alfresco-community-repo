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

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.coci.EditOnlineDialog;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * UI Action Evaluator - Edit document via HTTP or inline edit.
 * 
 * @author Kevin Roast
 */
public class EditDocHttpEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -3694679925715830430L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      DictionaryService dd = Repository.getServiceRegistry(fc).getDictionaryService();
      
      boolean result = false;
      
      // Since the reader returned of an empty translation is the reader of it's pivot translation, it makes 
      // no sens to edit it on-line.
      if(node.getAspects().contains(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
      {
         //result = false
      }
      // if the node is inline editable, the default http behaviour should 
      // always be used otherwise the configured approach is used
      else if (dd.isSubClass(node.getType(), ContentModel.TYPE_CONTENT))
      {
         if (node.hasAspect(ApplicationModel.ASPECT_INLINEEDITABLE) == true &&
             node.getProperties().get(ApplicationModel.PROP_EDITINLINE) != null &&
             ((Boolean)node.getProperties().get(ApplicationModel.PROP_EDITINLINE)).booleanValue() == true)
         {
            if ((node.isWorkingCopyOwner() == true && node.getProperties().get(ContentModel.PROP_WORKING_COPY_MODE) != null && 
                 node.getProperties().get(ContentModel.PROP_WORKING_COPY_MODE).equals(EditOnlineDialog.ONLINE_EDITING)) || 
               (node.hasAspect(ContentModel.ASPECT_WORKING_COPY) && node.hasPermission(PermissionService.WRITE)) ||
               (node.isLocked() == false && node.hasAspect(ContentModel.ASPECT_WORKING_COPY) == false))
            {
               result = true;
            }
         }
      }
      
      return result;
   }
}
