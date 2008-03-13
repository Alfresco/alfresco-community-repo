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

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.web.bean.coci.EditOnlineDialog;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * UI Action Evaluator - Edit document online via http.
 *
 */
public class EditDocOnlineHttpEvaluator extends CheckoutDocEvaluator
{

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      DictionaryService dd = Repository.getServiceRegistry(fc).getDictionaryService();

      boolean result = false;

      if(node.hasAspect(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
      {
         // this branch from EditDocHttpEvaluator
         // skip, result = false
      }
      else if (dd.isSubClass(node.getType(), ContentModel.TYPE_CONTENT))
      {
         Map<String, Object> props = node.getProperties();
         if ((node.hasAspect(ApplicationModel.ASPECT_INLINEEDITABLE) &&
               props.get(ApplicationModel.PROP_EDITINLINE) != null &&
               ((Boolean)props.get(ApplicationModel.PROP_EDITINLINE)).booleanValue() == true))
         {
            if (node.hasAspect(ContentModel.ASPECT_WORKING_COPY))
            {
               if (props.get(ContentModel.PROP_WORKING_COPY_MODE) != null && props.get(ContentModel.PROP_WORKING_COPY_MODE).equals(EditOnlineDialog.ONLINE_EDITING))
                  result = true;
            }
            else
            {
               result = super.evaluate(node);
            }
         }
      }

      return result;
   }

}