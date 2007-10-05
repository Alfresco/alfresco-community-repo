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

import org.alfresco.web.bean.groups.GroupsDialog;


/**
 * Evaluator that determines whether the add group user action should
 * be visible - only visible when the dialog is not showing the root
 * group.
 * 
 * @author Gavin Cornwell
 */
public class GroupActionEvaluator extends BaseActionEvaluator
{
   @Override
   public boolean evaluate(Object obj)
   {
      boolean result = true;
      
      if (obj instanceof GroupsDialog)
      {
         // if the object is the GroupsDialog check whether the group is null,
         // if it is it represents the root group so disallow the action
         result = (((GroupsDialog)obj).getGroup() != null);
      }
      else if (obj instanceof Map)
      {
         // if the object is a Map retrieve the group and check for null,
         // if it is it represents the root group so disallow the action
         Object group = ((Map)obj).get(GroupsDialog.PARAM_GROUP);
         result = (group != null);
      }
      else
      {
         result = super.evaluate(obj);
      }
      
      return result;
   }
}
