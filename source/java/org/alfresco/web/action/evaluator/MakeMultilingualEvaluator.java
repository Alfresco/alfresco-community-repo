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
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.alfresco.web.bean.repository.Node;

/**
 * Evaluates whether the Make Multilingual action should be visible. 
 * 
 * If the node is already Multilingual don't allow the action.
 * 
 * @author Yannick Pignot
 */
public class MakeMultilingualEvaluator extends BaseActionEvaluator
{
   public boolean evaluate(Node node)
   {
      FacesContext fc = FacesContext.getCurrentInstance();

      UserPreferencesBean userprefs = 
         (UserPreferencesBean) FacesHelper.getManagedBean(fc, "UserPreferencesBean");

      // the total number of available languages for the translation have to be greather that 0
      int contentFilterLanguagesCount  = userprefs.getContentFilterLanguages(false).length;

      return (node.isLocked() == false &&
              node.hasAspect(ContentModel.ASPECT_WORKING_COPY) == false &&
              node.hasAspect(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT) == false &&
              contentFilterLanguagesCount > 0);
   }
}