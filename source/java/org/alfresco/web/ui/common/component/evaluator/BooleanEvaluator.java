/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.ui.common.component.evaluator;

/**
 * @author kevinr
 * 
 * Evaluates to true if the value suppied is a boolean string of "true".
 */
public class BooleanEvaluator extends BaseEvaluator
{
   /**
    * Evaluate against the component attributes. Return true to allow the inner
    * components to render, false to hide them during rendering.
    * 
    * @return true to allow rendering of child components, false otherwise
    */
   public boolean evaluate()
   {
      boolean result = false;
      
      try
      {
         if (getValue() instanceof Boolean)
         {
            result = ((Boolean)getValue()).booleanValue();
         }
         else
         {
            result = Boolean.valueOf((String)getValue()).booleanValue();
         }
      }
      catch (Exception err)
      {
         // return default value on error
         s_logger.debug("Unable to evaluate value to boolean: " + getValue());
      }
      
      return result;
   }
}
